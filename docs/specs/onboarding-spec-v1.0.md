# SPEC DE ONBOARDING — Health Insights v1 (Phase 2)

**Autor:** CPO agent  
**Data:** 2026-05-05  
**Versão do spec:** onboarding-spec-v1.0  
**Status:** Pronto para handoff a Android Engineer + QA Engineer, com 2 dependências CISO marcadas inline

---

## 0. Bloqueios e dependências antes da implementação

Dois itens precisam de resolução antes da Tela 4 (Consentimento) ser codificada. Não bloqueiam Telas 1, 2, 3, 5, 6.

| ID | Quem resolve | Item | Bloqueia |
|---|---|---|---|
| ~~**CISO-1**~~ | CISO agent | ✓ Resolvido 2026-05-05. 1 tela com toggles aprovada para LGPD Art. 11, com as 6 condições obrigatórias documentadas em `consent-copy-v1.1.md`. | ~~Tela 4~~ |
| ~~**CISO-2**~~ | CISO agent | ✓ Resolvido 2026-05-05. `consent-copy-v1.1.md` escrito e pronto para uso. Cobre Calorias (gasto + ingestão), Peso e Treinos. | ~~Tela 4 (copy final)~~ |

---

## 1. Visão geral do fluxo

```
[Welcome] → [Profile] → [Goal] → [Consent] → [Connecting] → [First Insight] → [Dashboard]
   T1         T2         T3        T4           T5             T6              (out of scope)
```

**Total: 6 telas.** Tempo médio estimado: 90 a 150 segundos.

**Estado persistente:** booleano `onboardingComplete` em DataStore. Se `true`, app abre direto no Dashboard. Onboarding retoma na última tela completada + 1 se interrompido.

**Componentes compartilhados:**
- `OnboardingScaffold` — top bar opcional (botão voltar), corpo central, footer fixo com CTA.
- `OnboardingProgressIndicator` — 5 dots (T1 não conta como progresso).
- `DisclaimerBanner` — "Acompanhamento calórico. Não substitui orientação de nutricionista." Aparece em T3, T6 e em todo card de insight do Dashboard.

---

## 2. Tela 1 — Welcome

**Propósito:** comunicar a promessa do produto antes de pedir qualquer coisa.

**Layout vertical:**
1. Logo do app (topo, ~64dp, centralizado).
2. Headline (~32sp, bold, centralizado).
3. Subheadline (~16sp, regular, 16dp abaixo do headline).
4. Microcopy de privacidade (~12sp, rodapé acima do CTA).
5. CTA primário (filled, full-width, 56dp de altura).

**Copy sugerida:**
- **Headline:** "Saiba se está em déficit ou superávit calórico."
- **Subheadline:** "O Health Insights conecta seus dados do Samsung Health e mostra, em números, se você está perdendo, mantendo ou ganhando peso."
- **Microcopy:** "Seus dados ficam só no seu aparelho."
- **CTA:** "Começar"

**Critérios de aceitação:**
1. Tela renderiza em retrato e paisagem sem corte de texto.
2. CTA navega para Tela 2.
3. Botão back do sistema fecha o app.
4. TalkBack lê: headline → subheadline → microcopy → CTA.
5. Contraste WCAG AA. Touch target ≥ 48dp.
6. Nenhuma chamada de I/O, rede ou Health Connect.

---

## 3. Tela 2 — Profile

**Propósito:** coletar dados antropométricos para calcular TMB.

**Fórmula:** Mifflin-St Jeor.
```
Homens:    TMB = (10 × peso_kg) + (6.25 × altura_cm) − (5 × idade) + 5
Mulheres:  TMB = (10 × peso_kg) + (6.25 × altura_cm) − (5 × idade) − 161
```

**Campos (todos obrigatórios):**
- **Peso atual** — decimal, sufixo "kg", validação 30–300.
- **Altura** — inteiro, sufixo "cm", validação 100–250.
- **Idade** — inteiro, validação 13–100.
- **Sexo biológico** — segmented control: "Masculino" / "Feminino". Microcopy: "Usado apenas no cálculo da fórmula de Mifflin-St Jeor."

**CTA:** "Continuar" — desabilitado até todos os 4 campos válidos.

**Copy sugerida:**
- **Título:** "Vamos calcular seu metabolismo basal."
- **Subtítulo:** "Esses dados ficam só no seu aparelho e podem ser editados depois."
- **Link de privacidade (abaixo do subtítulo):** "Como tratamos esses dados →" (abre `privacy-policy-v1.md` em WebView interno). Obrigatório — exigência LGPD Art. 9 (CISO 2026-05-05).

**Critérios de aceitação:**
1. CTA só habilita quando todos os 4 campos são válidos.
2. Teclado numérico abre automaticamente para campos de número.
3. Dados persistidos em DataStore: `user_weight_kg`, `user_height_cm`, `user_age_years`, `user_sex` (`MALE`|`FEMALE`).
4. Voltar e retornar restaura valores.
5. Rotação preserva valores em digitação.
6. Nenhuma leitura de Health Connect. Sem rede.

