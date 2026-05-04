# CISO Security & Compliance Review — Health Insights
**Date:** 2026-05-04
**Author:** CISO Agent
**Audience:** Founder (solo developer)
**Review scope:** Sprint 1–2 output — CI/CD infrastructure, app skeleton, dependency baseline
**Status:** First formal security and compliance review — pre-feature, pre-data-collection

---

## Executive Summary

The project is in its cleanest possible state from a compliance standpoint: no health data is collected, no data is stored, no network calls are made. This is the best moment to establish the security architecture and compliance baseline — before a single health data point is ever processed. The foundation decisions being made now are load-bearing for the entire product.

**Overall posture: Acceptable for the current stage with four required remediations before Phase 2 begins.**

The keystore signing approach is sound with two improvements needed. The LGPD compliance foundation does not yet exist — it must be fully built during Phase 1 before any Health Connect API call is made. One critical finding in the build configuration requires immediate correction regardless of phase.

---

## Critical Finding — Immediate Action Required

**`isMinifyEnabled = false` in the release build type (`app/build.gradle.kts`, line 33).**

This is not a Phase 2 item. It must be corrected now, before any feature code exists.

Shipping a release AAB with minification and code shrinking disabled exposes the full class hierarchy, method names, and internal architecture to any party who decompiles the release APK. For a health app, this increases the attack surface by making it trivial to locate and target data processing code, database access patterns, and encryption key handling logic. It also violates Play Store guidelines for production apps handling sensitive data.

**Required action:** Set `isMinifyEnabled = true` and configure ProGuard/R8 rules in `proguard-rules.pro` before the first production release is signed and uploaded. This must be done before the `build-release` job in `ci-main.yml` produces an artifact that reaches the Play Store.

---

## 1. Keystore Security Assessment

### Assessment

The release keystore is stored as a base64-encoded GitHub Actions secret (`KEYSTORE_BASE64`) and decoded to a temporary file in `/tmp/keystore-XXXXXX.jks` during the `build-release` job in `ci-main.yml`. Four signing-related secrets are present: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. A cleanup step using `if: always()` ensures the temporary file is deleted after every run, successful or not.

### Legal/policy basis

Google Play requires a stable signing key for the lifetime of the app. Loss of the keystore means permanent inability to publish updates to existing users. LGPD does not directly govern CI/CD key management, but inadequate key security constitutes an operational risk that could interrupt the data controller's ability to meet data subject rights obligations (Art. 18) via timely app updates.

### Required action

Two improvements are required before the first production release ships:

1. **Backup the keystore outside GitHub.** The `.jks` file and its passwords must be stored in a second secure location independent of GitHub (encrypted password manager with file attachment, or offline encrypted storage). If the GitHub repository is deleted, the secrets purged, or the account compromised, the ability to publish updates is permanently lost. There is no recovery path with Google Play if the keystore is gone.

2. **Add keystore secret rotation documentation.** Document the procedure for rotating secrets if they are ever compromised. This is a one-page operational procedure, not a code change.

### Acceptable implementation patterns

- GitHub Actions encrypted secrets for CI use — current approach is correct.
- Decoding to `/tmp` with `mktemp` random path — correct; prevents path collision between parallel runs.
- Passing signing credentials as Gradle `-P` flags — correct; they do not appear in environment variable dumps or process lists.
- `if: always()` cleanup — correct; prevents persistence on runner failure.
- Offline encrypted backup of the `.jks` file (e.g., Bitwarden secure note with file attachment, or a hardware-encrypted USB kept offline).

### Prohibited patterns

- Storing the keystore or passwords in the repository, even in a branch, tag, or stash.
- Logging the keystore path or any signing credential in any step output.
- Using a shared or organization-level secret that another repository could also access.
- Relying on GitHub as the sole copy of the keystore.

### Verification

- Confirm that `KEYSTORE_PATH` is never echoed or written to any log file in the workflow.
- Confirm that a backup copy of the `.jks` file and its passwords exists outside GitHub before the first Play Store upload.
- Confirm that `if: always()` is present on the cleanup step — it is, verified in `ci-main.yml` lines 400–408.
- Run the `build-release` job once and verify that no `.jks` file persists on the runner after completion (GitHub Actions runner is ephemeral by design, but the explicit cleanup is belt-and-suspenders).

**Current rating: PASS with two required improvements (backup + rotation docs) before first Play Store submission.**

---

