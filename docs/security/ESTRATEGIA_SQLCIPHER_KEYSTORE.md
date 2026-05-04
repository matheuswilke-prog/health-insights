# PRE-08 — Estratégia de Chave SQLCipher + Android Keystore

**Documento:** PRE-08  
**Autor:** Security Reviewer  
**Data:** 2026-05-03  
**Bloqueia:** EP-01-04 (SQLCipher + Room: configuração segura do banco)  
**Revisão necessária antes de:** qualquer commit no módulo `:core:database`

---

## 1. Estratégia Aprovada

**CONDICIONALMENTE APROVADO** — A combinação SQLCipher (net.zetetic:android-database-sqlcipher, Apache 2.0) + Android Keystore com AES-256-GCM é tecnicamente sólida para proteção de dados de saúde em repouso no contexto de um app 100% on-device. A aprovação está condicionada à implementação estrita dos padrões desta seção — qualquer desvio invalida este sign-off e exige nova revisão.

---

## 2. Arquitetura de Chave — Implementação Mandatória

### 2.1 API do Android Keystore a usar

Usar `KeyGenerator` (chave simétrica AES), não `KeyPairGenerator` (assimétrica RSA/EC).

Justificativa: SQLCipher usa criptografia simétrica internamente. Uma chave AES gerada pelo Keystore é suficiente para proteger a passphrase e evita a complexidade desnecessária de criptografia assimétrica.

**Provider obrigatório:** `AndroidKeyStore` (string exata — case-sensitive).

### 2.2 Algoritmo e parâmetros mandatórios

```
Algoritmo: AES
Modo: GCM
Padding: NoPadding
Tamanho da chave: 256 bits
IV (nonce): 12 bytes (96 bits) — padrão GCM
Tag de autenticação: 128 bits (padrão GCM)
```

**Por que GCM e não CBC:** GCM fornece autenticação integrada (AEAD). CBC sem MAC é vulnerável a ataques de padding oracle. Em SDK 26+ o GCM é garantidamente disponível no Keystore de hardware quando o dispositivo possui Strongbox ou TEE.

**Comportamento no Keystore por versão de Android relevante ao projeto (minSdk 26):**

| Android | API | Keystore TEE | Comportamento relevante |
|---------|-----|-------------|------------------------|
| 8.0–8.1 | 26–27 | Obrigatório se presente | GCM suportado; sem Strongbox (chegou no 9.0) |
| 9.0     | 28  | StrongBox disponível    | `setIsStrongBoxBacked(true)` opcional mas recomendado se disponível |
| 10+     | 29+ | StrongBox mais prevalente | `setIsStrongBoxBacked(true)` com fallback gracioso |
| 12+     | 31+ | Locked Down Mode        | Sem impacto para geração de chave, apenas para uso com biometria |

**Armadilha conhecida (API 26–27):** Em algumas implementações de fabricante no Android 8.x, operações GCM no Keystore de hardware podem falhar silenciosamente e cair para software. Sempre verificar `KeyInfo.isInsideSecureHardware()` em instrumentedTests.

### 2.3 Como derivar a passphrase do SQLCipher a partir da chave do Keystore

Este é o passo mais crítico. SQLCipher aceita `ByteArray` como passphrase via `SQLiteDatabase.openOrCreateDatabase(file, passphrase: ByteArray, ...)` — não aceita `SecretKey` diretamente.

**Fluxo mandatório em duas camadas:**

```
[Keystore AES-256-GCM key]  ← protegida no TEE, não exportável
         |
         | cifra/decifra
         v
[raw_key_material: ByteArray(32)]  ← gerado aleatoriamente, armazenado CIFRADO
         |
         | usado como
         v
[SQLCipher passphrase: ByteArray(32)]  ← passado ao SupportFactory, zerado da memória após uso
```

**Passo a passo:**

1. **Primeira execução:** Gerar 32 bytes aleatórios via `SecureRandom` — este é o `raw_key_material`.
2. Cifrar `raw_key_material` usando a chave do Keystore com AES/GCM/NoPadding: produz `(iv: ByteArray(12), ciphertext: ByteArray(32 + 16))`.
3. Armazenar `iv + ciphertext` em `EncryptedSharedPreferences` (ou arquivo privado do app) — **nunca em texto claro**.
4. **Execuções subsequentes:** Ler `iv + ciphertext` do armazenamento, decifrar com a chave do Keystore, obter `raw_key_material`.
5. Passar `raw_key_material` como passphrase ao `SupportFactory` do SQLCipher.
6. **Zerar `raw_key_material` da memória imediatamente** após passar ao `SupportFactory` — preencher o `ByteArray` com zeros.

