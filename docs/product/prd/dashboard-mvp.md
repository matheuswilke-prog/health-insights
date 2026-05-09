# PRD - dashboard-mvp

Fonte de verdade: `docs/ROADMAP.md`.

## Resumo

`dashboard-mvp` e a primeira tela de valor apos o onboarding. Ela deve responder rapidamente:

- Qual e minha meta calorica diaria?
- Hoje estou em deficit, manutencao ou superavit?
- Tenho dados suficientes para calcular o balanco?
- Qual e meu peso mais recente?

O Dashboard substitui a ideia anterior de uma tela separada de First Insight. Ele e o payoff do onboarding.

## Objetivo

Permitir que o usuario entenda seu estado calorico diario e acompanhe peso usando apenas dados locais do Android Health Connect e dados criptografados do app.

O Dashboard deve reduzir incerteza sobre:

- Se o usuario esta alinhado ao objetivo escolhido no onboarding.
- Se a ingestao calorica esta disponivel no Health Connect.
- Se faltam permissoes ou dados para calcular o balanco.
- Se ha peso recente disponivel.

## Persona

Marcos, 32 anos, brasileiro, usuario Android/Galaxy Watch, treina de 3 a 5 vezes por semana, ja acompanha alimentacao ou calorias de alguma forma e quer saber se esta caminhando para perder, manter ou ganhar peso.

O MVP deve ser construido para usuarios Android/Health Connect. Outros wearables e apps podem alimentar o Health Connect, mas o produto nao deve expandir escopo para integrações especificas no MVP.

## Problema

Dados de calorias, peso e treinos existem em apps de saude, mas o usuario precisa interpretar sozinho se eles estao ajudando no objetivo. Sem contexto, os numeros viram registro, nao decisao.

O Dashboard deve transformar esses dados em uma leitura simples: meta, consumo/gasto quando disponiveis, estado do balanco e peso recente.

## Proposta de Valor

Em poucos segundos, o usuario entende se o dia esta coerente com seu objetivo calorico, sem conta, sem nuvem e sem inserir dados manualmente no MVP.

## Escopo

Incluido:

- Exibir meta calorica diaria calculada no onboarding/dominio.
- Exibir gasto calorico do dia quando disponivel.
- Exibir ingestao calorica do dia quando disponivel.
- Exibir balanco calorico quando gasto e ingestao forem confiaveis.
- Classificar o balanco em deficit, manutencao ou superavit.
- Exibir estado claro quando nao houver ingestao calorica no Health Connect.
- Exibir peso mais recente quando disponivel.
- Tratar dados parciais.
- Tratar permissoes negadas ou ausentes.
- Tratar Health Connect indisponivel/desatualizado.
- Recarregar dados ao abrir o app e ao retornar ao foreground.
- Funcionar sem backend, analytics ou telemetria.

## Fora de Escopo

- Lancamento manual de refeicoes.
- Lancamento manual de peso.
- Recomendacoes nutricionais.
- Coach virtual.
- IA generativa.
- Backend, cloud sync, login ou conta.
- Graficos complexos.
- Historico avancado de peso.
- Comparacoes semanais detalhadas.
- Passos, sono e frequencia cardiaca.
- Widgets Android.
- Notificacoes.
- Paywall.

## Fluxo do Usuario

Fluxo inicial:

`Onboarding concluido -> Dashboard`

Aberturas futuras:

`App aberto -> verifica onboarding_complete -> Dashboard`

Se houver dados completos, o usuario ve meta, gasto, ingestao, balanco e peso. Se faltarem dados/permissoes, o Dashboard deve mostrar o que e possivel e explicar o que falta.

## Blocos Funcionais

O PRD define blocos, nao layout visual final. O layout sera feito por ferramenta externa de design.

### 1. Meta Diaria

Mostra a meta calorica diaria do usuario.

Origem:
- Valor salvo em Room + SQLCipher como dado derivado de saude.
- Calculado a partir de perfil e objetivo.

### 2. Balanco do Dia

Mostra ingestao, gasto e saldo quando os dados estao disponiveis.

Regra conceitual:
- O Dashboard deve consumir a regra de dominio existente, nao recalcular livremente na UI.
- A regra atual do dominio considera `ingestao - (BMR + calorias ativas)`.
- A SPEC deve decidir explicitamente como lidar com `TotalCaloriesBurned` para evitar dupla contagem com BMR + ActiveCalories.

Interpretacao:
- Saldo negativo: deficit.
- Saldo positivo: superavit.
- Proximo de zero: manutencao.
- Sem ingestao: nao calcular balanco completo.

### 3. Estado de Ingestao Calorica

Quando a ingestao nao estiver disponivel:

- Explicar que o Health Connect nao trouxe dados de ingestao calorica.
- Nao sugerir entrada manual no MVP.
- Nao tratar como erro tecnico sem evidencia.
- Manter meta e gasto visiveis se existirem.

Exemplo conceitual:

```text
Nao encontramos ingestao calorica no Health Connect.
Sem esse dado, ainda nao da para calcular o balanco completo do dia.
```

### 4. Peso

Mostra o peso mais recente quando disponivel.

Primeiro corte do MVP:
- Peso mais recente.
- Data da medicao, se disponivel.

Fora do primeiro corte, salvo se a SPEC adicionar leitura de historico:
- Diferenca vs medicao anterior.
- Grafico/tendencia.

### 5. Estado de Dados

Explica limitacoes quando necessario:

- Permissao ausente.
- Health Connect indisponivel.
- Health Connect sem dados.
- Dados parciais.
- Erro inesperado.

## Estados Esperados

### Loading

O Dashboard esta carregando dados locais e Health Connect.

