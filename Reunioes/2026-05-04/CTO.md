# CTO Technical Analysis — Health Insights
**Date:** 2026-05-04
**Sprint reviewed:** EP-00 (CI/CD) + EP-01-01 (Multi-Module Bootstrap)
**Author:** CTO Agent

---

## Executive Summary

The foundation sprint was executed well given the constraints of bleeding-edge tooling. The stack choices are sound. The multi-module architecture is correctly shaped. Three issues demand attention before Phase 2 (Onboarding) can start, and one architectural risk — the `core:domain` isolation boundary — needs to be enforced now before the first line of feature code lands, or it will rot immediately.

---

## 1. Technical Debt and Gotchas Introduced

### 1.1 `android.disallowKotlinSourceSets=false` — Expiry Horizon: 6–18 months

This flag is a shim between two incompatible worlds: AGP 9.x uses `android.sourceSets` for its built-in Kotlin compilation, but KSP 2.x still registers generated sources via the old `kotlin.sourceSets` API. Without the flag, KSP-generated code is invisible to the Android build.

**When does this need to change?** When KSP migrates its source-set registration to the AGP 9.x API — tracked in [KSP issue #1840](https://github.com/google/ksp/issues/1840). As of May 2026, KSP 2.2.10 has not completed this migration. Monitor KSP release notes. When a release notes entry says "AGP 9.x source set registration migrated," remove the flag and verify the build. **Do not wait to be surprised by a KSP upgrade that silently breaks generation.** Pin `ksp` in `libs.versions.toml` and upgrade only intentionally until this is resolved.

**Risk if ignored:** A future transitive upgrade (e.g., via AGP bumping KSP transitively) could flip behavior and produce confusing "symbol not found" errors on KSP-generated Hilt or Room classes. The fix is trivial once identified, but the debugging cost is not.

### 1.2 `isMinifyEnabled = false` in Release Build Type

The release `buildType` in `app/build.gradle.kts` has minification disabled. This was acceptable for bootstrap — you cannot configure ProGuard rules before you have code. It is **not acceptable for any release that ships to users.**

Before the first internal track upload, minification and resource shrinking must be enabled:

```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

The `proguard-rules.pro` file is currently empty. It will need rules for: Hilt (usually handled by the Hilt plugin automatically), Room (ditto), SQLCipher (needs explicit `-keep` rules), and Health Connect data classes (any `@Serializable` or reflection-accessed models).

**Consequence of shipping without it:** AAB size will be 2–4x larger than necessary, and security-sensitive field names in health data models will be visible in the APK via reverse engineering.

### 1.3 Kover Coverage Threshold at 0%

The Kover threshold is deliberately set to 0% for bootstrap. This is correct. However, there is no explicit reminder in the CI pipeline of when to raise it. The threshold must be set before the first use case is merged into `main`, not after. The sequence is:

1. First use case merged → raise threshold to 60% for `core:domain`.
2. First ViewModel merged → raise threshold to 50% for the owning feature module.
3. Never apply a blanket project-wide threshold — it will be gamed by empty modules.

### 1.4 Sequential CI Jobs: Acceptable Cost Now, Monitor at Scale

The 5-job sequential chain (lint → unit-test → android-lint → build-check → ui-test) is the correct tradeoff for a solo developer: cheap jobs fail fast before expensive ones. At current scale (zero feature code), total CI time for a PR run is under 10 minutes. Once feature modules accumulate, the unit-test job will grow. The breakpoint for reconsidering parallelism is when the unit-test job alone exceeds 8 minutes. At that point, split into per-module matrix jobs. This is a future concern, not a present one.

### 1.5 Gradle Cache Key Covers Only Root-Level Files

The CI cache key uses `hashFiles('libs.versions.toml', '**/*.gradle.kts')`. Note that `libs.versions.toml` is in `gradle/libs.versions.toml`, not the root. If the cache key path `libs.versions.toml` is evaluated relative to the repo root and the file is at `gradle/libs.versions.toml`, the hash will always be the same (file not found = empty hash). This should be audited against actual cache hit rates in GitHub Actions. The correct path in the `hashFiles` call should be `'gradle/libs.versions.toml'`.

---

## 2. Stack Decisions: Validated and Concerns

### Validated — No Revisit Needed

| Decision | Verdict |
|---|---|
| AGP 9.2.0 + Kotlin 2.2.10 | Correct. Staying current avoids accumulating upgrade debt. The breakages encountered (KAPT removal, BaseExtension removal) were one-time migration costs, now paid. |
| KSP replacing KAPT | Correct and irreversible. KAPT is deprecated with AGP 9.x. No going back. |
| Hilt 2.59.2 | Correct. The 2.56 → 2.59 forced upgrade (due to `BaseExtension` removal in AGP 9.x) was unavoidable. 2.59 is stable. |
| `kotlin-compose` plugin — do not double-apply `kotlin-android` | This is an underdocumented gotcha that was correctly handled. Document it in `CLAUDE.md` under "Build Gotchas" so future sessions don't relearn it. |
| `core:domain` as pure `kotlin-jvm` module | Correct. The domain layer must have zero Android dependencies. This was executed correctly and must be enforced by convention. |
| GitHub Actions with KVM-accelerated emulator | Correct. The ubuntu-latest + KVM approach is the right cost/speed tradeoff. macOS runners would be 10x more expensive for the same outcome. |

### Needs Revisiting Before Feature Code

**`lifecycleRuntimeKtx = "2.6.1"` and `activityCompose = "1.8.0"` are outdated.** As of early 2026, Lifecycle 2.8.x and Activity Compose 1.9.x are current. These are not breaking changes, but they should be updated before the first feature lands to avoid accumulating version lag. Specifically, Lifecycle 2.8+ includes `collectAsStateWithLifecycle` fixes relevant to Compose state management in the feature modules.

**`composeBom = "2026.02.01"` needs verification.** The Compose BOM pins all Compose library versions. Confirm this BOM version exists and resolves correctly. If it was a speculative version chosen during bootstrap, it must be validated against the actual Maven Central catalog before the first Compose screen is written.

**`minSdk = 26` is too broad for Health Connect.** Health Connect requires Android 9 (API 28) at minimum, and full functionality requires Android 14 (API 34) for the platform-integrated version. With `minSdk = 26`, the app will install on Android 8 devices that cannot use Health Connect at all. The Onboarding feature must handle this gracefully — either bump `minSdk` to 28 (recommended) or implement a hard incompatibility gate in the Onboarding flow that blocks users below API 28.

**Recommendation: Bump `minSdk` to 28 now.** Android 8.x (API 26–27) market share is negligible (under 1% as of 2026). The compatibility code required to handle those devices for Health Connect adds complexity with zero product value.

---

## 3. Prerequisites Before Phase 2 (Onboarding) Can Start

These are blocking. None of them are large engineering tasks, but all of them must be resolved before feature code lands, or they become expensive retrofits.

### 3.1 Convention Plugins (Blockers)

The current build files have each module declaring its own `android { compileSdk { ... } }`, `compileOptions`, and `detekt` configuration independently. This is 13 modules × 3–5 duplicated blocks = significant drift risk. Before writing any feature code, extract convention plugins into `build-logic/`:

- `healthinsights.android.library` — wraps `android.library` plugin with standard SDK, compile options, detekt, and kover config
- `healthinsights.android.feature` — extends the library plugin adding Hilt, Compose, and navigation deps
- `healthinsights.kotlin.library` — wraps `kotlin.jvm` for pure Kotlin modules like `core:domain`

Without this, every SDK bump requires editing 13 files. With it, one file. This is a 1-day investment that pays back immediately on the first dependency update.

### 3.2 Room + SQLCipher Integration in `core:database`

The `core:database` module exists but contains no Room setup. Before the Onboarding feature can persist consent records or user preferences, the database layer must exist. The integration decision is made (Room + SQLCipher), but the implementation prerequisites are:

1. Add Room dependencies to `libs.versions.toml` (`room-runtime`, `room-ktx`, `room-compiler` via KSP)
2. Add SQLCipher dependency (`net.zetetic:android-database-sqlcipher`)
3. Configure the encrypted `RoomDatabase` instance in `core:database` with a `DatabaseFactory` that is Hilt-injectable
4. Define the encryption key management strategy (see Section 5)

This must be done as its own task before Onboarding, not interleaved with UI work.

### 3.3 Health Connect Permission Declaration

The `feature:health-connect` module is empty. Before the Onboarding flow can ask for permissions, the Health Connect permission manifest entries and the `HealthConnectClient` availability check must exist in `feature:health-connect`. The Onboarding UI will depend on this module — it cannot be written in a vacuum.

Minimum viable `feature:health-connect` for Onboarding:
- `HealthConnectClient.getSdkStatus()` wrapper
- `PermissionController.createRequestPermissionResultContract()` integration
- A `HealthConnectAvailabilityUseCase` in `core:domain` that returns one of: `Available`, `NotInstalled`, `ApiLevelTooLow`

### 3.4 Navigation Setup

13 feature modules with no navigation graph. Onboarding is the entry point of the app. Before writing a single Onboarding composable, Compose Navigation must be configured in `:app` with typed navigation routes. Use the Navigation Compose type-safe API (available since Navigation 2.8) with `@Serializable` route data classes. Add `navigation-compose` to `libs.versions.toml` and wire the `NavHost` in `MainActivity`.

### 3.5 Raise `isMinifyEnabled` and Configure ProGuard

As noted in Section 1.2, this must be done before the first internal release. Set up a `staging` build type (mirrors release but with debug signing and test flags) for developer testing with minification enabled, so ProGuard issues are caught during development rather than at release time.

---

## 4. Architectural Risks

### 4.1 `core:domain` Purity Will Be Violated Without Enforcement

The `core:domain` module is correctly a pure `kotlin-jvm` module today. It has zero dependencies beyond the JVM. This is the most important architectural constraint in the entire project. It will be violated the moment a developer (or an AI agent) reflexively adds a convenience dependency — for example, adding `kotlinx.coroutines.android` (Android-specific dispatcher) instead of the platform-agnostic `kotlinx.coroutines.core`.

**Enforcement mechanism required:** Add a custom Gradle task or Android Lint rule that fails the build if `core:domain`'s dependency set contains any `com.android.*` or `androidx.*` artifact. Without this, the purity will be lost within three PRs.

### 4.2 Feature Module Coupling Is Not Prevented

Features should not depend on each other. `feature:dashboard` should not import from `feature:sleep`. Today this is fine because the modules are empty. Once they have code, accidental coupling will happen. The conventional solution is to disallow `feature:*` to `feature:*` dependencies in Gradle — either via a custom Gradle plugin that checks dependency declarations, or by convention enforced in code review. For a solo developer, a CI-enforced lint rule is more reliable than code review self-discipline.

### 4.3 Data Layer Split Is Premature

The current module split has both `core:data` and `core:database` as separate modules. For the current feature scope (local health data, on-device only, no network), this split adds boilerplate without benefit. `core:data` is the repository implementation layer; `core:database` is the Room/SQLCipher layer. Having them separate means every repository in `core:data` depends on `core:database`, and `core:data` exists solely to hold that dependency.

**Recommendation:** Keep the split as defined — it is the correct architecture for when `core:network` becomes real. But `core:network` should remain empty until there is a concrete feature that requires network access. Do not add Retrofit or OkHttp until a feature requires them. The current empty module is fine as a placeholder.

### 4.4 No Error Handling Architecture

The project has no defined error handling strategy. Before the first use case is written, decide: does the domain layer expose `Result<T>`, `sealed class Either<Error, T>`, or Kotlin's native `kotlin.Result`? This decision must be made once and applied uniformly. Retrofitting error handling across 13 modules after the fact is expensive.

**Recommendation:** Use `kotlin.Result` in use cases (returns `Result<T>`). It is stdlib, requires no dependencies, and works correctly with Flow via `flow { emit(runCatching { ... }) }`. Avoid custom `Either` types — they add cognitive overhead without material benefit for a solo developer.

---

## 5. Next Technical Decision: Encryption Key Management for SQLCipher

This is the decision that must be made before writing a single line of Room code.

**The question:** Where does the SQLCipher encryption key live?

There are three options. Two are wrong.

**Option A (wrong): Hardcoded key in source code.** Trivially extractable via APK decompilation. Never acceptable for health data.

**Option B (wrong): Key in SharedPreferences unencrypted.** Readable by any process with root access. Not acceptable.

**Option C (correct): Android Keystore + EncryptedSharedPreferences.**

The implementation is:
1. Generate a 256-bit AES key in the Android Keystore on first app launch. The key is hardware-backed on devices with a secure element (most Android 6+ devices).
2. Use this AES key to encrypt the SQLCipher passphrase.
3. Store the encrypted passphrase in `EncryptedSharedPreferences` (which uses the same Keystore key under the hood).
4. On each app launch, retrieve and decrypt the passphrase from `EncryptedSharedPreferences`, pass it to the `SupportFactory` that SQLCipher requires.

The passphrase is never stored in plaintext anywhere. The Keystore key never leaves the secure element.

**Practical implementation:**

```
core:database/
  DatabaseKeyProvider.kt   — retrieves or generates the SQLCipher passphrase
  HealthDatabase.kt        — Room database using SupportFactory(passphrase)
  DatabaseModule.kt        — Hilt module providing the singleton database
```

`DatabaseKeyProvider` must be the first thing called when the app initializes, before any database access. If key generation fails (rare — Keystore unavailable), the app must handle this gracefully in Onboarding, not crash.

**Escalate to CISO agent** for review of this key management design before implementation. The CISO should confirm: key rotation strategy (what happens if the Keystore key is invalidated by biometric change), backup behavior (encrypted backups must not include the passphrase in cleartext), and whether the design satisfies LGPD Art. 46 technical security measures for special category data.

---

## 6. Summary: What Needs to Happen Before First Feature Commit

In priority order:

1. **Fix the Gradle cache key path** (`libs.versions.toml` → `gradle/libs.versions.toml`) — 15-minute fix, do it now.
2. **Bump `minSdk` from 26 to 28** — one-line change, zero feature impact, removes dead compatibility surface.
3. **Update `lifecycleRuntimeKtx` and `activityCompose`** to current versions in `libs.versions.toml`.
4. **Create convention plugins** in `build-logic/` — 1-day investment, blocks nothing but should be done before module count grows.
5. **Implement `core:database` foundation** (Room + SQLCipher + Keystore key management, reviewed by CISO) — required before any data persistence.
6. **Implement `feature:health-connect` availability layer** — required before Onboarding can check device compatibility.
7. **Wire Compose Navigation** in `:app` — required before the first screen exists.
8. **Define error handling strategy** (`kotlin.Result` in domain layer) — required before the first use case is written.
9. **Enable `isMinifyEnabled = true`** for release builds and configure ProGuard — required before first internal track upload.
10. **Set Kover threshold** (60% for `core:domain`) when the first use case is merged.

Items 1–3 can be done in a single PR today. Items 4–8 are the Phase 2 technical bootstrap — they define the skeleton into which Onboarding feature code will be placed.

---

*CTO Agent — Health Insights | 2026-05-04*