**Por que não usar o output da chave Keystore diretamente como passphrase:** A chave Keystore não é exportável (`setKeyUsageRequirements` impede extração do material bruto via API pública). A indireção com `raw_key_material` cifrado é o padrão correto.

### 2.4 Onde e quando gerar a chave

**Geração da chave Keystore:** Lazy init na primeira abertura do banco — dentro do `EncryptedDatabaseFactory`. Verificar se a chave já existe antes de gerar (`keyStore.containsAlias(KEY_ALIAS)`). Se não existe, gerar.

**Geração do `raw_key_material`:** Simultaneamente à geração da chave Keystore, na primeira execução. Cifrar e armazenar imediatamente.

**Não usar eager init (Application.onCreate)** — atrasar até o ponto de uso minimiza a janela de tempo em que o material de chave existe na memória.

### 2.5 O que acontece se a chave do Keystore for deletada ou invalidada

**Causas de invalidação:**
- Usuário remove o PIN/padrão/biometria do dispositivo (se `setUserAuthenticationRequired(true)` — ver 2.6)
- Factory reset
- App desinstalado e reinstalado (chaves são apagadas)
- Usuário apaga dados do app

**Política mandatória (sem exceções):**

Se a chave Keystore não puder ser recuperada (`KeyPermanentlyInvalidatedException`, `KeyStoreException`, ou ausência de alias):

1. **Falhar ruidosamente** — lançar uma exceção customizada `DatabaseKeyUnavailableException` que sobe pela call stack.
2. **Não tentar abrir o banco sem chave** — um banco criptografado aberto com passphrase errada corrompe dados em algumas versões do SQLCipher.
3. **Não tentar recriar a chave silenciosamente** — isso resultaria em um banco inacessível com dados perdidos sem aviso ao usuário.
4. **Apresentar ao usuário** (via camada de UI — fora do escopo desta story) uma tela de erro explicando que os dados locais precisam ser excluídos e o onboarding refeito (equivalente ao "Direito ao Esquecimento" forçado por circunstância técnica).
5. **Log de auditoria:** registrar o evento de falha de chave com timestamp e tipo de erro — **sem nenhum material de chave no log**.

### 2.6 Uso de `setUserAuthenticationRequired` — DECISÃO: NÃO para MVP

**Recomendação: NÃO usar `setUserAuthenticationRequired(true)` no MVP.**

Justificativa:

1. O threat model definido pelo projeto é "exposição inadvertida via SDK terceiro, storage não criptografado, ou leak em logs" — não inclui acesso físico por adversário que desbloqueou o dispositivo.
2. `setUserAuthenticationRequired(true)` invalida a chave quando o usuário remove biometria/PIN, forçando perda de dados — experiência inaceitável para dados de saúde de longo prazo sem backend de backup.
3. A criptografia AES-256-GCM em repouso com chave no Keystore (TEE) já atende ao requisito de dados em repouso para o threat model atual.
4. `setUserAuthenticationRequired(true)` com `setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG or AUTH_DEVICE_CREDENTIAL)` pode ser adicionado em versão futura se o CISO elevar o threat model para incluir acesso físico.

**Se o CISO decidir habilitar em versão futura:** usar `setUserAuthenticationParameters(timeout = 0, types = KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)` — o `timeout = 0` exige autenticação a cada uso da chave (mais seguro que um timeout fixo). Requer tratamento de `UserNotAuthenticatedException` em toda operação de banco.

---

## 3. Código de Referência (Kotlin)

O Android Engineer deve seguir exatamente esta estrutura. As chamadas de API marcadas com `// CRITICO` não podem ser alteradas sem nova revisão de segurança.

