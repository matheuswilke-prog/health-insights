# Health Insights — Plano do MVP

> **Documento de direção do CEO.** Consolida decisões do C-suite (CTO, CPO, CMO, CFO, CISO) sobre o escopo mínimo viável que valida a proposta de valor central: transformar dados brutos do Samsung Health em insights acionáveis.
>
> **Restrição diretiva do founder, válida para todas as seções abaixo:** testes e segurança são prioridades absolutas — não dimensões a serem otimizadas depois. Esse princípio está refletido no escopo, na arquitetura e nos agentes adicionais propostos.
>
> Versão: 1.0 — Data: 2026-05-03

---

## Proposta de Valor do MVP

**Frase única:** *Health Insights mostra ao usuário, em uma frase, o que mudou na sua saúde esta semana — usando os dados que o Samsung Health já coleta, sem nuvem, sem conta.*

**O que o MVP precisa provar:**
1. Que conseguimos ler dados reais do Samsung Health via Health Connect e transformá-los em pelo menos um insight semanal que o usuário considera útil (não apenas um número).
2. Que o usuário aceita o consentimento LGPD bem desenhado e completa o onboarding até ver o primeiro insight com dados próprios.
3. Que o modelo on-device + freemium tem retenção W1 suficiente para justificar a próxima iteração.

**O que o MVP NÃO precisa provar (deixar para depois):** monetização, escala, breadth de features, integração com outros wearables.

---

## Público-Alvo (hipótese inicial)

**Persona primária — "Marcos, 32 anos, dono de Galaxy Watch":**
- Comprou um Samsung Galaxy Watch nos últimos 18 meses, usa diariamente.
- Treina 3–5x por semana (corrida, academia ou ciclismo recreativo). Não é atleta; é entusiasta consistente.
- Olha o Samsung Health quase todo dia, mas se frustra: vê números isolados ("ontem 7.840 passos"), nunca tendências interpretadas.
- Sente desconforto em apps de saúde que pedem cadastro, e-mail e enviam dados para a nuvem.
- Idade-tecnológica: confortável com Android, instala apps com regularidade, não é early adopter de fitness apps caros (Whoop, Strava Premium).
- País: Brasil (foco inicial — alinhado com obrigação LGPD).

**Por que essa persona, não "qualquer usuário Samsung Health":**
- Tem o dispositivo (pré-requisito técnico).
- Tem o problema (já usa Samsung Health e sente a falta de insights).
- Tem capacidade de pagar uma compra única acessível.
- É alcançável organicamente via comunidades Samsung (r/GalaxyWatch, grupos brasileiros de Galaxy Watch no Telegram/Discord, Galaxy Store).

**Hipóteses ainda a validar (pós-MVP):**
- Se o segmento "recuperação de problema de saúde" (pacientes pós-cirurgia, hipertensos monitorando FC) é uma persona secundária mais valiosa.
- Se o público feminino com foco em ciclo + sono é segmentável separadamente (provavelmente sim — adiar para v1.1).

**Decisão:** o MVP é construído para Marcos. Toda escolha de feature, copy e UI deve passar pelo teste "isso ressoa com Marcos?".

---

## Escopo do MVP — Features Incluídas

Cinco features, na ordem em que devem ser construídas. Ordem importa — cada uma depende da anterior estar estável.

### 1. Onboarding com Consentimento LGPD
- Tela de valor antes de pedir permissão (mostra exemplo de insight com dados fictícios).
- Solicitação granular de permissões via Health Connect: passos, sono, frequência cardíaca, treinos. Nutrição **fora** do MVP.
- Linguagem do consentimento aprovada pelo CISO: específica por tipo de dado, finalidade explícita, "100% on-device".
- Registro de consentimento (timestamp + versão do termo) salvo criptografado on-device.
- Onboarding termina apenas quando o usuário vê o primeiro insight real.

