# PRE-04 — Regras do Resumo Semanal

**Versão:** 1.0  
**Data:** 2026-05-03  
**Autor:** Data and Analytics Insights Designer — Health Insights  
**Hard blocker para:** EP-06

---

## Visão Geral

O Resumo Semanal é gerado toda **segunda-feira** e cobre a semana anterior (segunda a domingo). Ele consiste em exatamente 3 bullets determinísticos: Passos, Sono e FC em repouso. Cada bullet pode estar em um dos estados: Positivo, Neutro, Negativo, Dado Parcial ou Suprimido.

**Janela de geração:** toda segunda-feira (dia da semana = 2, ISO 8601), para a semana Mon[t-7] a Sun[t-1].  
**Ponto de corte:** 00:00 hora local do dispositivo na segunda-feira de geração.

---

## Regra 1: Passos

### Dados de entrada
- **Fonte Health Connect:** `StepsRecord`
- **Campos usados:** `count` (número de passos), `startTime`, `endTime`
- **Agregação:** soma de `count` por dia calendário (00:00–23:59 hora local). Um dia com qualquer `StepsRecord` sobreposição é considerado com dados.
- **Janela atual:** segunda a domingo da semana imediatamente anterior à data de geração (ex: se hoje é Mon 2026-05-04, janela = Mon 2026-04-27 a Sun 2026-05-03)
- **Janela baseline:** segunda a domingo da semana retrasada (ex: Mon 2026-04-20 a Sun 2026-04-26)

### Lógica de comparação
- **Total atual:** soma de passos de todos os dias com dados na janela atual
- **Total baseline:** soma de passos de todos os dias com dados na janela baseline
- **Delta:** `(total_atual - total_baseline) / total_baseline × 100`
- **Direção:** maior é melhor

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Positivo | delta > +10% | American Heart Association: variação de +10% indica progresso consistente em atividade física |
| Neutro | -10% ≤ delta ≤ +10% | AHA: faixa de manutenção aceitável |
| Negativo | delta < -10% | AHA: declínio relevante em atividade física |

### Copy templates (PT-BR)

- **Positivo:** "Você andou **{total_atual_formatado} passos** esta semana — {delta_abs}% a mais que na semana anterior. Continue assim!"
- **Neutro:** "Você manteve um ritmo consistente: **{total_atual_formatado} passos** esta semana, similar à semana anterior."
- **Negativo:** "Você andou **{total_atual_formatado} passos** esta semana — {delta_abs}% a menos que na semana anterior."
- **Sem baseline (semana anterior sem dados):** "Você andou **{total_atual_formatado} passos** esta semana. Sem dados da semana anterior para comparar."
- **Dados insuficientes:** "Dados insuficientes esta semana para Passos."

**Formatação de números:**
- `total_atual_formatado`: inteiro com separador de milhar em PT-BR (ex: `42.350`)
- `delta_abs`: valor absoluto do delta arredondado para o inteiro mais próximo, sem sinal (ex: `15`)

### Condição de supressão
- Suprimir se a janela atual tiver **menos de 3 dias com dados de passos** (StepsRecord com `count > 0`)
- Suprimir se **todos os valores de `count` na janela atual forem zero**
- Se a janela baseline tiver menos de 3 dias com dados: mostrar copy "Sem baseline" (sem comparação percentual)

### Edge cases

