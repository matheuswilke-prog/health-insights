# PRE-07 — Regras de Insight da Análise de Sono

**Versão:** 1.0  
**Data:** 2026-05-03  
**Autor:** Data and Analytics Insights Designer — Health Insights

---

## Visão Geral

A tela de Análise de Sono exibe: (1) um gráfico de barras com a duração de sono de cada noite dos últimos 7 dias, (2) uma linha de referência visual em 7h, (3) uma duração média semanal e (4) um headline interpretado comparando esta semana com a anterior.

**Definição de noite:** uma sessão de sono é atribuída ao dia do `endTime` (hora local) — o dia em que o usuário acordou. Isso significa que o sono que começa na noite de domingo e termina na manhã de segunda pertence à segunda-feira.

**Janela dos últimos 7 dias:** D-6 a D-0 (hoje), onde D-0 = hoje. Se o usuário ainda não acordou (nenhuma sessão com `endTime` hoje), D-0 é excluído dos cálculos e a janela efetiva é D-7 a D-1.

---

## 1. Duração Média Semanal

### Dados de entrada
- **Fonte Health Connect:** `SleepSessionRecord`
- **Campos usados:** `startTime`, `endTime`
- **Janela:** últimas 7 noites com `endTime` nos últimos 7 dias (D-6 a D-0 inclusive)
- **Noite válida:** noite cujo `endTime` está na janela E cuja duração total (ver seção 4 para regras de agregação de múltiplas sessões e filtros) é ≥ 60 minutos

### Cálculo

```
noites_validas = [n para n em janela se duracao(n) >= 60 min]
media_semanal = soma(duracao(n) para n em noites_validas) / len(noites_validas)
```

Resultado em minutos, exibido convertido para horas e minutos.

### Mínimo de noites para exibir média

| Noites válidas | Comportamento |
|---|---|
| 0 noites | Estado vazio: "Sem dados de sono nos últimos 7 dias." |
| 1–2 noites | Estado parcial: exibir média com aviso (ver copy abaixo) |
| 3–7 noites | Estado completo: exibir média normalmente |

### Copy templates (PT-BR)

- **Estado completo (≥ 3 noites):** "Você dormiu em média **{media_horas}h {media_min}min** nos últimos 7 dias."
  - Exemplo: "Você dormiu em média **7h 23min** nos últimos 7 dias."
- **Estado parcial (1–2 noites):** "Você dormiu em média **{media_horas}h {media_min}min** nos últimos 7 dias ({n_noites} noite{plural} registrada{plural})."
  - Exemplo (1 noite): "Você dormiu em média **6h 45min** nos últimos 7 dias (1 noite registrada)."
  - Exemplo (2 noites): "Você dormiu em média **7h 10min** nos últimos 7 dias (2 noites registradas)."
- **Estado vazio (0 noites):** "Sem dados de sono nos últimos 7 dias."

**Quando incluir contexto da meta de 7h (AASM):**
- Incluir contexto da meta somente no estado completo (≥ 3 noites) E quando a média calculada for **< 6h 30min** (390 minutos)
- Copy adicional (exibida como subtexto abaixo da média): "A recomendação para adultos é de pelo menos 7h por noite (AASM)."
- Não exibir esse contexto quando a média ≥ 6h 30min (evitar negatividade desnecessária)
- Não exibir esse contexto nos estados parcial ou vazio

**Formatação:**
- `media_horas`: parte inteira de `media_semanal / 60`
- `media_min`: `media_semanal mod 60` arredondado para o inteiro mais próximo
- Se `media_min = 0`: exibir somente "{media_horas}h" (omitir "0min")
- `n_noites`: inteiro (1 ou 2)
- `plural`: "s" se `n_noites > 1`, "" se `n_noites == 1`; "s" no feminino segue o mesmo padrão

---

## 2. Headline Interpretado

### Dados de entrada
- **Semana atual (V):** últimas 7 noites com `endTime` em D-6 a D-0
- **Semana anterior (A):** as 7 noites anteriores a isso, com `endTime` em D-13 a D-7

### Cálculo