### 2. Dashboard Diário
- Uma tela "Hoje" com um indicador-herói (passos do dia em contexto: vs. média dos últimos 7 dias).
- Três cards secundários: Sono da última noite, FC em repouso de hoje, último treino registrado.
- Cada card lidera com a interpretação ("Você dormiu 47 minutos a menos que sua média"), número bruto vem em segundo plano.
- Estados completos: empty (sem dados ainda), loading, error (Health Connect indisponível ou permissão revogada).

### 3. Tendência Semanal de Passos
- Gráfico de barras dos últimos 7 dias com linha da média móvel.
- Headline interpretado: "Esta semana você caminhou 12% mais que a anterior" ou variantes neutras/negativas.
- Permite navegar para a semana anterior (até 4 semanas — limite do MVP).

### 4. Análise de Sono (últimos 7 dias)
- Duração média + variação. Headline: "Você dormiu em média 6h32 — abaixo do ideal de 7h."
- Gráfico de barras horizontal com linha da meta.
- Sem score composto no MVP (CPO: não criar métrica não validada).

### 5. Resumo Semanal (insight consolidado)
- Tela acessível pelo dashboard, gerada toda segunda-feira.
- Três bullets gerados a partir de regras determinísticas (sem ML/LLM no MVP):
  - Mudança nos passos vs. semana anterior.
  - Mudança no sono médio vs. semana anterior.
  - Variação na FC em repouso vs. semana anterior.
- **Esta é a feature que valida a proposta de valor central.** Sem isto, é apenas mais um visualizador de Samsung Health.

### Recursos transversais obrigatórios no MVP

- **Tela de Configurações** com: revogar consentimento, exportar dados (JSON local), apagar todos os dados (LGPD Art. 18).
- **Política de privacidade** acessível do onboarding e das configurações (link + cópia local empacotada no app).
- **Crash-safe**: aplicação não pode crashar com dados ausentes ou permissões parciais. Estados de erro são specs, não exceções.

---

## Escopo do MVP — Fora do MVP (explicitamente)

Lista de rejeições explícitas. Cada item foi avaliado contra: (1) serve ao core de "raw data → insight"? (2) cabe no estágio greenfield? (3) introduz risco de privacidade ou compliance? (4) qual o custo de oportunidade?

| Feature | Status | Razão |
|---|---|---|
| Nutrição / contagem de calorias | Rejeitada (v1.x) | Dados nutricionais no Samsung Health são esparsos e dependem de input manual; insight de baixa qualidade. |
| Treinos detalhados (splits, zonas de FC) | Rejeitada (v1.x) | Cada modalidade pede análise específica — complexidade que dilui o foco em "insight semanal". |
| Backup em nuvem / sync entre dispositivos | Rejeitada (v2+) | Viola posicionamento on-device. Custo de compliance LGPD. Não prova nada novo no MVP. |
| Conta de usuário, login, e-mail | Rejeitada permanente para MVP | Adiciona fricção de onboarding, exige dados pessoais sem necessidade, contradiz "sem cadastro". |
| Compartilhamento social ("compartilhe seu progresso") | Rejeitada (v1.x) | Health data + redes sociais = risco LGPD. CISO veta sem revisão profunda. |
| Insights por LLM (resumo gerado por IA) | Rejeitada (v1.x) | Exigiria envio de dados para servidor — quebra on-device. Reavaliar se modelo embarcado (Gemini Nano) madurar. |
| Wearables não-Samsung (Garmin, Fitbit, Apple) | Rejeitada (v2+) | Foco do MVP é validar com público Samsung. Cross-platform expande compliance e teste. |
| Push notifications de insight semanal | Adiada para v1.1 | Quer-se primeiro validar abertura orgânica. Notificação exige consentimento separado e infra. |
| Streaks / gamificação | Adiada para v1.1 | CMO gosta como retention hook, mas exige dados de retenção do MVP para calibrar antes. |
| Modo escuro customizável / temas | Rejeitada permanente | Use sistema. Não é parte da proposta de valor. |
| Widget de homescreen | Adiada para v1.1 | Avaliar após primeira release; widget complica testes e estado. |
| Tablet / foldable layouts | Adiada para v1.1 | Foco do MVP: telefones Samsung 6"–6.8". |
| Suporte iOS / cross-platform | Rejeitada permanente | Samsung Health SDK é Android-only; Health Connect é Android. Cross-platform não tem mercado aqui. |

