---
agent: cto-health-insights
project: Health Insights
last-updated: 2026-05-04
sprint: EP-00 (CI/CD) + EP-01-01 (Multi-Module Bootstrap)
---

# CTO Agent Memory — Health Insights

## Finalized Stack (Do Not Revisit Without New Evidence)

| Layer | Decision | Version | Notes |
|---|---|---|---|
| AGP | 9.2.0 | — | Bleeding edge. Forced several migrations (KAPT removed, BaseExtension removed). Stay current. |
| Kotlin | 2.2.10 | — | Paired with KSP 2.2.10-2.0.2. Must stay in sync. |
| KSP | 2.2.10-2.0.2 | — | Replaces KAPT permanently. Pin and upgrade intentionally. |
| Hilt | 2.59.2 | — | Bumped from 2.56 to fix AGP 9.x BaseExtension removal. Stable. |
| Compose BOM | 2026.02.01 | — | Verify this resolves on Maven Central before first screen. |
| Architecture | MVVM + Clean Architecture | — | Repository → UseCases → ViewModels. Unidirectional data flow. No exceptions. |
| DI | Hilt | — | No Koin. No manual DI. |
| Async | Coroutines + Flow | — | No RxJava. |
| Database | Room + SQLCipher | TBD | Not yet implemented. See encryption section below. |
| CI | GitHub Actions | — | ubuntu-latest + KVM for emulator. Sequential jobs. |

## Module Structure (13 submodules)

```
:app
:core:common        — android.library
:core:data          — android.library (repository implementations)
:core:database      — android.library + Hilt + KSP (Room + SQLCipher, not yet wired)
:core:domain        — kotlin-jvm ONLY (pure Kotlin, zero Android deps — ENFORCE THIS)
:core:network       — android.library (empty until a feature needs network)
:core:ui            — android.library + kotlin-compose
:feature:dashboard
:feature:health-connect
:feature:insights
:feature:onboarding
:feature:settings
:feature:sleep
:feature:workouts
```

Namespace scheme: `com.healthinsights.{core|feature}.{module}`

## Critical Build Gotchas (Hard-Won)

1. **`kotlin-compose` plugin subsumes `kotlin-android`** — do NOT apply both. Double-applying causes `Cannot add extension with name 'kotlin'`. Modules using Compose: apply `kotlin-compose` only.

2. **`android.disallowKotlinSourceSets=false` in `gradle.properties`** — Required shim between KSP source set registration (uses `kotlin.sourceSets`) and AGP 9.x (expects `android.sourceSets`). Remove when KSP migrates. Track KSP issue #1840. Pin KSP version; upgrade only intentionally.

3. **All Gradle task names require `:app:` prefix** — Gradle 9.4.1 does not resolve unqualified task names in multi-project builds. CI commands: `:app:ktlintCheck`, `:app:detekt`, `:app:assembleDebug`, etc.

4. **Gradle cache key bug** — CI `hashFiles` uses `'libs.versions.toml'` but the file lives at `gradle/libs.versions.toml`. Must fix to `'gradle/libs.versions.toml'`.

## Rejected Alternatives

| Alternative | Rejected Reason |
|---|---|
| KAPT | Deprecated + incompatible with AGP 9.x built-in Kotlin |
| Koin | No advantage over Hilt here; Hilt has better Jetpack integration |
| Flutter/KMM | No cross-platform problem to solve; native Kotlin is correct |
| RxJava | No existing integration; Coroutines + Flow covers all async needs |
| Direct SQLite (unencrypted) | Health data is special-category under LGPD — must encrypt at rest |
| Custom Either type for error handling | Adds cognitive overhead; `kotlin.Result` is stdlib and sufficient |

## Approved Libraries (in use)

- `ktlint` 12.1.2 — style enforcement
- `detekt` 1.23.7 — static analysis; Compose exemptions in `detekt.yml` (FunctionNaming ignores @Composable, MagicNumber ignores property declarations)
- `kover` 0.9.1 — coverage; threshold at 0% during bootstrap
- `reactivecircus/android-emulator-runner@v2` — CI instrumented test runner

