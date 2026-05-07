# Health Insights — Visual System v1

> Sistema visual canônico para todas as telas. **Esta é a fonte da verdade** para cor, tipografia, espaçamento e princípios. Specs textuais (`docs/specs/*`) descrevem **o quê**; este documento descreve **como deve parecer**.
>
> Versão: 1.0 — 2026-05-06 — alinhado com WelcomeScreen reescrita pós-revisão de design.

---

## 1. Princípios (anti-slop)

Toda tela deve passar por estes 5 testes antes de virar PR:

1. **O número primeiro.** Headlines liderados por dados (ex: "−482 kcal"), não por adjetivos vagos ou perguntas retóricas.
2. **Sinal explícito.** Mostrar sempre +/− e a cor semântica (déficit/superávit/manter). Nunca depender só de cor (acessibilidade).
3. **Branco respira.** Fundo claro neutro `#FAFAF7`. **Nunca** gradientes saturados, círculos decorativos roxos, halos coloridos atrás de texto.
4. **Privacidade visível.** Microcopy "no aparelho" + ícone de escudo, fixo acima do CTA primário em todas as telas críticas (Welcome, Consent, Settings).
5. **Sem placeholder slop.** Sem ícones decorativos genéricos, sem emojis, sem "HI" em círculo. Use o glyph oficial (gráfico crescente) ou uma stripe de placeholder explícita.

---

## 2. Paleta

| Token | Valor | Uso |
|---|---|---|
| `bg` | `#FAFAF7` | Background base |
| `bg-elev` | `#FFFFFF` | Surface elevada (cards) |
| `bg-sunken` | `#F2F1EC` | Áreas afundadas, chips |
| `ink-1` | `#0E1116` | Texto primário |
| `ink-2` | `#3A3F47` | Texto secundário |
| `ink-3` | `#6B7079` | Texto terciário/captions |
| `ink-4` | `#9AA0A8` | Disabled / placeholder |
| `hairline` | `rgba(14,17,22,0.08)` | Bordas sutis |
| `brand` | `oklch(0.62 0.12 155)` (~ `#6FA47A`) | Sage/saúde — CTA secundário, headline accent |
| `brand-tint` | `oklch(0.96 0.04 155)` | Backgrounds suaves brand |
| **Semânticas** | | |
| `deficit` | `oklch(0.62 0.12 155)` (verde sage) | Emagrecendo / abaixo da meta |
| `surplus` | `oklch(0.68 0.14 35)` (coral) | Ganhando / acima da meta |
| `maintain` | `oklch(0.62 0.06 250)` (azul sutil) | Mantendo |

**Banidos no MVP:** roxo `#4F3D8A`, gradientes verticais saturados, círculos decorativos coloridos como fundo.

---

## 3. Tipografia

| Estilo | Família | Tamanho / Peso / Tracking | Uso |
|---|---|---|---|
| `displayLarge` | Inter Tight | 44 / 600 / -3.5% | Hero number ("−482 kcal") |
| `displayMedium` | Inter Tight | 34 / 600 / -2.5% | Welcome headline |
| `headlineMedium` | Inter Tight | 26 / 600 / -2% | Section title |
| `titleMedium` | Inter | 16 / 600 / -1% | Card title |
| `bodyLarge` | Inter | 16 / 500 | Texto principal |
| `bodyMedium` | Inter | 14 / 400 | Texto suporte |
| `labelSmall` | JetBrains Mono | 11 / 500 / +6% upper | Labels/captions, dados tabulares |

**Crítico:** números sempre com `font-feature-settings: 'tnum'` (tabular nums) — alinhamento vertical em listas e charts.

Em Compose: `Modifier.fontVariationSettings(...)` ou `androidx.compose.ui.text.PlatformTextStyle` com `includeFontPadding=false`.

---

## 4. Forma e espaço