---

## 4. Tela 3 — Goal

**Propósito:** capturar o objetivo do usuário.

**3 cards selecionáveis (radio):**
- **Emagrecer** — "Déficit de 500 kcal por dia. Cerca de 0,5 kg por semana."
- **Manter peso** — "Ingestão alinhada ao seu gasto diário."
- **Ganhar massa** — "Superávit de 300 kcal por dia. Cerca de 0,3 kg por semana."

**DisclaimerBanner** abaixo dos cards (fixo, não dismissível).

**CTA:** "Continuar" — desabilitado até um card selecionado.

**Copy sugerida:**
- **Título:** "Qual é o seu objetivo?"
- **Subtítulo:** "Você pode mudar quando quiser."

**Critérios de aceitação:**
1. Apenas um card selecionado por vez.
2. Estado selecionado: borda primária + ícone de check (não só cor).
3. Objetivo persistido: `user_goal` (`LOSE`|`MAINTAIN`|`GAIN`).
4. DisclaimerBanner lido pelo TalkBack ao chegar na tela.
5. Touch target de cada card ≥ 56dp.

---

## 5. Tela 4 — Consent *(BLOQUEADA POR CISO-1 e CISO-2 — codificar estrutura; aguardar copy v1.1)*

**Propósito:** obter consentimento explícito e granular para Health Connect, em conformidade com LGPD Art. 11.

**Toggle 1 — Calorias** `[CISO-COPY-PENDING]`
- Descrição provisória: "Lê seu gasto calórico diário e, se disponível, sua ingestão calórica. Usado para calcular seu balanço diário."
- Toggle default OFF. Permissões: `READ_TOTAL_CALORIES_BURNED`, `READ_ACTIVE_CALORIES_BURNED`, `READ_NUTRITION`.

**Toggle 2 — Peso** `[CISO-COPY-PENDING]`
- Descrição provisória: "Lê seu histórico de peso para acompanhar variação ao longo do tempo."
- Toggle default OFF. Permissão: `READ_WEIGHT`.

**Toggle 3 — Treinos**
- Descrição: "O que o app lê: tipo de atividade e duração. O app não lê GPS, rota ou localização."
- Toggle default OFF. Permissão: `READ_EXERCISE`.

**CTA primário:** "Concordo — permitir acesso aos dados de saúde" — desabilitado até scroll completo da tela.  
**CTA secundário (link):** "Agora não — usar com funções limitadas."

**Comportamento dos toggles:**
- Default OFF para todos. Pré-marcar é dark pattern e fere LGPD Art. 8 § 3.
- Toggles independentes entre si.
- Tocar "Concordo" com 0 toggles ON → dialog de confirmação "Continuar em modo manual?".
- Permissão negada no dialog nativo do Android → microcopy "Permissão negada. Você pode liberar mais tarde em Configurações."

**Critérios de aceitação:**
1. Todos os toggles iniciam OFF.
2. CTA "Concordo" só habilita após ScrollView atingir o final.
3. Para cada toggle ON, dispara request correto via `HealthPermissionsContract`.
4. Cada permissão (concedida ou negada) persiste registro criptografado em Room: `data_type`, `granted_at`, `policy_version` ("consent-copy-v1.1"), `granted`.
5. "Agora não" vai direto para T6 em modo manual.
6. Health Connect não instalado → dialog "Instalar agora?" com opções "Instalar" e "Continuar em modo manual".

---

## 6. Tela 5 — Connecting *(condicional: pulada se T4 → modo manual)*

**Propósito:** mostrar progresso enquanto sincroniza dados e calcula o primeiro insight.

**Layout:** CircularProgressIndicator + texto de status dinâmico. Sem CTA, sem botão voltar.

**Textos de status:**
- "Buscando seu gasto calórico dos últimos 15 dias..."
- "Calculando seu metabolismo basal..."
- "Quase lá..."

**Tempo mínimo:** 1.5s. **Tempo máximo:** 15s (depois vai para T6 com fallback).

**Critérios de aceitação:**
1. Aparece apenas após concessão de ≥ 1 permissão em T4.
2. Não aparece em modo manual.
3. Botão back do sistema ignorado.
4. Tempo mínimo de 1.5s respeitado mesmo se cálculo for instantâneo.

---

## 7. Tela 6 — First Insight

**Propósito:** entregar o payoff emocional do onboarding.

**Layout:**
1. Headline grande: "Sua meta diária."
2. Hero number (~48sp): ex. **"2.180 kcal/dia"**
3. Texto de explicação (varia conforme objetivo de T3).
4. Card "Como calculamos" (expansível, fechado por default).
5. DisclaimerBanner (fixo, não dismissível).
6. CTA: "Ir para o painel".

**Regra de cálculo:**
```
TMB     = Mifflin-St Jeor (dados de T2)
AVG     = média de gasto calórico dos últimos 15 dias do HC (se ≥ 7 dias disponíveis)
BASE    = AVG se disponível; senão TMB × 1.4 (fator de atividade leve)
MOD     = -500 (LOSE) | 0 (MAINTAIN) | +300 (GAIN)

Meta diária = BASE + MOD
```

