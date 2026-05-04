# PRE-05 — Regras de Insight do Dashboard Diário

**Versão:** 1.0  
**Data:** 2026-05-03  
**Autor:** Data and Analytics Insights Designer — Health Insights

---

## Visão Geral

O Dashboard Diário é a tela principal do app. Ele é atualizado em tempo real conforme novos dados chegam do Samsung Galaxy Watch via Health Connect. É composto por 4 elementos: Hero Metric (Passos de hoje), Card Sono, Card FC em Repouso e Card Último Treino.

**Ponto de referência temporal:** "hoje" = dia calendário atual no fuso horário local do dispositivo. "Ontem" = dia calendário anterior.

---

## Elemento 1: Hero Metric — Passos de Hoje

### Dados de entrada
- **Fonte Health Connect:** `StepsRecord`
- **Campos usados:** `count`, `startTime`, `endTime`
- **Valor hoje:** soma de `count` de todos os `StepsRecord` com `startTime` ou sobreposição com o dia calendário atual (00:00–23:59 hora local)
- **Histórico:** soma de `count` por dia para cada um dos 7 dias anteriores ao dia atual (D-1 a D-7)
- **Média de referência:** média aritmética dos dias do histórico que possuem pelo menos 1 `StepsRecord` com `count > 0`

### Lógica de comparação
- **Delta:** `(passos_hoje - média_histórico) / média_histórico × 100`
- **Direção:** maior é melhor
- **Atualização:** reativa — recalculada a cada sync com Health Connect

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Acima da média | delta > +10% | AHA: +10% como limiar de destaque positivo |
| Na média | -10% ≤ delta ≤ +10% | AHA: faixa de manutenção |
| Abaixo da média | delta < -10% | AHA: declínio relevante |

### Copy templates (PT-BR)

- **Acima da média:** "**{passos_hoje_formatado}** passos hoje — {delta_abs}% acima da sua média dos últimos 7 dias."
- **Na média:** "**{passos_hoje_formatado}** passos hoje — dentro da sua média dos últimos 7 dias."
- **Abaixo da média:** "**{passos_hoje_formatado}** passos hoje — {delta_abs}% abaixo da sua média dos últimos 7 dias."
- **Sem dados de hoje (mas com histórico):** "Aguardando dados de passos de hoje..."
- **Sem histórico (mas com dados de hoje):** "**{passos_hoje_formatado}** passos hoje."
- **Sem dados de hoje E sem histórico:** "Nenhum dado de passos disponível ainda."
- **Início do dia (dados parciais de hoje):** O valor exibido é o acumulado do dia — sem indicação de "parcial". A comparação é sempre contra a média histórica completa dos dias anteriores.

**Formatação:**
- `passos_hoje_formatado`: inteiro com separador de milhar PT-BR (ex: `12.450`)
- `delta_abs`: inteiro, valor absoluto arredondado (ex: `23`)

### Condição de supressão do insight (não do card)
- O número de passos de hoje é sempre exibido se disponível
- A copy comparativa (acima/na/abaixo da média) é suprimida se o histórico tiver menos de 3 dias com dados
- Nesse caso, exibir copy "Sem histórico"

### Edge cases

| Cenário | Tratamento |
|---|---|
| Passos hoje = 0 (usuário não usou relógio) | Exibir "Sem dados de hoje" — 0 não é exibido como valor válido se não houver nenhum StepsRecord |
| StepsRecord com count = 0 existe para hoje | Tratar como sem dados — exibir "Aguardando dados de passos de hoje..." |
| Hoje é o primeiro dia de uso do app | Exibir "Sem histórico" |
| Apenas 1-2 dias no histórico | Copy "Sem histórico" para comparação; exibir valor de hoje normalmente |
| Delta > +200% | Cap em "+200% ou mais" |
| Delta < -80% | Cap em "-80% ou mais" |
| Virada de fuso horário durante o dia | Recalcular "hoje" com o fuso atual do dispositivo; passos já registrados em fuso anterior são recontabilizados |
| Health Connect offline / erro de sync | Exibir último valor conhecido com timestamp "Atualizado às {HH:mm}" |

---

## Elemento 2: Card Sono — Última Noite

### Dados de entrada
- **Fonte Health Connect:** `SleepSessionRecord`
- **Campos usados:** `startTime`, `endTime`
- **Valor "última noite":** duração total de sono (em minutos) do `SleepSessionRecord` (ou soma de sessões) cujo `endTime` cai no dia atual (hoje) OU no dia anterior (ontem), escolhendo o mais recente disponível.
  - Preferência: `endTime` hoje (usuário ainda dormindo ou acordou hoje)
  - Fallback: `endTime` ontem (usuário acordou ontem)
  - Se ambos existirem: usar o de `endTime` hoje
- **Histórico:** duração de sono (em minutos) por noite para cada uma das 7 noites anteriores à "última noite" identificada acima
- **Média de referência:** média aritmética das noites do histórico com duração ≥ 60 minutos

