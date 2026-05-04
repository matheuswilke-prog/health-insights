# Texto de Consentimento — Health Insights v1
<!-- Documento: consent-copy-v1.md -->
<!-- Versão: 1.0 | Data de vigência: 2026-05-04 -->
<!-- Base legal: LGPD Art. 11, II, a — consentimento explícito para dados de saúde -->

---

## Título da tela

```
Antes de começar — seus dados de saúde
```

<!-- LGPD Art. 9 — o controlador deve informar o titular antes do início do tratamento -->

---

## Parágrafo de introdução

```
O Health Insights é operado por Matheus Wilke (matheus.wilke@gmail.com),
responsável pelo tratamento dos seus dados de saúde neste aplicativo.

Para mostrar seus insights de saúde, o app precisa ler dados do Android
Health Connect — a plataforma do Android que reúne informações do seu
relógio e do seu celular. Nesta tela explicamos exatamente o que será
lido, para que serve cada informação e quais são os seus direitos.

Leia com calma. Você decide o que permite.
```

<!-- LGPD Art. 9, I — identificação do controlador e finalidade do tratamento -->
<!-- LGPD Art. 8, § 6 — linguagem clara e acessível é obrigatória -->

---

## Seção de tipos de dados

### Dados de Passos

```
O que o app lê: a contagem de passos registrada pelo Android Health
Connect (StepsRecord) — o total diário e o histórico dos últimos meses.

Para que serve: mostrar quantos passos você deu hoje, comparar com
dias anteriores e calcular sua tendência semanal de atividade.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 6, III — princípio da necessidade: somente o mínimo necessário -->

### Dados de Sono

```
O que o app lê: o horário em que você dormiu e acordou, e a duração
total de cada noite (SleepSessionRecord), conforme registrado pelo
Health Connect.

Para que serve: mostrar tendências de duração do sono ao longo do
tempo e identificar a regularidade dos seus horários de descanso.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 11 — dado de saúde de categoria especial, exige consentimento explícito -->

### Frequência Cardíaca

```
O que o app lê: suas medições de frequência cardíaca em repouso e
durante o dia (RestingHeartRateRecord e HeartRateRecord), registradas
pelo Health Connect.

Para que serve: exibir o histórico da sua frequência cardíaca em
repouso ao longo do tempo, para seu acompanhamento pessoal.

Importante: o app não faz diagnósticos médicos. As informações são
apenas para seu próprio acompanhamento. Consulte um médico para
avaliações de saúde.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 11 — dado de saúde de categoria especial -->

### Dados de Exercício

```
O que o app lê: o tipo de atividade física (caminhada, corrida,
pedalada etc.) e a duração de cada sessão (ExerciseSessionRecord).
O app não lê GPS, rota ou localização — somente o tipo e o tempo
de cada atividade.

Para que serve: incluir seus dias de exercício no resumo semanal
de atividades e mostrar sua frequência de prática ao longo do tempo.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 6, III — dado mínimo necessário: sem GPS, sem rota -->

---

## Garantia on-device

```
Seus dados nunca saem do seu aparelho.

O Health Insights não tem servidores. Não faz upload de nenhum dado.
Não compartilha informações com terceiros. Não usa seus dados para
publicidade. Tudo é processado e guardado só aqui no seu celular,
com criptografia.
```

<!-- LGPD Art. 9, V — informação sobre compartilhamento com terceiros (resposta: nenhum) -->
<!-- LGPD Art. 6, VII — princípio da segurança -->

---

## Período de retenção

```
Seus dados de saúde ficam guardados no seu aparelho por até 12 meses.
Você pode mudar esse prazo a qualquer momento em
Configurações → Privacidade → Meus Dados.

Quando você desinstalar o app, todos os dados são apagados
automaticamente.
```

<!-- LGPD Art. 9, III — forma e duração do tratamento devem ser informadas ao titular -->
<!-- LGPD Art. 15 — encerramento do tratamento quando o prazo determinado se encerra -->

