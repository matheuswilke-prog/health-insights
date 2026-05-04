# Health Insights — Backlog do MVP

> Fonte de verdade: decisões do CEO/CPO. Nada neste backlog foi inventado pelo PM — toda feature reflete um épico aprovado.
> WIP limit: 1 feature ativa por vez (dev solo). Bloqueadores sobem imediatamente ao PM.
> Data de criação: 2026-05-03

---

## Status do Backlog

| Épico | Descrição | Nº Stories | Status |
|-------|-----------|------------|--------|
| EP-00 | Pré-requisitos (inputs de agentes) | 9 entregáveis | Aguardando agentes |
| EP-01 | Setup & Infraestrutura | 6 stories | A fazer |
| EP-02 | Onboarding com Consentimento LGPD | 5 stories | A fazer |
| EP-03 | Dashboard Diário | 5 stories | A fazer |
| EP-04 | Tendência Semanal de Passos | 4 stories | A fazer |
| EP-05 | Análise de Sono | 4 stories | A fazer |
| EP-06 | Resumo Semanal | 4 stories | A fazer |
| EP-07 | Configurações | 4 stories | A fazer |
| **Total** | | **41 itens** | |

---

## EP-00: Pré-requisitos (bloqueantes — antes de qualquer código)

> Estes não são stories de código. São entregáveis que outros agentes devem completar antes que o Android Engineer possa iniciar as stories que dependem deles. O PM rastreia cada item aberto como bloqueador ativo.

| ID | Agente responsável | Entregável | Bloqueia |
|----|-------------------|------------|---------|
| PRE-01 | Compliance Docs | Rascunho da Política de Privacidade (PT-BR, LGPD Art. 9) | EP-02 |
| PRE-02 | CISO / Compliance Docs | Aprovar copy de consentimento explícito para coleta de dados de saúde (passos, sono, frequência cardíaca) | EP-02 |
| PRE-03 | Compliance Docs | Definir lista exata de dados coletados, finalidade e prazo de retenção (tabela LGPD) | EP-02, EP-07 |
| PRE-04 | Data Insights Designer | Entregar documento com as 3 regras de insight do Resumo Semanal (limiar de melhora, mensagem neutra, mensagem de alerta) | EP-06 inteiro |
| PRE-05 | Data Insights Designer | Especificação visual do Dashboard Diário (layouts dos 4 estados: Loading, Empty, Content, Error) | EP-03 |
| PRE-06 | Data Insights Designer | Especificação visual da tela de Tendência Semanal de Passos (gráfico, eixos, estados) | EP-04 |
| PRE-07 | Data Insights Designer | Especificação visual da tela de Análise de Sono (card de duração, estágios, estados) | EP-05 |
| PRE-08 | Security Reviewer | Aprovar estratégia de chave SQLCipher + Android Keystore antes do EP-01-04 | EP-01 |
| PRE-09 | Release Engineer | Configurar repositório GitHub, branch protection (main), secrets de CI (KEYSTORE, etc.) | EP-01 |

---

## EP-01: Setup & Infraestrutura

> Todas as stories deste épico são P0 Bloqueador. Nenhuma feature pode começar sem EP-01 completo.

---

### EP-01-01 — Bootstrap do Projeto Multi-Module Gradle

**Módulo(s):** [`:app`, `:core:common`, `:core:design`, `:core:domain`, `:core:database`, `:core:datastore`, `:core:health-connect`, `:core:testing`, `:feature:onboarding`, `:feature:dashboard`, `:feature:steps`, `:feature:sleep`, `:feature:weekly-summary`, `:feature:settings`]
**Prioridade:** P0 Bloqueador
**Depende de:** PRE-09
**Pré-requisito de agente:** Release Engineer: repositório GitHub criado com branch protection ativo

Como Android Engineer, quero o projeto Gradle multi-module criado com todos os módulos declarados para que cada feature possa ser desenvolvida de forma isolada e paralela.

**Critérios de aceitação:**
- [ ] Todos os 14 módulos declarados no `settings.gradle.kts` e compilando sem erros
- [ ] `build.gradle.kts` de cada módulo usa convention plugins (ou catálogo de versões `libs.versions.toml`)
- [ ] Hilt configurado no `:app` e propagado para todos os módulos feature
- [ ] Projeto abre e sincroniza no Android Studio sem erros
- [ ] `./gradlew build` retorna BUILD SUCCESSFUL em máquina limpa

**Definition of Done:**
- [ ] Implementação completa
- [ ] CI verde

---

### EP-01-02 — GitHub Actions CI (build + lint + test)

