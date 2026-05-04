# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Health Insights is a greenfield Android app that reads Samsung Health data (steps, sleep, heart rate, workouts, nutrition) and transforms it into actionable insights via charts and dashboards. No code exists yet — architecture and stack are being defined.

See `PROJETO.txt` for the full product vision, planned feature set, monetization model, and compliance requirements.

## Planned Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture (Repository + UseCases + ViewModels) |
| Data source | Samsung Health SDK / Android Health Connect |
| Local DB | Room + SQLCipher (encrypted) |
| DI | Hilt |
| Async | Coroutines + Flow |
| CI | GitHub Actions |

These are working decisions — validate with the CTO agent before implementing.

## Non-Negotiables

- **All health data stays on-device** by default. No network transmission without explicit user opt-in reviewed by the CISO agent.
- **Encrypt at rest** — Room + SQLCipher. Never plain SQLite for biometric fields.
- **LGPD compliance** — Health data is a special category under Art. 11. Explicit consent required. Data subject rights (view, export, delete) must be built from v1.
- **Samsung Health ToS** — No re-identification, no ad use, no data sale, minimum necessary access only.

## AI Agent Team

This project uses a C-suite of Claude subagents in `.claude/agents/`. Invoke them for decisions in their domain before implementing:

| Agent | Invoke when… |
|---|---|
| `ceo-health-insights` | Feature prioritization, roadmap, scope trade-offs, anything strategic |
| `cto-health-insights` | Stack choices, architecture patterns, library selection, API integration |
| `cpo-health-insights` | Screen design, user flows, feature specs, UX decisions |
| `cmo-health-insights` | Feature naming, app store copy, positioning, target audience |
| `cfo-health-insights` | Monetization model, pricing, build vs. buy, SDK cost evaluation |
| `ciso-health-insights` | Anything touching health data storage, consent flows, third-party SDKs |

**Default rule**: if a decision affects health data in any way, invoke the CISO agent first.

## When Code Exists

Once the Android project is initialized, update this file with:
- `./gradlew` commands for build, test, and lint
- Module structure and layer boundaries
- Any deviations from the planned stack above