---

## Resumo dos direitos do titular

```
Você tem os seguintes direitos sobre seus dados, exercíveis a qualquer
momento em Configurações → Privacidade → Meus Dados:

• Ver todos os dados que o app armazena sobre você
• Exportar seus dados em formato aberto
• Apagar todos os seus dados permanentemente

Esses direitos estão garantidos pela Lei Geral de Proteção de Dados
(LGPD), Art. 18.
```

<!-- LGPD Art. 9, VI — informação sobre o direito de não fornecer consentimento e as consequências -->
<!-- LGPD Art. 18 — direitos do titular: confirmação, acesso, eliminação, portabilidade -->

---

## Aviso de revogação

```
Você pode cancelar esta autorização a qualquer momento em
Configurações → Privacidade → Revogar Acesso à Saúde.

Se você revogar o acesso, o app para de ler novos dados de saúde
e apaga os dados que já estavam guardados. Você não perde nenhuma
função do app — apenas as telas que dependem dos dados revogados
ficarão indisponíveis.
```

<!-- LGPD Art. 8, § 5 — o consentimento pode ser revogado a qualquer momento -->
<!-- LGPD Art. 18, IX — direito de revogação do consentimento -->

---

## Link para a política de privacidade

```
Leia nossa Política de Privacidade completa para todos os detalhes
sobre como tratamos seus dados.
```

[LINK_POLITICA_PRIVACIDADE]

<!-- LGPD Art. 9 — o controlador deve informar o titular sobre todos os aspectos do tratamento -->

---

## Label do botão de consentimento afirmativo

```
Concordo — permitir acesso aos dados de saúde
```

<!-- LGPD Art. 8 — consentimento deve ser manifestado por ação afirmativa e inequívoca -->
<!-- LGPD Art. 11, II, a — consentimento explícito para dados de saúde -->

---

## Label da opção de recusa

```
Agora não — usar o app com funções limitadas
```

<!-- LGPD Art. 8, § 5 — a recusa não pode causar prejuízo ao titular -->
<!-- LGPD Art. 9, VI — o titular deve ser informado das consequências de não consentir -->

---

## Notas de implementação (não exibidas ao usuário)

As seguintes diretrizes são obrigatórias para que este copy constitua
consentimento válido nos termos da LGPD:

1. **Nenhum botão pré-selecionado.** O estado padrão é "não consentido".
   Consentimento por omissão não é válido (Art. 8, § 3).

2. **Sem agrupamento forçado.** Cada tipo de dado deve ter sua própria
   tela de consentimento separada na implementação final. Este documento
   apresenta todos os elementos do fluxo de forma unificada para revisão
   legal, mas a UX deve exibir uma tela por tipo de dado.

3. **Recusa sem punição.** Ao recusar, o usuário acessa o app com as
   funções não dependentes do dado recusado disponíveis. O app não pode
   bloquear o acesso por negativa de consentimento (Art. 8, § 5).

4. **Registro do consentimento.** Ao tocar no botão de aceite, o app deve
   registrar: tipo de dado consentido, timestamp ISO-8601, versão deste
   documento (`consent-copy-v1.0`). Esse registro é armazenado criptografado
   e não pode ser apagado sem que o titular solicite exclusão dos dados.

5. **Tela completa visível antes da ação.** O usuário deve poder rolar a
   tela e ler todo o conteúdo antes de o botão de aceite ficar habilitado.
   Consentimento obtido antes da leitura completa é questionável (Art. 8).

6. **Versão deste documento:** `consent-copy-v1.0` — qualquer alteração
   material (novo tipo de dado, nova finalidade) exige nova versão e
   novo consentimento explícito do usuário.

---

*Health Insights — Texto de Consentimento v1.0 | Vigente a partir de 2026-05-04*
*Controlador: Matheus Wilke — matheus.wilke@gmail.com*
*Próxima revisão obrigatória: antes de qualquer mudança no escopo de dados ou finalidades*
