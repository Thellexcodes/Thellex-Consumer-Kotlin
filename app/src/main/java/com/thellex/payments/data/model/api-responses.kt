package com.thellex.payments.data.model

typealias AdminRampTransactionsResponse = PaginatedResponse<List<AdminRampTransactionDTO>>
typealias PaginatedTransactionsHistoryResponse = PaginatedResponse<List<ITransactionHistoryDto>>
typealias PaginatedRampTransactionsHistoryResponse = PaginatedResponse<List<IFiatCryptoRampTransactionsDto>>
typealias PaginatedNotificationResponse = PaginatedResponse<List<NotificationEntity>>