**Regra:** qualquer feature não listada acima e não em "Features Incluídas" também está fora. O MVP fecha aqui.

---

## Arquitetura Técnica do MVP

> Decisão consolidada com base nos princípios do CTO. Stack já estava planejada e é confirmada — sem mudanças.

### Stack confirmada

| Camada | Decisão | Razão |
|---|---|---|
| Linguagem | **Kotlin** (nativo) | Único stack viável dado Health Connect e Samsung Health SDK. Cross-platform foi descartado. |
| UI | **Jetpack Compose** | Padrão moderno Android. Sem XML em telas novas. |
| Arquitetura | **MVVM + Clean Architecture** | Repository + UseCase + ViewModel. Camadas: `data/`, `domain/`, `ui/`. |
| Async | **Coroutines + Flow** | Sem RxJava. |
| DI | **Hilt** | Sem manual DI, sem Koin. |
| Acesso a dados de saúde | **Health Connect** (primário) | Plataforma oficial Android moderna. Samsung Health flui via Health Connect em Android 14+. Samsung Health SDK direto só se for necessário acessar campos não expostos via HC. |
| Banco local | **Room + SQLCipher** | Criptografia at-rest obrigatória (CISO). Chave gerada e armazenada no Android Keystore. |
| Storage de chaves | **Android Keystore + EncryptedSharedPreferences** | Para chave do SQLCipher e flags de consentimento. |
| Charting | **Vico (compose-friendly)** OU primitivos Compose customizados | Decisão final do CTO + simplify pass. MPAndroidChart está em modo manutenção. **Crítico:** rejeitar qualquer biblioteca de charts que faça telemetria. |
| Build | **Gradle (KTS) + Version Catalogs** | Padrão moderno. |
| CI | **GitHub Actions** | Free, suficiente para solo dev. |

### Estrutura de módulos (multi-module Gradle)

```
:app                       → Composição da aplicação, navegação, DI graph
:core:design               → Compose theme, tokens, componentes reusáveis
:core:common               → Utilitários, extensões, types compartilhados
:core:database             → Room + SQLCipher, DAOs, migrations
:core:datastore            → DataStore criptografado para flags/preferências
:core:health-connect       → Wrapper sobre Health Connect API
:core:domain               → UseCases, modelos de domínio puros
:feature:onboarding        → Telas de valor + consentimento
:feature:dashboard         → Tela "Hoje"
:feature:steps             → Tendência semanal de passos
:feature:sleep             → Análise de sono
:feature:weekly-summary    → Resumo semanal (geração de regras)
:feature:settings          → Privacidade, exportação, exclusão
```

Razões: (a) limites de teste claros — feature modules têm seus próprios testes unitários; (b) build paralelo mais rápido; (c) impede dependências circulares; (d) `core:health-connect` isola toda interação com SDK externo, simplificando mocks em testes.

### Padrões obrigatórios

- **Unidirectional data flow** — eventos UI sobem, estado desce. Sem exceção.
- **Sealed UI state** — toda tela expõe `UiState` (Loading | Empty | Content | Error) como sealed interface. Estado vazio é uma feature, não um bug.
- **Domain models ≠ DTOs** — Health Connect records são mapeados para modelos de domínio; UI nunca toca em tipos do SDK.
- **Sem dados de saúde em logs.** Logger central com filtro automático (CISO standing order).

