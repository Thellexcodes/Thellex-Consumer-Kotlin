package com.thellex.pay.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thellex.pay.data.model.BaseSettingsCache
import com.thellex.pay.data.model.ChainInfoDto
import com.thellex.pay.data.model.DepositTokenDto
import com.thellex.pay.data.model.TokenInfo
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG_BASE_SETTINGS_DS = "BaseSettingsDataStore"

private val Context.baseSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "base_settings"
)

private object Keys {
    val LAST_FETCH_TIMESTAMP = longPreferencesKey("last_fetch_timestamp")
    val CHAINS_JSON = stringPreferencesKey("supported_chains_json")
    val TOKENS_JSON = stringPreferencesKey("supported_tokens_json")
}

/* ─────────────────────────────────────────────
   SAVE BASE SETTINGS
   ───────────────────────────────────────────── */

suspend fun Context.saveBaseSettings(
    chains: List<ChainInfoDto>,
    tokens: List<DepositTokenDto>,
    timestamp: Long = System.currentTimeMillis()
) {
    baseSettingsDataStore.edit { prefs ->

        /* ───── Serialize chains ───── */

        val serializedChains = chains
            .filter { it.id != null }
            .joinToString("|") { chain ->

                val tokensSerialized = chain.supportedTokens.joinToString(",") { token ->
                    listOf(
                        token.symbol.name,
                        token.name,
                        token.decimals.toString(),
                        token.iconDisplay
                    ).joinToString("~")
                }

                listOf(
                    chain.id.name,
                    chain.displayName,
                    chain.fee.toString(),
                    chain.minimumWithdrawal.toString(),
                    chain.arrivalTime,
                    chain.iconUrl,
                    tokensSerialized
                ).joinToString(";")
            }

        /* ───── Serialize deposit tokens ───── */
        val serializedDepositTokens = tokens.joinToString("|") { token ->
            listOf(
                token.name,
                token.ticker,
                token.iconUrl
            ).joinToString("~")
        }

        prefs[Keys.LAST_FETCH_TIMESTAMP] = timestamp
        prefs[Keys.CHAINS_JSON] = serializedChains
        prefs[Keys.TOKENS_JSON] = serializedDepositTokens
    }

    Log.d(
        TAG_BASE_SETTINGS_DS,
        "BASE SETTINGS SAVED | chains=${chains.size} | tokens=${tokens.size} | timestamp=$timestamp"
    )
}

/* ─────────────────────────────────────────────
   READ BASE SETTINGS CACHE
   ───────────────────────────────────────────── */

suspend fun Context.getBaseSettingsCache(): BaseSettingsCache? {
    return baseSettingsDataStore.data.map { prefs ->

        val timestamp = prefs[Keys.LAST_FETCH_TIMESTAMP]
        val chainsRaw = prefs[Keys.CHAINS_JSON]
        val tokensRaw = prefs[Keys.TOKENS_JSON]

        if (
            timestamp == null ||
            chainsRaw.isNullOrBlank() ||
            tokensRaw.isNullOrBlank()
        ) {
            Log.d(
                TAG_BASE_SETTINGS_DS,
                "CACHE MISS | timestamp=$timestamp | chains=${!chainsRaw.isNullOrBlank()} | tokens=${!tokensRaw.isNullOrBlank()}"
            )
            return@map null
        }

        /* ───── Deserialize chains ───── */
        val chains = chainsRaw.split("|").mapNotNull { item ->
            val parts = item.split(";")

            // ✅ MUST be 7 now
            if (parts.size != 7) return@mapNotNull null

            val supportedTokens = parts[6]
                .takeIf { it.isNotBlank() }
                ?.split(",")
                ?.mapNotNull { tokenRaw ->
                    val tokenParts = tokenRaw.split("~")
                    if (tokenParts.size != 4) return@mapNotNull null

                    runCatching {
                        TokenInfo(
                            symbol = TokenEnum.valueOf(tokenParts[0]),
                            name = tokenParts[1],
                            decimals = tokenParts[2].toInt(),
                            iconDisplay = tokenParts[3]
                        )
                    }.getOrNull()
                }
                ?: emptyList()

            runCatching {
                ChainInfoDto(
                    id = SupportedBlockchainEnum.valueOf(parts[0]),
                    displayName = parts[1],
                    fee = parts[2].toDouble(),
                    minimumWithdrawal = parts[3].toInt(),
                    arrivalTime = parts[4],
                    iconUrl = parts[5],
                    supportedTokens = supportedTokens
                )
            }.getOrNull()
        }

        /* ───── Deserialize deposit tokens ───── */
        val depositTokens = tokensRaw.split("|").mapNotNull { raw ->
            val parts = raw.split("~")
            if (parts.size != 3) return@mapNotNull null

            runCatching {
                DepositTokenDto(
                    name = parts[0],
                    ticker = parts[1],
                    iconUrl = parts[2]
                )
            }.getOrNull()
        }

        Log.d(
            TAG_BASE_SETTINGS_DS,
            "CACHE HIT | chains=${chains.size} | depositTokens=${depositTokens.size} | timestamp=$timestamp"
        )

        BaseSettingsCache(
            timestamp = timestamp,
            chains = chains,
            depositTokens = depositTokens
        )
    }.first()
}