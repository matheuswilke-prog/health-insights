---
name: Princípios de consentimento — scroll vs. ação afirmativa, granularidade real
description: Regras CISO sobre o que constitui consentimento válido na UI do Health Insights — scroll-to-bottom não é consentimento; granularidade significa permissões independentes de fato.
type: feedback
---

Regra: **scroll-to-bottom não é consentimento; é prova de oportunidade de leitura.** O consentimento se materializa pelo toque afirmativo no toggle de cada tipo de dado + toque no CTA "Concordo". Liberar o CTA somente após scroll completo é reforço aceitável da informação prévia (Art. 9), mas nunca substitui ato afirmativo (Art. 8 §3).

Regra: **granularidade significa permissões independentes de fato.** Cada toggle deve disparar o request da permissão correspondente do Health Connect de forma isolada. Combinações 1/3, 2/3, 3/3 toggles ON precisam todas funcionar. Não pode haver "Aceitar tudo" mascarado nem dependência implícita entre toggles.

**Why:** decisões CPO podem facilmente confundir prova de leitura com ato de consentimento (já vi em consent-copy-v1.0 item 5: "tela completa visível antes do botão habilitar" — texto que a leitura crua poderia interpretar como "consentimento por scroll"). E "granularidade" sem implementação independente vira teatro de compliance — usuário marca um toggle e o app lê outras permissões não autorizadas. Ambos são padrões frequentes em apps brasileiros que falham em auditoria LGPD.

**How to apply:** revisar todo PR que toque a Tela de Consentimento e qualquer fluxo de re-consentimento. No code review, verificar (a) que o handler do botão "Concordo" lê o estado de cada toggle individualmente e dispara só os contracts dos toggles ON; (b) que o estado inicial dos toggles é OFF mesmo após rotação/recriação da activity; (c) que o registro persistido em Room tem uma linha por `data_type` (não uma linha "consentimento global"). Recusar PRs que pré-selecionem toggles "para conveniência do usuário".
