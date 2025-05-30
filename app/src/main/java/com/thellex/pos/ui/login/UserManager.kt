package com.thellex.pos.ui.login

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserManager {
    private lateinit var context: Context

    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    private val ID_KEY = stringPreferencesKey("user_id")
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val EMAIL_KEY = stringPreferencesKey("user_email")
    private val LOGIN_KEY = booleanPreferencesKey("user_logged_in")

    fun init(appContext: Context) {
        context = appContext.applicationContext
        observeDataStore()
    }

//    private val dataStore = context.dataStore

    private fun observeDataStore() {
//        CoroutineScope(Dispatchers.IO).launch {
//            context.dataStore.data.collect { prefs ->
//                _userState.value = User(
//                    id = prefs[ID_KEY] ?: "",
//                    name = prefs[NAME_KEY] ?: "",
//                    email = prefs[EMAIL_KEY] ?: "",
//                    isLoggedIn = prefs[LOGIN_KEY] ?: false
//                )
//            }
//        }
    }

    suspend fun updateUser(user: User) {
//        context.dataStore.edit { prefs ->
//            prefs[ID_KEY] = user.id
//            prefs[NAME_KEY] = user.name
//            prefs[EMAIL_KEY] = user.email
//            prefs[LOGIN_KEY] = user.isLoggedIn
//        }
    }

    suspend fun logout() {
//        context.dataStore.edit { it.clear() }
    }
}