## 2. LGPD Readiness Gap Analysis

### Assessment

Health Insights intends to read step count, sleep data, heart rate, and workout data from Health Connect. All of these are health data (dados de saúde) and constitute a sensitive personal data special category under LGPD Art. 11. Processing this data without the full compliance stack in place is a legal violation, not a process gap.

The current state: zero health data is collected or stored. This is compliant. The gap is that nothing is in place to enable compliant collection in Phase 2. Every item below is a legal prerequisite for the first `HealthConnectClient.readRecords()` call.

### Legal/policy basis

- **LGPD Art. 11** — health data processing requires explicit consent as the lawful basis.
- **LGPD Art. 8** — consent must be specific, informed, freely given, in writing or equivalent, and revocable.
- **LGPD Art. 18** — data subject rights must be operational before data is first collected.
- **LGPD Art. 48** — breach notification obligations require a working incident response plan.
- **Google Play Health Data Policy** — privacy policy required in Play Store listing and accessible from within the app before health data permissions are requested.
- **Health Connect API requirements** — declared use of Health Connect data types requires Google Play permissions declaration form completion before production submission.

### Required items before first health data collection — priority order

**Priority 1 — Blocking. Must exist before any Health Connect API call.**

1. **LGPD consent copy (final, approved text).** The specific language the user sees on the consent screen. Must name each data type, each purpose, the data controller identity, the retention period, the on-device-only guarantee, and the revocation mechanism. The CISO agent will draft this; the founder must approve it. This document becomes a legal artifact. See Section 4 of this review for required elements.

2. **Consent record storage design.** Every consent act must be logged: timestamp, consent version number, which data types were accepted. This record must itself be stored encrypted on device. The schema must be defined before `core/database` is implemented. The consent record is evidence of LGPD compliance — it must survive app updates and data deletions (it is the record that the user requested deletion, which is itself a legally significant event).

3. **Consent revocation path.** The user must be able to withdraw consent at any time from within the app. This means a settings screen exists with a "Revoke data access and delete my data" action before any data is collected. It does not need to be beautiful for Phase 2; it must exist and work.

4. **Data deletion implementation.** LGPD Art. 18, VI and VII — the user has the right to delete data processed with consent. The delete path must be implemented in `core/database` when Room is added. A deletion of all health data must be complete: every table, every cache, every backup. Verify with a test that checks the database is empty after deletion.

5. **Privacy policy document (accessible in-app and in Play Store listing).** A published URL is required for the Play Store health data declaration. Must be accessible from the consent screen and from app settings. Must cover: data controller identity, data types collected, purposes, retention periods, data subject rights procedure, and ANPD contact. The founder must either draft this or engage a lawyer with LGPD expertise. The CISO agent can provide the required section structure, but the final text is a legal document that requires human approval.

**Priority 2 — Required before Phase 2 is complete (before first TestFlight/internal track upload).**

6. **Encryption key setup using Android Keystore.** Before Room + SQLCipher is initialized, the key management architecture must be designed. The SQLCipher database key must be generated and stored in the Android Keystore system — never in SharedPreferences, never hardcoded. The `core/database` module must not be written without this being the specified implementation.

7. **Data minimization verification checklist.** A written (even a simple markdown file in the repo) list of exactly which Health Connect record types will be read, with a one-line justification for each. This is required to complete the Google Play permissions declaration and is good discipline for the minimum necessary principle.

**Priority 3 — Required before public release (Play Store).**

8. **Google Play health permissions declaration form.** Submitted in Play Console. Requires the approved data minimization list and the published privacy policy URL.

9. **Data subject rights implementation verification.** Confirmation test coverage for: data access (user can see their data), data deletion (verified complete), consent revocation (verified revokes Health Connect permissions and triggers deletion).

10. **Incident response checklist.** A simple document (even a `SECURITY.md` in the repository) defining the detection-to-ANPD-notification procedure per LGPD Art. 48. For a solo developer this is a one-page checklist.

### Verification

Phase 2 (Onboarding) is gated: no PR to `feature/onboarding` merges to `main` until items 1 through 5 are complete and reviewed by the founder. The CISO agent must be invoked to review the consent screen implementation before any Phase 2 code ships.

---

## 3. Health Connect Permission Scope

### Assessment

The minimum necessary principle (LGPD Art. 6, III and Samsung Health ToS) requires requesting only the data types needed for features that exist in the current release. Speculative permission requests for future features are prohibited.

