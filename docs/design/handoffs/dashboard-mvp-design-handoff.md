# Design Handoff - dashboard-mvp

Fonte do prompt: `docs/design/prompts/dashboard-mvp-design-prompt.md`.
Fonte de produto: `docs/product/prd/dashboard-mvp.md`.
Fonte de comportamento: `docs/specs/dashboard-mvp-spec.md`.
Fonte visual: `docs/design/visual-system-v1.md`.

Data de recebimento: 2026-05-08.

## Arquivos Recebidos

Arquivos fornecidos pelo design externo:

- `docs/design/handoffs/dashboard-mvp/dashboard-mvp.jsx`
- `docs/design/handoffs/dashboard-mvp/App.html`
- `docs/design/handoffs/dashboard-mvp/tokens.css`
- `docs/design/handoffs/dashboard-mvp/design-canvas.jsx`
- `docs/design/handoffs/dashboard-mvp/android-frame.jsx`
- `docs/design/handoffs/dashboard-mvp/screens.jsx`
- `docs/design/handoffs/dashboard-mvp/app-screens.jsx`

## Escopo do Handoff

O arquivo principal para implementacao do Dashboard MVP e `dashboard-mvp.jsx`.

Os demais arquivos compoem o canvas/preview visual:

- `App.html`: organiza os artboards e carrega React/Babel via CDN.
- `tokens.css`: define tokens visuais usados pelo mockup.
- `design-canvas.jsx`: wrapper do canvas de design.
- `android-frame.jsx`: moldura visual Android.
- `screens.jsx` e `app-screens.jsx`: telas auxiliares de onboarding/settings usadas no canvas.

Para implementacao Android/Compose, usar `dashboard-mvp.jsx` como referencia visual principal e `tokens.css` como referencia de tokens.

## Estados Entregues

O handoff cobre os 8 estados obrigatorios da SPEC:

1. `complete`: Dashboard completo.
2. `no-intake`: sem ingestao calorica registrada hoje.
3. `partial-perm`: permissao parcial, com peso disponivel e calorias negadas/ausentes.
4. `hc-unavailable`: Health Connect indisponivel.
5. `no-weight`: sem peso registrado.
6. `error`: erro inesperado com CTA `Tentar novamente`.
7. `loading`: skeleton leve.
8. `local-invalid`: estado local invalido com CTA `Refazer configuracao`.

## Estrutura Visual Aprovada

Hierarquia principal:

1. Top bar com data, titulo `Hoje` e acesso a configuracoes.
2. Hero card de saldo do dia.
3. Linha com `Meta diaria` e `Peso mais recente`.
4. Card de ingestao calorica.
5. Card de gasto calorico.
6. Microcopy de privacidade no rodape.

Racional aprovado:

- O numero principal lidera a tela.
- Estados parciais preservam blocos funcionais.
- A meta local continua visivel quando Health Connect esta indisponivel.
- A tela evita graficos avancados e foco ornamental.
- O design nao adiciona entrada manual de refeicao ou peso.

## Tokens e Estilo

Tokens recebidos em `tokens.css` estao alinhados ao sistema visual:

- Background: `#FAFAF7`.
- Surface elevada: `#FFFFFF`.
- Texto primario: `#0E1116`.
- Texto secundario: `#3A3F47`.
- Texto terciario: `#6B7079`.
- Deficit: sage/verde.
- Superavit: coral.
- Manutencao: azul sutil.
- Cards com radius grande.
- Botao primario escuro com texto branco.
- Numeros com fonte tabular.

Implementacao Compose deve mapear esses valores para `MaterialTheme` e tokens de `:core:ui`, evitando cores hardcoded em features quando houver token equivalente.

## Review Contra SPEC

Status geral: aprovado para Dev Plan com ajustes menores de implementacao.

### Aprovado

- Cobre todos os estados obrigatorios da SPEC.
- Nao calcula saldo quando falta ingestao.
- Nao calcula saldo quando calorias/permissao estao ausentes.
- Peso mostra apenas valor mais recente e data.
- Nao ha tendencia/grafico de peso.
- Health Connect indisponivel preserva a meta local.
- Estado local invalido tem CTA para refazer configuracao.
- BMR isolado aparece como metabolismo basal estimado, nao como gasto total.
- Estados semanticos usam texto e cor.
- A tela segue a direcao visual calma, utilitaria e data-first.

### Ajustes Necessarios na Implementacao

1. Copy final deve usar acentos corretos em portugues.
   - Os arquivos recebidos aparecem com mojibake em alguns trechos quando lidos no terminal.
   - A implementacao deve usar strings UTF-8 corretas: `ingestão`, `permissão`, `configuração`, `indisponível`, `déficit`, `superávit`.

2. Data do topo nao pode ser hardcoded.
   - O mockup usa `ter, 6 mai`.
   - A implementacao deve usar a data local atual do aparelho.

3. Evitar copy `Health Connect off-line`.
   - Preferir `Health Connect indisponivel` ou `Nao conseguimos acessar o Health Connect agora`.

4. Retry precisa ter estado ocupado.
   - O design inclui estado `loading`, mas o botao `Tentar novamente` no estado de erro nao mostra busy/loading por si.
   - Implementacao deve desabilitar o CTA durante retry e mostrar loading local, conforme SPEC.

5. Rotulo `BMR + ativas` deve ser traduzido para usuario final se aparecer.
   - Preferir `basal + ativas` ou `metabolismo basal + ativas`.
   - `BMR` pode ficar apenas em comentario tecnico ou teste.

6. `App.html` nao deve ser tratado como fonte de verdade do fluxo.
   - Ele ainda contem secoes antigas/auxiliares como onboarding, connecting, settings e outro dashboard.
   - Para o Dashboard MVP, usar somente a secao `Dashboard MVP - 8 estados` e o arquivo `dashboard-mvp.jsx`.

7. O preview HTML depende de CDN.
   - Isso e aceitavel para handoff visual.
   - Nao usar essa dependencia no app Android.

## Mapeamento para Compose

Componentes provaveis:

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

Estados esperados:

- `Loading`
- `Content`
- `PartialContent`
- `PermissionMissing`
- `HealthConnectUnavailable`
- `NoIntake`
- `NoWeight`
- `Error`
- `LocalStateInvalid`

## Criterios de Aceite Visual

Antes de fechar implementacao:

- Conferir em viewport Android comum se os textos nao quebram de forma ruim.
- Conferir estado completo e os 7 estados alternativos.
- Conferir que saldo nao aparece quando ingestao ou calorias ativas faltam.
- Conferir que o peso nao exibe tendencia.
- Conferir que deficit/manutencao/superavit nao dependem apenas de cor.
- Conferir que CTAs tem touch target adequado.
- Conferir que a tela nao usa gradientes, roxo dominante, blobs, emojis ou ilustracoes decorativas.

## Decisao

Design aprovado para seguir para Dev Plan e implementacao, desde que os ajustes listados sejam tratados na etapa de desenvolvimento.