### Performance budgets (não negociáveis para release)

- Cold start até primeiro frame com conteúdo: < 2s em Galaxy A54 (mid-range de referência).
- Query do dashboard (últimos 30 dias): < 500ms.
- APK/AAB inicial: < 20MB.

---

## Estratégia de Testes (prioridade do founder)

> **Princípio orientador:** testes não são uma camada extra — são pré-requisito para toque em código de produção. Pull request sem testes correspondentes não passa do CI.

### Pirâmide de teste

| Nível | Cobertura alvo | Ferramentas | O que testar |
|---|---|---|---|
| **Unit (domain + viewmodel)** | ≥ 85% nas camadas `domain/` e `viewmodel/` | JUnit5, Truth, MockK, Turbine (Flow), kotlinx-coroutines-test | UseCases, regras de geração de insight semanal, mapeamento de DTOs, cálculos de tendência. |
| **Integration (data layer)** | 100% dos DAOs e repositories | Room in-memory, fakes para Health Connect | Schema migrations, queries críticas, cifragem (smoke test). |
| **UI (Compose)** | Cada tela do MVP com pelo menos: happy path + empty + error | Compose UI testing (`createComposeRule`), Robolectric onde fizer sentido | Estados sealed corretamente renderizados, content descriptions presentes, navegação. |
| **Instrumented (E2E mínimo)** | 1 teste por fluxo crítico do MVP | Espresso + UI Automator, ou Maestro | Onboarding completo até ver primeiro insight; revogação de consentimento. |
| **Property-based** | Funções de geração de insight | Kotest property testing | Garantir invariantes — ex: insight de "mudança %" nunca produz NaN, nunca quebra com 0 dados. |

### Testes de regras de insight — atenção especial

A geração de insight semanal é o **núcleo do produto**. Esta lógica precisa de:
- Dataset sintético versionado (`/test-fixtures/insights/`) com cenários: usuário regular, usuário com gaps, usuário só-com-dados-de-um-dia, mudanças extremas, edge cases (semana com daylight savings, viagem entre fusos).
- Cada regra de insight tem teste com input determinístico e output esperado em snapshot.
- Refactoring de regras só passa se snapshots forem revisados explicitamente.

### Testes de segurança como first-class

- Teste verificando que o banco SQLCipher não abre com chave vazia ou incorreta.
- Teste verificando que logs nunca contêm valores numéricos de FC, passos ou sono (regex check em log capture).
- Teste verificando que `EncryptedSharedPreferences` falha ao ler com chave Keystore inválida.

### Pipeline CI obrigatório (GitHub Actions)

```
PR → 1) ktlint/detekt → 2) unit + integration tests → 3) compose UI tests
   → 4) build debug AAB → 5) Lint Android → 6) (manual gate) instrumented tests on emulator
```

**Regra:** main protegida; merge só com CI verde. Sem exceções "vou consertar amanhã".

### Cobertura medida e visível

- Kover ou JaCoCo gerando relatório por módulo.
- Threshold por módulo no Gradle: build falha se cobertura cair abaixo do baseline.

---

## Segurança e Privacidade no MVP (prioridade do founder)

> Princípios derivados do CISO. **No MVP, segurança e privacidade são features explícitas, não overhead.**

### Princípios não-negociáveis

1. **Dados ficam no dispositivo.** O MVP não envia nenhum byte de dado de saúde para qualquer servidor. Sem analytics que toquem em campos de saúde. Sem crash reporters por enquanto (ver abaixo).
2. **Tudo at-rest é criptografado.** Room+SQLCipher para qualquer dado biométrico. EncryptedSharedPreferences para flags de consentimento. Chave gerada uma vez via `MasterKey` e armazenada no Android Keystore.
3. **Consentimento explícito, granular e revogável.** Cada tipo de dado (passos, sono, FC, treinos) tem permissão e finalidade declaradas separadamente.
4. **Direitos do titular operacionais desde o dia 1.**
   - **Acessar:** todos os dados visíveis nas telas do app são os dados armazenados. Sem dados ocultos.
   - **Exportar:** botão em Configurações que exporta JSON descriptografado para o storage do usuário (Storage Access Framework).
   - **Apagar:** "Apagar todos os meus dados" zera o banco, revoga permissões Health Connect e reseta o app ao estado pré-onboarding.