```
media_V = média de duração das noites válidas (≥ 60 min) da semana V
media_A = média de duração das noites válidas (≥ 60 min) da semana A
delta_min = media_V - media_A  (em minutos, pode ser negativo)
```

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Positivo (dormiu mais) | delta_min > +20 minutos | Sleep Foundation: diferença de ≥20 min na média semanal é perceptível e clinicamente relevante |
| Neutro | -20 min ≤ delta_min ≤ +20 min | Sleep Foundation: variação de ±20 min dentro da variabilidade natural do sono |
| Negativo (dormiu menos) | delta_min < -20 minutos | Sleep Foundation: queda de >20 min na média indica padrão de restrição de sono |

> **Importante:** o threshold é em **minutos absolutos** (não percentual), pois pequenas durações de sono podem gerar deltas percentuais grandes e enganosos. Ex: de 6h para 6h20min é +5,5% mas é uma diferença absoluta de 20 min — mais relevante para sono.

### Condição de exibição do headline
- Semana V: mínimo de **3 noites válidas** para gerar headline
- Semana A: mínimo de **3 noites válidas** para comparação
- Se V < 3 noites: exibir copy "Dados insuficientes"
- Se A < 3 noites (mas V ≥ 3): exibir copy "Sem semana anterior para comparar"

### Copy templates (PT-BR)

- **Positivo:** "Você dormiu **{delta_abs_horas_min}** a mais por noite esta semana em relação à anterior."
  - Exemplo (delta = 35 min): "Você dormiu **35 min** a mais por noite esta semana em relação à anterior."
  - Exemplo (delta = 90 min): "Você dormiu **1h 30min** a mais por noite esta semana em relação à anterior."
- **Neutro:** "Seu padrão de sono ficou estável esta semana — em média **{media_V_horas}h {media_V_min}min** por noite."
- **Negativo:** "Você dormiu **{delta_abs_horas_min}** a menos por noite esta semana em relação à anterior."
  - Exemplo (delta = -45 min): "Você dormiu **45 min** a menos por noite esta semana em relação à anterior."
- **Sem semana anterior para comparar (V ≥ 3, A < 3):** "Em média **{media_V_horas}h {media_V_min}min** de sono por noite esta semana. Sem dados da semana anterior para comparar."
- **Dados insuficientes (V < 3 noites):** "Dados insuficientes esta semana para Sono."

**Formatação de delta absoluto em horas/minutos:**
```
se delta_abs < 60 min: exibir "{delta_abs} min"
se delta_abs >= 60 min: exibir "{h}h {m}min" onde h = delta_abs ÷ 60 (inteiro), m = delta_abs mod 60
se m = 0: exibir somente "{h}h"
```

**Formatação de média:**
- Mesmas regras da seção 1

---

## 3. Referência Visual de 7h no Gráfico

### Definição

Uma linha horizontal tracejada posicionada na marca de 420 minutos (7 horas) no eixo Y do gráfico de barras de sono. É puramente informativa — não influencia nenhum cálculo, threshold ou copy de insight.

### Especificações visuais

