# PRE-06 — Regras de Insight da Tendência Semanal de Passos

**Versão:** 1.0  
**Data:** 2026-05-03  
**Autor:** Data and Analytics Insights Designer — Health Insights

---

## Visão Geral

A tela de Tendência Semanal de Passos exibe um gráfico de barras (um por dia da semana) com uma linha de média móvel sobreposta, um headline interpretado no topo e navegação para semanas anteriores (até 4 semanas atrás). O usuário vê a semana atual por padrão e pode navegar para trás.

**Definição de semana:** Segunda (00:00) a Domingo (23:59) hora local. A semana atual pode estar incompleta (ex: hoje é quarta — apenas Seg, Ter e Qua têm dados).

---

## 1. Headline Interpretado

### Dados de entrada
- **Fonte Health Connect:** `StepsRecord`
- **Campos usados:** `count`, `startTime`
- **Agregação:** soma de `count` por dia calendário para cada semana

### Janelas de comparação

**Semana visualizada (V):** a semana que o usuário está vendo no momento (default = semana atual).
**Semana anterior (A):** a semana imediatamente anterior à semana visualizada (V-1).

**Semana atual incompleta:** se hoje é dia X da semana V, somente dias de Segunda até hoje têm potencial de dados. Dias futuros da semana são ignorados (não há dados).

**Cálculo do total por semana:**
- `total_V` = soma de passos de todos os dias da semana V que possuem pelo menos 1 `StepsRecord` com `count > 0`
- `total_A` = soma de passos de todos os dias da semana A que possuem pelo menos 1 `StepsRecord` com `count > 0`

**Delta:**
```
delta = (total_V - total_A) / total_A × 100
```

**Condição de exibição do headline:**
- Semana V: mínimo 1 dia com dados (ao menos 1 barra no gráfico)
- Semana A: mínimo 1 dia com dados para comparação

Se V não tem dados: headline = "Sem dados de passos para esta semana."  
Se A não tem dados mas V tem: headline = "Sem semana anterior para comparar."

### Categorias e thresholds

| Categoria | Condição | Fonte do threshold |
|---|---|---|
| Positivo | delta > +10% | AHA: progresso mensurável em atividade semanal |
| Neutro | -10% ≤ delta ≤ +10% | AHA: manutenção de padrão de atividade |
| Negativo | delta < -10% | AHA: declínio relevante |

### Copy templates (PT-BR)

- **Positivo:** "Esta semana você caminhou **{delta_abs}% mais** que a anterior — {total_V_formatado} passos no total."
- **Neutro:** "Você manteve um ritmo similar à semana anterior — {total_V_formatado} passos esta semana."
- **Negativo:** "Esta semana você caminhou **{delta_abs}% menos** que a anterior — {total_V_formatado} passos no total."
- **Semana V sem dados:** "Sem dados de passos para esta semana."
- **Semana A sem dados (V tem dados):** "**{total_V_formatado} passos** registrados. Sem semana anterior para comparar."
- **Dados insuficientes (V com 0 dias):** "Sem dados de passos para esta semana."

**Para semanas passadas (navegação):** substituir "Esta semana" por "Nessa semana" e "anterior" por contexto relativo:
- **Positivo (navegação):** "Nessa semana você caminhou **{delta_abs}% mais** que a semana anterior — {total_V_formatado} passos."
- **Neutro (navegação):** "Nessa semana o ritmo foi similar à semana anterior — {total_V_formatado} passos."
- **Negativo (navegação):** "Nessa semana você caminhou **{delta_abs}% menos** que a semana anterior — {total_V_formatado} passos."

**Formatação:**
- `total_V_formatado`: inteiro com separador de milhar PT-BR (ex: `42.350`)
- `delta_abs`: inteiro, valor absoluto arredondado (ex: `18`)

### Edge cases do headline

| Cenário | Tratamento |
|---|---|
| Semana V sem nenhum dia com dados | "Sem dados de passos para esta semana." |
| Semana A sem nenhum dia com dados | Copy "Sem semana anterior para comparar" |
| Semana V incompleta (ex: hoje é quarta) | Comparar totais como estão — não normalizar por número de dias. Não adicionar "(semana incompleta)" no headline |
| Semana V tem apenas 1 dia de dados | Headline gerado normalmente — limiar mínimo é 1 dia |
| Delta > +200% | Cap no texto: "mais de 200% mais" |
| Delta < -80% | Cap no texto: "mais de 80% menos" |
| Todos os count = 0 na semana V | Tratar como sem dados |
| Semana A tem dados mas todos count = 0 | Tratar A como sem dados |

---

## 2. Média Móvel para o Gráfico

### Definição

A linha de média móvel é sobreposta ao gráfico de barras diárias para suavizar variações e mostrar tendência.

