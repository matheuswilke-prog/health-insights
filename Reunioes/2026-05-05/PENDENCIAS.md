# Pendências — 2026-05-05

## Corrigir antes de qualquer outra coisa

**privacy-policy-v1.md — linha "NÃO coleta"**
Remover passos, sono e FC da lista de dados não coletados. Esses dados são candidatos a versões futuras — listá-los como "nunca coletados" cria compromisso errado. A policy deve listar apenas o que coleta hoje; quando um novo dado entrar, a policy é atualizada naquele momento.

---

## Tasks de engenharia pendentes (em ordem)

| # | Task | Observação |
|---|---|---|
| 5 | Infra: convention plugins em `build-logic/` | Reduz edições em cascata a cada update de SDK |
| 6 | Infra: Room + SQLCipher em `core:database` | Inclui `UserProfileEntity` + `ConsentRecordEntity` + `daily_calorie_target` |
| 7 | Infra: Health Connect permissions em `feature:health-connect` | Necessário para T4 funcionar |
| 8 | Implementar T1 — Welcome | Livre para dev |
| 9 | Implementar T2 — Profile | Livre para dev (lembrar do link de privacy policy) |
| 10 | Implementar T3 — Goal | Livre para dev |
| 11 | Implementar T4 — Consent | Copy final em `consent-copy-v1.1.md` |
| 12 | Implementar T5 — Connecting | Condicional — não aparece em modo manual |
| 13 | Implementar T6 — First Insight | `daily_calorie_target` em Room + SQLCipher, não DataStore |
