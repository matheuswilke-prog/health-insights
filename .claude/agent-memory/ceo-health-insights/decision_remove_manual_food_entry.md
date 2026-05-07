---
name: Decisão — remover entrada manual de refeições (2026-05-05)
description: CEO recomendou ao founder adiar a remoção da entrada manual até confirmar disponibilidade real do NutritionRecord no Health Connect para o usuário-alvo brasileiro.
type: project
---

**Decisão (2026-05-05):** NÃO remover a entrada manual de refeições do MVP até que a disponibilidade real do `NutritionRecord` no Health Connect seja confirmada para o perfil do usuário-alvo ("Marcos", Brasil, Galaxy Watch).

**Why:** o Health Connect expõe `NutritionRecord`, mas a probabilidade de o usuário-alvo ter esse dado preenchido automaticamente via Samsung Health / Galaxy Watch é baixa-a-moderada. Galaxy Watch não registra ingestão calórica autonomamente — o usuário precisa logar refeições manualmente em Samsung Health ou em um app compatível. "Marcos" faz tracking no ChatGPT hoje, não num app que sincronize com Health Connect. Remover a entrada manual antes de confirmar esse dado significa que o app poderia exibir balanço calórico incompleto (apenas gasto, sem ingestão), o que invalida a proposta de valor core.

**Risco principal identificado:** se `NutritionRecord` estiver vazio para a maioria dos usuários-alvo, o app vira um tracker de gasto calórico apenas — produto diferente do prometido ao Marcos.

**Posição estratégica:** a entrada manual não é contradição com "mais integrado que ChatGPT" — é a ponte de migração. Marcos já faz o tracking; o app só muda onde ele faz o registro. Sem a entrada manual, o diferencial "mais automático" só se materializa se o usuário já usava outro app de nutrição conectado ao Health Connect.

**How to apply:** antes de qualquer decisão de remover `food_log` / entrada manual, exigir dado concreto de qual % dos usuários-alvo no Brasil tem `NutritionRecord` populado no Health Connect. Até esse dado existir, manter a tabela `food_log` no escopo do MVP e a decisão do CTO sobre Room+SQLCipher inalterada.

**Escopo remanescente para Room+SQLCipher sem food_log:** `UserProfileEntity` (TMB + biometria), `ConsentRecordEntity` (LGPD), `daily_calorie_target` (goal derivado) — ainda justificam a dependência.