**Módulo(s):** [`.github/workflows`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-01, PRE-09
**Pré-requisito de agente:** Release Engineer: secrets de keystore configurados no repositório

Como Android Engineer, quero um pipeline de CI que execute build, lint e testes a cada PR para que nenhum código quebrado entre na branch main.

**Critérios de aceitação:**
- [ ] Workflow `ci.yml` executa em todo push para `main` e em todo Pull Request
- [ ] Steps: checkout → Java setup → Gradle cache → `assembleDebug` → `lint` → `testDebugUnitTest`
- [ ] Falha de lint ou teste quebra o pipeline (exit code != 0)
- [ ] Tempo de execução < 15 minutos em PR típico
- [ ] Badge de status do CI exibido no README

**Definition of Done:**
- [ ] Implementação completa
- [ ] CI verde

---

### EP-01-03 — Módulo `:core:testing` com Fakes Base

**Módulo(s):** [`:core:testing`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-01
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero um módulo de testing com fakes reutilizáveis dos repositórios e use cases para que cada módulo feature possa escrever testes unitários sem dependências reais.

**Critérios de aceitação:**
- [ ] `FakeHealthConnectRepository` implementa a interface real com dados configuráveis
- [ ] `FakeStepsRepository`, `FakeSleepRepository` disponíveis
- [ ] `TestCoroutineRule` / `MainDispatcherRule` disponível para todos os módulos
- [ ] `FakeDataStore` disponível para testes de preferências
- [ ] Módulo `testImplementation` acessível por todos os `:feature:*` e `:core:*`

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários dos próprios fakes passando
- [ ] CI verde

---

### EP-01-04 — SQLCipher + Room: configuração segura do banco

**Módulo(s):** [`:core:database`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-01, PRE-08
**Pré-requisito de agente:** Security Reviewer: aprovar estratégia de chave (PRE-08)

Como Android Engineer, quero o banco de dados Room configurado com SQLCipher e chave gerenciada pelo Android Keystore para que os dados de saúde em repouso estejam criptografados.

**Critérios de aceitação:**
- [ ] Banco Room usa `net.zetetic:android-database-sqlcipher` como SupportFactory
- [ ] Chave gerada/recuperada via `AndroidKeyStore` (AES-256-GCM, sem exportação)
- [ ] Nenhum dado de saúde gravado em banco sem criptografia ativa
- [ ] Abertura do banco falha ruidosamente (não silenciosamente) se a chave não estiver disponível
- [ ] Nenhum campo de saúde (passos, sono, FC) aparece em logs (Logcat, Crashlytics ou similares)

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando — DAOs com 100% de cobertura (InstrumentedTest com banco em memória criptografado)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-01-05 — Health Connect: permissões e cliente base

**Módulo(s):** [`:core:health-connect`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-01
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero o cliente Health Connect configurado com as permissões necessárias declaradas no manifest para que as features possam solicitar e verificar acesso aos dados de saúde.

**Critérios de aceitação:**
- [ ] `HealthConnectClient` injetado via Hilt como singleton
- [ ] Permissões `READ_STEPS`, `READ_SLEEP`, `READ_HEART_RATE` declaradas no `AndroidManifest.xml`
- [ ] `HealthConnectAvailability` verificada na inicialização (disponível / não disponível / versão desatualizada)
- [ ] Interface `HealthConnectRepository` definida em `:core:domain` (sem implementação de negócio aqui)
- [ ] Nenhum dado de saúde aparece em logs

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura — testar os 3 estados de disponibilidade com fake)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-01-06 — DataStore: preferências criptografadas do usuário

**Módulo(s):** [`:core:datastore`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-01
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero um DataStore criptografado (EncryptedSharedPreferences ou Jetpack Security) para armazenar preferências do usuário (consentimento LGPD, configurações) de forma segura.

**Critérios de aceitação:**
- [ ] `UserPreferencesDataStore` usa `EncryptedSharedPreferences` ou `androidx.security:security-crypto`
- [ ] Campos obrigatórios: `lgpdConsentGiven: Boolean`, `lgpdConsentTimestamp: Long`, `onboardingCompleted: Boolean`
- [ ] Nenhum campo é PII — apenas flags e timestamps
- [ ] Interface `UserPreferencesRepository` definida em `:core:domain`

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

## EP-02: Onboarding com Consentimento LGPD

> Pré-requisito hard: PRE-01, PRE-02 e PRE-03 devem estar completos antes de qualquer story deste épico.

---

### EP-02-01 — Tela de Boas-vindas (Welcome Screen)

**Módulo(s):** [`:feature:onboarding`, `:core:design`]
**Prioridade:** P1 Must-have
**Depende de:** EP-01-01, EP-01-02, EP-01-06
**Pré-requisito de agente:** Data Insights Designer: especificação visual da tela de boas-vindas; PRE-01 (política de privacidade rascunhada)

Como Marcos (usuário Samsung Galaxy Watch), quero ver uma tela de boas-vindas clara ao abrir o app pela primeira vez para que eu entenda o propósito do app antes de aceitar qualquer coleta de dados.

**Critérios de aceitação:**
- [ ] Tela exibida somente se `onboardingCompleted == false` no DataStore
- [ ] Exibe nome do app, propósito em uma frase e logotipo
- [ ] Botão "Começar" navega para a tela de consentimento LGPD
- [ ] Nenhuma coleta de dados ocorre nesta tela
- [ ] Estado loading: skeleton/placeholder enquanto DataStore carrega a flag
- [ ] Estado content: tela completa renderizada
- [ ] Estado error: mensagem genérica se DataStore falhar ao ler a flag

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 3 estados (Loading, Content, Error)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-02-02 — Tela de Consentimento LGPD

**Módulo(s):** [`:feature:onboarding`, `:core:datastore`, `:core:design`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-02-01, PRE-01, PRE-02, PRE-03
**Pré-requisito de agente:** CISO/Compliance Docs: copy de consentimento aprovado (PRE-02); Política de Privacidade rascunhada (PRE-01); tabela LGPD de dados coletados (PRE-03)

Como Marcos, quero ler o que será coletado e dar consentimento explícito e granular antes de qualquer coleta para que eu exerça meu direito LGPD de saber e decidir sobre meus dados.

**Critérios de aceitação:**
- [ ] Exibe o copy de consentimento aprovado pelo CISO/Compliance (texto não pode ser alterado sem nova aprovação)
- [ ] Lista explícita dos dados coletados: passos, sono, frequência cardíaca — com finalidade de cada um
- [ ] Link funcional para a Política de Privacidade completa (abre WebView ou navegador)
- [ ] Checkbox de aceite individual para cada tipo de dado (passos, sono, FC) — ou aceite único com lista explícita (conforme decisão Compliance)
- [ ] Botão "Aceitar e Continuar" habilitado somente após interação ativa do usuário com o(s) checkbox(es)
- [ ] Botão "Recusar" disponível — ao recusar, app encerra ou exibe tela informando que não pode funcionar sem consentimento
- [ ] Ao aceitar: `lgpdConsentGiven = true`, `lgpdConsentTimestamp = System.currentTimeMillis()` gravados no DataStore
- [ ] Aceite gravado no DataStore antes de qualquer solicitação de permissão Health Connect
- [ ] Estado loading: indicador enquanto salva consentimento
- [ ] Estado content: formulário completo
- [ ] Estado error: mensagem se DataStore falhar ao gravar (com retry)

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-02-03 — Solicitação de Permissões Health Connect

**Módulo(s):** [`:feature:onboarding`, `:core:health-connect`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-02-02, EP-01-05
**Pré-requisito de agente:** nenhum (consentimento LGPD já garantido por EP-02-02)

Como Marcos, quero ser guiado para conceder permissões ao Health Connect logo após o consentimento LGPD para que o app possa ler meus dados de saúde de forma legítima.

**Critérios de aceitação:**
- [ ] Solicitação de permissões ocorre somente após `lgpdConsentGiven == true`
- [ ] Tela explicativa (rationale) exibida antes do diálogo do sistema para `READ_STEPS`, `READ_SLEEP`, `READ_HEART_RATE`
- [ ] Se Health Connect não instalado: exibe mensagem com link para a Play Store
- [ ] Se usuário negar permissões: app continua mas exibe estado de dados indisponível nas features afetadas (não trava)
- [ ] Se usuário conceder: navega para o Dashboard
- [ ] Permissões não solicitadas novamente se já concedidas
- [ ] Estado loading: indicador durante verificação de disponibilidade do Health Connect
- [ ] Estado content: tela de rationale + botão para abrir diálogo do sistema
- [ ] Estado error: Health Connect indisponível ou versão desatualizada — mensagem clara com ação

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-02-04 — Navegação pós-onboarding e guarda de rota

**Módulo(s):** [`:app`, `:feature:onboarding`]
**Prioridade:** P1 Must-have
**Depende de:** EP-02-02, EP-02-03
**Pré-requisito de agente:** nenhum

Como Marcos, quero que ao abrir o app novamente eu vá direto ao Dashboard (sem repetir o onboarding) para que minha experiência seja contínua.

**Critérios de aceitação:**
- [ ] Se `onboardingCompleted == true`, a rota inicial é o Dashboard (não o Onboarding)
- [ ] `onboardingCompleted` gravado como `true` somente após consentimento aceito E permissões respondidas (mesmo que negadas)
- [ ] Deep links externos não bypassam a verificação de onboarding
- [ ] Navegação usa Jetpack Navigation Component — sem back stack exposto ao usuário no onboarding
- [ ] Estado loading: splash/tela em branco enquanto DataStore carrega flags de roteamento

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo Loading e os dois branches de navegação (onboarding vs dashboard)
- [ ] CI verde

---

### EP-02-05 — Use Cases de Onboarding

**Módulo(s):** [`:core:domain`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-06, EP-01-05
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero use cases de domínio para o fluxo de onboarding (verificar consentimento, salvar consentimento, verificar onboarding completo) para que a lógica de negócio fique isolada de UI e infraestrutura.

**Critérios de aceitação:**
- [ ] `CheckLgpdConsentUseCase`: retorna estado atual do consentimento
- [ ] `SaveLgpdConsentUseCase`: persiste aceite com timestamp — rejeita chamada se já existir aceite válido
- [ ] `IsOnboardingCompletedUseCase`: retorna Boolean
- [ ] `CompleteOnboardingUseCase`: marca onboarding como concluído
- [ ] Todos os use cases são classes independentes (não um god-class), injetáveis via Hilt

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM — usar fakes de EP-01-03)
- [ ] CI verde

---

## EP-03: Dashboard Diário

> Pré-requisito: EP-01 completo, EP-02 completo, PRE-05 entregue pelo Data Insights Designer.

---

### EP-03-01 — Use Cases do Dashboard Diário

**Módulo(s):** [`:core:domain`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-03, EP-01-05, EP-02-05
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero use cases de domínio que agreguem os dados do dia atual (passos, sono da noite anterior, FC média) para que o ViewModel do Dashboard tenha uma API limpa de negócio.

**Critérios de aceitação:**
- [ ] `GetDailyStepsUseCase`: retorna `Flow<Result<Int>>` com passos do dia atual
- [ ] `GetLastNightSleepUseCase`: retorna `Flow<Result<SleepSummary>>` com duração e estágios da noite anterior
- [ ] `GetDailyHeartRateUseCase`: retorna `Flow<Result<HeartRateSummary>>` com média e range do dia
- [ ] Cada use case lida com o estado de permissão revogada (retorna `Result.Error` com tipo `PermissionRevoked`)
- [ ] Cada use case lida com Health Connect indisponível (retorna `Result.Error` com tipo `HealthConnectUnavailable`)

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] CI verde

---

### EP-03-02 — Repositório e DAO de Cache do Dashboard

**Módulo(s):** [`:core:database`, `:core:health-connect`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-04, EP-01-05
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero um DAO e repositório que façam cache dos dados diários no banco Room/SQLCipher para que o Dashboard exiba dados mesmo quando o Health Connect estiver temporariamente indisponível.

**Critérios de aceitação:**
- [ ] `DailySnapshotDao` com operações `insert`, `getByDate`, `deleteOlderThan(days: Int)`
- [ ] Política de retenção: dados de mais de 90 dias são excluídos automaticamente
- [ ] `DailySnapshotRepository` implementa interface de `:core:domain` — tenta Health Connect primeiro, cai para cache
- [ ] Nenhum campo PII gravado (sem nome, e-mail, ID de conta)
- [ ] Nenhum dado de saúde em logs

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando — DAOs com 100% de cobertura (InstrumentedTest)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-03-03 — ViewModel do Dashboard Diário

**Módulo(s):** [`:feature:dashboard`]
**Prioridade:** P1 Must-have
**Depende de:** EP-03-01, EP-03-02
**Pré-requisito de agente:** PRE-05 (spec visual do dashboard)

Como Android Engineer, quero um ViewModel que exponha um `StateFlow<DashboardUiState>` com os 4 estados de UI para que a tela seja reativa e testável.

**Critérios de aceitação:**
- [ ] `DashboardUiState` sealed class com: `Loading`, `Empty` (sem dados para hoje), `Content(steps, sleep, heartRate)`, `Error(type: ErrorType)`
- [ ] `ErrorType` distingue: `PermissionRevoked`, `HealthConnectUnavailable`, `Generic`
- [ ] ViewModel não contém lógica de negócio — delega aos use cases
- [ ] Refresh manual exposto como evento no ViewModel
- [ ] ViewModel sobrevive a mudança de configuração

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura — testar os 4 estados com fakes)
- [ ] CI verde

---

### EP-03-04 — Tela do Dashboard Diário (Compose)

**Módulo(s):** [`:feature:dashboard`, `:core:design`]
**Prioridade:** P1 Must-have
**Depende de:** EP-03-03, PRE-05
**Pré-requisito de agente:** PRE-05 (spec visual entregue pelo Data Insights Designer)

Como Marcos, quero ver no Dashboard meus passos do dia, qualidade do sono da noite anterior e frequência cardíaca média para que eu tenha uma visão rápida da minha saúde ao abrir o app.

**Critérios de aceitação:**
- [ ] Estado Loading: Shimmer/skeleton nos cards de passos, sono e FC
- [ ] Estado Empty: ilustração + mensagem "Nenhum dado registrado para hoje" + botão de refresh
- [ ] Estado Content: card de passos (número + meta visual), card de sono (horas + qualidade), card de FC (média + range)
- [ ] Estado Error — PermissionRevoked: banner informativo + botão "Gerenciar Permissões" que abre Configurações do Health Connect
- [ ] Estado Error — HealthConnectUnavailable: mensagem + link para instalar/atualizar Health Connect
- [ ] Estado Error — Generic: mensagem genérica + botão retry
- [ ] Pull-to-refresh funcional
- [ ] Acessibilidade: todos os elementos com contentDescription

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] CI verde

---

### EP-03-05 — Navegação para features a partir do Dashboard

**Módulo(s):** [`:feature:dashboard`, `:app`]
**Prioridade:** P1 Must-have
**Depende de:** EP-03-04
**Pré-requisito de agente:** nenhum

Como Marcos, quero poder navegar do Dashboard para Tendência de Passos, Análise de Sono e Resumo Semanal tocando nos respectivos cards para que eu acesse o detalhe de cada métrica.

**Critérios de aceitação:**
- [ ] Card de passos navega para `:feature:steps`
- [ ] Card de sono navega para `:feature:sleep`
- [ ] Ícone/botão de Resumo Semanal navega para `:feature:weekly-summary`
- [ ] Ícone de Configurações navega para `:feature:settings`
- [ ] Navegação usa Jetpack Navigation Component — rotas tipadas

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes de UI cobrindo os 4 destinos de navegação
- [ ] CI verde

---

## EP-04: Tendência Semanal de Passos

> Pré-requisito: EP-03 completo, PRE-06 entregue pelo Data Insights Designer.

---

### EP-04-01 — Use Cases de Tendência Semanal de Passos

**Módulo(s):** [`:core:domain`]
**Prioridade:** P1 Must-have
**Depende de:** EP-03-01, EP-01-03
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero um use case que retorne os passos dos últimos 7 dias agrupados por dia para que o gráfico de tendência possa ser renderizado.

**Critérios de aceitação:**
- [ ] `GetWeeklyStepsTrendUseCase`: retorna `Flow<Result<List<DailySteps>>>` com 7 entradas (dias sem dado retornam 0 ou `null` — definir com CPO)
- [ ] Período: D-6 até D+0 (hoje), calculado em UTC
- [ ] Lida com permissão revogada e Health Connect indisponível (tipos de erro explícitos)
- [ ] Não expõe dados de dias além da janela de 7 dias

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] CI verde

---

### EP-04-02 — DAO e cache de passos semanais

**Módulo(s):** [`:core:database`]
**Prioridade:** P1 Must-have
**Depende de:** EP-03-02
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero que os dados de passos semanais sejam cacheados no banco SQLCipher para que o gráfico funcione offline.

**Critérios de aceitação:**
- [ ] `StepsDailyDao` com `upsertDay`, `getRange(startDate, endDate)`, `deleteOlderThan`
- [ ] Política de retenção: 90 dias (consistente com EP-03-02)
- [ ] Sem PII, sem dados em logs

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando — DAO com 100% de cobertura (InstrumentedTest)
- [ ] CI verde

---

### EP-04-03 — ViewModel da Tendência Semanal de Passos

**Módulo(s):** [`:feature:steps`]
**Prioridade:** P1 Must-have
**Depende de:** EP-04-01, EP-04-02
**Pré-requisito de agente:** PRE-06 (spec visual)

Como Android Engineer, quero um ViewModel que exponha `StateFlow<StepsTrendUiState>` com os 4 estados para que a tela de passos seja reativa.

**Critérios de aceitação:**
- [ ] `StepsTrendUiState` sealed class: `Loading`, `Empty`, `Content(days: List<DailySteps>, weeklyTotal: Int, dailyAverage: Int)`, `Error(type: ErrorType)`
- [ ] Sem lógica de negócio no ViewModel

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura — 4 estados)
- [ ] CI verde

---

### EP-04-04 — Tela de Tendência Semanal de Passos (Compose)

**Módulo(s):** [`:feature:steps`, `:core:design`]
**Prioridade:** P1 Must-have
**Depende de:** EP-04-03, PRE-06
**Pré-requisito de agente:** PRE-06 (spec visual entregue pelo Data Insights Designer)

Como Marcos, quero ver um gráfico de barras dos meus passos dos últimos 7 dias com total semanal e média diária para que eu identifique tendências de atividade.

**Critérios de aceitação:**
- [ ] Estado Loading: skeleton do gráfico e cards de resumo
- [ ] Estado Empty: mensagem "Sem dados de passos para os últimos 7 dias" + botão refresh
- [ ] Estado Content: gráfico de barras (7 dias), total semanal, média diária — conforme spec PRE-06
- [ ] Estado Error — PermissionRevoked: banner + botão "Gerenciar Permissões"
- [ ] Estado Error — HealthConnectUnavailable: mensagem + link para instalar/atualizar
- [ ] Estado Error — Generic: mensagem + retry
- [ ] Eixo X com rótulos de dia (Seg, Ter… ou D-6, D-5…) — conforme spec
- [ ] Acessibilidade: contentDescription em cada barra com valor e dia

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] CI verde

---

## EP-05: Análise de Sono

> Pré-requisito: EP-03 completo, PRE-07 entregue pelo Data Insights Designer.

---

### EP-05-01 — Use Cases de Análise de Sono

**Módulo(s):** [`:core:domain`]
**Prioridade:** P1 Must-have
**Depende de:** EP-03-01, EP-01-03
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero use cases que retornem os dados de sono da noite anterior e a tendência semanal de sono para que as telas de análise tenham uma API de domínio clara.

**Critérios de aceitação:**
- [ ] `GetLastNightSleepDetailUseCase`: retorna `Flow<Result<SleepDetail>>` com duração total, estágios (leve, profundo, REM, acordado) e hora de início/fim
- [ ] `GetWeeklySleepTrendUseCase`: retorna `Flow<Result<List<DailySleep>>>` — 7 dias, mesmo padrão de EP-04-01
- [ ] Lida com permissão revogada e Health Connect indisponível

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] CI verde

---

### EP-05-02 — DAO e cache de sono

**Módulo(s):** [`:core:database`]
**Prioridade:** P1 Must-have
**Depende de:** EP-03-02
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero que os dados de sono sejam cacheados no banco SQLCipher para que a análise funcione offline.

**Critérios de aceitação:**
- [ ] `SleepSessionDao` com `upsertSession`, `getByDate`, `getRange(startDate, endDate)`, `deleteOlderThan`
- [ ] Modelo de dados inclui estágios como JSON criptografado ou campos separados (decisão de arquitetura a documentar no PR)
- [ ] Política de retenção: 90 dias
- [ ] Sem PII, sem dados em logs

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando — DAO com 100% de cobertura (InstrumentedTest)
- [ ] CI verde

---

### EP-05-03 — ViewModel da Análise de Sono

**Módulo(s):** [`:feature:sleep`]
**Prioridade:** P1 Must-have
**Depende de:** EP-05-01, EP-05-02
**Pré-requisito de agente:** PRE-07 (spec visual)

Como Android Engineer, quero um ViewModel que exponha `StateFlow<SleepAnalysisUiState>` com os 4 estados.

**Critérios de aceitação:**
- [ ] `SleepAnalysisUiState` sealed class: `Loading`, `Empty`, `Content(lastNight: SleepDetail, weeklyTrend: List<DailySleep>)`, `Error(type: ErrorType)`
- [ ] Sem lógica de negócio no ViewModel

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura — 4 estados)
- [ ] CI verde

