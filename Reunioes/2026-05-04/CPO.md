# CPO Product Review — Health Insights
**Date:** 2026-05-04
**Author:** CPO Agent
**Stage:** Post-Foundation (Sprint 2 complete) / Pre-Feature
**Next milestone:** Onboarding phase start

---

## Executive Summary

The project has a solid technical foundation. Two sprints of infrastructure work have produced a CI/CD pipeline that works and a 13-module Gradle project with Hilt wired up. That foundation is worth protecting — it will pay dividends throughout the build.

The product, however, does not yet exist. No screen has been designed, no user flow has been specified, and no product decision has been made that would let an engineer open `feature/onboarding` and write a line of UI code without guessing. This review identifies what must be resolved before any feature work begins.

---

## 1. Module Structure vs. Planned UX Flows — Alignment Assessment

### What exists
```
feature/onboarding
feature/dashboard
feature/health-connect
feature/insights
feature/sleep
feature/workouts
feature/settings
```

### Verdict: Structurally sound, one gap, one naming risk

**Aligned:**

- `feature/onboarding` correctly exists as a standalone module. Onboarding is a distinct first-run UX flow that ends with a permission grant and a first insight. It should not be embedded in dashboard or health-connect. Correct call.
- `feature/dashboard` maps directly to the Dashboard Diário phase. The module boundary is right.
- `feature/sleep` is a correct separation. Sleep analysis (Phase 5) is meaningfully different from the sleep *card* on the dashboard. The feature module supports the deeper analysis screen, while the sleep card data will be surfaced via `core/domain` use cases shared with dashboard. No problem here.
- `feature/settings` correctly exists as its own module. Settings is not a feature in the user-facing sense, but it carries LGPD-mandatory functionality (consent revocation, data export, data deletion) that is complex enough to merit isolation.
- `feature/workouts` anticipates future work. Not in the MVP as a standalone feature, but having the module scaffold costs nothing and prevents future module-splitting pain.

**The gap — `feature/steps` is missing:**

The backlog includes Phase 4 (Tendência de Passos — bar chart, moving average, interpreted headline, week navigation). There is no `feature/steps` module. Steps trend is a distinct screen, not a sub-screen of dashboard. Steps data on the dashboard is a card; the steps trend feature is a full analytical view. These belong in separate modules.

Recommendation: create `feature/steps` before Phase 4 begins. It does not block onboarding, but the CTO should be aware of this gap so it is scaffolded correctly alongside Phase 3 rather than bolted on later.

**The naming risk — `feature/health-connect`:**

`feature/health-connect` is not a user-facing feature. Health Connect is the data source — it is a technical integration layer, not something the user navigates to. There is no "Health Connect screen" in the product. This module appears to be the integration layer for Health Connect SDK interactions. If so, it belongs in `core/data` or `core/network`, not in `feature/`. Placing SDK integration code under `feature/` creates a false impression that this is a navigable user-facing screen and may cause future confusion about where permission request logic lives.

Recommendation: clarify with the CTO whether `feature/health-connect` contains:
(a) The Health Connect SDK integration and permission request logic — in which case it should move to `core/data` or remain in `feature/` only if it also owns the permission request UI (which is part of onboarding, not a standalone feature screen), or
(b) A screen the user navigates to — in which case the name needs to change because "Health Connect" is meaningless to users.

This needs a CTO + CPO alignment call before onboarding work starts, because the permission request flow in onboarding will need to call into whatever module owns Health Connect access.

**`feature/insights` — scope undefined:**

There is no Phase in the backlog that maps cleanly to a standalone "Insights" screen. Insights appear on the dashboard (cards), at the end of onboarding (first insight), and in the weekly summary. Is `feature/insights` the Weekly Summary (Phase 6)? A future AI/ML insights feature? An insights feed? The module exists without a product definition. This is not urgent, but the CPO needs to define what `feature/insights` owns before the CTO wires any navigation graph to it.

---

## 2. Product Decisions Blocking Onboarding — What Must Be Spec'd First

The following decisions are unresolved. Each one is a direct blocker on writing the first line of onboarding UI code. Ordering them by dependency:

### BLOCKER 1 — The value proposition screen: what is the promise?

The first screen of onboarding must communicate what the app does for the user before asking for anything. This requires a concrete decision on:

- **The headline**: one sentence that states the product's value. The PROJETO.txt has the product vision, but the onboarding screen needs a single, user-vocabulary headline. "Turn your Samsung Health data into insights" is a draft, not a spec.
- **The supporting visual**: static illustration, animated data visualization, screenshot of the dashboard, or pure typographic treatment. Each choice has different build cost and different credibility signal.
- **The CTA**: "Get Started" (generic), "Connect Samsung Health" (direct), "See what your data says" (benefit-framed). This copy is CMO territory and must be finalized before the screen is built.

