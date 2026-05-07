---
name: Classificação LGPD — daily_calorie_target e dados de Tela 2 (Profile)
description: Decisões CISO de 2026-05-05 sobre como classificar daily_calorie_target (valor derivado) e dados antropométricos da Tela 2 (peso/altura/idade/sexo) para fins de retenção e consentimento.
type: project
---

Decisões de classificação LGPD tomadas em 2026-05-05:

**1. `daily_calorie_target` (valor derivado em DataStore).** Classificado como **dado de saúde de categoria especial — Art. 11**. Justificativa: é função direta de TMB (peso/altura/idade/sexo) + média de gasto calórico (TotalCaloriesBurned dos últimos 15 dias). É um dado biométrico inferido. Mesmo sendo "apenas um número derivado", revela informação sobre o metabolismo basal e o objetivo de perda/ganho de peso do usuário. Tratamento: deve ser excluído quando o usuário revoga consentimento ou apaga dados; deve ser exportado no fluxo de portabilidade (Art. 18 V); deve ser eliminado na desinstalação.

**2. Dados de Tela 2 (peso atual, altura, idade, sexo biológico).** Classificados como **dado de saúde — Art. 11** (peso e sexo biológico explicitamente; altura e idade são dados pessoais Art. 5 que, combinados com peso/sexo, geram dado de saúde). Tratamento conjunto sob Art. 11. Justificativa: peso é dado de saúde por natureza; sexo biológico em contexto de cálculo metabólico é dado de saúde; altura e idade isoladamente seriam Art. 5, mas o produto os usa exclusivamente em contexto biométrico (TMB).

**3. Microcopy "Esses dados ficam só no seu aparelho e podem ser editados depois" (Tela 2).** **Insuficiente** para constituir consentimento ou aviso completo nos termos da LGPD. **Necessário ajuste:** adicionar link para Política de Privacidade na própria Tela 2, abaixo da microcopy, com label "Como tratamos esses dados". Esse link deve estar visível antes do CTA "Continuar". Justificativa: Art. 9 exige informação prévia sobre controlador, finalidade, retenção e direitos — microcopy "fica no aparelho" cobre apenas a parte de transmissão, não as outras quatro. Como Tela 2 é entrada de dados de saúde, o aviso completo é obrigatório.

**Why:** classificar derivados como Art. 5 facilitaria retenção mais permissiva mas cria risco de auditoria — ANPD trata inferências biométricas como Art. 11 desde o Enunciado CD/ANPD nº 1/2023. Melhor classificar como Art. 11 e tratar uniformemente.

**How to apply:** ao implementar fluxo de delete/export/revoke, incluir `daily_calorie_target` E os 4 campos de Tela 2 no escopo da operação. No spec da Tela 2, exigir do CPO o ajuste do link "Como tratamos esses dados" antes da implementação. No teste de QA, validar que apagar dados de saúde também apaga `daily_calorie_target` e os campos de profile. Em qualquer feature futura que crie novos valores derivados de dados de saúde, classificar por padrão como Art. 11.
