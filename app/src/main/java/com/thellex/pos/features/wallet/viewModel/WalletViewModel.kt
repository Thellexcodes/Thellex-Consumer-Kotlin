package com.thellex.pos.features.wallet.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel


class WalletViewModel(application: Context):ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    init {
    }
}