---

### EP-05-04 — Tela de Análise de Sono (Compose)

**Módulo(s):** [`:feature:sleep`, `:core:design`]
**Prioridade:** P1 Must-have
**Depende de:** EP-05-03, PRE-07
**Pré-requisito de agente:** PRE-07 (spec visual entregue pelo Data Insights Designer)

Como Marcos, quero ver a duração e os estágios do meu sono da noite anterior, além da tendência dos últimos 7 dias, para que eu compreenda a qualidade do meu descanso.

**Critérios de aceitação:**
- [ ] Estado Loading: skeleton dos cards e gráfico
- [ ] Estado Empty: mensagem "Nenhum dado de sono registrado" + botão refresh
- [ ] Estado Content: card de duração total (horas:minutos), breakdown de estágios (leve/profundo/REM/acordado), tendência semanal — conforme spec PRE-07
- [ ] Estado Error — PermissionRevoked: banner + botão "Gerenciar Permissões"
- [ ] Estado Error — HealthConnectUnavailable: mensagem + link
- [ ] Estado Error — Generic: mensagem + retry
- [ ] Acessibilidade: contentDescription em cada estágio

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] CI verde

---

## EP-06: Resumo Semanal

> HARD BLOCKER: PRE-04 (3 regras de insight do Data Insights Designer) DEVE estar completo e aprovado pelo CPO antes de qualquer story deste épico poder ser iniciada. Nenhum código de EP-06 começa sem esse documento.