**Janela:** média móvel de **7 dias** centrada no ponto atual.  
**Cálculo:** para cada dia D no eixo X do gráfico, a média móvel é calculada como a média aritmética dos passos dos 7 dias centrados em D (D-3 a D+3), considerando apenas dias com dados disponíveis.

> **Por que 7 dias?** Reduz o ruído semanal (ex: finais de semana vs. dias úteis) e alinha com o ciclo de hábitos do usuário.

### Regra de cálculo

```
Para cada dia D na janela exibida:
  janela_mm = dias de [D-3 até D+3] que possuem StepsRecord com count > 0
  
  se len(janela_mm) >= 3:
    ponto_mm[D] = média(passos de cada dia em janela_mm)
  senão:
    ponto_mm[D] = null (gap — não exibir ponto)
```

**Mínimo de dias para calcular ponto:** 3 dias na janela de 7 (centrada). Isso garante que pontos nos extremos do gráfico (início/fim do histórico navegável) ainda sejam calculados se houver 3 dias disponíveis.

### Exibição de gaps

- Pontos com `null` não são renderizados
- A linha de média móvel é desenhada apenas entre pontos consecutivos não-nulos
- Se houver gap entre dois pontos válidos (ex: D+1 e D+4 têm valores, D+2 e D+3 são null), a linha é **interrompida** — não interpola entre pontos separados por mais de 1 gap

### Dados históricos para a média móvel

- Para calcular a média móvel dos primeiros dias da janela visualizada, o Android Engineer deve buscar dados de até 3 dias antes do início da janela
- Para calcular a média móvel dos últimos dias da janela, deve buscar até 3 dias após o fim da janela (para semanas passadas completas)
- Para a semana atual: os dias futuros (D+1 a D+3 de hoje) são ignorados — a janela de 7 centrada em hoje usa D-3 a hoje

### Exibição no gráfico

- Eixo X: dias da semana (Seg, Ter, Qua, Qui, Sex, Sáb, Dom) como rótulos abreviados
- Eixo Y: passos (escala dinâmica baseada no máximo do intervalo exibido + 20% de padding)
- Barra sem dados: altura zero, cor diferenciada (cinza claro) para indicar dia sem registro
- Linha de média móvel: traçada em cima das barras, cor distinta (ex: linha azul sobre barras verdes)
- Dia atual: barra destacada com borda ou cor diferente

---

## 3. Navegação para Semanas Anteriores

### Definição

O usuário pode navegar até **4 semanas atrás** a partir da semana atual. A semana mais antiga acessível é a 4ª semana anterior (V-4).

**Estrutura de navegação:**
```
← [Semana anterior]   [Semana atual / semana visualizada]   [Semana seguinte] →
```
- O botão "Semana seguinte" é desabilitado quando o usuário está na semana atual
- O botão "Semana anterior" é desabilitado quando o usuário está em V-4

**Rótulo do período exibido no header:**
- Semana atual (parcial): "Seg {DD/MM} – hoje"
- Semana atual (domingo — completa): "Seg {DD/MM} – Dom {DD/MM}"
- Semanas passadas completas: "Seg {DD/MM} – Dom {DD/MM}"

### Headline para semanas navegadas

O headline sempre compara a semana visualizada (V) com a semana imediatamente anterior a ela (A = V-1):

- Se o usuário navega para V-1: headline compara V-1 vs. V-2
- Se o usuário navega para V-2: headline compara V-2 vs. V-3
- Se o usuário navega para V-3: headline compara V-3 vs. V-4
- Se o usuário navega para V-4: headline compara V-4 vs. V-5 (mesmo que V-5 seja além da janela de navegação — os dados de V-5 devem ser buscados para o cálculo)

### Copy para estados especiais de semanas navegadas

| Estado | Copy |
|---|---|
| Semana visualizada sem dados | "Nenhum dado de passos registrado nessa semana." |
| Semana visualizada com dados parciais (1–6 dias) | Headline normal — não indicar "parcial" no headline |
| Semana anterior (A) sem dados | "**{total_V_formatado} passos** registrados. Sem semana anterior para comparar." |
| Semana visualizada = semana atual incompleta | Usar copy "Esta semana" (não "Nessa semana") |

### Edge cases de navegação

| Cenário | Tratamento |
|---|---|
| Usuário tenta navegar além de V-4 | Botão desabilitado — não permitir |
| Semana V-4 completamente sem dados | Exibir semana com todas as barras vazias + "Nenhum dado de passos registrado nessa semana." |
| Dados de V-5 necessários para headline de V-4 mas indisponíveis | Usar copy "Sem semana anterior para comparar" para V-4 |
| Usuário navega rapidamente entre semanas | Debounce de 300ms na navegação — não disparar múltiplas buscas simultâneas |
| Health Connect retorna erro para semana navegada | Exibir "Erro ao carregar dados. Tente novamente." com botão de retry |