5. **Minimização.** Health Connect permissions solicitadas: `Steps`, `SleepSession`, `HeartRate`, `ExerciseSession`. **Apenas read.** Sem write. Sem background read no MVP.
6. **Política de retenção:** dados além de 90 dias são automaticamente apagados em background — configurável em Configurações entre 30/90/180/365 dias. Default: 90 dias (minimização).

### Compliance — implementação no MVP

| Requisito | Fonte | Implementação no MVP |
|---|---|---|
| Base legal LGPD Art. 11 | LGPD | Consentimento explícito, registrado com timestamp + versão do termo no banco criptografado. |
| Política de privacidade acessível | Google Play Health Data Policy | Página `:feature:settings` + URL pública (a definir antes do release). Mesmo conteúdo. |
| Disclosure no Play Store | Google Play | Data Safety form preenchido pelo founder com revisão CISO antes do submit. |
| Health Connect permissions declaration | Google Play | Formulário de permissões sensíveis preenchido antes do upload. |
| Samsung Health ToS | Samsung | Auditoria CISO antes de ativar SDK direto (se necessário). HC-only no MVP evita 80% das obrigações ToS. |
| Direito de exclusão (Art. 18) | LGPD | Implementado no MVP. |
| Direito de portabilidade | LGPD | Exportação JSON no MVP. |

### SDKs aprovados / proibidos no MVP

**Aprovados:**
- Hilt, Coroutines, Flow, Room, SQLCipher, Health Connect, Compose toolkit — todos open source, sem telemetria.
- Vico (charting) — sob revisão final do CTO; alternativa: charts custom Compose.

**Proibidos no MVP (sem exceção):**
- Firebase Analytics, Firebase Crashlytics, Mixpanel, Amplitude — qualquer telemetria.
- Google AdMob ou qualquer ad SDK.
- Sentry hospedado, Bugsnag — adiados até definirmos modo de uso compatível com LGPD (provavelmente self-hosted Sentry com PII scrubbing, mas v1.1+).

**Decisão tática:** MVP sem crash reporter. Crashes em pré-release são reportados manualmente via testers. CISO + CTO definem em v1.1 a estratégia compliant para crash reporting.

### Threat model resumido

- **Ameaça primária:** vazamento inadvertido via SDK terceiro ou logs. **Mitigação:** lista de SDKs proibidos, logger central com sanitização.
- **Ameaça secundária:** dispositivo perdido com app destrancado. **Mitigação:** SQLCipher com chave Keystore exige integridade do dispositivo; recomendação no onboarding para ativar bloqueio de tela.
- **Ameaça terciária:** análise estática do APK. **Mitigação:** R8/ProGuard com regras adequadas; nenhum endpoint hardcoded (já que não há backend).

---

## Monetização no MVP

### Decisão: lançar o MVP **gratuito sem paywall**.

**Racional (CFO + CEO):**
1. O MVP existe para validar uso e retenção. Paywall prematuro confunde o sinal.
2. Freemium real exige uma feature premium madura para gatear — não temos ainda.
3. One-time purchase é o modelo final desejado; não dá pra precificar com confiança antes de saber qual a duração de uso típica.
4. Lançar gratuito reduz fricção de aquisição inicial e maximiza dados de retenção W1/W4.

### Estrutura preparada para v1.1 (ainda não exposta no MVP)