---

### EP-06-01 — Use Cases do Resumo Semanal

**Módulo(s):** [`:core:domain`]
**Prioridade:** P1 Must-have
**Depende de:** EP-04-01, EP-05-01, PRE-04
**Pré-requisito de agente:** PRE-04 (regras de insight — obrigatório antes de começar)

Como Android Engineer, quero use cases que calculem o resumo semanal e apliquem as regras de insight definidas pelo Data Insights Designer para que o Resumo Semanal reflita as decisões de produto.

**Critérios de aceitação:**
- [ ] `GetWeeklySummaryUseCase`: agrega dados de passos (EP-04-01) e sono (EP-05-01) da semana
- [ ] Aplica as 3 regras de insight conforme documento PRE-04 (limiares, mensagem neutra, mensagem de alerta) — sem desvio das regras aprovadas
- [ ] Retorna `Flow<Result<WeeklySummary>>` com `insightMessage: String` e `insightType: InsightType` (Improvement, Neutral, Alert)
- [ ] Lida com dados parciais (menos de 7 dias de dados) — regra de negócio a definir em PRE-04

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura — testar as 3 regras de insight com dados controlados)
- [ ] CI verde

---

### EP-06-02 — ViewModel do Resumo Semanal

**Módulo(s):** [`:feature:weekly-summary`]
**Prioridade:** P1 Must-have
**Depende de:** EP-06-01
**Pré-requisito de agente:** PRE-04 (aprovado)

