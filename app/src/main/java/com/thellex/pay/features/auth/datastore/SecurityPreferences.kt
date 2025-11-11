package com.thellex.pay.features.auth.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object SecurityPreferences {
    val IS_PIN_SET = booleanPreferencesKey("is_pin_set")
    val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
}