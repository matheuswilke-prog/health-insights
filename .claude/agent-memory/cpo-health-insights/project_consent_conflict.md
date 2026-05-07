---
name: Consent screen conflict open
description: Founder pediu 1 tela única com toggles; consent-copy-v1.md exige 1 tela por tipo de dado. Pendente decisão CISO.
type: project
---

Conflito aberto entre decisão do founder e doc legal aprovado:

- **Founder (2026-05-05):** "1 tela com toggles" para o consentimento do Health Connect.
- **consent-copy-v1.md (Nota de implementação 2):** "Sem agrupamento forçado. Cada tipo de dado deve ter sua própria tela de consentimento separada na implementação final."

O CPO seguiu a decisão do founder no spec de onboarding (Phase 2), mas marcou a tela de consentimento como **bloqueada por revisão CISO** antes da implementação. CISO precisa decidir:

(a) Toggles granulares + descrição completa por tipo numa tela única satisfazem LGPD Art. 11 (consentimento explícito por tipo de dado de saúde)? Em caso afirmativo, atualizar consent-copy-v1.md para v1.1 removendo a restrição de "tela por tipo".
(b) Caso contrário, retomar o modelo de 4 telas e revalidar a decisão com o founder.

**Why:** consent-copy-v1.md foi escrito antes das respostas do founder; a Nota 2 reflete uma posição conservadora do CISO. A decisão de UX mudou e o CISO ainda não revisou.

**How to apply:** não permitir merge da feature de onboarding consent screen sem revisão CISO. Spec assume modelo de 1 tela; se CISO mudar, refazer apenas a parte de consentimento (Tela 4 do spec), sem refazer o resto.