```kotlin
// ────────────────────────────────────────────────
// Módulo: :core:database
// Arquivo: KeystoreHelper.kt
// ────────────────────────────────────────────────

private const val KEYSTORE_PROVIDER = "AndroidKeyStore" // CRITICO: string exata
private const val KEY_ALIAS = "health_insights_db_key"  // CRITICO: não alterar após produção
private const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
private const val KEY_SIZE = 256                         // CRITICO: 256 bits
private const val GCM_IV_LENGTH = 12                     // CRITICO: 96 bits para GCM
private const val GCM_TAG_LENGTH = 128                   // CRITICO: bits, não bytes
private const val RAW_KEY_LENGTH = 32                    // bytes (256 bits)

object KeystoreHelper {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    }

    /**
     * Retorna a SecretKey existente ou gera uma nova.
     * CRITICO: nunca exportar o material da chave — a chave fica no TEE.
     */
    fun getOrCreateKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            generateKey()
        }
    }

    private fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_PROVIDER) // CRITICO: provider
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_SIZE)                           // CRITICO
            .setBlockModes(BLOCK_MODE)                      // CRITICO: GCM
            .setEncryptionPaddings(PADDING)                 // CRITICO: NoPadding
            .setRandomizedEncryptionRequired(true)          // CRITICO: IV gerado pelo Keystore
            // NÃO usar setUserAuthenticationRequired(true) no MVP — ver seção 2.6
            .build()
        keyGen.init(spec)
        return keyGen.generateKey()
    }

    /**
     * Cifra dados com a chave do Keystore.
     * Retorna iv + ciphertext como um único ByteArray.
     * CRITICO: IV é gerado pelo Keystore (setRandomizedEncryptionRequired = true).
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("$KEY_ALGORITHM/$BLOCK_MODE/$PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv                                  // IV gerado pelo Keystore
        check(iv.size == GCM_IV_LENGTH) { "IV size mismatch: ${iv.size}" }
        val ciphertext = cipher.doFinal(plaintext)
        // Retornar iv || ciphertext para armazenamento
        return iv + ciphertext
    }

    /**
     * Decifra dados cifrados por encrypt().
     * Lança KeyPermanentlyInvalidatedException se a chave foi invalidada.
     * CRITICO: não capturar essa exceção aqui — deixar subir para tratamento na Factory.
     */
    fun decrypt(ivAndCiphertext: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val iv = ivAndCiphertext.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = ivAndCiphertext.copyOfRange(GCM_IV_LENGTH, ivAndCiphertext.size)
        val cipher = Cipher.getInstance("$KEY_ALGORITHM/$BLOCK_MODE/$PADDING")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(ciphertext)
    }
}

// ────────────────────────────────────────────────
// Arquivo: RawKeyManager.kt
// Responsável por gerenciar o raw_key_material
// ────────────────────────────────────────────────

class RawKeyManager @Inject constructor(
    private val encryptedPrefs: SharedPreferences // CRITICO: EncryptedSharedPreferences
) {
    companion object {
        private const val PREF_KEY = "db_raw_key_enc"
    }

    /**
     * Retorna o raw_key_material (32 bytes) para uso como passphrase SQLCipher.
     * CRITICO: o caller DEVE zerar o ByteArray retornado após uso.
     *
     * @throws DatabaseKeyUnavailableException se a chave Keystore foi invalidada
     */
    fun getOrCreateRawKey(): ByteArray {
        val stored = encryptedPrefs.getString(PREF_KEY, null)
        return if (stored != null) {
            // Chave já existe — decifrar
            val ivAndCiphertext = Base64.decode(stored, Base64.NO_WRAP)
            try {
                KeystoreHelper.decrypt(ivAndCiphertext)
            } catch (e: KeyPermanentlyInvalidatedException) {
                // CRITICO: não silenciar — relançar como exceção de domínio
                throw DatabaseKeyUnavailableException("Keystore key permanently invalidated", e)
            } catch (e: Exception) {
                throw DatabaseKeyUnavailableException("Failed to decrypt raw key", e)
            }
        } else {
            // Primeira execução — gerar raw_key_material
            val rawKey = ByteArray(RAW_KEY_LENGTH).also {
                SecureRandom().nextBytes(it) // CRITICO: SecureRandom, nunca Random
            }
            val encrypted = KeystoreHelper.encrypt(rawKey)
            encryptedPrefs.edit()
                .putString(PREF_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .apply()
            rawKey
            // Nota: rawKey NÃO é zerado aqui pois será retornado ao caller.
            // O caller (EncryptedDatabaseFactory) é responsável por zerá-lo.
        }
    }
}

// ────────────────────────────────────────────────
// Arquivo: EncryptedDatabaseFactory.kt
// ────────────────────────────────────────────────

class EncryptedDatabaseFactory @Inject constructor(
    private val rawKeyManager: RawKeyManager
) {

    /**
     * Cria o SupportSQLiteOpenHelper.Factory para uso no Room.
     * CRITICO: passphrase zerada da memória após abertura.
     */
    fun create(): SupportSQLiteOpenHelper.Factory {
        var passphrase: ByteArray? = null
        return try {
            passphrase = rawKeyManager.getOrCreateRawKey() // CRITICO: ByteArray(32)
            // CRITICO: SupportFactory do SQLCipher recebe o ByteArray diretamente
            SupportFactory(passphrase)
            // Nota: SupportFactory copia internamente o conteúdo do ByteArray.
            // Zeramos imediatamente após a construção.
        } catch (e: DatabaseKeyUnavailableException) {
            // CRITICO: falha ruidosa — não abrir banco sem chave válida
            throw e
        } finally {
            // CRITICO: zerar passphrase da memória em qualquer caso (sucesso ou falha)
            passphrase?.fill(0)
        }
    }
}

// ────────────────────────────────────────────────
// Arquivo: DatabaseKeyUnavailableException.kt
// ────────────────────────────────────────────────

class DatabaseKeyUnavailableException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// ────────────────────────────────────────────────
// Arquivo: AppDatabase.kt (Room database)
// ────────────────────────────────────────────────

@Database(
    entities = [/* DAOs aqui */],
    version = 1,
    exportSchema = true // CRITICO: manter para rastreabilidade de migrações
)
abstract class AppDatabase : RoomDatabase() {
    // DAOs aqui

    companion object {
        fun create(
            context: Context,
            factory: SupportSQLiteOpenHelper.Factory
        ): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "health_insights.db"
            )
                .openHelperFactory(factory) // CRITICO: sem isso, Room usa SQLite sem criptografia
                .fallbackToDestructiveMigration(false) // CRITICO: false — não destruir dados silenciosamente
                .build()
        }
    }
}

// ────────────────────────────────────────────────
// Arquivo: DatabaseModule.kt (Hilt)
// ────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): SharedPreferences {
        // CRITICO: EncryptedSharedPreferences para armazenar o raw_key cifrado
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "db_key_prefs",             // nome do arquivo — não alterar após produção
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        factory: EncryptedDatabaseFactory
    ): AppDatabase {
        return AppDatabase.create(context, factory.create())
    }
}
```

