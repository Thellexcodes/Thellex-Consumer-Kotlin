package com.thellex.pay.features.fiat.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thellex.pay.data.model.IBankInfoRequestDto
import com.thellex.pay.features.wallet.model.IRateDto
import com.thellex.pay.features.wallet.model.IRatesDto

class CryptoToFiatViewModel(application: Application) : AndroidViewModel(application) {

    val paymentReason = MutableLiveData("")
    val network = MutableLiveData("")
    val sourceAddress = MutableLiveData("")
    val assetCode = MutableLiveData("")
    val country = MutableLiveData("")
    val fiatCode = MutableLiveData("NGN")

    val netFiatAmount = MutableLiveData(0.0)
    val netAssetAmount = MutableLiveData(0.0)
    val fiatAmount = MutableLiveData(0.0)
    val currentRate = MutableLiveData<IRateDto?>()
    val feePercentage = MutableLiveData(0.0)
    val bankInfo = MutableLiveData<IBankInfoRequestDto?>()
    val mainFiatAmount = MutableLiveData(0.0)
    val mainAssetAmount = MutableLiveData(0.0)
    var feeFiat = MutableLiveData(0.0)
    val feeUSD = MutableLiveData(0.0)

    fun setRampData(
        paymentReason: String,
        network: String,
        sourceAddress: String,
        assetCode: String,
        country: String,
        fiatCode: String,
        netFiatAmount: Double = 0.0,
        netAssetAmount: Double = 0.0,
        bankInfo: IBankInfoRequestDto? = null,
    ) {
        this.paymentReason.value = paymentReason
        this.network.value = network
        this.sourceAddress.value = sourceAddress
        this.assetCode.value = assetCode
        this.country.value = country
        this.fiatCode.value = fiatCode
        this.netFiatAmount.value = netFiatAmount
        this.netAssetAmount.value = netAssetAmount
        this.bankInfo.value = bankInfo
    }

    fun updateRate(rates: List<IRatesDto>) {
        val selectedRate = rates.find { it.fiatCode == fiatCode.value }
        currentRate.value = selectedRate?.rate

        val rate = selectedRate?.rate
        val feeBasis = rate?.fee ?: 0.0
        val feeDivisor = rate?.feeDivisor ?: 100.0

        feePercentage.value = calculateFeeDecimal(feeBasis, feeDivisor)
    }

    private fun calculateFeeDecimal(feeBasis: Double, feeDivisor: Double): Double {
        return if (feeDivisor != 0.0) feeBasis / (feeDivisor * 100.0) else 0.0
    }

    fun updateAmounts(feeFiat: Double, feeUSD: Double, fiatAmount: Double, cryptoAmount: Double, fiatA: Double) {
        this.feeFiat.value = feeFiat
        this.feeUSD.value = feeUSD
        this.netFiatAmount.value = fiatAmount
        this.netAssetAmount.value = cryptoAmount - feeUSD
        this.mainAssetAmount.value = cryptoAmount
        this.fiatAmount.value = fiatA
    }

    fun clearData() {
        paymentReason.value = ""
        network.value = ""
        sourceAddress.value = ""
        assetCode.value = ""
        country.value = ""
        fiatCode.value = "NGN"
        netFiatAmount.value = 0.0
        netAssetAmount.value = 0.0
        currentRate.value = null
        feePercentage.value = 0.0
        bankInfo.value = null
        mainFiatAmount.value = 0.0
        mainAssetAmount.value = 0.0
        feeFiat.value = 0.0
        feeUSD.value = 0.0
    }
}
