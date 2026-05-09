# Prompt de Design Externo - dashboard-mvp

Fonte de produto: `docs/product/prd/dashboard-mvp.md`.
Fonte de comportamento: `docs/specs/dashboard-mvp-spec.md`.
Fonte visual: `docs/design/visual-system-v1.md`.

Use este prompt em Claude Design, Figma AI ou ferramenta equivalente para criar o design da tela `dashboard-mvp`.

---

## Prompt para a ferramenta de design

Voce e um designer senior de produto mobile. Crie o design da tela principal de um app Android chamado Health Insights.

O objetivo da tela e mostrar rapidamente se o usuario esta em deficit, manutencao ou superavit calorico no dia, usando dados locais do Android Health Connect e dados criptografados do app. O app e 100% on-device, sem conta, sem nuvem, sem backend, sem analytics e sem telemetria.

Esta tela e o payoff apos o onboarding. Nao crie landing page, tela de marketing ou tutorial. A primeira tela deve ser o Dashboard funcional.

## Produto

Health Insights e um app Android para pessoas que ja acompanham saude/calorias e querem entender se o dia esta coerente com seu objetivo calorico.

Persona inicial:
- Marcos, 32 anos.
- Brasileiro.
- Usuario Android/Galaxy Watch.
- Treina de 3 a 5 vezes por semana.
- Ja acompanha alimentacao ou calorias de alguma forma.
- Quer saber rapidamente se esta caminhando para perder, manter ou ganhar peso.

## Escopo da tela

A tela deve conter blocos para:

1. Meta diaria
   - Mostra a meta calorica diaria.
   - Exemplo: `Meta diaria: 2200 kcal`.
   - Pode mostrar o objetivo: emagrecer, manter ou ganhar massa.

2. Balanco do dia
   - Quando completo, mostra:
     - ingestao do dia;
     - gasto estimado do dia;
     - saldo calorico;
     - status: deficit, manutencao ou superavit.
   - Formula de produto:
     - `gasto_estimado_do_dia = BMR + calorias_ativas_do_dia`
     - `saldo_calorico = ingestao_calorica_do_dia - gasto_estimado_do_dia`
   - Manutencao: saldo entre `-250 kcal` e `+250 kcal`, inclusive.
   - Deficit: saldo menor que `-250 kcal`.
   - Superavit: saldo maior que `+250 kcal`.

3. Ingestao calorica
   - Mostra calorias ingeridas hoje quando existem.
   - Se nao houver dado, usar copy amigavel:

```text
Ainda nao ha ingestao calorica registrada hoje no Health Connect.
Quando esse dado aparecer, calculamos o balanco completo do dia.
```

4. Gasto calorico
   - Mostra calorias ativas e gasto estimado quando disponivel.
   - Se calorias ativas estiverem ausentes, nao desenhar saldo.
   - Se BMR aparecer sozinho, rotular como `metabolismo basal estimado`, nunca como gasto total do dia.

5. Peso
   - Mostra apenas o peso mais recente.
   - Mostra data da medicao se disponivel.
   - Nao mostrar tendencia, grafico de peso ou diferenca vs medicao anterior no primeiro corte.

6. Estado de dados
   - Mostra lacunas de permissao, ausencia de dados ou Health Connect indisponivel.
   - Estados parciais nao devem esconder blocos que funcionam.

## Estados obrigatorios para desenhar

Crie versoes da tela para estes estados:

1. Dashboard completo
   - Meta: `2200 kcal`
   - Ingestao: `1850 kcal`
   - BMR: `1700 kcal`
   - Calorias ativas: `620 kcal`
   - Gasto estimado: `2320 kcal`
   - Saldo: `-470 kcal`
   - Status: deficit
   - Peso: `82,4 kg`, medido hoje

2. Sem ingestao calorica
   - Meta e gasto disponiveis.
   - Ingestao ausente.
   - Nao mostrar saldo como se ingestao fosse zero.
   - Mostrar a copy de ausencia de ingestao.

3. Permissao parcial
   - Peso permitido.
   - Calorias negadas ou ausentes.
   - Mostrar peso.
   - Explicar que calorias dependem de permissao/Health Connect.
   - CTA possivel: `Ajustar permissoes`.

4. Health Connect indisponivel
   - Meta local ainda aparece.
   - Blocos dependentes de Health Connect ficam indisponiveis.
   - CTA possivel: `Abrir Health Connect` ou `Atualizar Health Connect`.

5. Sem peso registrado
   - Calorias disponiveis.
   - Peso ausente.
   - Mostrar estado vazio do bloco de peso sem parecer erro.

6. Erro inesperado
   - Mensagem amigavel.
   - CTA: `Tentar novamente`.
   - Mostrar estado de loading no retry.
   - Nao usar texto tecnico, stack trace ou codigo de erro.

7. Loading
   - Estado inicial simples.
   - Deve parecer rapido e leve.
   - Nao bloquear a tela visualmente de forma pesada.

