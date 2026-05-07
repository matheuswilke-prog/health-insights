---
name: Storage de daily_calorie_target e dados derivados de biometria
description: Decisão CTO de 2026-05-05 — derivados biométricos vão em Room+SQLCipher; DataStore plain só para flags não-reguladas
type: project
---

**Decisão (2026-05-05):** `daily_calorie_target` e qualquer derivado biométrico (TMB, gasto médio, modificador de objetivo) persistem em Room+SQLCipher na tabela `user_profile` (singleton row id=1) dentro de `:core:database`. DataStore plain reservado a flags não-sensíveis: `onboarding_complete`, `last_completed_step`, tema, idioma.

**Why:** CISO vetou DataStore não criptografado para `daily_calorie_target` por ser derivado de TMB (idade/peso/altura/sexo) + gasto calórico — dado de saúde sob LGPD Art. 11. O `consent-copy-v1.1` (2026-05-05) afirma "Tudo é processado e guardado só aqui no seu celular, com criptografia" — DataStore plain contradiria a copy. `EncryptedSharedPreferences`/`androidx.security:security-crypto` está em maintenance-only desde 2024 (Google recomenda migração) — não ancorar dado regulado em lib em fim de vida.

**How to apply:** Toda futura proposta de persistir métrica derivada de dado biométrico (TMB, balanço calórico, peso médio, frequência média, etc.) deve ir para a mesma `HealthInsightsDatabase` (SQLCipher). 1 chave mestra (Android Keystore via MasterKey), 1 ponto de export/delete para LGPD Art. 18, 1 política de retenção de 12 meses. Dependências aprovadas no `libs.versions.toml`: `androidx.room:*` 2.7.0, `net.zetetic:sqlcipher-android` 4.6.1, `androidx.sqlite:sqlite-ktx` 2.4.0, `androidx.security:security-crypto` 1.1.0-alpha06 (apenas para wrapper de MasterKey, não para guardar dado de saúde).

**Pendência:** spec `docs/specs/onboarding-spec-v1.0.md` linha 220 ainda diz "persiste `daily_calorie_target` em DataStore" — pedir ao CPO para corrigir para Room.
