# Copy de Consentimento — Health Insights Onboarding

**Versão:** v1.0
**Referência legal:** LGPD Art. 11, II, a — consentimento explícito para dados de saúde
**Última atualização:** [DATA DE VIGÊNCIA]

---

## Notas de Implementação

Antes dos textos por tela, registram-se as seguintes diretrizes de implementação obrigatórias para conformidade LGPD:

1. **Nenhum consentimento deve ser pré-marcado** — o estado padrão de todos os toggles/checkboxes deve ser "não consentido" (off).
2. **Recusa não bloqueia o app** — ao recusar qualquer consentimento, o usuário prossegue no onboarding; o app simplesmente não exibe as funcionalidades dependentes daquele dado.
3. **Ordem das telas** — as 4 telas de consentimento devem aparecer em sequência, uma por tipo de dado, após a tela de boas-vindas e antes da tela principal do app.
4. **Telas independentes** — cada tela é autônoma; o usuário pode aceitar uma e recusar outra sem restrições.
5. **Registro de consentimento** — ao tocar no botão de aceite, o app deve registrar localmente: tipo de dado, timestamp ISO-8601, versão da política. Ao tocar em recusa, registrar a recusa com os mesmos metadados.
6. **Link para Política** — cada tela deve conter link âncora para a Política de Privacidade completa (abre no navegador ou WebView).
7. **Acessibilidade** — todos os textos devem passar por testes de contraste WCAG AA e ser compatíveis com TalkBack.

---

## Tela 1 — Dados de Passos

---

### Título da tela
```
Seus dados de passos
```
*(22 caracteres — máximo: 40)*

---

### Corpo explicativo
```
O Health Insights pode ler a contagem de passos registrada
no seu dispositivo pelo Android Health Connect.

Usamos esses dados para mostrar quantos passos você deu
hoje, esta semana e nos últimos meses — direto na tela
inicial do app.

Tudo fica guardado só aqui no seu celular.
Nenhuma informação é enviada para a internet.
```

---

### Declaração de finalidade
```
Usamos seus dados de passos para exibir seu histórico
de atividade física e calcular médias e tendências
de movimento ao longo do tempo.
```

---

### Declaração on-device (frase fixa — igual em todas as telas)
```
Seus dados nunca saem do seu dispositivo.
O Health Insights não tem servidores e não acessa
a internet para armazenar ou processar seus dados.
```

---

### Lembrete de revogação (frase fixa — igual em todas as telas)
```
Você pode cancelar esta permissão quando quiser
em Configurações > Privacidade.
```

---

### Label do botão de aceite
```
Permitir acesso aos passos
```
*(30 caracteres — exatamente no máximo)*

---

### Label do botão de recusa
```
Agora não
```
*(9 caracteres — máximo: 30)*

---

## Tela 2 — Dados de Sono

---

### Título da tela
```
Seus dados de sono
```
*(18 caracteres — máximo: 40)*

---

### Corpo explicativo
```
O Health Insights pode ler os registros de sono
capturados pelo Android Health Connect — geralmente
vindos do seu smartwatch ou do próprio celular.

Usamos essas informações para mostrar quanto tempo
você dormiu a cada noite e como esse padrão varia
ao longo das semanas.

Esses dados ficam salvos apenas no seu aparelho.
Ninguém além de você tem acesso a eles.
```

---

### Declaração de finalidade
```
Usamos seus dados de sono para exibir a duração
e a regularidade do seu sono e mostrar tendências
ao longo do tempo.
```

---

### Declaração on-device (frase fixa)
```
Seus dados nunca saem do seu dispositivo.
O Health Insights não tem servidores e não acessa
a internet para armazenar ou processar seus dados.
```

---

### Lembrete de revogação (frase fixa)
```
Você pode cancelar esta permissão quando quiser
em Configurações > Privacidade.
```

---

### Label do botão de aceite
```
Permitir acesso ao sono
```
*(23 caracteres — máximo: 30)*

---

### Label do botão de recusa
```
Agora não
```
*(9 caracteres — máximo: 30)*

---

## Tela 3 — Frequência Cardíaca

---

### Título da tela
```
Sua frequência cardíaca
```
*(23 caracteres — máximo: 40)*

---