- Billing flow stub atrás de feature flag interna.
- Domain model já contempla `entitlement: Free | Premium` mas sempre devolve `Premium` no MVP.
- Razão: evitar refactor doloroso em v1.1; a infra de billing fica plumbed mas inerte.

### Plano de monetização v1.1 (referência, não MVP)

- **Modelo:** freemium com one-time purchase "Unlock Everything" (alinhado com PROJETO.txt).
- **Faixa de preço inicial a testar:** R$ 19,90 a R$ 39,90 (BRL). Líquido pós-Play: 70% no primeiro ano.
- **Gates premium prováveis:** histórico além de 90 dias, exportação avançada (CSV + PDF), insights customizados (definir metas).
- **Acionamento:** após v1.0 estável + dados de 4 semanas de uso real.

---

## Agentes Adicionais Necessários

> O C-suite atual cobre direção. Precisamos de **agentes de execução** para construir o MVP sem que o CEO ou CTO precisem virar engenheiros. Cada agente abaixo deve ser criado em `.claude/agents/`.

### 1. Android Engineer Agent

- **Nome:** `android-engineer-health-insights`
- **Cor:** `blue`
- **Modelo sugerido:** `sonnet` (maioria das tarefas) com escalada para `opus` em decisões arquiteturais ambíguas.
- **Responsabilidade principal:** implementar features Kotlin + Compose seguindo os specs do CPO e a arquitetura do CTO. Escreve código de produção e seus testes acompanhantes.
- **Quando invocar:** sempre que houver tarefa concreta de implementação — nova tela, novo UseCase, novo DAO, novo wrapper de Health Connect. Recebe spec do CPO + decisão arquitetural do CTO como input.
- **Limites:** não aprova bibliotecas (CTO faz), não escreve consent copy (CISO + CMO), não decide UX (CPO). Implementa o que foi decidido.

### 2. QA / Test Engineer Agent

- **Nome:** `qa-test-engineer-health-insights`
- **Cor:** `cyan`
- **Modelo sugerido:** `sonnet`.
- **Responsabilidade principal:** dono da estratégia de testes. Escreve fixtures, define cenários de teste, mantém suíte de regras de insight, cuida da matriz de cobertura, audita PRs procurando lacunas de teste.
- **Quando invocar:**
  - Antes de cada feature, para definir os casos de teste obrigatórios.
  - Após cada PR de feature, para revisar se a cobertura cumpriu o threshold e se cenários de erro foram cobertos.
  - Quando houver regressão ou bug em produção — escrever o teste que a teria pegado antes do fix.
- **Princípio crítico (founder):** nenhum PR de feature passa sem o sign-off deste agente.

### 3. Data / Analytics Designer Agent

- **Nome:** `data-insights-designer-health-insights`
- **Cor:** `magenta`
- **Modelo sugerido:** `opus`.
- **Responsabilidade principal:** desenhar as **regras** que transformam séries temporais de Samsung Health em insights interpretados. Define limiares, janelas de comparação, copy interpretativo ("você dormiu 23% menos esta semana"), trata edge cases de dados esparsos.
- **Quando invocar:**
  - Para cada nova feature de insight (Resumo Semanal é o primeiro caso).
  - Quando uma regra produzir output ambíguo ou enganoso.
  - Quando o CPO precisar de copy-template para um headline interpretativo.
- **Saída típica:** documento de regras (input → condição → output template) + dataset de exemplo + casos de teste para o QA agent codificar.

### 4. Security Reviewer Agent

- **Nome:** `security-reviewer-health-insights`
- **Cor:** `crimson`
- **Modelo sugerido:** `opus`.
- **Responsabilidade principal:** revisar **código** com lente de segurança. Diferente do CISO (que define política), este agente lê diffs procurando: vazamento de PII em logs, uso correto de Keystore, queries SQL parametrizadas, falta de tratamento em erros de permissão, dependências introduzidas sem aprovação.
- **Quando invocar:**
  - Em todo PR que toque em `:core:database`, `:core:health-connect`, `:core:datastore`, `:feature:onboarding` ou `:feature:settings`.
  - Antes de cada release candidate (auditoria full).
  - Após adoção de qualquer nova dependência.