8. Estado local invalido
   - Caso raro: configuracao local precisa ser refeita.
   - Copy curta sugerida:

```text
Precisamos refazer sua configuracao local para continuar.
```

   - CTA: `Refazer configuracao`.

## Regras visuais obrigatorias

Siga o sistema visual do Health Insights:

- Fundo claro neutro `#FAFAF7`.
- Surface elevada `#FFFFFF`.
- Texto primario `#0E1116`.
- Texto secundario `#3A3F47`.
- Texto terciario `#6B7079`.
- Bordas sutis `rgba(14,17,22,0.08)`.
- Brand sage `#6FA47A`.
- Deficit: sage/verde `#6FA47A`.
- Superavit: coral `#D68B6A`.
- Manutencao: azul sutil `#6F8AB5`.
- Tipografia: Inter / Inter Tight.
- Numeros com aparencia tabular.
- Numeros importantes devem liderar a hierarquia visual.
- Touch targets minimos de 56dp.
- Cards podem ter radius proximo de 22dp.
- Botoes primarios com fundo `#0E1116`, texto branco, altura 56dp e radius 16dp.

Principios:
- O numero vem primeiro.
- Estados semanticos devem usar texto + cor, nunca apenas cor.
- Privacidade deve aparecer de forma discreta, especialmente em estados de permissao.
- A tela deve parecer utilitaria, calma e confiavel.
- A tela deve ser mobile-first para Android.

## Nao fazer

- Nao criar entrada manual de refeicao.
- Nao criar entrada manual de peso.
- Nao criar tela separada de First Insight.
- Nao incluir passos, sono, frequencia cardiaca, treinos detalhados, coach, IA ou recomendacao nutricional.
- Nao criar graficos avancados.
- Nao criar paywall.
- Nao criar login/conta.
- Nao usar gradientes saturados.
- Nao usar roxo dominante.
- Nao usar circulos decorativos, halos, blobs ou fundos ornamentais.
- Nao usar emojis.
- Nao usar icones genericos decorativos.
- Nao prometer diagnostico, prescricao, recomendacao medica ou nutricional.
- Nao mostrar health data em contexto de log/debug.

## Copy sugerida

Use portugues do Brasil, direto, simples e sem tom medico.

Exemplos:

- `Hoje`
- `Meta diaria`
- `Saldo do dia`
- `Voce esta em deficit`
- `Dentro da faixa de manutencao`
- `Acima da meta de hoje`
- `Ingestao registrada`
- `Gasto estimado`
- `Calorias ativas`
- `Metabolismo basal estimado`
- `Peso mais recente`
- `Dados ficam no aparelho`
- `Ajustar permissoes`
- `Tentar novamente`
- `Abrir Health Connect`

Evite:
- `falha critica`
- `erro fatal`
- `voce falhou`
- `voce deve`
- `recomendamos comer`
- qualquer linguagem prescritiva de saude.

## Acessibilidade

- Contraste adequado em todos os textos.
- Status deficit/manutencao/superavit com label textual alem de cor.
- Loading e erro precisam ser compreensiveis por leitor de tela.
- Botoes e areas tocaveis com minimo de 56dp.
- Textos devem caber em telas pequenas.
- Nao dependa de grafico para transmitir a informacao principal.

## Formato esperado de retorno

Entregue um handoff textual e visual com:

1. Estrutura da tela principal.
2. Hierarquia dos blocos.
3. Estados desenhados.
4. Copy final sugerida para cada estado.
5. Tokens de cor usados.
6. Tipografia e tamanhos recomendados.
7. Espacamentos principais.
8. Comportamento de CTAs.
9. Observacoes de acessibilidade.
10. Qualquer asset necessario, com descricao clara.

Se gerar Figma:
- Nomeie a pagina como `Dashboard MVP`.
- Crie frames separados para cada estado obrigatorio.
- Use nomes claros:
  - `01 Dashboard completo`
  - `02 Sem ingestao`
  - `03 Permissao parcial`
  - `04 Health Connect indisponivel`
  - `05 Sem peso`
  - `06 Erro`
  - `07 Loading`
  - `08 Estado local invalido`

Se gerar HTML/mockup:
- Entregue um unico arquivo com todos os estados visiveis lado a lado ou em secoes.
- Evite dependencias externas complexas.
- Use tokens comentados no CSS.

## Criterio de aprovacao do design

O design sera aprovado se:
- O Dashboard completo comunica saldo, meta, gasto, ingestao e peso em poucos segundos.
- Estados vazios parecem normais e nao bugs.
- Permissoes parciais nao quebram a tela.
- Health Connect indisponivel ainda preserva a meta local.
- Nao existe entrada manual no MVP.
- Nao existe tendencia/grafico de peso no primeiro corte.
- A hierarquia visual prioriza numeros e estado calorico.
- A tela respeita privacidade, acessibilidade e o sistema visual.