Como Android Engineer, quero um ViewModel que exponha `StateFlow<WeeklySummaryUiState>` para que a tela de resumo seja reativa.

**Critérios de aceitação:**
- [ ] `WeeklySummaryUiState` sealed class: `Loading`, `Empty`, `Content(summary: WeeklySummary)`, `Error(type: ErrorType)`
- [ ] Sem lógica de negócio no ViewModel

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura — 4 estados)
- [ ] CI verde

---

### EP-06-03 — Tela do Resumo Semanal (Compose)

**Módulo(s):** [`:feature:weekly-summary`, `:core:design`]
**Prioridade:** P1 Must-have
**Depende de:** EP-06-02, PRE-04
**Pré-requisito de agente:** PRE-04 (regras de insight + especificação visual aprovados)

Como Marcos, quero ver um resumo da minha semana com totais de passos, médias de sono e uma mensagem de insight personalizada para que eu entenda como foi minha semana de saúde.

**Critérios de aceitação:**
- [ ] Estado Loading: skeleton dos cards e insight
- [ ] Estado Empty: mensagem "Dados insuficientes para gerar resumo semanal" (menos de X dias de dados — conforme PRE-04)
- [ ] Estado Content: total de passos da semana, média diária de sono, mensagem de insight (Improvement / Neutral / Alert) conforme regras PRE-04
- [ ] Estado Error — PermissionRevoked: banner + botão "Gerenciar Permissões"
- [ ] Estado Error — Generic: mensagem + retry
- [ ] Insight de alerta exibido com visual diferenciado (cor/ícone conforme spec)
- [ ] Acessibilidade: contentDescription na mensagem de insight

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] CI verde