- **Output:** veredicto APROVAR / REJEITAR + lista de findings com severidade. Reject bloqueia merge.

### 5. Release / DevOps Agent

- **Nome:** `release-engineer-health-insights`
- **Cor:** `gray`
- **Modelo sugerido:** `sonnet`.
- **Responsabilidade principal:** dono do CI/CD, GitHub Actions, Fastlane (futuro), versionamento, notas de release, gestão de secrets. Garante que o pipeline cumpre os gates de teste e segurança definidos.
- **Quando invocar:**
  - Setup inicial do GitHub Actions.
  - Mudança de gate no pipeline.
  - Configuração do upload para Play Console (interno → fechado → produção).
  - Investigação de falhas intermitentes de CI.

### 6. Compliance Documentation Agent

- **Nome:** `compliance-docs-health-insights`
- **Cor:** `olive`
- **Modelo sugerido:** `sonnet`.
- **Responsabilidade principal:** redigir e manter os documentos de compliance: política de privacidade (PT-BR + EN), termos de uso, conteúdo do Data Safety form do Play Store, relatório de impacto LGPD básico, registros de consentimento. Trabalha sob direção do CISO.
- **Quando invocar:**
  - Antes do submit ao Play Console.
  - Sempre que o escopo de dados acessados mudar.
  - Para revisar copy de consentimento antes do CPO finalizar a tela.

### Quem NÃO criamos no MVP (e por quê)

- **UI Designer Agent (Figma/visual):** o CPO produz specs em texto e o Android Engineer implementa com componentes do `:core:design`. Sem mockups dedicados no MVP.
- **Backend Engineer Agent:** não há backend. Adiar até v2 ou nunca.
- **Localization Agent:** MVP é PT-BR only. Inglês entra em v1.1.
- **Customer Support Agent:** sem usuários reais ainda.

---

## Próximos Passos

Sequência mínima viável para sair de "plano" para "primeiro PR":

1. **Founder cria os 6 agentes adicionais** em `.claude/agents/` usando o template existente do C-suite. CEO valida cada definition file antes do uso. *(Bloqueador para tudo abaixo.)*
2. **CISO finaliza o copy do consentimento** — texto exato para cada uma das 4 permissões Health Connect. Compliance Documentation Agent rascunha a política de privacidade.
3. **CTO publica documento de bootstrap** — repositório Gradle multi-module com a estrutura definida acima, pré-populado com lint, detekt, kover e GitHub Actions baseline. Inclui um "hello world" no `:feature:dashboard` apenas para validar o pipeline ponta-a-ponta.
4. **Data Insights Designer especifica as 3 regras do Resumo Semanal** — input, lógica, copy templates, edge cases. QA codifica os testes a partir desse spec antes da implementação começar.
5. **CPO entrega specs detalhados de Onboarding e Dashboard** (estados happy/empty/error/loading para cada tela). CMO valida copy.
6. **Android Engineer começa a implementação** na ordem das features listadas (Onboarding → Dashboard → Tendência de Passos → Sono → Resumo Semanal). Cada PR passa pelo Security Reviewer + QA Engineer.
7. **Internal alpha** após Onboarding + Dashboard estarem verdes em todos os gates. Founder usa por 1 semana com dados próprios.
8. **Release decision**: ao completar todas as 5 features com cobertura ≥ 85% em domain/viewmodel e auditoria de segurança limpa, CEO + CISO assinam o go/no-go para Play Console internal track.

**O CEO revisa este plano em cada marco e atualiza** se evidência da execução contradisser uma decisão aqui. Nenhuma seção é sagrada — exceto as obrigações de testes, segurança e LGPD, que são linha vermelha do founder.
