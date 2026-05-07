# Pendências pós C-suite review — 2026-05-04

## Resolvido na última sessão
- `isMinifyEnabled = true` + ProGuard rules
- `minSdk = 28` em todos os módulos
- Cache key do CI corrigido
- `consent-copy-v1.md` e `privacy-policy-v1.md` escritos

---

## Pendente — requer decisão sua (founder)

~~**P0 — Sua revisão e aprovação dos docs legais.**~~ ✓ Aprovado em 2026-05-05. Revisão final antes de publicar na Play Store.
- `docs/legal/consent-copy-v1.md`
- `docs/legal/privacy-policy-v1.md`

~~**P0 — Backup do keystore offline.**~~ ✓ Confirmado em 2026-05-05. Cópia no PC local + GitHub Secrets.

~~**P1 — 4 perguntas do CPO para o spec de onboarding**~~ ✓ Respondidas em 2026-05-05.
1. Promessa: acompanhamento de perda/ganho de peso via déficit/superávit calórico. Foco em matemática, não medicina.
2. Consentimento: 1 tela com toggles.
3. Primeiro insight: TMB + média de gasto calórico dos últimos 15 dias → recomendação de ingestão diária conforme objetivo.
4. Pós-onboarding: Dashboard.
- Dados: Samsung Health (gasto calórico, ingestão se disponível, peso se disponível) + entrada manual pelo usuário.
- Disclaimer obrigatório: acompanhamento calórico, não substitui nutricionista.

---

## Pendente — trabalho de engenharia (não bloqueia você, bloqueia o código)

| Item | Quem levantou | Impacto |
|---|---|---|
| Convention plugins em `build-logic/` | CTO | Sem isso, cada update de SDK edita 13 arquivos |
| Room + SQLCipher em `core:database` | CTO | Onboarding não persiste nada sem banco |
| Health Connect permissions em `feature:health-connect` | CTO | Onboarding não pede permissão sem isso |
| Navigation graph no `:app` | CTO | Nenhuma tela navega para outra |

---

## O que desbloqueia tudo

Suas respostas às 4 perguntas do CPO. Com elas, o CPO escreve o spec completo e a engenharia executa a Phase 2 em paralelo com os itens técnicos acima.