The v1 feature set as defined in `PROJETO.txt` and the CEO review: steps, sleep, heart rate, weekly summary. Workouts are listed but not clearly defined as v1 scope. Nutrition is listed in the Samsung Health data inventory but is not in the v1 feature list.

### Legal/policy basis

- **LGPD Art. 6, III** — data minimization: processing must be limited to the minimum necessary for its purposes.
- **Samsung Health / Health Connect ToS** — minimum necessary access only; do not request access to all available data types speculatively.
- **Google Play Health Data Policy** — every requested Health Connect permission must correspond to a feature declared in the Play Store listing.

### Required action

Before implementing the Health Connect permission request in Phase 2, produce a data type decision table (see below) and have the CISO agent approve it. Any data type not on the approved list must not appear in the `AndroidManifest.xml` health permissions block.

### MVP v1 — Approved data types

| Health Connect Record Type | Feature | Justification | Status |
|---|---|---|---|
| `StepsRecord` | Steps trends, dashboard summary | Core v1 feature explicitly listed | **Approved** |
| `SleepSessionRecord` | Sleep analysis | Core v1 feature explicitly listed | **Approved** |
| `RestingHeartRateRecord` | Heart rate history | Core v1 feature explicitly listed | **Approved** |
| `HeartRateRecord` | Heart rate history, workout context | Supports heart rate feature | **Approved** |
| `ExerciseSessionRecord` | Weekly summary context | Listed as v1 feature, limited to session metadata only — not GPS, not route | **Conditionally approved — session data only** |

### Data types that must NOT be requested in v1

| Health Connect Record Type | Reason for exclusion |
|---|---|
| `NutritionRecord` | Not a v1 feature. Requesting without a feature is speculative collection. Prohibited. |
| `BloodGlucoseRecord` | Not in product scope. Clinical data type — requires specific purpose justification. Prohibited. |
| `BloodPressureRecord` | Not in product scope. Clinical data type. Prohibited. |
| `BodyFatRecord` | Not a v1 feature. Prohibited. |
| `OxygenSaturationRecord` | Not in product scope. Clinical data type. Prohibited. |
| `MenstrualCycleRecord` | Not in product scope. Highly sensitive. Prohibited in v1. |
| `LocationRecord` / Route data | Not required for any v1 insight. Location is a separate sensitive category. Prohibited. |
| `READ_HEALTH_DATA_HISTORY` (background read) | Background health data access requires additional Play Store justification. Do not request unless a specific feature requires it — none do in v1. |

### Acceptable implementation patterns

- Request permissions at the point of first use (onboarding), not at app install.
- Display the pre-permission education screen before the system permission dialog.
- Request permissions individually by data type — do not request all at once.
- Handle permission denial gracefully: show which features are unavailable without that data type. Do not block the entire app.

### Prohibited patterns

- Requesting Health Connect permissions for data types not mapped to an existing, implemented feature.
- Requesting permissions at app startup before any explanation is shown.
- Using granted permissions to read data types beyond what was described in the consent screen.
- Caching raw Health Connect records beyond the period required for the feature computation.

### Verification

- `AndroidManifest.xml` must be reviewed before any Phase 2 PR merges. The CISO agent will audit the `<uses-permission>` block against this approved list.
- The Google Play permissions declaration form must map each requested permission to a specific declared feature.

---

## 4. Consent Copy Requirements

### Assessment

The consent screen in Phase 2 is the legal mechanism through which Health Insights obtains the explicit consent required by LGPD Art. 11, II, a to process health data. A consent screen that is vague, incomplete, or uses legalese is legally defective — it does not constitute valid LGPD consent regardless of whether the user tapped "I agree."

The consent screen language must be finalized and approved before any Phase 2 code is written. This is because the consent copy is not just UI text — it defines the legal scope of what the app is permitted to do with the data.

### Legal/policy basis

- **LGPD Art. 8** — consent requirements: written form (or equivalent), specific, informed, unambiguous.
- **LGPD Art. 9** — the data controller must inform the data subject of: purpose, form and duration of processing, identity of the controller, information about third-party sharing, data subject rights, and consequences of refusing consent.
- **LGPD Art. 11** — health data requires explicit consent (not merely informed consent — the standard is higher).
- **Google Play Health Data Policy** — in-app disclosure requirements must be met before health permissions are requested.

### Required elements — the consent screen must contain all of the following

