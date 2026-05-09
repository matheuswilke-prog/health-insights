# Dev Plan - dashboard-mvp

Fonte de produto: `docs/product/prd/dashboard-mvp.md`.
Fonte de comportamento: `docs/specs/dashboard-mvp-spec.md`.
Fonte visual: `docs/design/handoffs/dashboard-mvp-design-handoff.md`.
Fonte geral: `docs/ROADMAP.md`.

Status: pronto para implementacao.

## Objetivo

Implementar o Dashboard MVP como primeira tela de valor apos o onboarding.

A tela deve mostrar:

- meta calorica diaria;
- saldo calorico do dia quando calculavel;
- ingestao calorica do dia;
- gasto estimado do dia;
- peso mais recente;
- estados de permissao parcial, Health Connect indisponivel, sem ingestao, sem peso, erro, loading e estado local invalido.

## Escopo

Inclui:

- Substituir o placeholder atual do Dashboard.
- Usar regra `BMR + ActiveCaloriesBurned`.
- Atualizar limiar de manutencao para `-250 kcal` a `+250 kcal`, inclusive.
- Nao usar `TotalCaloriesBurned` no primeiro corte do calculo.
- Nao calcular saldo quando ingestao ou calorias ativas nao forem confiaveis.
- Mostrar peso mais recente sem tendencia.
- Recarregar dados ao abrir a tela e ao retornar ao foreground com cache curto e single-flight.
- Exibir retry com estado ocupado.
- Redirecionar para onboarding/configuracao inicial quando estado local estiver invalido.
- Respeitar consentimentos e permissoes efetivas.

Fora do escopo:

- Entrada manual de refeicao.
- Entrada manual de peso.
- Tendencia/grafico de peso.
- Passos, sono, frequencia cardiaca.
- Treinos no Dashboard.
- Backend, login, analytics, telemetria.
- Settings completas de privacidade.
- Refatorar onboarding alem do necessario para navegar ao Dashboard real.

## Estado Atual do Codigo

Observacoes relevantes:

- `app/src/main/java/com/healthinsights/app/AppNavHost.kt` ainda usa `DashboardPlaceholder()`.
- `feature/dashboard/src/main` existe, mas ainda nao contem uma tela Compose real.
- `core/domain` ja tem:
  - `GetDailyBalanceUseCase`;
  - `DailyCaloricBalance`;
  - `BalanceStatus`;
  - `HealthConnectRepository`;
  - `HealthDataReadResult`.
- `feature/health-connect` ja implementa leituras de:
  - calorias ativas;
  - nutricao;
  - peso mais recente.
- `DailyCaloricBalance.MAINTAIN_THRESHOLD_KCAL` ainda esta em `100`, mas a SPEC exige `250`.
- Comentarios do dominio ainda citam `+/-100 kcal`; devem ser atualizados junto com o codigo.
- O retorno de peso atual e apenas `Float?`; a SPEC pede data da medicao quando disponivel. Primeiro passo pode mostrar peso sem data ou ampliar o contrato para retornar valor + timestamp.

## Modulos e Arquivos Provaveis

App:

- `app/src/main/java/com/healthinsights/app/AppNavHost.kt`
- `app/src/main/java/com/healthinsights/app/MainViewModel.kt`

Dashboard:

- `feature/dashboard/build.gradle.kts`
- `feature/dashboard/src/main/kotlin/com/healthinsights/feature/dashboard/DashboardScreen.kt`
- `feature/dashboard/src/main/kotlin/com/healthinsights/feature/dashboard/DashboardViewModel.kt`
- `feature/dashboard/src/main/kotlin/com/healthinsights/feature/dashboard/DashboardUiState.kt`
- `feature/dashboard/src/main/kotlin/com/healthinsights/feature/dashboard/DashboardUiModel.kt`
- `feature/dashboard/src/main/kotlin/com/healthinsights/feature/dashboard/DashboardFormatter.kt`
- `feature/dashboard/src/test/kotlin/com/healthinsights/feature/dashboard/DashboardViewModelTest.kt`
- `feature/dashboard/src/test/kotlin/com/healthinsights/feature/dashboard/DashboardFormatterTest.kt`

Domain:

- `core/domain/src/main/kotlin/com/healthinsights/core/domain/model/DailyCaloricBalance.kt`
- `core/domain/src/main/kotlin/com/healthinsights/core/domain/usecase/GetDailyBalanceUseCase.kt`
- `core/domain/src/main/kotlin/com/healthinsights/core/domain/repository/HealthConnectRepository.kt`
- `core/domain/src/test/kotlin/com/healthinsights/core/domain/usecase/GetDailyBalanceUseCaseTest.kt`

Health Connect:

- `feature/health-connect/src/main/kotlin/com/healthinsights/feature/healthconnect/repository/HealthConnectRepositoryImpl.kt`
- `feature/health-connect/src/test/kotlin/com/healthinsights/feature/healthconnect/HealthConnectRepositoryImplTest.kt`