Aceite:
- Nao bloquear indefinidamente.
- UI comunica carregamento simples.

### Content Completo

Condicao:
- Meta existe.
- Gasto disponivel.
- Ingestao disponivel.
- Health Connect disponivel.

Resultado:
- Exibe meta, gasto, ingestao, saldo e status do balanco.
- Exibe peso se disponivel.

### Sem Ingestao Calorica

Condicao:
- Gasto/meta existem.
- Ingestao ausente ou zero sem evidencia de alimento registrado.

Resultado:
- Nao calcular balanco como se fosse zero real.
- Explicar ausencia.
- Mostrar o que ainda e util: meta, gasto, peso se houver.

### Dados Parciais

Condicao:
- Apenas alguns dados/permissoes estao disponiveis.

Resultado:
- Mostrar blocos validos.
- Ocultar ou substituir blocos impossiveis.
- Explicar lacunas.

### Permissao Ausente ou Negada

Condicao:
- Usuario nao concedeu uma permissao necessaria.

Resultado:
- Explicar qual dado esta indisponivel.
- CTA deve abrir fluxo/configuracoes adequadas do Health Connect quando tecnicamente possivel.
- Nao insistir em permissao de forma abusiva.

### Health Connect Indisponivel

Condicao:
- Health Connect nao instalado, desatualizado ou API indisponivel.

Resultado:
- Explicar o problema.
- CTA para instalar/atualizar/abrir configuracao quando aplicavel.
- App nao deve crashar.

### Sem Dados Suficientes

Condicao:
- Health Connect disponivel, permissao concedida, mas sem registros uteis.

Resultado:
- Comunicar ausencia de dados como estado normal.
- Evitar parecer bug.

### Erro Inesperado

Condicao:
- Falha de leitura/processamento nao prevista.

Resultado:
- Mensagem amigavel.
- CTA de tentar novamente.
- Sem expor stack trace ou dado de saude.

## Dados Necessarios

Health Connect:
- Calorias gastas.
- Ingestao calorica.
- Peso.

Dados locais:
- Perfil do usuario.
- Objetivo.
- Meta calorica diaria.
- Consentimentos.
- Flag `onboarding_complete`.

Treinos:
- Ficam disponiveis no escopo de dados do MVP, mas nao precisam aparecer no primeiro corte do Dashboard, salvo decisao posterior na SPEC.

## Regras de Privacidade e LGPD

- Nenhum dado sai do aparelho.
- Nenhum analytics ou telemetria.
- Dados sensiveis/derivados ficam em Room + SQLCipher.
- Health data nao deve aparecer em logs.
- Dashboard deve respeitar consentimentos granulares.
- Dados ausentes por permissao negada devem ser comunicados sem punir o usuario.
- Retencao padrao do MVP: 12 meses.

## Requisitos Nao Funcionais

Performance:
- Dashboard deve carregar rapidamente ao abrir o app.
- Sem polling continuo no MVP.
- Recarregar ao abrir e ao retornar ao foreground.

Clareza:
- Usuario deve entender o que existe, o que falta e o que o app consegue calcular.

Confiabilidade:
- Dados parciais nao podem gerar conclusao enganosa.
- Ingestao ausente nao deve ser tratada como zero consumido.

## Critérios de Sucesso

Produto:
- Usuario entende sua meta diaria e se existe balanco calculavel.
- Usuario entende por que o balanco nao aparece quando falta ingestao.

UX:
- Estados vazios/erro parecem intencionais, nao falha.
- Usuario sabe qual permissao/dado esta faltando.

Tecnico:
- Dashboard nao crasha com Health Connect ausente, permissao negada ou dados parciais.
- Leitura e renderizacao passam nos testes definidos.

## Riscos e Mitigacoes

### Ausencia de Ingestao Calorica

Risco:
- Muitos usuarios nao terao app que sincronize alimentacao com Health Connect.

Mitigacao:
- Comunicar claramente que sem ingestao nao ha balanco completo.
- Mostrar meta, gasto e peso quando possivel.
- Nao prometer entrada manual no MVP.

### Dupla Contagem de Gasto

Risco:
- Misturar `TotalCaloriesBurned` com BMR + active calories pode inflar gasto.

Mitigacao:
- SPEC deve definir uma fonte canonica de gasto para o Dashboard.
- Testes devem cobrir a regra escolhida.

### Peso Sem Historico

Risco:
- Mostrar tendencia com apenas uma medicao seria enganoso.

Mitigacao:
- Primeiro corte mostra apenas peso mais recente.
- Tendencia entra apenas se a SPEC implementar leitura de historico com pelo menos duas medicoes.

### Permissoes Parciais

Risco:
- Usuario concede peso, mas nao calorias; ou concede calorias, mas nao nutricao.

Mitigacao:
- Dashboard mostra blocos independentes e explica lacunas por categoria.

## Perguntas em Aberto para SPEC

1. A regra canonica de gasto sera `BMR + ActiveCaloriesBurned` ou `TotalCaloriesBurned`?
2. O primeiro corte do Dashboard deve ler historico de peso ou apenas peso mais recente?
3. Quais CTAs exatos sao tecnicamente viaveis para reabrir permissao/configuracao Health Connect?
4. Qual tempo maximo aceitavel de loading antes de exibir erro/empty state?
5. O Dashboard deve observar foreground/resume em ViewModel, Activity ou camada de navegacao?

## Critérios de Aceite do PRD

- Escopo esta limitado ao MVP calorico/peso.
- Nao inclui entrada manual.
- Nao inclui passos, sono ou frequencia cardiaca.
- Define estados de dados parciais e erro.
- Reforca privacidade/on-device.
- Deixa layout visual para design externo.
- Explicita perguntas tecnicas para a SPEC.