**Element 1 — Controller identity**
The app must identify itself as the data controller. Required text (adapt name):
> "Health Insights (operated by [founder full name or company name]) is the controller of your health data."

**Element 2 — Data types listed explicitly, individually**
Each data type must be named. No bundling into "health data." Required format:
> "To provide your insights, Health Insights will read from Health Connect:
> - Step count (daily and historical)
> - Sleep session duration and timing
> - Resting heart rate and heart rate measurements
> - Exercise session records (type and duration only — no GPS or route data)"

**Element 3 — Purpose binding, one sentence per data type**
Each data type must be linked to its specific purpose. Required format:
> "Step count: to show you daily activity trends and weekly comparisons.
> Sleep data: to show you sleep duration trends and consistency scores.
> Heart rate: to show you resting heart rate history over time.
> Exercise sessions: to include workout days in your weekly activity summary."

**Element 4 — On-device-only guarantee**
This is Health Insights' strongest privacy claim and a product differentiator. It must be stated plainly:
> "Your health data never leaves your device. It is not uploaded to any server, not shared with any third party, and not used for advertising."

**Element 5 — Retention period**
> "Your health data is stored on your device for up to 12 months. You can change this in Settings at any time."

**Element 6 — Data subject rights summary**
> "You can view, export, or permanently delete all your health data from Settings > Privacy > My Data at any time."

**Element 7 — Consent revocation**
> "You can withdraw this consent at any time from Settings > Privacy > Revoke Health Access. Withdrawing consent will stop Health Insights from reading new health data. Your previously stored data will be deleted."

**Element 8 — Link to privacy policy**
A tappable link to the full privacy policy must be present on the consent screen. Required text:
> "Read our full Privacy Policy [link]."

**Element 9 — Explicit consent action**
The consent button must be affirmative and unambiguous. The label must not be "OK," "Continue," or "Next."
Required: **"I agree — give Health Insights access to my health data"** or equivalent.
The consent must be a separate, deliberate act — not part of a general onboarding "get started" flow.

**Element 10 — Denial path**
The user must be able to decline without being punished. Required:
A clearly visible "No thanks — I'll use limited features" option that proceeds to a degraded-but-functional app state.

### Acceptable implementation patterns

- A full-screen pre-permission education screen (not a dialog) with scrollable content covering all 10 elements above.
- Separate consent screen from the system Health Connect permission dialog — the user sees the consent screen first, understands what they are agreeing to, then the OS dialog appears.
- The consent record (timestamp + version) stored immediately when the user taps "I agree," before any Health Connect read is attempted.

### Prohibited patterns

- "By using this app you agree to our terms" passive consent — not LGPD compliant.
- Consent bundled into a general terms of service acceptance.
- Consent obtained after the first health data read has already occurred.
- Consent copy written in legal language the target user cannot understand.
- Consent screen that cannot be scrolled to reveal all required elements before the user can tap "I agree."
- A single "Accept all" button that does not name the specific data types being consented to.

### Verification

The CISO agent must review and explicitly approve the consent screen implementation (UI + consent record storage) before the `feature/onboarding` branch merges to `main`. The review will check:
1. All 10 required elements are present.
2. The consent record is written to the encrypted database before any Health Connect read.
3. The denial path works and leads to a functional (if limited) app state.
4. The privacy policy link resolves to an accessible, published URL.

---

## 5. First Privacy Risk to Address

### Assessment

**The single biggest compliance risk in the current project state is the absence of a finalized, legally defensible consent copy, combined with the structural temptation to begin Phase 2 (Onboarding) implementation before that copy is approved.**

This is not a speculative future risk. The CEO review (2026-05-04) identifies this as the active blocker and recommends assigning the CISO agent to draft the consent copy. The risk is concrete: if a developer begins building the onboarding flow with placeholder consent text ("We will explain this later"), the consent flow gets shipped with that placeholder or with inadequate text that was never reviewed. This has happened in every health app project that has prioritized "build the screen first, write the words later."

### Legal/policy basis

- **LGPD Art. 11, II, a** — processing of health data requires explicit consent. If the consent is defective (vague, bundled, or not purposefully specific), the entire processing is unlawful, not just the consent step.
- **LGPD Art. 8, § 1** — the burden of proving that consent was obtained validly falls on the data controller. A defective consent screen means the controller cannot prove lawful basis for any data already collected.
- This is not a theoretical risk. ANPD (Autoridade Nacional de Proteção de Dados) has sanctioned apps for inadequate health data consent. Health data violations attract the highest sanction level under LGPD.

