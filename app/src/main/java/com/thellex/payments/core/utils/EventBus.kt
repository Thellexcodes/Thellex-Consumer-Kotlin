package com.thellex.payments.core.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.ITransactionHistoryDto

object EventBus {
    private val _transactionUpdate = MutableLiveData<ITransactionHistoryDto>()
    val transactionUpdate: LiveData<ITransactionHistoryDto> = _transactionUpdate
    private val _fiatCryptoTransactionUpdate = MutableLiveData<IFiatCryptoRampTransactionsDto>()
    val fiatCryptoTransactionUpdate: LiveData<IFiatCryptoRampTransactionsDto> = _fiatCryptoTransactionUpdate

    fun postTransactionUpdate(transaction: ITransactionHistoryDto) {
        _transactionUpdate.postValue(transaction)
    }

    fun postFiatCryptoTransactionUpdate(transaction: IFiatCryptoRampTransactionsDto) {
        _fiatCryptoTransactionUpdate.postValue(transaction)
    }
}