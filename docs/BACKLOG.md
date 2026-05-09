# Health Insights - Backlog do MVP

Fonte: `docs/ROADMAP.md`. Este backlog e uma lista executavel derivada do roadmap; se houver conflito, o roadmap vence.

WIP recomendado: 1 story ativa por vez.

## Status Atual

Concluido ou parcialmente concluido:
- Estrutura Gradle multi-modulo.
- Convention plugins.
- Room + SQLCipher para perfil/consentimento.
- Health Connect wrapper inicial.
- Domain use cases de BMR, meta diaria e balanco calorico.
- Onboarding T1-T5 em implementacao existente.
- NavHost extraido de `MainActivity`.
- Tema centralizado em `:core:ui`.

Proxima frente: fechar Dashboard MVP e alinhar o fluxo `Welcome -> Profile -> Goal -> Consent -> Dashboard`.

## EP-00 - Alinhamento Documental

### EP-00-01 - Consolidar Fonte da Verdade

Prioridade: P0

Escopo:
- Criar/atualizar `docs/ROADMAP.md`.
- Espelhar `CLAUDE.md` e `AGENTS.md`.
- Remover docs historicos obsoletos.
- Reescrever backlog para o MVP calorico.

Aceite:
- `docs/ROADMAP.md` define produto, MVP, dados, retencao, onboarding e ordem de execucao.
- `CLAUDE.md` e `AGENTS.md` apontam para o roadmap.
- Nao ha documentos ativos prometendo passos/sono/FC no MVP.

## EP-01 - Fundacao Tecnica

### EP-01-01 - Build, Modulos e Qualidade

Status: feito, manter.

Aceite de manutencao:
- `gradlew.bat ktlintCheck` passa.
- `gradlew.bat testDebugUnitTest` passa.
- `gradlew.bat :app:assembleDebug` passa.

### EP-01-02 - Banco Criptografado

Status: feito, manter e ampliar conforme novas entidades.

Aceite:
- Dados sensiveis em Room + SQLCipher.
- Chave protegida via Android Keystore/EncryptedSharedPreferences.
- Teste JVM nao tenta executar SQLCipher nativo.
- Teste instrumentado cobre abertura com chave correta/incorreta.

### EP-01-03 - Health Connect MVP

Status: base feita, precisa completar leituras conforme Dashboard.

Aceite:
- Ler calorias gastas.
- Ler ingestao calorica quando disponivel.
- Ler peso mais recente.
- Ler treinos apenas se necessario para contexto.
- Retornar estados explicitos para indisponibilidade/erro.

## EP-02 - Onboarding MVP

Fluxo alvo: `Welcome -> Profile -> Goal -> Consent -> Dashboard`.

### EP-02-01 - Ajustar Persistencia do Onboarding

Prioridade: P0

Problema:
- Dados sensiveis de perfil/meta nao devem ser persistidos em DataStore plain.

Aceite:
- Perfil, objetivo, meta diaria e consentimentos sao salvos em Room + SQLCipher ao concluir o fluxo.
- `onboarding_complete` fica em DataStore plain.
- Se o app morrer antes da conclusao, o onboarding reinicia sem recuperar dados sensiveis intermediarios.
- Testes cobrem conclusao do fluxo e persistencia.

### EP-02-02 - Consentimento Granular

Prioridade: P0

Aceite:
- Usa `docs/legal/consent-copy-v1.1.md`.
- Toggles default OFF.
- Consentimentos registrados por tipo de dado com `policyVersion = consent-copy-v1.1`.
- Health Connect nao e lido antes do registro de consentimento.
- Permissoes parciais sao aceitas.
- Sem entrada manual no MVP; permissoes negadas geram estados vazios/indisponiveis no Dashboard.

### EP-02-03 - Navegacao Pos-Onboarding

Prioridade: P0

Aceite:
- Consent concluido navega para Dashboard.
- Back stack do onboarding e limpo.
- Dashboard e rota inicial quando `onboarding_complete = true`.
- Nao existe rota obrigatoria de First Insight separada.

## EP-03 - Dashboard MVP

### EP-03-01 - Contrato de Estado do Dashboard

Prioridade: P0

Modulo: `:feature:dashboard`

Aceite:
- `DashboardUiState` sealed interface com `Loading`, `Empty`, `Content`, `Error`.
- `Error` distingue Health Connect indisponivel, permissao ausente e erro generico.
- ViewModel nao contem logica de calculo pesada; delega para use cases.

### EP-03-02 - Conteudo Principal do Dashboard

Prioridade: P0