**Who must resolve this:** CPO for the information architecture and CTA framing; CMO for the exact copy. Both must produce outputs before the Android Engineer touches `feature/onboarding`.

### BLOCKER 2 — The LGPD consent model: how many screens, what structure?

The backlog lists "4 separate consent screens for Steps, Sleep, Heart Rate, Exercise." This is a PM-level task decomposition, not a UX specification. Before building, the following must be decided:

- **Sequential or grouped**: Are these four separate full screens with individual accept/decline interactions, or a single scrollable screen with four labeled toggles? The CISO agent must advise on whether LGPD Art. 11 requires individual per-data-type consent screens or whether granular opt-ins on one screen satisfy the explicit consent requirement.
- **What happens if a user declines one type**: Can the app function with only steps consent granted? Can it function with no sleep consent? The empty states for every dashboard card depend on this answer.
- **Decline path**: If the user declines all four consents, where do they go? To a permanently restricted version of the app? To an exit screen? This is a product decision with significant retention implications.
- **The consent record schema**: timestamp, policy version, data type, boolean — the CISO must define this schema before `core/database` can store it.

**Who must resolve this:** CISO agent (compliance model and consent record schema), CPO (UX of the consent flow and decline path), CTO (data model and encrypted storage).

### BLOCKER 3 — The first insight: what data, what rule, what fallback?

The onboarding spec ends with "first insight delivered." But:

- What insight is shown? The backlog says it "uses Dashboard data" but does not specify which metric or what the insight rule is.
- What is the minimum data window needed to generate a valid insight? 7 days of steps? 1 night of sleep?
- What happens if the user has no data (brand new Samsung Health account, just bought the device)?

The "first insight" is the emotional payoff of the entire onboarding flow. It cannot be left as "show something from the Dashboard." The Data Insights Designer must define the rule before the Android Engineer can build it.

**Who must resolve this:** Data Insights Designer (insight rule and data minimum), CPO (the UX of the insight reveal), with a fallback UX specified by CPO for zero-data users.

### BLOCKER 4 — Navigation out of onboarding

Where does onboarding end? The backlog implies onboarding leads to the dashboard. But:

- Is onboarding a one-time flow that is never accessible again? Or can the user re-run it from settings (e.g., to add a consent they previously declined)?
- Does the app's navigation graph have a "logged in / first-run complete" state persisted locally, and if so, which module owns that state?

This is a CTO + CPO joint decision. The CTO needs to know what state to persist; the CPO needs to define what happens on re-launch.

---

## 3. Top UX Concern for the Onboarding Phase

**The hardest design decision: the partial-consent user path.**

If a user grants Steps consent but declines Sleep and Heart Rate, the dashboard will be partially populated. This creates the hardest product design problem in the entire onboarding phase — harder than the value proposition screen, harder than the permission request timing.

Here is why it is hard:

The dashboard is designed as a four-card view: steps, sleep, heart rate, last workout. If the user only consented to steps, three of the four cards are permanently empty — not loading, not erroring, but *intentionally absent by user choice*. The UX for this state is genuinely difficult:

- **Option A — Show empty cards with "You haven't granted access to this data"**: Honest, but creates a dashboard that looks broken 75% of the time for a partial-consent user. Every launch is a reminder of what they said no to. High pressure to consent, which risks being read as a dark pattern.
- **Option B — Hide cards for non-consented data types**: Cleaner experience, but the dashboard shrinks to one card for a partial-consent user. This may feel so sparse it communicates that the app is barely functional, increasing uninstall risk.
- **Option C — Placeholder cards with a soft "add more data" prompt**: A middle path — show the card layout but with a low-friction "enable" prompt inside the card instead of the data. This preserves visual structure and creates a non-pressured re-entry point for consent. This is my recommendation, but it requires the CISO to confirm that "enable" prompts inside the main app UI do not constitute repeated consent requests in violation of LGPD.

Until this question is answered — by the CPO in collaboration with the CISO — neither the onboarding consent screens nor the dashboard UiState for empty/declined can be properly specified. Everything downstream of this decision is underspecified.

**The second-hardest decision: the "explain before asking" screen count.**

The spec calls for a value proposition screen before any permission request. One screen. But the product promises insights about steps, sleep, heart rate, *and* workouts. That is four distinct value propositions. Can one static screen establish enough trust and value context to justify four Health Connect permission requests? Or does the onboarding need one screen per data type — showing what the user will get from that data, then requesting that specific permission?

