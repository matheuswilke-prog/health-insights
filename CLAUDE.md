# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Health Insights is an Android app that reads Samsung Health data (via Health Connect) and transforms it into actionable insights. The product focus is **caloric deficit/surplus tracking for weight loss or gain** — not a general health dashboard. All processing stays on-device. No backend, no accounts, no cloud sync.

Target persona: "Marcos", 32, Brazilian, Galaxy Watch owner, trains 3–5x/week, already does manual calorie tracking and wants something more integrated and automatic.

Full product specs: `docs/MVP_PLAN.md`. Current open tasks: `Reunioes/2026-05-05/PENDENCIAS.md`.

## Build Commands

Run from `C:\Dev\Claude-Code\Health-insights\` using `gradlew.bat` (Windows) or `./gradlew` (Linux).

```bash
# Style and static analysis
./gradlew :app:ktlintCheck
./gradlew :app:ktlintFormat
./gradlew :app:detekt
./gradlew :app:lintDebug

# Tests
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest       # requires emulator/device
./gradlew :app:koverXmlReport
./gradlew :app:koverVerify

# Build
./gradlew :app:assembleDebug
./gradlew :app:bundleRelease                   # requires signing config

# Single test class
./gradlew :module:testDebugUnitTest --tests "com.healthinsights.SomeTest"
```

Replace `:app:` with any module (e.g., `:core:database:testDebugUnitTest`).

## Architecture

### Module structure

```
:app                       → NavHost, DI root, Application class, Compose theme
:core:domain               → UseCases, domain models, repository interfaces (pure Kotlin, no Android)
:core:database             → Room + SQLCipher, DAOs, entities, migrations
:core:ui                   → Shared Compose components (currently empty — theme lives in :app)
:core:common               → Shared utilities (currently empty)
:core:data / :core:network → Currently empty
:feature:onboarding        → Value prop + LGPD consent screens (WelcomeScreen done, others pending)
:feature:health-connect    → Health Connect SDK wrapper (isolates SDK from domain layer)
:feature:dashboard         → Daily caloric balance (empty)
:feature:insights          → Weekly insight generation (empty)
:feature:settings          → Consent revocation, export, delete-all (empty)
:feature:sleep / :feature:workouts → Future screens (empty)
```

**Implemented so far:** `:app`, `:core:domain`, `:core:database`, `:feature:health-connect`, `:feature:onboarding` (partial — WelcomeScreen only).

### Convention plugins (`build-logic/src/main/kotlin/`)

| Plugin | Used by |
|--------|---------|
| `healthinsights.android.application` | `:app` |
| `healthinsights.android.library` | Android library modules |
| `healthinsights.android.library.compose` | Modules with Compose UI |
| `healthinsights.android.feature` | All `:feature:*` modules — extends `library.compose` + `hilt` |
| `healthinsights.android.hilt` | Modules using DI |
| `healthinsights.kotlin.library` | Pure Kotlin modules (`:core:domain`) |

Convention plugins use `versionCatalogs.named("libs")` (not the type-safe `libs` accessor) because Gradle 9.x doesn't generate type-safe accessors for precompiled script plugins in included builds. Regular module `build.gradle.kts` files use the type-safe `libs.xxx` accessor normally.

### Layer rules

- **Domain models ≠ SDK types** — Health Connect records are mapped to domain models in `:feature:health-connect`. Nothing above that layer touches Health Connect types directly.
- **UDF only** — UI events bubble up, state flows down. `UiState` is a sealed interface with `Loading | Empty | Content | Error` variants. Every screen must handle all four.
- **No health data in logs** — Logger must sanitize before output. This is a CISO standing order, not a preference.

### Storage decisions

| Data type | Storage | Reason |
|---|---|---|
| Biometric-derived values, `UserProfileEntity` | Room + SQLCipher | LGPD Art. 11 — biometric derivatives are sensitive health data |
| Consent records | Room + SQLCipher | Requires timestamp + policy version, tamper-evident |
| Non-sensitive flags (`onboarding_complete`, theme) | DataStore plain | Not health data |

SQLCipher key: 32 random bytes generated via `SecureRandom`, stored encrypted in `EncryptedSharedPreferences` backed by Android Keystore (`DatabaseKeyProvider.kt`). Uses `SupportOpenHelperFactory` (SQLCipher 4.x API — package is `net.zetetic.database.sqlcipher`, not the old `net.sqlcipher.database`).

## Non-Negotiables

- **On-device only** — no network calls for health data. CISO must sign off before any external transmission.
- **Encrypt at rest** — Room + SQLCipher for all biometric or derived health fields. Never plain SQLite.
- **LGPD compliance** — explicit, granular, revocable consent per data type (steps, sleep, heart rate, exercise). Consent record stored with timestamp + policy version.
- **No telemetry SDKs** — Firebase Analytics, Crashlytics, Mixpanel, Amplitude, AdMob are banned in the MVP.
- **Health Connect permissions** — `READ_STEPS`, `READ_SLEEP`, `READ_HEART_RATE`, `READ_EXERCISE`. Read-only. No background reads in MVP.

## Visual System

**Fonte da verdade:** `docs/design/visual-system-v1.md`. Toda decisão de cor, tipografia, espaço e copy de UI deve vir desse documento. Specs textuais (`docs/specs/*`) descrevem **o quê**; o visual system descreve **como deve parecer**.

**Tokens em código:** `core/ui/src/main/kotlin/com/healthinsights/core/ui/theme/Theme.kt` — `HealthInsightsTheme { … }` envolve toda screen. Use `MaterialTheme.colorScheme.*`, nunca `Color(0xFF…)` hardcoded em features.

**Mockups de referência:** projeto Anthropic Designer "health-insights" → `Health Insights.html`. Contém 3 variações por tela. Quando reimplementar uma screen, cite a variante escolhida no PR (ex: "Welcome A — Apple Health minimal").

**Princípios anti-slop (linha vermelha):**
1. O número primeiro — headlines liderados por dado, não por adjetivos.
2. Sinal explícito (+/− e cor semântica `deficit`/`surplus`/`maintain`).
3. Background neutro `#FAFAF7`. **Banidos:** gradientes saturados, círculos decorativos, roxo `#4F3D8A`.
4. Microcopy de privacidade visível acima de todo CTA crítico.
5. Sem ícones decorativos genéricos, sem emojis. Só dado, gráfico ou stripe de placeholder.

**Refactor pendente:** `feature/onboarding/.../WelcomeScreen.kt` está fora do sistema (gradiente roxo + "HI" em círculo + curva inferior). Substituir seguindo `visual-system-v1.md` § 8 antes de iniciar Telas 2–6.


## Test Strategy

Tests are a pre-condition for merging.

| Level | Target | Tools |
|---|---|---|
| Unit (domain + viewmodel) | ≥ 85% coverage | JUnit4, MockK, kotlinx-coroutines-test, Robolectric |
| Integration (data layer) | 100% DAOs + repos | Room in-memory DB, Health Connect fakes |
| UI (Compose) | Happy path + empty + error per screen | `createComposeRule`, Robolectric |
| Instrumented (E2E) | 1 test per critical flow | Espresso + UI Automator |

Security-specific tests required:
- SQLCipher database does not open with empty or wrong key.
- Logs never contain numeric HR, steps, or sleep values (regex check on log capture).

## CI Pipeline (GitHub Actions)

PR gate (cheap checks first):
```
lint (ktlint + detekt) → unit tests + kover → android lint → build debug APK → UI tests (emulator API 34)
```

`ci-main.yml` builds and signs the release AAB on merge to `main`. Branch protection on `main` requires all 5 jobs green.

## AI Agent Team

Agents live in `.claude/agents/`. Invoke them before committing to decisions in their domain.

| Agent | Domain |
|---|---|
| `ceo-health-insights` | Feature scope, roadmap, strategic trade-offs |
| `cto-health-insights` | Architecture, library selection, API integration |
| `cpo-health-insights` | Screen specs, user flows, UX decisions |
| `cmo-health-insights` | Copy, App Store positioning, target audience |
| `cfo-health-insights` | Monetization, pricing, SDK cost |
| `ciso-health-insights` | Health data storage, consent flows, third-party SDKs |
| `android-engineer-health-insights` | Kotlin/Compose implementation |
| `qa-test-engineer-health-insights` | Test strategy, coverage, fixtures |
| `data-insights-designer-health-insights` | Insight rules, thresholds, copy templates |
| `security-reviewer-health-insights` | Code-level security review on PRs |
| `release-engineer-health-insights` | CI/CD, GitHub Actions, versioning |
| `compliance-docs-health-insights` | Privacy policy, Data Safety form, LGPD docs |

**Default rule**: any decision touching health data → invoke CISO first.

Agent memory is stored per-agent in `.claude/agent-memory/<agent-name>/`.

## Key Docs

- `docs/MVP_PLAN.md` — Full MVP scope, architecture decisions, test strategy, rejected features
- `docs/specs/onboarding-spec-v1.0.md` — Onboarding screen-by-screen spec
- `docs/legal/consent-copy-v1.1.md` — Final consent copy (CISO-approved)
- `docs/legal/POLITICA_DE_PRIVACIDADE.md` — Privacy policy (PT-BR)
- `docs/BACKLOG.md` — Feature backlog
- `docs/CI_SETUP.md` — GitHub repo + secrets setup guide
- `Reunioes/` — Meeting notes and pending tasks by date
