# Health Insights - Roadmap e Fonte da Verdade

Este documento e a fonte principal de produto, escopo e ordem de execucao do Health Insights. `CLAUDE.md` e `AGENTS.md` devem apontar para este arquivo e podem espelhar apenas instrucoes operacionais para agentes.

Ultima decisao de produto: 2026-05-08.

## Produto

Health Insights e um app Android, 100% on-device, que le dados do Android Health Connect e mostra se o usuario esta em deficit, manutencao ou superavit calorico, junto com a evolucao do peso.

O MVP nao e um dashboard geral de saude. Passos, sono, frequencia cardiaca e analises avancadas de treinos fazem parte da visao final do app, mas ficam fora do MVP.

## Persona Inicial

Marcos, 32 anos, brasileiro, usuario de Android/Galaxy Watch, treina de 3 a 5 vezes por semana, ja acompanha alimentacao ou calorias de alguma forma e quer entender automaticamente se esta caminhando para perder, manter ou ganhar peso.

## Escopo do MVP

O MVP valida uma proposta central: o app consegue transformar dados locais de calorias e peso em uma resposta diaria simples e util.

Inclui:
- Onboarding com explicacao de privacidade, perfil corporal, objetivo e consentimento LGPD.
- Leitura Health Connect de calorias gastas, ingestao calorica, peso e treinos quando disponiveis.
- Registro criptografado de perfil, meta diaria, consentimentos e dados sensiveis derivados em Room + SQLCipher.
- Dashboard inicial com meta diaria, balanco calorico do dia, peso mais recente e estados de falta de dados/permissao.
- Configuracoes minimas de privacidade: ver consentimentos, revogar acesso, exportar dados e apagar dados locais.
- Politica de privacidade empacotada/acessivel no app.

Fora do MVP:
- Lancamento manual de refeicoes ou peso.
- Passos, sono e frequencia cardiaca.
- Insights semanais gerais de saude.
- Graficos avancados de treino.
- Conta, login, cloud sync, backend ou backup em nuvem.
- Analytics, ads, crash reporter hospedado ou qualquer SDK de telemetria.
- Paywall ou monetizacao ativa.

## Visao Pos-MVP

Depois que o MVP calorico estiver estavel, o app final pode expandir para:
- Passos e tendencias semanais.
- Sono e consistencia de descanso.
- Frequencia cardiaca em repouso e variacoes.
- Treinos com contexto de frequencia e carga.
- Resumo semanal combinando varios sinais.
- Monetizacao com desbloqueio unico, somente apos validacao de uso.

Esses itens devem ser documentados como futuro, nao como promessa do MVP.

## Dados e Permissoes do MVP

Dados Health Connect:
- Calorias gastas: `TotalCaloriesBurnedRecord` e `ActiveCaloriesBurnedRecord`.
- Ingestao calorica: `NutritionRecord`, somente total calorico disponivel no Health Connect.
- Peso: `WeightRecord`.
- Treinos: `ExerciseSessionRecord`, apenas tipo e duracao.

Dados locais derivados:
- Perfil corporal necessario para TMB: peso informado, altura, idade e sexo biologico.
- Objetivo: perder, manter ou ganhar peso.
- Meta calorica diaria.
- Registros de consentimento por tipo de dado.

O app nao coleta no MVP: passos, sono, frequencia cardiaca, GPS, rota, localizacao, macros detalhados, nomes de alimentos, fotos, identificadores pessoais, conta, e-mail ou dados de dispositivo.

## Retencao de Dados

Decisao atual: 12 meses por padrao.

Racional:
- Peso e balanco calorico ganham valor com horizonte maior que 90 dias.
- O app nao transmite dados; o risco principal e local e mitigado por SQLCipher + Android Keystore.
- A policy vigente ja esta alinhada com 12 meses.

Regra:
- MVP usa 12 meses fixos como padrao documentado.
- Configuracao para alterar retencao pode entrar depois, em Settings avancado.
- Qualquer mudanca de retencao padrao exige atualizacao de policy e avaliacao CISO.