- **Tipo de linha:** tracejada (dash pattern: 4dp traço, 4dp espaço)
- **Cor:** cinza médio (#9E9E9E ou equivalente no design system do app)
- **Espessura:** 1dp
- **Posicionamento:** sobreposta ao gráfico, abaixo das barras (z-order: linha atrás das barras)
- **Extensão:** da borda esquerda à borda direita da área do gráfico

### Copy do tooltip/label

O label da linha deve ser exibido como texto estático à direita da linha (ou acima, se o espaço for limitado):

```
"7h (recomendado)"
```

- Fonte: menor que o rótulo do eixo Y (ex: 10sp vs. 12sp)
- Cor: mesma da linha (#9E9E9E)
- O tooltip (ao tocar na linha) deve exibir: **"Recomendação de sono para adultos: 7–9 horas por noite (AASM, 2024)"**

### Quando exibir a linha

- **Sempre** — independente dos dados disponíveis ou da média calculada
- Mesmo que o gráfico esteja vazio (estado sem dados): a linha de referência ainda é exibida para contextualizar o eixo Y
- A linha não é interativa além do tooltip ao toque

---

## 4. Edge Cases Específicos de Sono

### 4.1 Sessão de sono que cruza meia-noite

**Regra:** uma sessão de sono é sempre atribuída ao dia do `endTime` (hora local do dispositivo no momento da geração). O dia "em que o usuário acordou" é a âncora.

**Exemplo:**
- `startTime` = Dom 22:30, `endTime` = Seg 06:45 → noite pertence à Segunda-feira
- `startTime` = Sáb 23:50, `endTime` = Dom 07:20 → noite pertence ao Domingo

**Implicação para janela semanal:**
- Uma janela de D-6 a D-0 pode incluir uma sessão que começou em D-7 (se terminou em D-6)
- O Android Engineer deve buscar `SleepSessionRecord` com `endTime` no intervalo da janela, independentemente de quando o `startTime` cai

**Implementação:**
```
query: SleepSessionRecord onde endTime >= janela_inicio AND endTime <= janela_fim
```

### 4.2 Múltiplas sessões em uma noite (soneca + sono principal)

**Regra:** agregar todas as sessões cujo `endTime` cai no mesmo dia calendário.

**Passos:**
1. Agrupar `SleepSessionRecord` por `date(endTime)` (hora local)
2. Para cada grupo (dia): somar todas as durações
3. Se a soma total do grupo ≥ 60 min: noite válida com duração = soma
4. Se a soma total < 60 min: noite inválida (ver edge case 4.3)

**Exemplo:**
- Noite de Quarta: sessão A (startTime=Ter 23:30, endTime=Qua 02:00, duração=150 min) + sessão B (startTime=Qua 05:00, endTime=Qua 07:30, duração=150 min)
- Ambas com `endTime` na Quarta → soma = 300 min (5h) → noite válida

**Separação no gráfico:** o gráfico exibe uma única barra por dia com a soma total. Não há separação visual entre sono principal e soneca no MVP.

**Caso especial — soneca diurna:**
- Uma sessão com `startTime` e `endTime` ambos dentro de um mesmo dia (ex: 14:00–15:30 → soneca de 90 min)
- Essa sessão é atribuída ao mesmo dia do `endTime`
- Se no mesmo dia houver também uma sessão noturna (que cruzou meia-noite), a soneca é somada à noite
- Se a soneca for a única sessão do dia com ≥ 60 min: será incluída como "noite" — limitação aceita no MVP

### 4.3 Sessão de sono < 60 minutos (soneca curta isolada)

**Regra:** filtrar sessões individuais com duração < 60 minutos que são a **única sessão do dia**.

**Lógica:**
```
para cada dia D na janela:
  sessoes_D = lista de SleepSessionRecord com endTime no dia D
  duracao_total_D = soma de (endTime - startTime) para s em sessoes_D
  
  se duracao_total_D >= 60 min:
    noite_valida(D) = duracao_total_D
  senão:
    noite_invalida(D) — não incluir na média
```

**Justificativa:** sessões < 60 min isoladas são sonecas curtas que distorcem a média de sono noturno. O threshold de 60 min é pragmático e alinhado com a definição de "episódio de sono principal" na literatura do sono.

**No gráfico:** dias com apenas sonecas curtas (< 60 min total) exibem barra de altura zero ou barra com hachura diferente para indicar "dado insuficiente". Não exibir barra como se fosse sono noturno de 30 min.

**Copy no gráfico para dias sem noite válida:** sem tooltip especial no MVP — barra zero é suficiente.

### 4.4 Validação adicional de registros

| Problema | Tratamento |
|---|---|
| `endTime` antes de `startTime` | Ignorar registro — log de erro interno |
| Duração calculada negativa | Ignorar registro — log de erro interno |
| Duração > 24 horas | Ignorar registro — duração implausível; log de aviso |
| `SleepSessionRecord` com campos obrigatórios ausentes | Ignorar registro |
| Fuso horário muda durante o sono (ex: viagem) | Usar fuso horário local **no momento da geração do insight** para converter `endTime` e determinar o dia calendário |
| Health Connect retorna erro parcial (alguns dias OK, outros erro) | Processar os dias disponíveis; indicar "dados parciais" na copy se < 3 noites válidas |