**Casos:**
- **Caso A (≥ 7 dias de dados):** hero com AVG real. Card mostra média dos 15 dias.
- **Caso B (< 7 dias ou modo manual):** hero com fallback `TMB × 1.4`. Card: "Estimativa de gasto diário (atividade leve): X kcal. Quanto mais você usar o app, mais precisa fica sua meta."
- **Caso C (erro total):** hero com `TMB × 1.4 + MOD`, nota "calculada de forma conservadora".

**Copy sugerida:**
- **Headline:** "Sua meta diária."
- LOSE: "Para emagrecer cerca de 0,5 kg por semana."
- MAINTAIN: "Para manter seu peso atual."
- GAIN: "Para ganhar cerca de 0,3 kg por semana."
- **Card título:** "Como calculamos"
- **CTA:** "Ir para o painel"

**Critérios de aceitação:**
1. Hero number: máx 4 dígitos, sem decimais.
2. Texto de explicação varia conforme `user_goal`.
3. Card começa fechado; tap expande com animação de 200ms.
4. DisclaimerBanner sempre visível (sticky ou fixo no rodapé).
5. Tocar "Ir para o painel": persiste `onboarding_complete = true` em DataStore (plain) e `daily_calorie_target` em Room + SQLCipher (dado Art. 11 — decisão CTO/CISO 2026-05-05); navega com `popUpTo("onboarding") { inclusive = true }` (remove back-stack do onboarding).
6. Botão back do sistema desabilitado nesta tela.
7. Hero number com contraste WCAG AAA (7:1).

---

## 8. Transição para o Dashboard

- **Hero:** repete a meta diária de T6.
- **Card "Hoje":** empty state com CTA "+ Registrar refeição".
- **Card "Peso":** peso do HC se disponível; senão peso de T2 com CTA "+ Registrar peso".
- **Card "Gasto":** gasto de hoje se disponível; senão CTA "Habilitar fonte de dados".

**Critérios:**
1. Meta diária no Dashboard idêntica à de T6.
2. Sem flicker ou tela branca na transição.
3. Back no Dashboard fecha o app — não retorna ao onboarding.

---

## 9. Fluxos alternativos consolidados

| Cenário | Caminho | Comportamento de T6 |
|---|---|---|
| Caminho feliz com dados | T1→T2→T3→T4→T5→T6→Dashboard | Hero usa AVG real (15 dias). |
| Caminho feliz sem 15 dias completos | Idem | AVG dos dias disponíveis se ≥ 7; senão fallback. |
| Permissão parcial | T1→T2→T3→T4→T5→T6 | Depende do conjunto concedido. |
| Modo manual | T1→T2→T3→T4→T6 (pula T5) | Sempre fallback. |
| HC não instalado | Dialog → modo manual | Idem modo manual. |
| Permissão negada no Android | Toggle volta a OFF → parcial ou manual | Conforme conjunto efetivo. |
| App matado durante onboarding | Retoma na última tela completada + 1 | — |

---

## 10. Fora de escopo (Phase 2)

1. Re-onboarding após update.
2. Importação de histórico de outros apps.
3. Customização de déficit/superávit.
4. Cálculo alternativo de TMB.
5. Login / criação de conta.
6. Tutorial guiado do Dashboard.
7. Growth loops (convite de amigos).
8. Paywall no onboarding.
9. Configuração de notificações.
10. Edição de T2/T3 fora das próprias telas.
11. Múltiplos perfis.
12. i18n (v1 = PT-BR apenas).

---

## 11. Handoffs

| Para | Ação |
|---|---|
| **Android Engineer** | Implementar T1, T2, T3, T5, T6. T4: codificar estrutura; aguardar copy CISO v1.1 para strings finais. |
| **QA Engineer** | Construir matriz de casos de teste a partir dos critérios de aceitação + 7 cenários da seção 9. |
| **CISO agent** | Resolver CISO-1 (1 tela com toggles vs. LGPD Art. 11) e CISO-2 (consent-copy v1.1 para Calorias/Peso/Treinos). |
| **CMO agent** | Validar copy de T1 (headline/subheadline/CTA) e DisclaimerBanner. |
| **CTO agent** | Confirmar navigation graph com `popUpTo` no `:app`. Confirmar schema do registro de consentimento em Room. |
| **CFO agent** | Confirmar que onboarding é 100% gratuito. |

---

## 12. Próximo passo concreto

Acionar CISO agent com 2 perguntas:
1. **CISO-1:** "Toggles granulares + descrição completa + default OFF + scroll-to-bottom em 1 tela atendem LGPD Art. 11?"
2. **CISO-2:** "Atualizar `consent-copy-v1.md` para v1.1 cobrindo Calorias, Peso e Treinos."

Sem essas respostas, T4 fica em rascunho. As outras 5 telas podem entrar em desenvolvimento imediato.