---

### EP-06-04 — Período do Resumo Semanal (cálculo de janela)

**Módulo(s):** [`:core:domain`]
**Prioridade:** P1 Must-have
**Depende de:** EP-06-01
**Pré-requisito de agente:** nenhum

Como Android Engineer, quero uma utilidade de domínio que calcule a janela semanal (início e fim) de forma consistente para que todos os use cases usem a mesma definição de "semana".

**Critérios de aceitação:**
- [ ] `WeekWindowCalculator` define semana como segunda a domingo (ISO 8601) ou D-6 a D+0 — CPO escolhe, documentado no PR
- [ ] Cálculo é timezone-aware (usa ZonedDateTime ou equivalente)
- [ ] Reutilizado por EP-06-01, EP-04-01 e EP-05-01 (refatoração se necessário)

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura — testar mudança de timezone e virada de ano)
- [ ] CI verde

---

## EP-07: Configurações

> Todas as stories deste épico são P0 — são obrigações LGPD, não features opcionais. Pré-requisito: PRE-03 completo.

---

### EP-07-01 — Tela principal de Configurações (Compose)

**Módulo(s):** [`:feature:settings`, `:core:design`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-01-06, EP-02-05, PRE-03
**Pré-requisito de agente:** Compliance Docs: tabela LGPD de dados coletados (PRE-03)

Como Marcos, quero uma tela de Configurações com acesso rápido às opções de privacidade e permissões para que eu exerça meus direitos LGPD a qualquer momento.

**Critérios de aceitação:**
- [ ] Lista de itens: "Privacidade e Consentimento", "Permissões de Saúde", "Exportar meus dados", "Excluir meus dados", "Política de Privacidade", "Versão do app"
- [ ] Estado loading: skeleton enquanto carrega preferências do DataStore
- [ ] Estado content: lista completa renderizada
- [ ] Estado error: mensagem se DataStore falhar
- [ ] Acessibilidade: todos os itens com contentDescription

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 3 estados (Loading, Content, Error)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-07-02 — Gerenciamento de Consentimento LGPD nas Configurações

**Módulo(s):** [`:feature:settings`, `:core:datastore`, `:core:domain`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-07-01, EP-02-05, PRE-01, PRE-03
**Pré-requisito de agente:** Compliance Docs: PRE-01 e PRE-03; CISO: PRE-02

Como Marcos, quero ver e gerenciar meu consentimento LGPD nas Configurações para que eu possa revogar o consentimento ou ver quando o consenti.

**Critérios de aceitação:**
- [ ] Exibe data/hora do consentimento original
- [ ] Exibe lista dos dados consentidos (conforme PRE-03)
- [ ] Botão "Revogar Consentimento" disponível — abre diálogo de confirmação com consequências claras
- [ ] Ao revogar: `lgpdConsentGiven = false`, dados locais excluídos (chamar `EP-07-04`), app retorna ao Onboarding
- [ ] Link para Política de Privacidade atualizada
- [ ] Estado loading, content e error com mensagens específicas

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-07-03 — Gerenciamento de Permissões Health Connect nas Configurações

**Módulo(s):** [`:feature:settings`, `:core:health-connect`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-07-01, EP-01-05
**Pré-requisito de agente:** nenhum

Como Marcos, quero ver o status das permissões do Health Connect nas Configurações e poder abrir o gerenciador para alterá-las para que eu tenha controle sobre quais dados o app acessa.

**Critérios de aceitação:**
- [ ] Exibe status atual de cada permissão: `READ_STEPS`, `READ_SLEEP`, `READ_HEART_RATE` (Concedida / Negada)
- [ ] Botão "Gerenciar Permissões" abre as Configurações do Health Connect (Intent correto)
- [ ] Status atualizado ao retornar das Configurações do sistema (`onResume`)
- [ ] Estado loading, content e error

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

### EP-07-04 — Exclusão de Dados Locais (Direito ao Esquecimento LGPD)

**Módulo(s):** [`:feature:settings`, `:core:database`, `:core:datastore`]
**Prioridade:** P0 Bloqueador
**Depende de:** EP-07-01, EP-03-02, EP-04-02, EP-05-02, EP-01-06
**Pré-requisito de agente:** Compliance Docs: PRE-03 (confirmação do escopo de dados a excluir)

Como Marcos, quero poder excluir todos os meus dados locais do app para que eu exerça o direito ao esquecimento previsto na LGPD.

**Critérios de aceitação:**
- [ ] Botão "Excluir meus dados" abre diálogo de confirmação com lista dos dados que serão excluídos
- [ ] Ao confirmar: todos os registros dos DAOs são deletados (`DailySnapshotDao`, `StepsDailyDao`, `SleepSessionDao`)
- [ ] DataStore limpo: `lgpdConsentGiven = false`, `onboardingCompleted = false`, timestamp zerado
- [ ] App navega para o Onboarding após exclusão
- [ ] Operação é atômica — se falhar, rollback total (não excluir parcialmente)
- [ ] Log de auditoria: registrar evento de exclusão sem PII (apenas timestamp e resultado: sucesso/falha)
- [ ] Estado loading durante a exclusão (não pode ser interrompida pelo usuário)
- [ ] Estado error: mensagem se a exclusão falhar com instrução para tentar novamente

**Definition of Done:**
- [ ] Implementação completa
- [ ] Testes unitários passando (≥85% cobertura domain/VM — testar sucesso, falha parcial, rollback)
- [ ] Testes de UI cobrindo os 4 estados (Loading, Empty, Content, Error)
- [ ] Security Reviewer sign-off
- [ ] CI verde

---

## Ordem de Execução Recomendada

> Esta é a ordem sequencial na qual o Android Engineer deve pegar as stories, respeitando dependências e o WIP limit de 1 feature ativa. Stories dentro de um mesmo número podem ser feitas em paralelo se o WIP limit permitir.

1. **EP-01-01** — Bootstrap do Projeto Multi-Module Gradle _(aguardar PRE-09)_
2. **EP-01-02** — GitHub Actions CI _(pode rodar em paralelo com EP-01-01 após PRE-09)_
3. **EP-01-03** — Módulo `:core:testing` com Fakes Base
4. **EP-01-04** — SQLCipher + Room _(aguardar PRE-08)_
5. **EP-01-05** — Health Connect: permissões e cliente base
6. **EP-01-06** — DataStore: preferências criptografadas
7. **EP-02-05** — Use Cases de Onboarding _(domínio, base para toda feature:onboarding)_
8. **EP-02-01** — Tela de Boas-vindas _(aguardar spec de designer)_
9. **EP-02-02** — Tela de Consentimento LGPD _(aguardar PRE-01, PRE-02, PRE-03)_
10. **EP-02-03** — Solicitação de Permissões Health Connect
11. **EP-02-04** — Navegação pós-onboarding e guarda de rota
12. **EP-03-01** — Use Cases do Dashboard Diário
13. **EP-03-02** — Repositório e DAO de Cache do Dashboard
14. **EP-03-03** — ViewModel do Dashboard Diário _(aguardar PRE-05)_
15. **EP-03-04** — Tela do Dashboard Diário _(aguardar PRE-05)_
16. **EP-03-05** — Navegação para features a partir do Dashboard
17. **EP-04-01** — Use Cases de Tendência Semanal de Passos
18. **EP-04-02** — DAO e cache de passos semanais
19. **EP-04-03** — ViewModel da Tendência Semanal de Passos _(aguardar PRE-06)_
20. **EP-04-04** — Tela de Tendência Semanal de Passos _(aguardar PRE-06)_
21. **EP-05-01** — Use Cases de Análise de Sono
22. **EP-05-02** — DAO e cache de sono
23. **EP-05-03** — ViewModel da Análise de Sono _(aguardar PRE-07)_
24. **EP-05-04** — Tela de Análise de Sono _(aguardar PRE-07)_
25. **EP-06-04** — Período do Resumo Semanal (cálculo de janela) _(pode antecipar após EP-04-01/EP-05-01)_
26. **EP-06-01** — Use Cases do Resumo Semanal _(HARD BLOCKER: aguardar PRE-04 aprovado)_
27. **EP-06-02** — ViewModel do Resumo Semanal
28. **EP-06-03** — Tela do Resumo Semanal
29. **EP-07-01** — Tela principal de Configurações _(aguardar PRE-03)_
30. **EP-07-03** — Gerenciamento de Permissões Health Connect nas Configurações
31. **EP-07-02** — Gerenciamento de Consentimento LGPD nas Configurações
32. **EP-07-04** — Exclusão de Dados Locais (Direito ao Esquecimento LGPD)

---

> **Regra de ouro:** Nenhuma story entra em desenvolvimento sem (1) pré-requisitos de agente entregues, (2) story anterior concluída conforme Definition of Done, e (3) CI verde. Bloqueadores sobem ao PM imediatamente — não espere o fim da sprint.