| Cenário | Tratamento |
|---|---|
| Sem dados na semana atual (0 dias) | Suprimido — bullet não aparece |
| Sem dados na semana baseline | Exibir copy "Sem baseline" sem delta |
| Apenas 1 ou 2 dias de dados na semana atual | Suprimido (abaixo do mínimo de 3 dias) |
| Delta > +200% | Exibir "+200% ou mais" no lugar do valor calculado na copy |
| Delta < -80% | Exibir "-80% ou mais" no lugar do valor calculado na copy |
| Todos os valores de count = 0 na semana atual | Tratar como sem dados — suprimido |
| Todos os valores de count = 0 na semana baseline | Tratar baseline como ausente — copy "Sem baseline" |
| Virada de fuso horário (ex: viagem) | Usar o fuso horário local **no momento do registro** de cada `StepsRecord`. O dia calendário é determinado pelo fuso horário do dispositivo no momento da geração do resumo. Passos registrados em fuso diferente são atribuídos ao dia calendário local do momento de `startTime` convertido para o fuso atual do dispositivo. |
| `StepsRecord` sobreposto a dois dias calendários | Atribuir integralmente ao dia de `startTime` (hora local) |
| Health Connect retorna erro ao buscar dados | Suprimir bullet com log de erro interno; não exibir mensagem de erro ao usuário |
| Semana com feriado nacional (ex: Carnaval) | Nenhum tratamento especial — regra é agnóstica a feriados |

### Fixtures para QA

**Fixture 1 — Cenário Positivo padrão**
- Semana atual: [8.000, 9.500, 10.200, 7.800, 11.000, 12.500, 9.000] passos (Seg–Dom) = 68.000 total
- Semana baseline: [7.000, 8.000, 9.000, 6.500, 9.000, 10.000, 8.500] passos = 58.000 total
- Delta: (68.000 - 58.000) / 58.000 × 100 = +17,24% → arredonda para +17%
- Output esperado: Categoria = Positivo; Copy = "Você andou **68.000 passos** esta semana — 17% a mais que na semana anterior. Continue assim!"

**Fixture 2 — Cenário Neutro**
- Semana atual: [8.000, 8.200, 7.900, 8.100, 8.300, 7.800, 8.000] passos = 56.300 total
- Semana baseline: [8.100, 8.000, 8.200, 7.900, 8.000, 8.300, 7.800] passos = 56.300 total
- Delta: 0% → dentro de ±10%
- Output esperado: Categoria = Neutro; Copy = "Você manteve um ritmo consistente: **56.300 passos** esta semana, similar à semana anterior."

**Fixture 3 — Cenário Negativo**
- Semana atual: [3.000, 2.500, 4.000, 0, 3.500, 2.000, 3.000] passos = 18.000 total (dia com 0 ainda conta como dado se StepsRecord existe)
- Semana baseline: [8.000, 9.000, 8.500, 8.000, 9.500, 10.000, 8.500] passos = 61.500 total
- Delta: (18.000 - 61.500) / 61.500 × 100 = -70,73% → exibir "-70%" (acima do cap de -80%)
- Output esperado: Categoria = Negativo; Copy = "Você andou **18.000 passos** esta semana — 70% a menos que na semana anterior."

**Fixture 4 — Sem dados na semana baseline**
- Semana atual: [10.000, 9.500, 11.000, 9.000, 10.500, 12.000, 9.500] passos = 71.500 total (7 dias com dados)
- Semana baseline: sem registros de StepsRecord
- Output esperado: Categoria = Sem baseline; Copy = "Você andou **71.500 passos** esta semana. Sem dados da semana anterior para comparar."

**Fixture 5 — Supressão por dados insuficientes (2 dias)**
- Semana atual: apenas Segunda (5.000) e Terça (6.000) com dados; Quarta a Domingo sem registros
- Semana baseline: [8.000, 8.000, 8.000, 8.000, 8.000, 8.000, 8.000] = 56.000
- Output esperado: Bullet suprimido — não aparece no resumo semanal

---

## Regra 2: Sono

### Dados de entrada
- **Fonte Health Connect:** `SleepSessionRecord`
- **Campos usados:** `startTime`, `endTime` (duração calculada como `endTime - startTime` em minutos)
- **Agregação:** média aritmética de duração (em minutos) por **noite de sono** na janela. Uma noite é identificada pela data do `endTime` (hora local) — a noite "pertence" ao dia em que o usuário acordou.
- **Janela atual:** noites cujo `endTime` cai entre terça-feira 00:00 e segunda-feira 23:59 da semana coberta pelo resumo (ou seja, as noites de segunda para terça até domingo para segunda — 7 noites possíveis)
- **Janela baseline:** mesma lógica para a semana retrasada

