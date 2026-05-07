---
name: Consent copy scope drift
description: consent-copy-v1.md cobre passos/sono/FC/exercício; produto agora foca calorias/peso. Requer nova versão do doc.
type: project
---

O documento `docs/legal/consent-copy-v1.md` está desalinhado com o produto após o pivot de 2026-05-05.

**Cobertura atual do v1:** Passos, Sono, Frequência Cardíaca, Exercício.
**Produto atual precisa:** Calorias gastas (TotalCaloriesBurned + ActiveCaloriesBurned), Calorias ingeridas (NutritionRecord), Peso (WeightRecord), Exercício (ExerciseSessionRecord, mantido).

**O que isto bloqueia:** o spec de onboarding referencia toggles para "Calorias", "Peso", "Treinos". O texto exato de cada toggle (descrição da finalidade, base legal) precisa estar no consent-copy-v1.1 antes da implementação.

**Why:** LGPD Art. 9 e Art. 11 exigem que a finalidade seja informada por tipo de dado tratado. Não posso reutilizar o texto de "Passos" para "Calorias" — finalidade é diferente (atividade vs. balanço calórico).

**How to apply:** escalar para CISO antes de finalizar copy da Tela 4 (Consentimento). Spec atual usa copy provisória marcada como "[CISO-COPY-PENDING]" para os tipos novos; copy final vem do consent-copy-v1.1.