| Token | Valor |
|---|---|
| `r-sm` | 10 dp |
| `r-md` | 16 dp (botões) |
| `r-lg` | 22 dp (cards) |
| `r-xl` | 28 dp (sheets) |
| `r-pill` | 999 (chips) |
| Padding tela | 24 dp horizontal |
| Padding card | 20 dp |
| Touch target mínimo | 56 dp |

---

## 5. Componentes-chave

### Botão primário
- Altura: 56 dp · radius 16 dp · cor: `ink-1` sólido (preto), texto branco · peso 600.
- Estado disabled: `ink-4` background.
- **Não** usar `brand` como cor primária do botão na Welcome — `ink-1` tem mais peso visual e é mais consistente com Apple Health.

### Card de dado (hero)
```
┌──────────────────────────┐
│ ● Hoje · prévia          │  ← row label (ink-3, 13sp)
│                          │
│ −482 kcal                │  ← display 44/600 tnum
│ Você está em déficit.    │  ← bodyLarge, "déficit" colorido
│                          │
│ ▁▂▃▅▃▆▄                  │  ← mini bar chart 7 dias
│ S T Q Q S S D            │
└──────────────────────────┘
```

### Microcopy de privacidade
`🛡 Seus dados ficam no aparelho. Sem nuvem, sem conta.`
Sempre acima do CTA primário em telas de coleta/consentimento.

---

## 6. Mapeamento → Compose

```kotlin
// core/ui/src/main/kotlin/com/healthinsights/core/ui/Theme.kt

val LightColors = lightColorScheme(
    background = Color(0xFFFAFAF7),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0E1116),
    onSurface = Color(0xFF0E1116),
    onSurfaceVariant = Color(0xFF3A3F47),
    primary = Color(0xFF0E1116),       // ink-1 — botão primário
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6FA47A),     // brand sage
    onSecondary = Color(0xFFFFFFFF),
    outline = Color(0x140E1116),       // hairline
)

object SemanticColors {
    val deficit  = Color(0xFF6FA47A)  // verde sage
    val surplus  = Color(0xFFD68B6A)  // coral
    val maintain = Color(0xFF6F8AB5)  // azul muted
}

val Shapes = Shapes(
    small  = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large  = RoundedCornerShape(22.dp),
)
```

---

## 7. Telas de referência

Mockups HTML lado a lado: ver projeto Anthropic Designer "health-insights" — `Health Insights.html` no canvas. Inclui 3 variações de Welcome e 3 de conexão Samsung Health, todas validadas neste sistema.

**Direção escolhida (default para implementação):** *a definir pelo CPO/CEO.* As variantes são:

- **A — Apple Health minimal**: prévia de saldo + bar chart 7 dias.
- **B — Editorial**: pergunta direta + lista de promessas + brand CTA.
- **C — Card-stack**: metáfora visual de fontes que se cruzam.

---

## 8. Ajustes obrigatórios na WelcomeScreen.kt atual

A versão em `feature/onboarding/.../WelcomeScreen.kt` (gradiente roxo + "HI" em círculo + curva inferior) está **fora do sistema** e deve ser substituída. Itens a remover:

1. `heroGradient` roxo — substituir por `MaterialTheme.colorScheme.background`.
2. `CurvedTopShape` — não há curva no novo sistema; transição é uma hairline horizontal.
3. Logo "HI" em círculo translúcido — usar glyph SVG (linha gráfico crescente) + wordmark "Health Insights".
4. Subhead "Veja seu saldo calórico real..." — mover headline para cima e usar a copy do spec (`docs/specs/onboarding-spec-v1.0.md` § 2): "Saiba se está em déficit ou superávit calórico."
5. CTA "Ver meu saldo" → "Começar" (alinha com spec § 2).

---

## 9. Fora do escopo do sistema v1

- Modo escuro: tokens existem mas tema escuro não é prioridade do MVP. Aguardar v1.1.
- Animações: sem Lottie, sem motion graphics. Apenas transições Material3 padrão.
- Ilustrações: zero. Só dado, gráfico ou stripe de placeholder.
