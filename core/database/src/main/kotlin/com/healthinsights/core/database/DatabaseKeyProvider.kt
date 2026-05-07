package com.healthinsights.core.database

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Generates and retrieves the SQLCipher passphrase.
 *
 * Security design:
 * - Passphrase is 32 random bytes, generated once on first launch via [SecureRandom].
 * - Passphrase is stored encrypted inside [EncryptedSharedPreferences], which uses a
 *   hardware-backed AES256-GCM [MasterKey] from the Android Keystore.
 * - The passphrase is NEVER logged or stored in plaintext.
 */
internal class DatabaseKeyProvider(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getOrCreatePassphrase(): ByteArray {
        val stored = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (stored != null) {
            return android.util.Base64.decode(stored, android.util.Base64.NO_WRAP)
        }

        val passphrase = ByteArray(PASSPHRASE_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, android.util.Base64.encodeToString(passphrase, android.util.Base64.NO_WRAP))
            .apply()
        return passphrase
    }

    private companion object {
        const val PREFS_NAME = "health_insights_db_key_prefs"
        const val KEY_DB_PASSPHRASE = "db_passphrase"
        const val PASSPHRASE_SIZE_BYTES = 32
    }
}
