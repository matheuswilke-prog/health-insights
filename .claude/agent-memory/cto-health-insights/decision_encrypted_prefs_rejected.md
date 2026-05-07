---
name: EncryptedSharedPreferences/EncryptedDataStore rejeitados para dado regulado
description: Por que androidx.security:security-crypto NÃO é usado como destino primário de dado de saúde
type: project
---

**Decisão (2026-05-05):** `androidx.security:security-crypto` (EncryptedSharedPreferences / EncryptedFile) é aprovado APENAS como wrapper para guardar a passphrase do SQLCipher, derivada do MasterKey do Android Keystore. NÃO é destino primário para dado de saúde, derivado biométrico ou registro de consentimento.

**Why:** A biblioteca está em status maintenance-only desde 2024 — Google recomenda migração para Tink, que ainda não tem API estável first-party para Android. EncryptedDataStore não é produto first-party do AndroidX (apenas comunidade). Ancorar dado regulado em lib em fim de vida cria dívida regulatória (auditoria LGPD pode questionar). Room+SQLCipher (`net.zetetic:sqlcipher-android`) tem manutenção ativa e é o padrão de mercado para SQLite cifrado em Android.

**How to apply:** Se um futuro requisito surgir (ex.: token OAuth, secret de API, chave derivada), `EncryptedSharedPreferences` é aceitável como cofre desses secrets — mas dado de saúde, derivados biométricos e registro de consentimento vão sempre para Room+SQLCipher.