---

## 4. Padrões Proibidos (Lista Explícita)

O Android Engineer **absolutamente NÃO pode** fazer nenhum dos itens abaixo. Qualquer PR que contenha um destes padrões será bloqueado:

### 4.1 Relacionados à chave e passphrase

- **PROIBIDO:** Hardcodar a passphrase como string ou ByteArray literal no código-fonte.
  ```kotlin
  // PROIBIDO — exemplo do que NÃO fazer:
  val passphrase = "minha_senha_secreta".toByteArray()
  val passphrase = byteArrayOf(1, 2, 3, 4, 5 /* ... */)
  ```

- **PROIBIDO:** Armazenar a passphrase ou o `raw_key_material` em `SharedPreferences` sem criptografia (sem `EncryptedSharedPreferences`).

- **PROIBIDO:** Armazenar a passphrase ou o `raw_key_material` em qualquer arquivo de texto, XML, JSON ou banco de dados sem criptografia.

- **PROIBIDO:** Usar `Random` (não criptograficamente seguro) para gerar `raw_key_material`. Somente `SecureRandom`.

- **PROIBIDO:** Derivar a passphrase de dados previsíveis: `deviceId`, `ANDROID_ID`, `Build.SERIAL`, `packageName`, timestamp de instalação, ou qualquer combinação destes.

- **PROIBIDO:** Não zerar o `ByteArray` da passphrase após uso — o `fill(0)` no bloco `finally` é mandatório.

- **PROIBIDO:** Gerar a chave Keystore com algoritmo diferente de AES-256-GCM. Em particular, proibido AES-128, AES/CBC, RSA como chave direta para o banco.

- **PROIBIDO:** Usar `setRandomizedEncryptionRequired(false)` — isso permitiria reutilização de IV, quebrando as garantias do GCM.