Core UI:

- `core/ui/src/main/kotlin/com/healthinsights/core/ui/theme/Theme.kt`
- criar componentes compartilhados apenas se houver reuso claro. Para primeiro corte, preferir componentes locais do Dashboard.

## Dependencias

Provaveis dependencias de modulo:

- `:feature:dashboard` depende de `:core:domain` e `:core:ui`.
- `:app` depende de `:feature:dashboard` para chamar `DashboardScreen`.

Evitar:

- adicionar bibliotecas novas;
- mover tipos do SDK Health Connect para UI;
- criar dependencia direta de `:feature:dashboard` para `:feature:health-connect`.

## Ordem de Implementacao

### 1. Corrigir regras de dominio

Objetivo: alinhar calculo com SPEC antes da UI.

Tarefas:

- Alterar `MAINTAIN_THRESHOLD_KCAL` de `100` para `250`.
- Atualizar comentarios/documentacao do dominio.
- Garantir que `TotalCaloriesBurned` nao participa da formula.
- Garantir que ingestao `<= 0` vira `NoIntakeData`.
- Garantir que, sem leitura confiavel de calorias ativas, o status nao vira deficit/manutencao/superavit.
- Decidir no codigo se erro de ingestao e erro de calorias ativas caem em `HealthConnectUnavailable` ou estados mais especificos de UI.

Testes:

- `GetDailyBalanceUseCaseTest` cobrindo:
  - deficit abaixo de `-250`;
  - manutencao em `-250`, `0`, `+250`;
  - superavit acima de `+250`;
  - ingestao zero;
  - ingestao negativa, se o tipo permitir;
  - falha/indisponibilidade Health Connect.

### 2. Ajustar contrato de peso

Objetivo: suportar bloco `Peso mais recente`.

Opcao conservadora para primeiro corte:

- Manter `getLatestWeightKg(): Float?`.
- UI mostra peso quando houver valor.
- Data aparece apenas se o contrato for ampliado.

Opcao melhor, se o impacto for pequeno:

- Criar modelo de dominio `LatestWeight(valueKg: Float, measuredAt: Instant?)`.
- Trocar contrato para `getLatestWeight(): LatestWeight?`.
- Atualizar implementacao Health Connect para mapear `WeightRecord.time`.
- Manter teste unitario do mapper/repositorio.

Decisao recomendada:

- Implementar `LatestWeight` agora, porque a SPEC e o design preveem data da medicao e o `WeightRecord` ja possui timestamp.

### 3. Criar modelos de UI do Dashboard

Objetivo: separar decisao de estado da composicao visual.

Criar:

- `DashboardUiState`
- `DashboardUiModel`
- modelos internos para:
  - meta;
  - saldo;
  - ingestao;
  - gasto;
  - peso;
  - banner global;
  - retry/loading local.

Estados minimos:

- `Loading`
- `Content`
- `NoIntake`
- `PartialPermission`
- `HealthConnectUnavailable`
- `NoWeight`
- `Error`
- `LocalStateInvalid`

Observacao:

- Pode haver um estado `Content` com blocos internos parciais em vez de sealed states separados para cada combinacao. O importante e que os blocos sejam independentes.

### 4. Implementar `DashboardViewModel`

Responsabilidades:

- Carregar perfil/meta via use cases/repositorios de dominio.
- Carregar balanco do dia via `GetDailyBalanceUseCase`.
- Carregar peso mais recente.
- Consultar disponibilidade/permissoes Health Connect via repositorio.
- Mapear dados para `DashboardUiState`.
- Implementar retry.
- Implementar cache em memoria com validade de 1 a 3 minutos.
- Implementar single-flight para evitar leituras paralelas.
- Ignorar cache quando mudar a data local.
- Expor evento para estado local invalido.

Eventos sugeridos:

- `Load`
- `Retry`
- `OnForeground`
- `OnOpenPermissions`
- `OnOpenHealthConnect`
- `OnReconfigure`
- `OnSettingsClick`

Saidas/eventos one-shot:

- abrir configuracoes/permissoes Health Connect;
- navegar para settings;
- invalidar onboarding e navegar para onboarding/configuracao inicial.

### 5. Implementar UI Compose local

Objetivo: converter o handoff React para Compose sem trazer dependencia de HTML/React.

Componentes locais:

- `DashboardScreen`
- `DashboardTopBar`
- `BalanceHeroCard`
- `UnavailableBalanceHeroCard`
- `DailyGoalCard`
- `LatestWeightCard`
- `IntakeCard`
- `EstimatedExpenditureCard`
- `GlobalDataBanner`
- `InlineDataNote`
- `DashboardSkeleton`
- `PrivacyFootnote`

Regras de implementacao:

- Usar `MaterialTheme.colorScheme` e tokens de `:core:ui`.
- Evitar cores hardcoded em feature quando houver token equivalente.
- Texto final em portugues com acentos corretos.
- Data do topo dinamica usando data local atual.
- Nao exibir `BMR` como texto para usuario final; usar `metabolismo basal estimado`.
- Trocar `Health Connect off-line` por `Health Connect indisponivel` ou copy equivalente.
- Saldo nao aparece sem ingestao ou sem calorias ativas confiaveis.
- Status deficit/manutencao/superavit deve ter texto e cor.
- Touch targets de CTA com minimo adequado.

### 6. Integrar no NavHost

Tarefas:

- Remover `DashboardPlaceholder()`.
- Expor `DashboardScreen` no modulo `:feature:dashboard`.
- Conectar `ROUTE_DASHBOARD` ao Dashboard real.
- Tratar evento `LocalStateInvalid`:
  - limpar `onboarding_complete`;
  - navegar para onboarding/configuracao inicial;
  - limpar back stack se necessario.
- Tratar clique de Settings:
  - se Settings ainda nao estiver pronta, pode deixar callback placeholder ou navegar para rota existente quando houver.

### 7. Fluxo de foreground

Objetivo: cumprir a SPEC sem polling continuo.

Tarefas:

- Observar ciclo de vida na tela ou no app usando API Compose adequada.
- Chamar `OnForeground` ao voltar para foreground.
- ViewModel decide se recarrega com base em:
  - cache expirado;
  - leitura em andamento;
  - data local mudou;
  - retorno de permissao/configuracao Health Connect.

### 8. Testes e previews

Unitarios:

- dominio: formula, threshold e estados.
- ViewModel: estados principais, retry, cache, single-flight, troca de dia, LocalStateInvalid.
- formatadores: kcal, peso, data, sinal positivo/negativo.

Compose/UI:

- previews ou screenshots manuais para os 8 estados do handoff.
- teste de UI se ja houver padrao no projeto; se nao houver, manter previews + validacao manual no primeiro corte.

Health Connect:

- atualizar testes existentes se o contrato de peso mudar.
- garantir que nutrition com energia ausente/zero vira sem ingestao.

## Riscos e Mitigacoes

Risco: UI depender de Health Connect SDK.

Mitigacao:
- manter SDK isolado em `:feature:health-connect`; UI consome dominio.

Risco: saldo enganoso com dado parcial.

Mitigacao:
- saldo so aparece quando ingestao e calorias ativas forem confiaveis.

Risco: estado local invalido prender usuario.

Mitigacao:
- ViewModel emite evento para invalidar onboarding e refazer configuracao.

Risco: retry/foreground gerar leituras paralelas.

Mitigacao:
- cache curto + single-flight no ViewModel ou camada de carregamento.

Risco: divergencia visual do handoff.

Mitigacao:
- implementar componentes seguindo `dashboard-mvp.jsx` e validar os 8 estados.

Risco: acentos/copy quebrados por encoding.

Mitigacao:
- escrever strings finais em UTF-8 nos arquivos Kotlin; revisar visualmente no app.

## Plano de Validacao

Comandos esperados:

- `C:\Dev\Claude-Code\Health-insights\gradlew.bat ktlintCheck`
- `C:\Dev\Claude-Code\Health-insights\gradlew.bat testDebugUnitTest`
- `C:\Dev\Claude-Code\Health-insights\gradlew.bat :app:assembleDebug`

Validacao manual:

- Onboarding concluido abre Dashboard real.
- Reabrir app com `onboarding_complete = true` abre Dashboard.
- Estado completo renderiza meta, saldo, ingestao, gasto e peso.
- Sem ingestao nao mostra saldo.
- Permissao parcial mostra blocos disponiveis.
- Health Connect indisponivel preserva meta local.
- Sem peso mostra vazio normal.
- Erro permite retry com estado ocupado.
- Estado local invalido retorna para configuracao inicial.
- Data do topo acompanha a data local.
- Textos com acentos aparecem corretamente.

Review obrigatoria antes de fechar:

- Produto/UX: conferir estados e copy.
- Seguranca/privacidade: Health Connect, consentimentos, Room/SQLCipher, logs e permissoes.
- Visual/design: comparar com handoff aprovado.

## Definicao de Pronto

Dashboard MVP esta pronto quando:

- placeholder foi removido;
- tela real cobre os 8 estados do handoff;
- calculo usa threshold de 250 kcal;
- saldo nao aparece com dados parciais;
- peso mais recente aparece sem tendencia;
- retry e foreground seguem SPEC;
- nao ha logs com health data;
- testes unitarios principais passam;
- `ktlintCheck`, `testDebugUnitTest` e `:app:assembleDebug` passam;
- validacao manual foi registrada em `docs/validation/dashboard-mvp-validation.md`.