## Onboarding

Fluxo do MVP:

`Welcome -> Profile -> Goal -> Consent -> Dashboard`

Decisao atual: nao ha T6 separado de First Insight. O payoff aparece diretamente no Dashboard, com a meta diaria e o balanco do dia.

Persistencia:
- Dados sensiveis de perfil e meta nao devem ser salvos em DataStore plain.
- Durante o onboarding, valores podem viver em estado de UI/session (`rememberSaveable` ou ViewModel).
- Ao concluir consentimento/entrar no Dashboard, perfil, objetivo, meta e consentimentos sao persistidos em Room + SQLCipher.
- `onboarding_complete` pode ficar em DataStore plain porque e uma flag nao sensivel.
- Se o processo morrer antes da conclusao, o usuario pode reiniciar o onboarding. Retomar etapa sensivel fica fora do MVP.

Modo sem dados:
- Se o usuario nao conceder permissoes ou Health Connect nao tiver dados suficientes, o Dashboard deve mostrar estados vazios/indisponiveis e explicar o que falta.
- Nao ha entrada manual no MVP.

## Arquitetura

Stack:
- Kotlin.
- Jetpack Compose.
- MVVM + Clean Architecture.
- Coroutines + Flow.
- Hilt.
- Room + SQLCipher.
- Android Health Connect.
- Gradle KTS + Version Catalog.

Modulos atuais:
- `:app`: composicao do app, NavHost, DI root, Application.
- `:core:domain`: modelos, interfaces de repositorio, use cases.
- `:core:database`: Room + SQLCipher, DAOs, entidades e repositorios locais.
- `:core:ui`: tema, tokens e componentes Compose compartilhados.
- `:core:common`: utilitarios compartilhados.
- `:core:data` e `:core:network`: reservados, vazios por enquanto.
- `:feature:health-connect`: wrapper Health Connect.
- `:feature:onboarding`: telas de onboarding.
- `:feature:dashboard`: dashboard do MVP.
- `:feature:settings`: privacidade/configuracoes.
- `:feature:insights`, `:feature:sleep`, `:feature:workouts`: reservados para pos-MVP.

Regras de camada:
- UI nunca toca tipos do SDK Health Connect.
- Health Connect types ficam isolados em `:feature:health-connect` e sao mapeados para dominio.
- Dados sensiveis ou derivados de saude ficam em Room + SQLCipher.
- Flags nao sensiveis podem usar DataStore plain.
- Sem logs com dados de saude, calorias, peso, idade, altura, FC, passos ou sono.
- Telas usam UDF: eventos sobem, estado desce.
- Telas relevantes usam sealed `UiState`: `Loading`, `Empty`, `Content`, `Error`.

## Design

Fonte visual: `docs/design/visual-system-v1.md`.

Regras:
- Usar `core/ui/src/main/kotlin/com/healthinsights/core/ui/theme/Theme.kt`.
- Features devem usar `MaterialTheme.colorScheme.*` e tokens compartilhados.
- Evitar cores hardcoded em features, salvo previews/testes isolados.
- O numero vem primeiro: telas devem liderar com dado/estado, nao com ornamentacao.
- Sem gradientes saturados, circulos decorativos, emojis ou icones genericos.
- Microcopy de privacidade deve aparecer antes de CTAs criticos.

## Legal e Privacidade

Fonte legal atual:
- `docs/legal/consent-copy-v1.1.md`.
- `docs/legal/privacy-policy-v1.md` (versao interna v1.2).

Regras:
- Consentimento granular por tipo de dado.
- Toggles default OFF.
- O app deve funcionar com permissoes parciais ou negadas, exibindo estados apropriados.
- Health data nao sai do aparelho.
- Nenhum SDK de telemetria no MVP.
- Mudanca de dado coletado, finalidade ou retencao exige nova revisao de policy/consentimento.