> **Nota de modelagem:** a janela de noites desloca 1 dia em relação à janela de passos porque o sono da noite de domingo para segunda pertence ao domingo — mas já é capturado pelo resumo seguinte. Para evitar ambiguidade, a janela de sono usa `endTime` como âncora: noites com `endTime` de Ter a Seg (7 noites) cobrem a semana de sono correspondente à semana calendário Mon–Sun.

### Lógica de comparação
- **Média atual:** média de minutos de sono das noites válidas na janela atual
- **Média baseline:** média de minutos de sono das noites válidas na janela baseline
- **Delta:** `(média_atual - média_baseline) / média_baseline × 100`
- **Direção:** maior é melhor

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Positivo | delta > +10% | Sleep Foundation: melhoria de 10% na duração semanal média é clinicamente relevante |
| Neutro | -10% ≤ delta ≤ +10% | Sleep Foundation: variação de ±10% considerada manutenção de padrão |
| Negativo | delta < -10% | Sleep Foundation: queda de 10% associada a déficit acumulado de sono |

### Copy templates (PT-BR)

- **Positivo:** "Você dormiu em média **{media_atual_horas}h {media_atual_min}min** por noite esta semana — {delta_abs}% a mais que na semana anterior."
- **Neutro:** "Seu sono manteve-se estável: em média **{media_atual_horas}h {media_atual_min}min** por noite, similar à semana anterior."
- **Negativo:** "Você dormiu em média **{media_atual_horas}h {media_atual_min}min** por noite esta semana — {delta_abs}% a menos que na semana anterior."
- **Sem baseline:** "Você dormiu em média **{media_atual_horas}h {media_atual_min}min** por noite esta semana. Sem dados da semana anterior para comparar."
- **Dados insuficientes:** "Dados insuficientes esta semana para Sono."

**Formatação de tempo:**
- `media_atual_horas`: parte inteira de `média_atual / 60` (ex: `7`)
- `media_atual_min`: `média_atual mod 60` arredondado para o inteiro mais próximo (ex: `23`)
- Exemplo: 453 minutos → "7h 33min"
- Se `media_atual_min = 0`: exibir somente "7h" (omitir "0min")

### Condição de supressão
- Suprimir se a janela atual tiver **menos de 3 noites com dados de sono válidos**
- Uma noite é válida se: existe pelo menos 1 `SleepSessionRecord` com duração ≥ 60 minutos com `endTime` na janela
- Se a janela baseline tiver menos de 3 noites válidas: usar copy "Sem baseline"

### Edge cases

| Cenário | Tratamento |
|---|---|
| Sem dados na semana atual | Suprimido |
| Sem dados na semana baseline | Copy "Sem baseline" |
| Apenas 1 ou 2 noites na semana atual | Suprimido |
| Delta > +200% | Cap em "+200% ou mais" |
| Delta < -80% | Cap em "-80% ou mais" |
| Sessão de sono que cruza meia-noite | Atribuir ao dia do `endTime` (hora local) — o dia em que o usuário acordou |
| Múltiplas sessões na mesma noite (ex: soneca 30min + noite 7h) | Somar todas as sessões com `endTime` na mesma data, desde que a soma total ≥ 60min. Filtrar sessões individuais < 60min que não fazem parte de um bloco principal (ver edge case abaixo) |
| Sessão de sono < 60 minutos (soneca isolada) | Filtrar — não incluir na média de sono noturno se for a única sessão da noite |
| Sessão de sono < 60 minutos + sessão ≥ 60 min na mesma noite | Somar ambas para a noite |
| Todos os registros < 60min na semana | Suprimido (nenhuma noite válida) |
| `SleepSessionRecord` com `endTime` antes de `startTime` | Ignorar registro — log de erro interno |
| Virada de fuso horário | `endTime` convertido para fuso local no momento da geração |
| Health Connect retorna erro | Suprimir bullet com log de erro interno |

### Fixtures para QA

