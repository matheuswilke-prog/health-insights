# Health Insights — CFO Analysis
**Date:** 2026-05-04
**Author:** CFO Agent (claude-sonnet-4-6)
**Stage:** Pre-revenue, pre-code

---

## Executive Summary

Health Insights has an enviable cost structure for a mobile app at this stage: near-zero cash burn, no vendor lock-in, and full offline operation that eliminates backend scaling risk. The financial risk is not today's spend — it is the monetization decision that must be locked in before the first paywall-adjacent screen is coded. This document establishes the financial baseline for all subsequent decisions.

---

## 1. Current Cost Structure

### GitHub Actions CI Usage

**Current configuration:** Each PR run takes ~20 minutes including emulator spin-up.

| Metric | Value |
|---|---|
| Free minutes/month (private repo) | 2,000 |
| Minutes per PR run | ~20 |
| PR runs per free tier before overage | **100 runs/month** |
| GitHub Actions overage rate (Linux) | $0.008/minute |

**Assumption:** Solo developer, active development phase. Estimate 2–4 PRs/day = ~60–120 CI runs/month.

**Risk assessment:**

- At 2 PRs/day (60 runs/month): 1,200 minutes used — **safely within free tier (60% utilization).**
- At 4 PRs/day (120 runs/month): 2,400 minutes — **overage of 400 minutes = ~$3.20/month.**
- At feature branch + PR discipline (feature branch CI + PR CI per feature): could hit 150–180 runs/month during sprint weeks = **$1.20–$14.40/month overage risk** in high-velocity periods.

**Mitigation (zero cost):**
1. Add a `paths-ignore` filter to skip CI on docs-only commits.
2. Separate lint/static analysis job from the emulator job — lint runs in <2 min and will not trigger the emulator on non-code changes.
3. Use `workflow_dispatch` for emulator runs during development; require emulator only on PRs targeting `main`.

**Projected implementation: none of these require paid tooling. Cash cost today: $0. Risk of overage: low if CI is scoped correctly.**

### Dependency Stack Cost

| Dependency | License | Cost |
|---|---|---|
| Kotlin, Compose, Room, Hilt, KSP | Apache 2.0 | $0 |
| SQLCipher for Android (Community) | Apache 2.0 | $0 |
| detekt, ktlint | Apache 2.0 / MIT | $0 |
| kover (code coverage) | Apache 2.0 | $0 |
| GitHub (private repo) | Free tier | $0 |
| Samsung Health SDK / Health Connect | Apache 2.0 | $0 |
| Google Play Developer account | One-time | **$25 (already paid or required)** |

**Total recurring monthly cash cost today: $0.**
**One-time cost if Play account not yet registered: $25.**

### Cost Structure Risk Flags

- **No cloud backend = no scaling cost cliff.** This is the correct architectural decision for this stage and this compliance profile. Do not introduce a backend until there is a revenue-justified reason.
- **GitHub Actions free tier is adequate for solo development at normal velocity.** Revisit if a second developer is added or CI run time increases (e.g., screenshot tests add 10–15 min/run).
- **SQLCipher open-source edition** covers the encryption requirement at zero cost. The commercial edition ($0 per-app fee, but requires a commercial license purchase for the Zetetic version) is not needed — the community Apache 2.0 edition is sufficient for an individual developer distributing via Play Store.

---

## 2. Monetization Model Recommendation

### Recommendation: Freemium with One-Time "Pro Unlock" Purchase

**Do not use a subscription at launch. Do not use ads. Ever.**

#### Model: Freemium + One-Time Purchase (OTP)

| Tier | Content | Price |
|---|---|---|
| Free | Dashboard (last 7 days), Steps trend (7 days), Sleep analysis (7 days), Onboarding | $0 |
| Pro (one-time) | All free features + 90-day history, Weekly summary, Data export, Future premium features | **R$ 29,90 / ~$5.99 USD** |

**Assumptions (explicit):**