## Libraries Not Yet Added (Needed Before Phase 2)

- `room-runtime`, `room-ktx`, `room-compiler` (KSP) — for `core:database`
- `net.zetetic:android-database-sqlcipher` — encryption for Room
- `navigation-compose` — Compose Navigation, type-safe API (Navigation 2.8+)
- `androidx.health.connect:connect-client` — for `feature:health-connect`

## Pending Decisions (Must Resolve Before Feature Code)

### Encryption Key Management for SQLCipher
**Decision:** Android Keystore + EncryptedSharedPreferences.
- Generate 256-bit AES key in Android Keystore on first launch (hardware-backed)
- Encrypt SQLCipher passphrase with this key
- Store encrypted passphrase in EncryptedSharedPreferences
- Never store passphrase in plaintext
- **Must be reviewed by CISO agent** before implementing (key rotation, backup behavior, LGPD Art. 46)
- See also: `docs/security/ESTRATEGIA_SQLCIPHER_KEYSTORE.md` (may contain prior analysis)

### Error Handling Strategy
**Decision:** `kotlin.Result<T>` in all use cases. Stdlib. No custom Either types.

## Architectural Constraints (Non-Negotiable)

1. `core:domain` must have ZERO `com.android.*` or `androidx.*` dependencies. Enforce with a Gradle validation task.
2. `feature:*` modules must NOT depend on other `feature:*` modules.
3. `core:network` stays empty until a concrete feature requires network access.
4. Health data stays on-device by default. No network transmission without CISO review.
5. Encrypt at rest — SQLCipher only. Never plain SQLite for biometric fields.

## Open Issues (Priority Order — as of 2026-05-04)

1. Fix Gradle cache key path: `'libs.versions.toml'` → `'gradle/libs.versions.toml'` in both CI YAML files
2. Bump `minSdk` from 26 to 28 (Health Connect requires API 28; API 26–27 market share <1%)
3. Update `lifecycleRuntimeKtx` (2.6.1 → 2.8.x) and `activityCompose` (1.8.0 → 1.9.x)
4. Create convention plugins in `build-logic/` before module count grows
5. Enable `isMinifyEnabled = true` + `isShrinkResources = true` for release builds; add ProGuard rules
6. Raise Kover threshold to 60% for `core:domain` when first use case is merged
7. Add `staging` build type (mirrors release, debug-signed) for testing with minification

## CI/CD Architecture

- `ci-pr.yml`: 5 sequential jobs — lint → unit-test → android-lint → build-check → ui-test
- `ci-main.yml`: same 5 + `build-release` (signs AAB with keystore decoded from `KEYSTORE_BASE64` secret)
- Keystore: decoded to `/tmp/keystore-*.jks`, deleted with `if: always()` cleanup step
- Play Store upload: stubbed in `ci-main.yml`, requires manual first upload to Play Console before activating
- Concurrency: PR runs auto-cancel on new push; main runs are never cancelled

## Performance Budgets (Defined)

- Cold start to first meaningful frame: < 2 seconds
- Dashboard query (last 30 days): < 500ms on mid-range hardware
- Background sync: must complete within WorkManager constraints
- AAB size: < 20MB initial release; flag any library adding > 2MB

## Phase 2 (Onboarding) Prerequisites

Before any Onboarding feature code can be written:
1. `core:database` foundation (Room + SQLCipher + Keystore key management, CISO reviewed)
2. `feature:health-connect` availability layer (`HealthConnectClient.getSdkStatus()`, permission contracts, `HealthConnectAvailabilityUseCase` in `core:domain`)
3. Compose Navigation wired in `:app` (`NavHost`, typed `@Serializable` routes, Navigation 2.8+)
4. Error handling strategy confirmed and documented
5. Convention plugins in `build-logic/`