## Ordem de Execucao

1. Alinhamento documental para este roadmap.
2. Fechar onboarding atual com Dashboard como destino direto.
3. Implementar Dashboard MVP de balanco calorico/peso.
4. Implementar Settings minimas de privacidade e dados.
5. Rodar hardening: testes, lint, seguranca, fluxos de permissao.
6. Preparar alpha interna.

## Fluxo Oficial de Desenvolvimento

Features medias/grandes devem seguir este fluxo:

`PRD -> SPEC -> Revisoes condicionais -> Design externo -> Dev Plan -> Implementacao -> Validacao -> Review final`

Tasks pequenas podem usar fluxo leve:

`Mini-spec -> Implementacao -> Validacao`

Use fluxo leve apenas para mudancas pequenas e localizadas, como ajuste de texto, doc, lint ou bug simples que nao altera produto, dados, seguranca, permissoes, arquitetura ou UI relevante.

### 1. PRD

Objetivo: explicar o problema, usuario, valor, escopo e nao-escopo.

Arquivo sugerido:
- `docs/product/prd/<feature>.md`

Conteudo minimo:
- Objetivo.
- Usuario/persona.
- Problema.
- Resultado esperado.
- Escopo.
- Fora de escopo.
- Metricas ou sinais de sucesso.
- Riscos de produto.

### 2. SPEC

Objetivo: transformar o PRD em comportamento implementavel.

Arquivo sugerido:
- `docs/specs/<feature>-spec.md`

Conteudo minimo:
- Fluxos.
- Estados `Loading`, `Empty`, `Content`, `Error`, quando aplicavel.
- Edge cases e caminhos fora do feliz.
- Dados lidos/gravados.
- Permissoes.
- Regras de privacidade.
- Requisitos de acessibilidade.
- Testes esperados.

### 3. Revisoes Condicionais

Nem toda task precisa de todas as revisoes. A regra e acionar a revisao quando o tipo de mudanca pedir.

Review de seguranca/privacidade e obrigatoria quando tocar:
- Health Connect.
- Consentimento.
- Room/SQLCipher.
- Exportacao ou exclusao de dados.
- Logs.
- Permissoes.
- Dependencias novas.
- Qualquer dado de saude ou dado derivado.

Review de infra/build e obrigatoria quando tocar:
- Gradle.
- CI/CD.
- Signing.
- R8/ProGuard.
- Estrutura de modulos.
- Dependencias.
- Performance de build.

Review de produto/UX e obrigatoria quando tocar:
- Onboarding.
- Dashboard.
- Copy de consentimento.
- Estados de erro/vazio.
- Escopo de MVP.
- Decisao de fluxo.

Review visual/design e obrigatoria para:
- Tela nova.
- Mudanca visual grande.
- Novo componente compartilhado.
- Mudanca em hierarquia, layout ou design system.

### 4. Design Externo

Design de telas sera feito por ferramenta externa, normalmente Claude Design, Figma ou outra ferramenta escolhida pelo founder.

Codex/Claude Code nao devem tentar ser a ferramenta criativa principal de design visual. Eles devem preparar o prompt de design e validar o retorno contra produto, specs, acessibilidade e restricoes tecnicas.

Arquivo sugerido para prompt:
- `docs/design/prompts/<feature>-design-prompt.md`

Arquivo sugerido para retorno/handoff:
- `docs/design/handoffs/<feature>-design-handoff.md`

O prompt de design deve incluir:
- Objetivo da tela.
- Publico/persona.
- Estados que a tela precisa cobrir.
- Dados reais ou placeholders permitidos.
- Regras do `docs/design/visual-system-v1.md`.
- Restrições de privacidade.
- Formato esperado de retorno.

O retorno ideal da ferramenta de design deve incluir:
- Estrutura de tela.
- Estados.
- Tokens/estilos.
- Medidas relevantes.
- Copy proposta.
- Observacoes de interacao.
- Assets, se houver.