**Fixture 1 — Cenário Positivo padrão**
- Semana atual: noites com 390, 420, 450, 480, 430, 460, 410 minutos (5 noites ≥ 60min + 2 acima)
  - Média: (390+420+450+480+430+460+410) / 7 = 3.040 / 7 ≈ 434 min = 7h 14min
- Semana baseline: noites com 350, 370, 360, 380, 370, 360, 380 minutos
  - Média: 2.570 / 7 ≈ 367 min = 6h 7min
- Delta: (434 - 367) / 367 × 100 = +18,25% → +18%
- Output esperado: Positivo; "Você dormiu em média **7h 14min** por noite esta semana — 18% a mais que na semana anterior."

**Fixture 2 — Cenário Neutro**
- Semana atual: 7 noites, média = 420 min (7h 0min)
- Semana baseline: 7 noites, média = 415 min
- Delta: (420 - 415) / 415 × 100 = +1,2% → Neutro
- Output esperado: Neutro; "Seu sono manteve-se estável: em média **7h** por noite, similar à semana anterior."

**Fixture 3 — Supressão por sonecas isoladas**
- Semana atual: 7 registros, todos com duração de 45 minutos (nenhuma noite válida ≥ 60min como sessão única)
- Output esperado: Bullet suprimido

**Fixture 4 — Sessão cruzando meia-noite**
- SleepSessionRecord: startTime = Dom 23:00, endTime = Seg 06:30 (duração = 450 min)
- Essa noite é atribuída à Segunda-feira (endTime)
- Semana atual: 4 outras noites válidas + essa = 5 noites válidas
- Output esperado: Noite contabilizada corretamente para Segunda; bullet gerado se ≥ 3 noites

**Fixture 5 — Múltiplas sessões na mesma noite**
- Noite de Quarta: sessão 1 = 30 min (soneca noturna), sessão 2 = 390 min (bloco principal)
- Ambas com `endTime` na Quarta-feira
- Soma = 420 min — noite válida
- Output esperado: Noite de Quarta = 420 min na média; noite válida contabilizada

---

## Regra 3: FC em Repouso

### Dados de entrada
- **Fonte Health Connect:** `HeartRateRecord`
- **Campos usados:** `samples[].beatsPerMinute`, `samples[].time`
- **Agregação:** mediana de todos os valores de `beatsPerMinute` classificados como FC em repouso na janela.
  - **Critério de repouso:** amostras coletadas entre 00:00 e 06:00 hora local OU amostras dentro de uma `SleepSessionRecord` ativa no mesmo período de tempo (sobreposição de `sample.time` com qualquer `SleepSessionRecord` da janela). Se nenhum dos dois critérios for atendido, usar amostras entre 00:00–06:00 como proxy.
- **Janela atual:** segunda a domingo da semana imediatamente anterior
- **Janela baseline:** segunda a domingo da semana retrasada

### Lógica de comparação
- **Mediana atual:** mediana de todas as amostras de FC em repouso na janela atual
- **Mediana baseline:** mediana de todas as amostras na janela baseline
- **Delta:** `(mediana_atual - mediana_baseline) / mediana_baseline × 100`
- **Direção:** menor é melhor (FC em repouso mais baixa indica melhor condicionamento cardiovascular)

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Positivo (FC caiu) | delta < -3% | American Heart Association: queda de ≥3% na FC repouso é indicador de melhora de aptidão cardiovascular |
| Neutro | -3% ≤ delta ≤ +3% | AHA: variação dentro de ±3% é fisiologicamente insignificante em curto prazo |
| Negativo (FC subiu) | delta > +3% | AHA: elevação de >3% pode indicar estresse, fadiga ou redução de condicionamento |

### Copy templates (PT-BR)

- **Positivo (FC caiu):** "Sua FC em repouso caiu para uma mediana de **{mediana_atual} bpm** esta semana — {delta_abs}% abaixo da semana anterior. Bom sinal!"
- **Neutro:** "Sua FC em repouso ficou estável: mediana de **{mediana_atual} bpm**, similar à semana anterior."
- **Negativo (FC subiu):** "Sua FC em repouso subiu para uma mediana de **{mediana_atual} bpm** esta semana — {delta_abs}% acima da semana anterior."
- **Sem baseline:** "Sua FC em repouso ficou em uma mediana de **{mediana_atual} bpm** esta semana. Sem dados da semana anterior para comparar."
- **Dados insuficientes:** "Dados insuficientes esta semana para FC em Repouso."