1. Target market is primarily Brazil (Samsung Health is dominant in Brazil; LGPD targets Brazilian users; developer is Brazilian). Pricing is in BRL. USD equivalent shown for reference.
2. Play Store reduced rate program applies from day 1 of revenue (solo developer, well under $1M threshold). Developer net = **85% of gross** on all purchases.
3. No paid user acquisition in Year 1. All installs are organic (ASO + community + word of mouth).
4. The free tier must deliver genuine value — not a crippled demo. Users who never convert still improve Play Store ratings and organic visibility.
5. One-time purchase chosen over subscription because: (a) the feature set at v1 does not justify a recurring commitment, (b) a subscription requires continuous new value delivery to contain churn, (c) health-conscious Brazilian users are more receptive to "pay once, own it" for a privacy-first app.

#### Why Not Subscription at Launch

A subscription at launch with 5–6 features is a credibility problem, not a pricing problem. Users will compare the recurring cost against the feature depth and churn within 1–2 months. A $2.99/month subscription that churns after 1.5 months yields R$~9 net. A R$29.90 one-time purchase yields R$~25.42 net with no churn risk. The OTP wins on LTV at this feature depth.

**Revisit subscription** when the feature set includes: AI-generated weekly health narratives, goal tracking, cross-metric correlation insights, or any cloud-backed feature that has a marginal cost per user. That is a Year 2 conversation.

#### Why Not Ads

Ads in a health app violate two constraints simultaneously: (a) Samsung Health ToS prohibits use of SDK data for advertising purposes, (b) LGPD Article 11 requires explicit consent for health data processing — ad network SDKs are data processors and would require a DPA with each network. The legal and compliance overhead exceeds the revenue potential. CPM for Android health apps in Brazil: R$2–R$8. At 1,000 MAU with 5 ad impressions/day: ~R$300–R$1,200/month. This is not worth the compliance exposure. Escalation to CISO agent required if ads are reconsidered.

---

## 3. First Financial Decision Before Writing Paywall Code

**The one question that must be answered: What is in the free tier versus the Pro tier, and is the free tier genuinely useful on its own?**

This is not a vague product question. It is a financial decision with direct revenue impact. The feature gate determines:

1. **Conversion rate**: Too generous free tier → low conversion. Too aggressive gate → uninstalls, bad reviews, zero LTV.
2. **Refund rate**: If users feel the free tier was a bait-and-switch, they request refunds (Play Store allows 2-hour refund window, plus chargebacks). High refund rate damages Play Store standing.
3. **ASO rating**: Free users who are satisfied but not paying still give 5-star reviews, which drives organic installs. Free users who feel cheated give 1-star reviews. The feature gate directly determines your app store rating trajectory.

**Recommended gate (proposed, not final — align with CPO agent):**

- **Free:** Last 7 days of data across all metrics. Enough to validate the insight quality.
- **Pro:** 30-day history minimum, 90-day preferred. Weekly summary. Export. Any future features.

**The 7-day window is the gate, not the features.** Users see all features during the first week organically. After 7 days, they know the value and are the most qualified buyers. This is the highest-conversion gate design for a data-insight app.

**Action required:** Schedule a joint CFO + CPO session to finalize the feature gate before any screen with a "locked" state is designed. This decision cannot be reversed without a forced update and user communication.

---

## 4. LGPD Compliance Cost Estimate

Health data is a **special category** under LGPD Article 11. This is not a standard privacy policy — it requires explicit, granular, informed consent and documented data subject rights. Cost breakdown:

### Time Cost (Developer Hours)

| Item | Estimated Hours | Notes |
|---|---|---|
| Onboarding consent flow (UI + logic) | 8–12h | Already planned as MVP feature 1 |
| Data deletion mechanism (Article 18) | 4–6h | Room delete + confirmation UI |
| Data export (Article 18 portability right) | 6–10h | JSON/CSV export of local DB |
| Encryption implementation (SQLCipher) | 3–5h | One-time setup |
| Privacy policy drafting | 4–8h | Can use verified template; see below |
| In-app data viewing (what data is stored) | 3–5h | Settings screen data audit view |
| **Total developer time** | **28–46 hours** | |