### 5. Dev Plan

Objetivo: quebrar a implementacao em passos pequenos.

Arquivo sugerido:
- `docs/dev-plans/<feature>-plan.md`

Conteudo minimo:
- Arquivos/modulos provaveis.
- Ordem de implementacao.
- Dependencias.
- Testes.
- Riscos.
- Plano de validacao.

### 6. Implementacao

Um agente desenvolvedor deve implementar por vez para evitar conflito. O outro pode revisar.

Preferencia:
- Codex ou Claude Code para implementacao com leitura do repositorio.
- Ferramentas externas para design visual.
- Gemini/Hermes/ChatGPT podem revisar PRD/SPEC quando fizer sentido.

### 7. Validacao

Arquivo sugerido:
- `docs/validation/<feature>-validation.md`

Checks esperados:
- `gradlew.bat ktlintCheck`
- `gradlew.bat testDebugUnitTest`
- `gradlew.bat :app:assembleDebug`
- Testes adicionais especificos da feature.
- Revisao contra PRD/SPEC.
- Revisao de seguranca/privacidade quando aplicavel.

## Reviews Periodicas

Seguranca/privacidade:
- Antes de alpha interna.
- Antes de release.
- Sempre que mudar dados, permissoes, consentimento, storage, exportacao, exclusao ou SDKs.

Infra/build:
- Quando mudar Gradle, CI, signing, dependencias, modulos ou R8/ProGuard.
- Revisao periodica mensal se o projeto estiver ativo.

Produto/documentacao:
- Ao fechar cada epico.
- Sempre que uma decisao alterar escopo, roadmap, dados coletados, retencao ou fluxo principal.

Design:
- A cada tela nova.
- Antes de implementar mudanca visual grande.

## Performance e Escala

O MVP nao precisa de teste de carga tradicional de backend porque nao ha backend, fila, servidor ou multiplas requisicoes simultaneas de usuarios contra infraestrutura propria.

O equivalente de escala para este app e performance local por usuario:
- Cold start ate primeiro conteudo.
- Tempo de leitura Health Connect.
- Queries Room/SQLCipher com historico de 30, 90 e 365 dias.
- Uso de memoria em telas com graficos/listas.
- Fluidez Compose.
- Consumo de bateria.
- Concorrencia local entre coroutines, banco e leituras Health Connect.
- Comportamento em aparelho medio, como Galaxy A54.

Se o app ganhar backend, sync, conta, notificacoes remotas ou qualquer infraestrutura compartilhada, testes de carga passam a ser obrigatorios para essa nova camada.

## Proxima Sprint

Objetivo: finalizar a transicao Onboarding -> Dashboard MVP.

Escopo recomendado:
- Atualizar docs e agentes para este roadmap.
- Ajustar spec de onboarding para remover T6 separado.
- Implementar `:feature:dashboard` inicial com sealed `DashboardUiState`.
- Usar os use cases existentes de BMR/meta/balanco calorico.
- Exibir estados: loading, sem ingestao calorica, Health Connect indisponivel, conteudo.
- Garantir que o Dashboard nao depende de entrada manual.
- Testes unitarios de ViewModel/use case e UI basica dos estados.

## Documentos Derivados Permitidos

Estes documentos podem existir, mas nao devem contradizer este roadmap:
- `docs/BACKLOG.md`: tarefas executaveis derivadas do roadmap.
- `docs/specs/onboarding-spec-v1.0.md`: spec detalhado do onboarding atual.
- `docs/legal/consent-copy-v1.1.md`: copy legal aprovada.
- `docs/legal/privacy-policy-v1.md`: politica de privacidade vigente.
- `docs/design/visual-system-v1.md`: sistema visual.
- `docs/security/ESTRATEGIA_SQLCIPHER_KEYSTORE.md`: referencia tecnica de seguranca.

Documentos historicos que contradizem o roadmap devem ser apagados ou substituidos por ponteiro para este arquivo.