**Formatação:**
- `mediana_atual`: inteiro arredondado (ex: `62`)
- `delta_abs`: valor absoluto do delta arredondado para 1 casa decimal, sem sinal (ex: `4.2`)

### Condição de supressão
- Suprimir se a janela atual tiver **menos de 3 dias com pelo menos 1 amostra de FC em repouso válida**
- Uma amostra é válida se: `beatsPerMinute` está no intervalo [30, 200] bpm
- Se a janela baseline tiver menos de 3 dias válidos: usar copy "Sem baseline"

### Edge cases

| Cenário | Tratamento |
|---|---|
| Sem dados na semana atual | Suprimido |
| Sem dados na semana baseline | Copy "Sem baseline" |
| Menos de 3 dias com amostras válidas | Suprimido |
| Delta > +200% | Cap em "+200% ou mais" (improvável fisiologicamente, mas defensivo) |
| Delta < -80% | Cap em "-80% ou mais" |
| Amostra com bpm = 0 | Ignorar — inválida |
| Amostra com bpm < 30 ou > 200 | Ignorar — fora do range fisiológico válido |
| Múltiplas amostras no mesmo minuto | Incluir todas na mediana — não deduplicar |
| FC durante exercício incluída por engano (ex: sensor registrando durante caminhada noturna às 05:50) | Aceitar se estiver no critério de horário (00:00–06:00); o critério de horário é proxy conservador. Não é possível distinguir exercício noturno de repouso sem accelerometer data no MVP |
| Virada de fuso horário | Horário das amostras convertido para fuso local no momento da geração |
| Apenas 1 amostra no dia | Válida — inclusa na mediana se bpm ∈ [30, 200] |
| Health Connect retorna erro | Suprimir bullet com log de erro interno |

### Fixtures para QA

**Fixture 1 — Cenário Positivo (FC caiu)**
- Semana atual: 7 dias, amostras de FC repouso resultando em mediana = 58 bpm
- Semana baseline: 7 dias, mediana = 62 bpm
- Delta: (58 - 62) / 62 × 100 = -6,45% → -6,5%
- Output esperado: Positivo; "Sua FC em repouso caiu para uma mediana de **58 bpm** esta semana — 6,5% abaixo da semana anterior. Bom sinal!"

**Fixture 2 — Cenário Neutro**
- Semana atual: mediana = 64 bpm
- Semana baseline: mediana = 63 bpm
- Delta: (64 - 63) / 63 × 100 = +1,59% → dentro de ±3%
- Output esperado: Neutro; "Sua FC em repouso ficou estável: mediana de **64 bpm**, similar à semana anterior."

**Fixture 3 — Cenário Negativo (FC subiu)**
- Semana atual: mediana = 72 bpm
- Semana baseline: mediana = 65 bpm
- Delta: (72 - 65) / 65 × 100 = +10,77% → +10,8%
- Output esperado: Negativo; "Sua FC em repouso subiu para uma mediana de **72 bpm** esta semana — 10,8% acima da semana anterior."

**Fixture 4 — Amostras inválidas filtradas**
- Semana atual: dia 1 tem amostras [55, 0, 220, 60, 58] bpm → válidas: [55, 60, 58]; dia 2 = [62, 63, 61]; dia 3 = [59, 60]; mais 4 dias sem dados
- Dias com dados válidos = 3 (mínimo atingido)
- Todas as amostras válidas: [55, 60, 58, 62, 63, 61, 59, 60] → mediana = 60 bpm
- Output esperado: Bullet gerado com mediana 60 bpm

**Fixture 5 — Supressão por dados insuficientes (2 dias)**
- Semana atual: apenas Segunda e Terça com amostras válidas; restante da semana sem registros
- Output esperado: Bullet suprimido