### Required action

**Before any Phase 2 code is written:**

1. The CISO agent drafts the full consent copy covering all 10 required elements from Section 4.
2. The founder reviews and approves the consent copy as a legal document.
3. The approved consent copy is stored as a versioned artifact in the repository (e.g., `docs/consent/consent-v1.md`) before any onboarding UI is implemented.
4. The CPO agent receives the approved text as an input specification — not the other way around.

The sequence must be: **Consent text approved → UI built around it.** Never: **UI built → consent text filled in later.**

### Acceptable implementation patterns

- Consent copy stored in a `strings.xml` resource file with a version string (e.g., `consent_version = "1.0"`) that is embedded in the consent record stored in the database.
- A consent version bump triggers a re-consent flow on next app open — required if the data types or purposes change.
- Legal review of the consent text by a Brazilian LGPD practitioner before the app is published to the Play Store (recommended, not required for Phase 2 development, but required before public release).

### Prohibited patterns

- Merging any onboarding code that contains placeholder consent text.
- Treating consent copy as a copywriting task rather than a legal compliance task.
- Changing the consent copy post-launch without incrementing the consent version and triggering re-consent.

### Verification

The `feature/onboarding` branch is blocked from merging to `main` until the CISO agent has issued a written approval of the consent screen implementation. This review gates Phase 2 completion. The CEO review independently confirms this sequencing requirement.

---

## Compliance Decisions Register

| Decision | Status | Owner | Blocking |
|---|---|---|---|
| Keystore backup outside GitHub | **Required — not done** | Founder | Before first Play Store upload |
| `isMinifyEnabled = true` in release build | **Required — not done** | CTO/Founder | Before first release AAB ships |
| Consent copy finalized | **Required — not done** | CISO (draft) + Founder (approve) | Phase 2 start |
| Privacy policy document | **Required — not done** | Founder (legal review recommended) | Before Play Store submission |
| Consent record storage schema | **Required — not designed** | CTO (design) + CISO (approve) | Phase 2 implementation |
| Consent revocation UI | **Required — not built** | CPO (UX) + CISO (verify) | Phase 2 completion |
| Data deletion implementation | **Required — not built** | CTO (implement) + CISO (test) | Phase 2 completion |
| Encryption key via Android Keystore | **Required — not designed** | CTO (design) + CISO (approve) | Phase 3 (any data storage) |
| Health Connect permission scope approved | **Approved in this document** | CISO | Phase 2 implementation |
| SDK inventory (current) | **Clean — zero third-party SDKs** | CISO (monitor) | Ongoing |
| Google Play permissions declaration | **Required — not submitted** | Founder | Before Play Store submission |
| Incident response checklist | **Required — not documented** | Founder | Before public release |

---

## Open Risks

| Risk | Severity | Likelihood | Mitigation |
|---|---|---|---|
| Onboarding built before consent copy approved | **Critical** | Medium | Enforce Phase 1 gate — no Phase 2 code without approved consent text |
| Keystore loss (GitHub only copy) | **Critical** | Low | Backup to offline encrypted storage before first Play Store upload |
| Release build ships without minification | **High** | High (currently `false`) | Correct `isMinifyEnabled` before any production AAB is built |
| Future SDK adds telemetry | **High** | Medium | CISO review required before any new dependency is added |
| Consent copy legally defective | **High** | Medium | Legal review by LGPD practitioner before public release |
| Data deletion incomplete on revocation | **High** | Medium | Verified by test coverage before Phase 2 merges |

---

## Cross-functional Actions Issued

- **CTO agent:** Set `isMinifyEnabled = true` and configure ProGuard/R8 rules before the first production AAB build. Design the Android Keystore-based encryption key management for SQLCipher before `core/database` is implemented.
- **CPO agent:** The consent screen must be built from the CISO-approved text (Section 4 of this document). The UI implementation follows the text specification — the text is not retrofitted to fit the UI.
- **CEO agent:** Phase 2 gate is confirmed: consent copy and privacy policy must be complete and CISO-approved before any `feature/onboarding` code ships to `main`.
- **Founder:** Review and approve consent copy draft when produced. Confirm keystore backup exists before first Play Store upload.

---

*CISO Agent — Health Insights | Review date: 2026-05-04 | Next review: Post-Phase 2 completion (consent flow implementation review)*