### Lógica de comparação
- **Delta:** `(duração_ontem_min - média_histórico_min) / média_histórico_min × 100`
- **Direção:** maior é melhor
- **Referência:** 7h (420 min) é linha de meta visual no gráfico — **não** é threshold de copy

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Mais que usual | delta > +15% | Sleep Foundation: +15% acima da média pessoal indica noite excepcionalmente boa |
| Similar | -15% ≤ delta ≤ +15% | Sleep Foundation: faixa de variação noturna normal |
| Menos que usual | delta < -15% | Sleep Foundation: queda de 15% da média pessoal é redução percebida pelo usuário |

### Copy templates (PT-BR)

- **Mais que usual:** "Você dormiu **{duracao_horas}h {duracao_min}min** — {delta_abs}% mais que seu costume."
- **Similar:** "Você dormiu **{duracao_horas}h {duracao_min}min** — dentro do seu padrão habitual."
- **Menos que usual:** "Você dormiu **{duracao_horas}h {duracao_min}min** — {delta_abs}% menos que seu costume."
- **Sem histórico (mas com dado de ontem):** "Você dormiu **{duracao_horas}h {duracao_min}min** ontem."
- **Sem dados da última noite:** "Nenhum dado de sono registrado para a última noite."
- **Soneca detectada (sessão < 60min, única disponível):** "Nenhum dado de sono registrado para a última noite." (sonecas não contam como noite válida)

**Formatação:**
- `duracao_horas`: parte inteira de `duração_min / 60`
- `duracao_min`: `duração_min mod 60` arredondado
- Se `duracao_min = 0`: exibir somente "Xh"
- `delta_abs`: inteiro, valor absoluto arredondado

### Condição de supressão do insight comparativo
- Exibir valor de duração sempre que disponível
- Suprimir comparação se histórico tiver menos de 3 noites válidas (≥ 60 min)

### Edge cases

| Cenário | Tratamento |
|---|---|
| Sem dados da última noite | Card exibe "Nenhum dado de sono registrado para a última noite." |
| Sessão única < 60 min | Tratar como sem dado noturno |
| Múltiplas sessões na mesma noite | Somar todas com `endTime` no mesmo dia; se soma ≥ 60 min, usar como dado noturno |
| Usuário ainda dormindo (SleepSessionRecord aberta, sem endTime) | Não exibir dado para noite em andamento — aguardar fechamento da sessão |
| Sessão cruzando meia-noite | Atribuir ao dia do `endTime` |
| Histórico < 3 noites válidas | Exibir duração sem comparação (copy "Sem histórico") |
| Delta > +200% | Cap em "+200% ou mais" |
| Delta < -80% | Cap em "-80% ou mais" |
| Fuso horário mudou durante o sono | Usar fuso do momento da geração para determinar o dia do `endTime` |

---

## Elemento 3: Card FC em Repouso — Hoje

### Dados de entrada
- **Fonte Health Connect:** `HeartRateRecord`
- **Campos usados:** `samples[].beatsPerMinute`, `samples[].time`
- **Critério de repouso para "hoje":** amostras com `time` entre 00:00 e 06:00 hora local de hoje, OU amostras que caem dentro de uma `SleepSessionRecord` cujo `endTime` é hoje
- **Valor hoje:** mediana de todos os valores de `beatsPerMinute` válidos (30–200 bpm) coletados hoje segundo o critério de repouso
- **Histórico:** mediana de FC em repouso para cada um dos 7 dias anteriores (D-1 a D-7), usando o mesmo critério de repouso por dia
- **Mediana de referência:** mediana das medianas diárias do histórico (mediana de 7 valores)

> **Nota de implementação:** a "mediana das medianas" pode divergir ligeiramente da mediana de todas as amostras brutas dos 7 dias, mas é a abordagem correta para normalizar por dia e evitar viés de dias com muitas amostras.

### Lógica de comparação
- **Delta:** `(mediana_hoje - mediana_histórico) / mediana_histórico × 100`
- **Direção:** menor é melhor

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Melhora (FC caiu) | delta < -3% | AHA: queda de ≥3% em relação à média recente é sinal de melhora |
| Estável | -3% ≤ delta ≤ +3% | AHA: variação fisiológica normal diária |
| Piora (FC subiu) | delta > +3% | AHA: elevação >3% pode indicar estresse, fadiga ou doença |

### Copy templates (PT-BR)

- **Melhora:** "FC em repouso: **{mediana_hoje} bpm** — {delta_abs}% abaixo da sua média recente."
- **Estável:** "FC em repouso: **{mediana_hoje} bpm** — estável em relação aos últimos 7 dias."
- **Piora:** "FC em repouso: **{mediana_hoje} bpm** — {delta_abs}% acima da sua média recente."
- **Sem histórico (mas com dado de hoje):** "FC em repouso hoje: **{mediana_hoje} bpm**."
- **Sem dados hoje:** "Nenhum dado de FC em repouso registrado hoje."

**Formatação:**
- `mediana_hoje`: inteiro arredondado (ex: `61`)
- `delta_abs`: valor absoluto com 1 casa decimal (ex: `4.1`)

