---
name: Navigation graph do onboarding
description: Decisão CTO de 2026-05-05 — onboarding é nested graph, popUpTo no graph route, retomada via DataStore last_completed_step
type: project
---

**Decisão (2026-05-05):** Onboarding implementado como nested graph dedicado em `:app` (`route = "onboarding"`, startDestination dinâmico). NavHost raiz resolve startDestination com `produceState` lendo DataStore antes de instanciar — evita flicker. T6 → Dashboard usa `popUpTo("onboarding") { inclusive = true }`. Retomada após app-kill via `last_completed_step` em DataStore (não-regulado).

**Why:** Nested graph é a única forma idiomática de `popUpTo` atômico em N telas — flat exigiria enumerar destinos e quebraria ao reordenar. `produceState` antes do NavHost atende o critério "sem flicker" da seção 8 do `onboarding-spec-v1.0.md`. `last_completed_step` no DataStore plain é aceitável (CISO ok) por não ser dado biométrico — apenas estado de UI.

**How to apply:** Para qualquer fluxo multi-tela com saída final que limpa back-stack (paywall futuro, re-onboarding pós-update, fluxo de exclusão LGPD), reusar o padrão: nested graph com route próprio + popUpTo no route do graph (não no startDestination). Cada ViewModel de tela escreve `markStepCompleted("X")` ANTES de `nav.navigate(...)` — fonte da verdade fica no destino. T5 e T6 obrigatoriamente com `BackHandler(enabled = true) { /* no-op */ }`. Dependências adicionadas: `androidx.navigation:navigation-compose` 2.8.5, `androidx.datastore:datastore-preferences` 1.1.1.

**Não fazer:** `clearBackStack()` (quebra navegação posterior); `popUpTo(Routes.WELCOME)` (frágil ao reordenar); enumerar destinos no popUpTo.