### Corpo explicativo
```
O Health Insights pode ler as medições de frequência
cardíaca (batimentos por minuto) registradas pelo
Android Health Connect.

Usamos esses dados para mostrar como sua frequência
cardíaca variou durante o dia, em repouso e durante
atividades — de forma visual e fácil de entender.

Esses dados ficam salvos só no seu celular e não
são compartilhados com ninguém.
```

---

### Declaração de finalidade
```
Usamos seus dados de frequência cardíaca para exibir
leituras históricas e variações ao longo do tempo,
para seu acompanhamento pessoal.
```

---

### Declaração on-device (frase fixa)
```
Seus dados nunca saem do seu dispositivo.
O Health Insights não tem servidores e não acessa
a internet para armazenar ou processar seus dados.
```

---

### Lembrete de revogação (frase fixa)
```
Você pode cancelar esta permissão quando quiser
em Configurações > Privacidade.
```

---

### Label do botão de aceite
```
Permitir acesso ao coração
```
*(26 caracteres — máximo: 30)*

---

### Label do botão de recusa
```
Agora não
```
*(9 caracteres — máximo: 30)*

---

> **Nota de conformidade — Tela 3:** Dados de frequência cardíaca são particularmente sensíveis. O copy evita deliberadamente qualquer referência a diagnóstico, risco cardiovascular ou interpretação clínica. O app deve exibir um aviso visível na tela de visualização dos dados de frequência cardíaca indicando que as informações não substituem avaliação médica profissional.

---

## Tela 4 — Dados de Exercício

---

### Título da tela
```
Seus dados de exercício
```
*(23 caracteres — máximo: 40)*

---

### Corpo explicativo
```
O Health Insights pode ler os registros de atividade
física — como caminhadas, corridas, pedaladas e outras
práticas — armazenados no Android Health Connect.

Usamos esses dados para mostrar com que frequência
você se exercitou, por quanto tempo e que tipo de
atividade realizou.

As informações ficam armazenadas somente no seu
dispositivo, sem nenhum envio para a internet.
```

---

### Declaração de finalidade
```
Usamos seus dados de exercício para exibir seu
histórico de atividades físicas e mostrar tendências
de frequência e duração ao longo do tempo.
```

---

### Declaração on-device (frase fixa)
```
Seus dados nunca saem do seu dispositivo.
O Health Insights não tem servidores e não acessa
a internet para armazenar ou processar seus dados.
```

---

### Lembrete de revogação (frase fixa)
```
Você pode cancelar esta permissão quando quiser
em Configurações > Privacidade.
```

---

### Label do botão de aceite
```
Permitir acesso ao exercício
```
*(28 caracteres — máximo: 30)*

---

### Label do botão de recusa
```
Agora não
```
*(9 caracteres — máximo: 30)*

---

## Resumo de Labels por Tela

| Tela | Dado | Botão Aceite | Botão Recusa |
|---|---|---|---|
| 1 | Passos | "Permitir acesso aos passos" | "Agora não" |
| 2 | Sono | "Permitir acesso ao sono" | "Agora não" |
| 3 | Frequência Cardíaca | "Permitir acesso ao coração" | "Agora não" |
| 4 | Exercício | "Permitir acesso ao exercício" | "Agora não" |

---

## Checklist de Conformidade por Tela

Para cada tela, verificar antes do lançamento:

- [ ] Nome do dado específico está explícito no título e no corpo (não "dados de saúde" genérico)
- [ ] Finalidade específica está declarada na "Declaração de finalidade"
- [ ] Nenhuma linguagem médica ou diagnóstica presente
- [ ] Nenhum botão está pré-selecionado
- [ ] Botão de recusa está visualmente equivalente ao de aceite (sem dark pattern — não menor, não cinza escuro, não oculto)
- [ ] Declaração on-device está presente
- [ ] Lembrete de revogação está presente
- [ ] Link para Política de Privacidade completa está acessível na tela
- [ ] Registro de consentimento/recusa é gravado localmente ao tocar em qualquer botão
- [ ] Tela passa em teste de acessibilidade TalkBack
- [ ] Tela passa em teste de contraste WCAG AA

---

*Health Insights — Copy de Consentimento v1.0*
*Vigente a partir de [DATA DE VIGÊNCIA]*