### 4.2 Relacionados ao armazenamento e acesso

- **PROIBIDO:** Abrir o banco Room sem `openHelperFactory(factory)` — sem isso, Room usa SQLite sem criptografia, e dados de saúde ficam expostos.

- **PROIBIDO:** Usar `fallbackToDestructiveMigration(true)` sem autorização explícita do Security Reviewer — pode destruir dados sem aviso.

- **PROIBIDO:** Criar banco de dados em memória (`inMemoryDatabaseBuilder`) em código de produção. Apenas em testes instrumentados.

- **PROIBIDO:** Exportar o banco de dados para locais acessíveis externamente (`/sdcard/`, `Environment.DIRECTORY_DOWNLOADS`, `FileProvider` público) sem consentimento explícito e criptografia de transporte.

### 4.3 Relacionados a logs e debug

- **PROIBIDO:** Logar qualquer valor de passphrase, `raw_key_material`, ou output de `KeystoreHelper.encrypt/decrypt` — nem em `BuildConfig.DEBUG`.

- **PROIBIDO:** Logar campos de dados de saúde (passos, sono, FC) em qualquer nível de log (`Log.d`, `Log.v`, `Timber.d`, etc.).

- **PROIBIDO:** Incluir `android:debuggable="true"` hardcodado no `AndroidManifest.xml` — deve ser controlado pelo build type.

- **PROIBIDO:** Incluir a passphrase ou material de chave em relatórios de crash (Firebase Crashlytics custom keys/values).

### 4.4 Relacionados à arquitetura

- **PROIBIDO:** Instanciar `EncryptedDatabaseFactory` fora do módulo Hilt — deve ser singleton gerenciado pelo container de DI.

- **PROIBIDO:** Expor `AppDatabase` como dependência direta em módulos de feature — apenas os DAOs devem ser injetados.

- **PROIBIDO:** Silenciar `DatabaseKeyUnavailableException` com um bloco `catch` vazio ou com fallback para banco sem criptografia.
  ```kotlin
  // PROIBIDO — exemplo do que NÃO fazer:
  try {
      factory.create()
  } catch (e: DatabaseKeyUnavailableException) {
      // abrir banco sem criptografia como fallback
      Room.databaseBuilder(context, AppDatabase::class.java, "health_insights.db").build()
  }
  ```

### 4.5 Relacionados a dependências

- **PROIBIDO:** Usar SQLCipher Enterprise (net.zetetic:sqlcipher-android pago) sem avaliação de licença pelo CFO.
- **PROIBIDO:** Introduzir qualquer SDK de analytics, ads ou third-party SDK com acesso ao contexto do banco sem revisão prévia do CISO.
- **PROIBIDO:** Usar versões SNAPSHOT ou RC de SQLCipher em produção — apenas releases estáveis pinados.

---

## 5. Testes de Segurança Mandatórios para EP-01-04

Todos os testes abaixo devem passar antes do sign-off final do Security Reviewer em EP-01-04.

### Testes Instrumentados (Android Instrumented Tests — requerem dispositivo/emulador)

**SEC-TEST-01: Banco inacessível sem SQLCipher**
- Criar o banco com SQLCipher e escrever um registro.
- Tentar abrir o arquivo `.db` com `android.database.sqlite.SQLiteDatabase.openDatabase()` (SQLite puro, sem passphrase).
- Resultado esperado: abertura falha com `SQLiteDatabaseCorruptException` ou arquivo não abre (header inválido para SQLite puro). O registro não pode ser lido.
- Verificar também via `file` command no emulador que os primeiros bytes do arquivo são `53 51 4C 69 74 65 20 66` (SQLite magic) — NÃO devem ser esses bytes se o SQLCipher está ativo. O header deve iniciar com dados cifrados.

**SEC-TEST-02: Banco inacessível com passphrase incorreta**
- Criar o banco com a passphrase correta e escrever um registro.
- Tentar abrir o mesmo arquivo com `SupportFactory(byteArrayOf(0, 1, 2))` (passphrase errada).
- Resultado esperado: lança exceção específica do SQLCipher (`net.sqlcipher.database.SQLiteException` com mensagem indicando falha de decriptografia). O registro não pode ser lido.
- O teste NÃO deve capturar a exceção silenciosamente — ela deve ser verificada com `assertThrows`.