Aceite:
- Mostra meta diaria calculada.
- Mostra balanco calorico do dia quando ha gasto e ingestao disponiveis.
- Mostra estado especifico quando nao ha ingestao calorica no Health Connect.
- Mostra peso mais recente quando disponivel.
- Mostra copy clara quando peso/calorias nao existem ainda.
- Nao oferece lancamento manual no MVP.

### EP-03-03 - Integracao com Domain

Prioridade: P0

Aceite:
- Reusa `GetDailyBalanceUseCase`.
- Usa `CalculateBmrUseCase` e `CalculateDailyTargetUseCase` onde necessario.
- Nao acessa SDK Health Connect diretamente.
- Testes unitarios cobrem deficit, manutencao, superavit, sem ingestao e Health Connect indisponivel.

### EP-03-04 - UI Compose do Dashboard

Prioridade: P1

Aceite:
- Usa `HealthInsightsTheme` de `:core:ui`.
- Sem cores hardcoded em production UI.
- Estados Loading, Empty, Content e Error renderizados.
- Acessibilidade basica em hero number, cards e CTAs.
- Back no Dashboard fecha app, nao volta ao onboarding.

## EP-04 - Settings e LGPD Minimo

### EP-04-01 - Tela de Privacidade

Status: feito para alpha local; manter antes de alpha publica.

Prioridade: P0 antes de alpha publica; P1 para alpha local.

Aceite:
- Exibe politica de privacidade.
- Exibe consentimentos atuais.
- Permite abrir configuracoes/permissoes Health Connect.

### EP-04-02 - Exportar Dados

Prioridade: P0 antes de release.

Aceite:
- Exporta JSON local com dados armazenados pelo app.
- Sem transmissao externa.
- Teste cobre formato e ausencia de campos nao coletados.

### EP-04-03 - Apagar Dados Locais

Prioridade: P0 antes de release.

Aceite:
- Apaga perfil, meta, consentimentos e caches locais.
- Reseta `onboarding_complete`.
- Nao tenta apagar dados originais do Health Connect; apenas deixa de le-los.
- Fluxo tem confirmacao clara.

## EP-05 - Pos-MVP

Nao implementar no MVP salvo nova decisao em `docs/ROADMAP.md`.

Futuro:
- Passos.
- Sono.
- Frequencia cardiaca.
- Resumo semanal multi-sinal.
- Analises avancadas de treino.
- Monetizacao.
- Retencao configuravel.

## Ordem Recomendada

1. EP-00-01.
2. EP-02-01.
3. EP-02-02.
4. EP-02-03.
5. EP-03-01.
6. EP-03-02.
7. EP-03-03.
8. EP-03-04.
9. EP-04-01.
10. EP-04-02.
11. EP-04-03.

## Definition of Done Padrao

Para qualquer story de codigo:
- PRD, SPEC ou Mini-spec existe conforme o tamanho da mudanca.
- Reviews condicionais foram feitas quando aplicaveis.
- Design externo foi solicitado/recebido para tela nova ou mudanca visual grande.
- Dev plan existe para feature media/grande.
- Implementacao completa.
- Testes proporcionais ao risco.
- `gradlew.bat ktlintCheck` passa.
- `gradlew.bat testDebugUnitTest` passa.
- `gradlew.bat :app:assembleDebug` passa.
- Sem dados de saude em logs.
- Sem SDK novo sem revisao explicita.

## Gates por Tipo de Mudanca

Use estes gates antes de mover uma story para implementacao.

| Tipo de mudanca | Gate obrigatorio |
|---|---|
| Produto/escopo/fluxo | PRD + SPEC + review produto/UX |
| Tela nova ou mudanca visual grande | SPEC + prompt para design externo + handoff visual |
| Health Connect/permissoes | SPEC + review seguranca/privacidade |
| Consentimento/politica/dados sensiveis | SPEC + review seguranca/privacidade |
| Room/SQLCipher/export/delete | SPEC + review seguranca/privacidade |
| Gradle/CI/deps/modulos/R8 | Dev plan + review infra/build |
| Bug pequeno sem impacto de escopo | Mini-spec + validacao |

## Documentos por Feature

Para feature media/grande, crie:

- `docs/product/prd/<feature>.md`
- `docs/specs/<feature>-spec.md`
- `docs/design/prompts/<feature>-design-prompt.md` quando houver UI nova
- `docs/design/handoffs/<feature>-design-handoff.md` quando houver retorno visual
- `docs/dev-plans/<feature>-plan.md`
- `docs/validation/<feature>-validation.md`

O nome da feature deve ser curto e estavel, por exemplo `dashboard-mvp`, `settings-privacy` ou `onboarding-consent`.