At an opportunity cost of R$100/hour (conservative solo developer rate for an otherwise billable hour):
**Developer time LGPD cost: R$2,800–R$4,600 equivalent.**

### Cash Cost

| Item | Low Estimate | High Estimate | Notes |
|---|---|---|---|
| Privacy policy legal review | R$0 | R$800 | R$0 if using ANPD-aligned template + self-review; R$800 for 1h legal consultation |
| DPO designation (small developer exemption) | R$0 | R$0 | Solo developers processing data only for the app's own purpose are exempt from mandatory DPO appointment under ANPD guidance for micro-enterprises |
| ANPD registration | R$0 | R$0 | Not required for apps below the data volume threshold |
| Legal entity formation (for Play Store) | R$0 | R$1,500 | Can publish as individual (CPF) on Play Store in Brazil — no CNPJ required |
| **Total cash cost** | **R$0** | **R$2,300** | |

**Practical recommendation:** Use the ANPD's published template base for health app privacy policies. Draft it yourself against the ANPD checklist. Spend R$500–R$800 on a 1-hour review with a Brazilian data privacy lawyer before submission to Play Store. Total cash outlay: ~R$600. Total time: ~35 hours including lawyer prep.

**Critical compliance items that are zero-cost but non-optional:**

1. Explicit consent must be recorded with timestamp and version — not just a checkbox. Store consent state in EncryptedSharedPreferences.
2. Data deletion must be verifiable — the user must be able to confirm all local data is gone.
3. The privacy policy must be accessible from within the app (Settings screen) and from the Play Store listing page before install.
4. Samsung Health ToS compliance is separate from LGPD but overlapping: the ToS prohibits using health data for any purpose other than the app's declared health function. Ads, analytics SDKs, and third-party sharing are explicitly prohibited.

---

## 5. Unit Economics Baseline

All figures in BRL. Play Store fee: 15% (reduced rate program assumed from first purchase).

### Pricing Assumption

Pro Unlock price: **R$29,90** (approximately R$0.82/day amortized over 1 year — below the "feels expensive" threshold for Brazilian health app buyers).

**Net revenue per conversion: R$29,90 × 0.85 = R$25,42**

### Conversion Rate Assumptions

| Scenario | Free-to-Paid Conversion | Basis |
|---|---|---|
| Conservative | 3% | Industry floor for a new, unproven app |
| Base case | 5% | Realistic for a well-gated, privacy-first health app with strong free tier |
| Optimistic | 8% | Achievable if ASO is strong and the app gets organic editorial features |

**Model on the base case (5%). Build for the conservative case (3%). Celebrate if optimistic.**

### Revenue Model at Key Install Milestones

| Total Installs | Conversion (5%) | Paid Users | Gross Revenue | Net Revenue (85%) |
|---|---|---|---|---|
| 500 | 25 | 25 | R$747,50 | R$635,38 |
| 1,000 | 50 | 50 | R$1,495,00 | R$1,270,75 |
| 5,000 | 250 | 250 | R$7,475,00 | R$6,353,75 |
| 10,000 | 500 | 500 | R$14,950,00 | R$12,707,50 |

**Note:** These are cumulative revenue figures, not monthly. One-time purchase means no churn and no recurring billing, but also no ongoing revenue from existing users. New revenue requires new installs.

### ARPU (Average Revenue Per User — across all users including free)

At 5% conversion, R$29,90 OTP:
**ARPU = R$29,90 × 0.05 × 0.85 = R$1,27 per install**

This is a useful number for evaluating future paid acquisition: any paid UA channel must deliver installs below R$1,27 to be profitable at current conversion. This rules out virtually all paid UA channels until conversion is validated organically.

### LTV

For a one-time purchase model with no subscription, **LTV = net revenue per conversion = R$25,42.**

There is no ongoing revenue stream from converted users in the OTP model. LTV only grows if:
- A second product or feature tier is introduced (e.g., a "Pro+" tier at Year 2)
- A subscription option is added for new features