**SEC-TEST-03: Comportamento quando Keystore indisponível (chave ausente)**
- Criar o banco normalmente.
- Deletar o alias do Keystore: `KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(KEY_ALIAS) }`.
- Tentar abrir o banco novamente via `EncryptedDatabaseFactory.create()`.
- Resultado esperado: `DatabaseKeyUnavailableException` é lançada. O banco não é aberto. Nenhum dado é gravado ou lido.

**SEC-TEST-04: Passphrase zerada da memória após uso**
- Instrumentar `EncryptedDatabaseFactory.create()` para capturar a referência ao `ByteArray` da passphrase antes do retorno.
- Após o retorno do `create()`, verificar que todos os bytes do `ByteArray` são `0x00`.
- Resultado esperado: `passphrase.all { it == 0.toByte() }` retorna `true`.

**SEC-TEST-05: Persistência da chave entre instâncias da factory**
- Criar o banco, escrever um registro e fechar o banco.
- Recriar `EncryptedDatabaseFactory` (simular reinício do processo via novo `Hilt` component).
- Abrir o banco com a nova factory.
- Resultado esperado: banco abre com sucesso e o registro gravado anteriormente é legível. A chave persiste corretamente no Keystore e o `raw_key_material` cifrado é recuperado.

**SEC-TEST-06: Nenhum dado de saúde em Logcat**
- Habilitar captura de logs durante gravação e leitura de um registro com campos de saúde (steps, sleepDuration, heartRate).
- Buscar no output de Logcat pelos valores dos campos gravados.
- Resultado esperado: nenhuma linha de log contém os valores dos campos de saúde.

**SEC-TEST-07: Integridade do banco após gravação (autenticação GCM)**
- Criar o banco e gravar dados.
- Corromper 1 byte no arquivo `.db` em offset aleatório fora do header.
- Tentar ler os dados.
- Resultado esperado: falha de leitura detectada (SQLCipher verifica a tag GCM). O banco não retorna dados corrompidos silenciosamente.

### Testes Unitários (JVM — sem dispositivo)

**SEC-TEST-08: KeystoreHelper — algoritmo correto**
- Em um `InstrumentedTest`, chamar `getOrCreateKey()` e verificar via `KeyInfo` que o algoritmo é AES-256, modo GCM, sem padding, e `isInsideSecureHardware()` em dispositivos com TEE.

**SEC-TEST-09: RawKeyManager — não armazena em texto claro**
- Após chamar `getOrCreateRawKey()`, ler o conteúdo bruto das `EncryptedSharedPreferences`.
- O valor armazenado deve ser Base64 de dados cifrados — verificar que o tamanho é `12 + 32 + 16 = 60 bytes` (IV + ciphertext + GCM tag) antes de Base64-encode.
- Verificar que o valor armazenado em texto não contém o `raw_key_material` em texto claro (comparação direta dos 32 bytes originais com o conteúdo da preferência).

**SEC-TEST-10: DatabaseKeyUnavailableException não é silenciada**
- Usar um mock de `RawKeyManager` que lança `DatabaseKeyUnavailableException`.
- Chamar `EncryptedDatabaseFactory.create()`.
- Verificar com `assertThrows<DatabaseKeyUnavailableException>` que a exceção não é capturada internamente.

---

## 6. Dependências a Adicionar no `libs.versions.toml`

