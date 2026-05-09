# AGENTS.md

This file guides Codex when working in this repository. Keep it mirrored with `CLAUDE.md` unless a tool-specific instruction is truly necessary.

## Source of Truth

The product and execution source of truth is `docs/ROADMAP.md`.

When planning or implementing work:
1. Read `docs/ROADMAP.md` first.
2. Use `docs/BACKLOG.md` only as the executable task list derived from the roadmap.
3. Use `docs/specs/onboarding-spec-v1.0.md` only for onboarding details.
4. Do not revive old scope from deleted meeting notes or obsolete MVP docs.

If any document conflicts with `docs/ROADMAP.md`, the roadmap wins.

## Product Direction

Health Insights is an Android app that reads Health Connect data and helps the user understand caloric balance and weight progress.

MVP focus:
- Caloric balance.
- Weight progress.
- Health Connect reads for calories, nutrition, weight and workouts.
- On-device processing only.
- No account, no backend, no cloud sync.

Future app vision:
- Steps, sleep, workouts, heart rate and broader health insights can come after MVP.
- These future areas must not leak into MVP scope unless `docs/ROADMAP.md` is updated.

## Current MVP Flow

`Welcome -> Profile -> Goal -> Consent -> Dashboard`

There is no separate First Insight screen in the current MVP plan. The Dashboard is the payoff screen and should show daily target, caloric balance and weight context.

Sensitive onboarding data must not be persisted in plain DataStore. Profile, goal, daily target and consent records belong in Room + SQLCipher when the flow is completed. `onboarding_complete` may remain in plain DataStore.

## Build Commands

Run from `C:\Dev\Claude-Code\Health-insights`.

```bash
./gradlew ktlintCheck
./gradlew testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

On Windows, use `gradlew.bat`.

## Architecture

Modules:
- `:app`: app composition, NavHost, DI root, Application.
- `:core:domain`: pure Kotlin domain models, repository interfaces and use cases.
- `:core:database`: Room + SQLCipher, DAOs, entities and local repository implementations.
- `:core:ui`: shared Compose theme, tokens and UI components.
- `:core:common`: shared utilities.
- `:core:data`, `:core:network`: reserved, currently empty.
- `:feature:health-connect`: Health Connect wrapper and SDK mapping.
- `:feature:onboarding`: onboarding screens.
- `:feature:dashboard`: MVP dashboard.
- `:feature:settings`: privacy/settings.
- `:feature:insights`, `:feature:sleep`, `:feature:workouts`: post-MVP placeholders.

Layer rules:
- UI never touches Health Connect SDK types.
- Health Connect SDK types stay isolated in `:feature:health-connect`.
- Domain models are not SDK records or database entities.
- Use UDF: events up, state down.
- User-facing screens should model `Loading`, `Empty`, `Content` and `Error` states when applicable.
- No health data in logs.

## Privacy and Security

Non-negotiables:
- On-device only.
- No telemetry SDKs, ads SDKs or hosted crash reporters in MVP.
- Room + SQLCipher for sensitive health data and derived health data.
- Android Keystore/EncryptedSharedPreferences for SQLCipher key storage.
- Consent is explicit, granular, revocable and recorded.
- Health data must not be logged.

MVP data retention is 12 months by default, as defined in `docs/ROADMAP.md` and the current privacy policy.

Legal references:
- `docs/legal/consent-copy-v1.1.md`
- `docs/legal/privacy-policy-v1.md`

## Design

Visual source of truth:
- `docs/design/visual-system-v1.md`
- `core/ui/src/main/kotlin/com/healthinsights/core/ui/theme/Theme.kt`

Rules:
- Screen design is produced by external design tools, usually Claude Design or Figma.
- Codex should provide design prompts and validate returned handoffs; it should not be the main creative design tool.
- Store design prompts in `docs/design/prompts/`.
- Store design handoffs in `docs/design/handoffs/`.
- Use `HealthInsightsTheme` from `:core:ui`.
- Prefer `MaterialTheme.colorScheme.*` and shared semantic colors.
- Avoid hardcoded colors in features except previews/tests.
- No decorative gradients, saturated blobs, emojis or generic decorative icons.
- Lead with numbers and explicit semantic signals.
- Privacy microcopy must appear before critical CTAs.

## Development Workflow

Official workflow for medium/large features:

`PRD -> SPEC -> Conditional reviews -> External design -> Dev Plan -> Implementation -> Validation -> Final review`

Light workflow for small localized tasks:

`Mini-spec -> Implementation -> Validation`

Document locations:
- PRD: `docs/product/prd/<feature>.md`
- SPEC: `docs/specs/<feature>-spec.md`
- Design prompt: `docs/design/prompts/<feature>-design-prompt.md`
- Design handoff: `docs/design/handoffs/<feature>-design-handoff.md`
- Dev plan: `docs/dev-plans/<feature>-plan.md`
- Validation: `docs/validation/<feature>-validation.md`

Conditional review rules:
- Security/privacy review is required for Health Connect, consent, Room/SQLCipher, export/delete data, logs, permissions, dependencies or any health data.
- Infra/build review is required for Gradle, CI/CD, signing, R8/ProGuard, modules, dependencies or build performance.
- Product/UX review is required for onboarding, dashboard, consent copy, error/empty states, MVP scope or flow decisions.
- Visual review/design handoff is required for new screens and large UI changes.

Traditional backend load tests are not required while the app remains fully on-device with no shared infrastructure. Use local performance validation instead: cold start, Health Connect read time, Room/SQLCipher query time, memory, battery and behavior on mid-range devices.

## Testing

Tests are required for production changes.

Expected checks before handoff:
- `gradlew.bat ktlintCheck`
- `gradlew.bat testDebugUnitTest`
- `gradlew.bat :app:assembleDebug`

Use instrumented tests for behavior that requires Android/native runtime, such as real SQLCipher encryption.

## Agent Coordination

Claude Code and Codex share this repository. Keep `CLAUDE.md` and `AGENTS.md` mirrored so both agents operate from the same assumptions.

When a task touches health data, consent, storage, permissions or SDKs, apply the privacy/security rules above before implementation.

Current next work should come from `docs/ROADMAP.md` and `docs/BACKLOG.md`, not from historical meeting notes.