### Condição de supressão do insight comparativo
- Exibir valor de hoje sempre que disponível
- Suprimir comparação se histórico tiver menos de 3 dias com dados válidos

### Edge cases

| Cenário | Tratamento |
|---|---|
| Sem dados de hoje | Card exibe "Nenhum dado de FC em repouso registrado hoje." |
| Amostras com bpm fora de [30, 200] | Filtrar — ignorar amostra |
| Amostras com bpm = 0 | Filtrar — ignorar amostra |
| Histórico < 3 dias válidos | Exibir mediana de hoje sem comparação |
| Hoje tem apenas 1 amostra válida | Mediana = aquele valor único — válido |
| Delta > +200% | Cap em "+200% ou mais" |
| FC durante atividade física nas primeiras horas | Aceitar — critério de horário 00:00–06:00 é proxy; não há forma de distinguir no MVP sem dados de accelerometer |
| Health Connect offline | Exibir último valor com timestamp |

---

## Elemento 4: Card Último Treino

### Dados de entrada
- **Fonte Health Connect:** `ExerciseSessionRecord`
- **Campos usados:** `startTime`, `endTime`, `exerciseType`
- **Valor:** o `ExerciseSessionRecord` mais recente disponível (maior `startTime`)
- **Tipo de atividade:** mapeamento de `exerciseType` para nome em PT-BR (ver tabela abaixo)
- **Duração:** `endTime - startTime` em minutos, arredondado para o inteiro mais próximo
- **Quando foi:** calculado em relação ao momento atual de exibição

> Este card é informativo — não é um insight calculado. Exibe fatos sobre o último treino registrado.

### Mapeamento de tipos de exercício (PT-BR)

| exerciseType (Health Connect) | Exibição PT-BR |
|---|---|
| RUNNING | Corrida |
| WALKING | Caminhada |
| CYCLING | Ciclismo |
| SWIMMING_OPEN_WATER / SWIMMING_POOL | Natação |
| STRENGTH_TRAINING | Musculação |
| YOGA | Yoga |
| HIIT | HIIT |
| ELLIPTICAL | Elíptico |
| ROWING_MACHINE | Remo |
| STAIR_CLIMBING | Escada |
| DANCING | Dança |
| FOOTBALL_AMERICAN / FOOTBALL_AUSTRALIAN / SOCCER | Futebol |
| BASKETBALL | Basquete |
| TENNIS | Tênis |
| VOLLEYBALL | Vôlei |
| BOXING | Boxe |
| MARTIAL_ARTS | Artes Marciais |
| PILATES | Pilates |
| CROSS_COUNTRY_SKIING / DOWNHILL_SKIING | Esqui |
| GOLF | Golfe |
| HIKING | Trilha |
| Qualquer outro tipo não listado | Atividade Física |

### Copy templates (PT-BR)

- **Treino hoje:** "**{tipo_treino}** hoje — {duracao_min} min"
  - Exemplo: "**Corrida** hoje — 35 min"
- **Treino ontem:** "**{tipo_treino}** ontem — {duracao_min} min"
  - Exemplo: "**Musculação** ontem — 45 min"
- **Treino há X dias (2–6 dias atrás):** "**{tipo_treino}** há {dias} dias — {duracao_min} min"
  - Exemplo: "**Ciclismo** há 3 dias — 60 min"
- **Treino há 7+ dias:** "**{tipo_treino}** há mais de uma semana — {duracao_min} min"
- **Nenhum treino registrado:** "Nenhum treino registrado ainda."

**Formatação:**
- `tipo_treino`: string PT-BR do mapeamento acima
- `duracao_min`: inteiro arredondado para o minuto mais próximo
- `dias`: inteiro (diferença em dias completos entre `startTime` do último treino e hoje)

### Lógica de "quando foi"

```
dias_atras = (data_atual - date(startTime_último_treino)).days

se dias_atras == 0 → "hoje"
se dias_atras == 1 → "ontem"
se 2 ≤ dias_atras ≤ 6 → "há {dias_atras} dias"
se dias_atras ≥ 7 → "há mais de uma semana"
```

### Edge cases

| Cenário | Tratamento |
|---|---|
| Nenhum ExerciseSessionRecord disponível | "Nenhum treino registrado ainda." |
| ExerciseSessionRecord com duração < 1 minuto | Exibir como "menos de 1 min" ou "1 min" (arredondar para cima) |
| exerciseType não mapeado | Exibir "Atividade Física" |
| ExerciseSessionRecord sem endTime (treino em andamento) | Não exibir treino em andamento — aguardar fechamento. Exibir o penúltimo treino concluído se disponível |
| Dois treinos no mesmo dia | Exibir apenas o mais recente (maior startTime) |
| ExerciseSessionRecord com startTime no futuro | Ignorar — filtrar registros com startTime > now() |
| Health Connect retorna erro | Exibir "Nenhum treino registrado ainda." com log de erro interno |
| Duração calculada = 0 (startTime == endTime) | Ignorar registro — tratar como sem dado |
