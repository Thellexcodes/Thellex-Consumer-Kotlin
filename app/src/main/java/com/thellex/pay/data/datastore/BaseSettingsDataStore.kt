package com.thellex.pay.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thellex.pay.data.model.ChainInfo
import com.thellex.pay.data.model.TokenInfo
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG_BASE_SETTINGS_DS = "BaseSettingsDataStore"

private val Context.baseSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "base_settings")

private object Keys {
    val LAST_FETCH_TIMESTAMP = longPreferencesKey("last_fetch_timestamp")
    val CHAINS_JSON = stringPreferencesKey("base_settings_json")
}

suspend fun Context.saveSupportedChains(
    chains: List<ChainInfo>,
    timestamp: Long = System.currentTimeMillis()
) {
    baseSettingsDataStore.edit { prefs ->

        val serializedChains = chains
            .filter { it.id != null }
            .joinToString("|") { chain ->

                val tokensSerialized = chain.supportedTokens.joinToString(",") { token ->
                    listOf(
                        token.symbol.name,
                        token.name,
                        token.decimals.toString(),
                        token.iconDisplay
                    ).joinToString(":")
                }

                listOf(
                    chain.id.name,
                    chain.displayName,
                    chain.fee.toString(),
                    chain.minimumWithdrawal.toString(),
                    chain.arrivalTime,
                    tokensSerialized
                ).joinToString(";")
            }

        prefs[Keys.LAST_FETCH_TIMESTAMP] = timestamp
        prefs[Keys.CHAINS_JSON] = serializedChains
    }

    Log.d(
        TAG_BASE_SETTINGS_DS,
        "SUPPORTED CHAINS SAVED | count=${chains.size} | timestamp=$timestamp"
    )
}

suspend fun Context.getSupportedChainsCache(): Pair<Long, List<ChainInfo>>? {
    return baseSettingsDataStore.data.map { prefs ->
        val timestamp = prefs[Keys.LAST_FETCH_TIMESTAMP]
        val raw = prefs[Keys.CHAINS_JSON]

        if (timestamp == null || raw.isNullOrBlank()) {
            Log.d(
                TAG_BASE_SETTINGS_DS,
                "CACHE MISS | timestamp=$timestamp | raw=${raw != null}"
            )
            return@map null
        }

        val chains = raw.split("|").mapNotNull { item ->
            val parts = item.split(";")
            if (parts.size != 6) return@mapNotNull null

            val tokens = parts[5]
                .takeIf { it.isNotBlank() }
                ?.split(",")
                ?.mapNotNull { tokenRaw ->
                    val tokenParts = tokenRaw.split(":")

                    if (tokenParts.size < 3) return@mapNotNull null

                    runCatching {
                        TokenInfo(
                            symbol = TokenEnum.valueOf(tokenParts[0]),
                            name = tokenParts[1],
                            decimals = tokenParts[2].toInt(),
                            iconDisplay = tokenParts.getOrNull(3).orEmpty()
                        )
                    }.getOrNull()
                }
                ?: emptyList()

            runCatching {
                ChainInfo(
                    id = SupportedBlockchainEnum.valueOf(parts[0]),
                    displayName = parts[1],
                    fee = parts[2].toDouble(),
                    minimumWithdrawal = parts[3].toInt(),
                    arrivalTime = parts[4],
                    supportedTokens = tokens
                )
            }.getOrNull()
        }

        Log.d(
            TAG_BASE_SETTINGS_DS,
            "CACHE HIT | count=${chains.size} | timestamp=$timestamp"
        )

        timestamp to chains
    }.first()
}
