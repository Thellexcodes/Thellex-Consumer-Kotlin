package com.thellex.payments.features.fiat.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thellex.payments.data.model.IBankInfoRequestDto
import com.thellex.payments.features.wallet.model.IRateDto
import com.thellex.payments.features.wallet.model.IRatesDto

class CryptoToFiatViewModel(application: Application) : AndroidViewModel(application) {
    val paymentReason = MutableLiveData<String?>()
    val network = MutableLiveData<String?>()
    val sourceAddress = MutableLiveData<String?>()
    val assetCode = MutableLiveData<String?>()
    val country = MutableLiveData<String?>()
    val fiatCode = MutableLiveData( "NGN")
    val fiatAmount = MutableLiveData<Double?>()
    val currentRate = MutableLiveData<IRateDto?>()
    val fee = MutableLiveData<Double?>()
    val bankInfo = MutableLiveData<IBankInfoRequestDto?>()
    val mainAssetAmount = MutableLiveData<Double?>()
    val mainFiatAmount = MutableLiveData<Double?>()

    fun setRampData(
        paymentReason: String,
        network: String,
        sourceAddress: String,
        assetCode: String,
        country: String,
        fiatCode: String,
        fiatAmount: Double,
        bankInfo: IBankInfoRequestDto? = null,
        mainFiatAmount: Double? = null,
        mainAssetAmount: Double? = null
    ) {
        this.paymentReason.value = paymentReason
        this.network.value = network
        this.sourceAddress.value = sourceAddress
        this.assetCode.value = assetCode
        this.country.value = country
        this.fiatCode.value = fiatCode
        this.fiatAmount.value = fiatAmount
        this.bankInfo.value = bankInfo
        this.mainFiatAmount.value = mainFiatAmount
        this.mainAssetAmount.value = mainAssetAmount
    }

    // Update rate and fee based on rates from RateViewModel
    fun updateRate(rates: List<IRatesDto>) {
        val selectedRate = rates.find { it.fiatCode == fiatCode.value }
        this.currentRate.value = selectedRate?.rate
        this.fee.value = selectedRate?.rate?.fee?.div(selectedRate.rate.feeDivisor ?: 100.0) ?: 0.0
    }

    fun clearData() {
        paymentReason.value = null
        network.value = null
        sourceAddress.value = null
        assetCode.value = null
        country.value = null
        fiatCode.value = null
        fiatAmount.value = null
        currentRate.value = null
        fee.value = null
        bankInfo.value = null
        mainFiatAmount.value = null
        mainAssetAmount.value = null
    }
}