```toml
[versions]
# SQLCipher — versão estável mais recente com suporte a Room SupportFactory
# CRITICO: pinar versão exata. Não usar "+" ou ranges.
sqlcipher = "4.5.7"

# Room — versão compatível com SQLCipher 4.5.x via SupportSQLiteOpenHelper
# SQLCipher 4.5.x implementa a API SupportSQLiteOpenHelper do Room 2.x
room = "2.6.1"

# Jetpack Security — para EncryptedSharedPreferences (armazenamento do raw_key cifrado)
# MasterKey.Builder disponível a partir de 1.1.0-alpha06; usar 1.1.0 stable
androidx-security-crypto = "1.1.0-alpha06"
# Nota: security-crypto 1.1.0 stable não foi lançado até a data deste documento.
# Usar 1.1.0-alpha06 é necessário para EncryptedSharedPreferences com API 26+.
# Monitorar releases em: https://developer.android.com/jetpack/androidx/releases/security

[libraries]
# SQLCipher para Android (licença Apache 2.0 — CONFIRMADA para uso em app comercial)
sqlcipher-android = { group = "net.zetetic", name = "android-database-sqlcipher", version.ref = "sqlcipher" }

# Room — runtime, ktx e compiler
room-runtime    = { group = "androidx.room", name = "room-runtime",    version.ref = "room" }
room-ktx        = { group = "androidx.room", name = "room-ktx",        version.ref = "room" }
room-compiler   = { group = "androidx.room", name = "room-compiler",   version.ref = "room" }

# Room testing — para InstrumentedTests com banco em memória criptografado
room-testing    = { group = "androidx.room", name = "room-testing",    version.ref = "room" }

# Jetpack Security Crypto — para EncryptedSharedPreferences
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "androidx-security-crypto" }

[plugins]
# Sem novos plugins necessários para esta story.
# ksp já deve estar configurado para Room (via EP-01-01).
```

**Notas de compatibilidade:**

- SQLCipher 4.5.x implementa `SupportSQLiteOpenHelper.Factory` via `net.sqlcipher.database.SupportFactory`, compatível com Room 2.x. Não é necessária nenhuma extensão adicional.
- SQLCipher 4.x usa SQLite 3.x internamente (verificar via `SQLiteDatabase.SQLITE_VERSION_INT` nos testes).
- A dependência `androidx.sqlite:sqlite` não precisa ser adicionada separadamente — Room já a inclui transitivamente.
- **Não adicionar** `net.zetetic:sqlcipher-android` (versão Enterprise/paga) — usar apenas `net.zetetic:android-database-sqlcipher` (open-source, Apache 2.0).

---

## 7. Checklist de Aprovação

Este checklist deve ser preenchido pelo Android Engineer no PR de EP-01-04 e verificado pelo Security Reviewer antes do merge.

- [ ] Chave gerada via Android Keystore com provider `"AndroidKeyStore"` (não hardcodada, não derivada de dados do dispositivo)
- [ ] Algoritmo AES-256 confirmado: `KeyProperties.KEY_ALGORITHM_AES`, `setKeySize(256)`, `setBlockModes(KeyProperties.BLOCK_MODE_GCM)`, `setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)`
- [ ] Passphrase (`raw_key_material`) não armazenada em texto claro em nenhum momento — sempre cifrada com a chave Keystore e armazenada em `EncryptedSharedPreferences`
- [ ] Passphrase zerada da memória (`passphrase.fill(0)`) em bloco `finally` após passar ao `SupportFactory`
- [ ] Fallback para chave inválida definido como falha ruidosa: `DatabaseKeyUnavailableException` lançada e não capturada silenciosamente; nenhum fallback para banco sem criptografia
- [ ] `setRandomizedEncryptionRequired(true)` ativo (padrão do Keystore — verificar que não foi desabilitado)
- [ ] `setUserAuthenticationRequired` NÃO utilizado no MVP (conforme decisão seção 2.6)
- [ ] `openHelperFactory(factory)` presente na configuração do Room — verificar que não existe nenhuma instância de `Room.databaseBuilder` sem esta chamada
- [ ] Todos os 10 testes de segurança (SEC-TEST-01 a SEC-TEST-10) implementados e passando
- [ ] Nenhum dado de saúde em Logcat (SEC-TEST-06 verde)
- [ ] Licença do SQLCipher verificada: `net.zetetic:android-database-sqlcipher` é Apache 2.0 (CONFIRMADO — ver repositório oficial github.com/sqlcipher/android-database-sqlcipher)
- [ ] Nenhuma dependência da lista proibida (seção 4) introduzida no PR
- [ ] Versões de dependências pinadas (sem `+` ou ranges) no `libs.versions.toml`
- [ ] `exportSchema = true` no `@Database` para rastreabilidade de migrações
- [ ] Sign-off do Security Reviewer registrado como comentário de aprovação no PR antes do merge

---

**Assinatura:** Security Reviewer — PRE-08 emitido em 2026-05-03.  
**Validade:** Este documento cobre a implementação do EP-01-04. Qualquer alteração na estratégia de chave (algoritmo, armazenamento, policy de autenticação) após o merge inicial requer nova revisão.
