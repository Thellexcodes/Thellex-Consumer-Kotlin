package com.thellex.payments.core.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thellex.payments.data.model.ITransactionHistoryDto

object EventBus {
    private val _transactionUpdate = MutableLiveData<ITransactionHistoryDto>()
    val transactionUpdate: LiveData<ITransactionHistoryDto> = _transactionUpdate

    fun postTransactionUpdate(transaction: ITransactionHistoryDto) {
        _transactionUpdate.postValue(transaction)
    }
}