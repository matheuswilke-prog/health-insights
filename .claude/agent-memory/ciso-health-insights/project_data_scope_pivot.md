---
name: Escopo de dados de saúde após pivot — v1.1 (2026-05-05)
description: Lista canônica dos tipos de dado de saúde tratados pelo Health Insights após pivot do produto para foco em balanço calórico. Substitui escopo da v1.0.
type: project
---

Tipos de dado de saúde aprovados para tratamento na v1.1 do Health Insights (decididos em 2026-05-05):

1. **Calorias gastas** — Health Connect: `TotalCaloriesBurned`, `ActiveCaloriesBurned`. Permissões: `READ_TOTAL_CALORIES_BURNED`, `READ_ACTIVE_CALORIES_BURNED`.
2. **Ingestão calórica** — Health Connect: `NutritionRecord` (apenas total de calorias). Permissão: `READ_NUTRITION`. Dado pode estar indisponível; fallback é lançamento manual local.
3. **Peso** — Health Connect: `WeightRecord`. Permissão: `READ_WEIGHT`.
4. **Treinos** — Health Connect: `ExerciseSessionRecord` (apenas tipo + duração). Permissão: `READ_EXERCISE`. **Proibido** ler GPS, rota, localização, FC durante atividade, ritmo, elevação.

**Removidos** da v1.0 e **proibidos** sem nova decisão CISO + nova versão de consent-copy + reconsentimento dos usuários: passos (`StepsRecord`), sono (`SleepSessionRecord`), frequência cardíaca (`HeartRateRecord`, `RestingHeartRateRecord`).

**Why:** pivot de produto para foco em déficit/superávit calórico tornou os 5 tipos da v1.0 desproporcionais (Art. 6 III — princípio da necessidade). Manter permissões não usadas violaria minimização e expandiria a superfície de risco de re-identificação (Samsung ToS).

**How to apply:** ao revisar qualquer feature ou SDK, conferir contra esta lista. Qualquer leitura de tipo fora dessa lista exige escalação ao founder + nova versão consent-copy + reconsentimento. **A `privacy-policy-v1.md` (vigente 2026-05-04) está desatualizada** — ainda lista passos/sono/FC; precisa virar v1.1 antes da publicação do consent-copy v1.1, senão há divergência entre o que o usuário consente (v1.1) e o que a policy diz (v1.0). Bloqueador a comunicar ao founder.