**This is the primary financial argument for eventually adding a subscription tier** — not to replace the OTP, but to offer a subscription for users who want the future roadmap (AI insights, cross-device sync if ever added, etc.) while the OTP remains available for users who want a fixed cost.

### LTV:CAC Target

- **Current CAC (organic):** R$0 direct cost. Time cost of ASO setup: ~10–15 hours = R$1,000–R$1,500 one-time, amortized across all installs.
- **LTV:CAC at 1,000 installs:** R$1,270 net revenue / R$1,500 ASO cost = 0.85:1 — not yet profitable on ASO alone at 1,000 installs.
- **LTV:CAC at 5,000 installs:** R$6,354 / R$1,500 = 4.24:1 — **healthy.** ASO investment pays back within the first 5,000 organic installs.
- **Minimum LTV:CAC for paid UA consideration:** 3:1. This requires a blended CAC below R$8.47. Current Play Store health app CPIs in Brazil are R$1.50–R$5.00 for organic-quality installs via ASO boost campaigns. **Paid UA becomes viable at ~5,000 organic installs if conversion validates at 5%+.**

### Break-Even on Development Time

**Assumption:** Solo developer invests 400 hours building MVP (conservative for a 6-feature app with CI/CD, LGPD compliance, and testing infrastructure).
**Opportunity cost:** R$100/hour = R$40,000 invested.
**Break-even install count:** R$40,000 / R$1,27 ARPU = **31,496 installs.**

At 5,000 installs/year (achievable with consistent ASO and community presence in Year 1), break-even on developer time is reached in approximately **Year 7** at current pricing — which confirms this is a passion project / portfolio project at this scale, not a primary income source. To compress break-even to 2 years, the app needs either 15,000+ installs/year or a subscription tier at R$9.90/month with 30%+ annual retention.

**This is not a discouragement — it is an accurate baseline.** Most successful solo app developers reach sustainability through scale + a subscription tier, not through OTP alone.

---

## 6. Key Financial Risks

| Risk | Probability | Financial Impact | Mitigation |
|---|---|---|---|
| CI minutes overage | Low | <R$80/month | Optimize CI triggers (docs filter, split jobs) |
| Play Store policy violation (health data) | Low | App removal = R$0 revenue | CISO agent review before publish |
| LGPD fine (ANPD) | Low while app is small | Up to 2% of Brazil revenue, max R$50M | Full compliance build from v1 |
| Samsung Health SDK revocation | Very Low | Feature removal required | Use Health Connect fallback as secondary data source |
| One-time purchase model failing to scale | Medium | LTV stagnation | Monitor conversion at 500 installs; trigger subscription model review at that point |
| Refund rate >5% | Low if feature gate is fair | Revenue reversal + Play Store flag | Define feature gate carefully before release |

---

## Summary of Decisions and Actions

| Decision | Status | Owner | Deadline |
|---|---|---|---|
| Monetization model: Freemium + OTP | **Recommended** | CFO + CEO | Before first paywall screen |
| Pro price: R$29,90 | **Proposed** | CFO + CMO | Before first paywall screen |
| Feature gate definition | **Pending CPO alignment** | CFO + CPO | Before first locked screen is designed |
| LGPD compliance scope | **Defined** | CFO + CISO | Before first onboarding screen is built |
| Play Store account registration | **Required** | Founder | Immediately (R$25 one-time) |
| CI optimization | **Recommended** | CTO | Before active development sprint |
| Subscription tier review | **Deferred to Year 2** | CFO | Trigger: 500 installs + conversion data |

---

*This document establishes the financial baseline for Health Insights as of 2026-05-04. All assumptions must be validated against actual data at the 500-install milestone. The monetization model recommendation (Freemium + OTP at R$29,90) and unit economics baseline (5% conversion, R$25,42 net LTV, R$1,27 ARPU) are subject to revision upon first real conversion data.*

*Next scheduled CFO review: upon first 100 installs or at 2026-08-04, whichever comes first.*