The single-screen approach is simpler to build and faster to complete. The per-data-type approach has higher conversion because each permission request is contextually justified. For a health app where the entire product value depends on permission grants, I lean toward contextual justification. But this needs to be validated — potentially with a simple prototype test — before committing the architecture.

---

## 4. Information I Still Need from the Founder

Before the CPO can write the first screen spec for onboarding, I need answers to four questions. In priority order:

**Question 1 — Target user's Samsung Health maturity**

Is the primary user someone who has been using Samsung Health for months and has historical data, or could they be a new Samsung device owner with zero health history? This single answer changes:
- Whether a "no data" onboarding state is a rare edge case or a common first-run scenario
- Whether the "first insight" at the end of onboarding can rely on historical data or must work from day-zero data
- The emotional frame of the value proposition screen (discovery vs. retrospective analysis)

**Question 2 — The partial-consent product stance**

Is Health Insights a "grant all or it barely works" app, or is it designed to deliver value even with partial data access? This is a product strategy question, not a UX question. The answer determines whether the consent flow is sequential (you must grant at least one), optional (grant what you want), or all-or-nothing (grant all or the app is not useful). I cannot spec the consent decline path until the founder makes this call.

**Question 3 — Monetization gate placement**

The monetization model is freemium with a one-time purchase for extended history and advanced insights. Does any part of the onboarding or the first insight touch the paywall? Specifically: is the "first insight" at the end of onboarding always free, or could it be an "advanced" insight that prompts an upgrade? If there is a paywall anywhere in the onboarding flow, the CISO and CPO need to co-spec that interaction carefully — a user who sees a paywall before they've finished setup will uninstall. I need the founder's call on whether onboarding is 100% free-experience territory.

**Question 4 — The product language: Portuguese or English?**

PROJETO.txt is written in Portuguese. The backlog items use Portuguese names (Dashboard Diário, Tendência de Passos, Análise de Sono). The CMO agent has not yet defined the target market. Is this app targeted at Brazilian Samsung users (Portuguese UI)? At an international audience (English)? Or is it planned as a localized app that ships in both? The onboarding copy, consent language, and LGPD compliance notices are all written in a specific language. This decision must be made before a single word of UI copy is written.

---

## Cross-Functional Handoffs Required Before Onboarding Starts

| Input needed | From | To | Blocks |
|---|---|---|---|
| Consent language for 4 data types (LGPD-compliant) | CISO | CPO + Compliance Docs | Consent screens |
| Consent record schema (fields, encryption approach) | CISO | CTO | `core/database` |
| Partial-consent product stance | Founder | CPO | All consent UX |
| First insight rule + data minimum | Data Insights Designer | CPO + Android Engineer | Onboarding end state |
| Value proposition copy | CMO | CPO | Screen 1 |
| `feature/health-connect` module clarification | CTO | CPO | Permission request flow ownership |
| Target language (PT / EN) | Founder | CMO + Compliance Docs | All copy |
| Partial-consent dashboard UX (Option A/B/C above) | CISO + CPO | Android Engineer | Dashboard UiState |

---

## What the Foundation Gets Right

This section exists because the CPO's job is not only to identify gaps — it is to protect good decisions.

The module structure is correct in its core philosophy: feature modules own their screens; core modules own shared infrastructure. The separation of `core/domain` (use cases, business logic) from `core/data` (repository implementations) from `core/database` (Room entities and DAOs) will prevent the most common Android architecture anti-patterns. The developer has not taken shortcuts. Do not let scope pressure erode these boundaries.

The CI pipeline (Sprint 1) should be treated as a non-negotiable quality gate throughout feature development. Every feature added must pass lint, unit tests, and UI tests before merge. The CPO formally endorses this constraint: no feature spec I produce will include "this doesn't need tests" language. Quality gates stay.

The decision to store consent records with timestamp and policy version is the right call. When the app eventually ships an updated privacy policy, the consent record schema will allow the app to identify users who consented under a prior policy version and re-request consent where required. This is not over-engineering — it is LGPD compliance built correctly from the start.

---

## Open Questions for Next CPO Session

1. Has the CISO agent been invoked for the consent record schema and the LGPD consent screen model?
2. Has the CMO agent been invoked for the value proposition screen copy and the app's language decision?
3. Has the Data Insights Designer been invoked for the "first insight" rule?
4. Is the `feature/health-connect` module owned by CTO or does it include CPO-spec'd UI? Needs resolution.
5. What is the `feature/insights` module's product definition?

---

*Next CPO action: pending founder answers to the four questions above. Once received, the first deliverable is the complete onboarding screen-by-screen spec, including all states (happy path, empty, error, partial-consent) for every screen. Estimated scope: 6–8 screens with full state tables.*
