package com.example.jonathangalvan.mirelex.Interfaces

class TransactionsInterface (
    var data: ArrayList<TransactionInterface>
){}

class TransactionInterface (
    var transactionTypeId: Int?,
    var transactionType: String?,
    var description: String?,
    var total: Number?,
    var totalFormated: String?,
    var date: String?,
    var time: String?,
    var type: String?,
    var orderId: Int?,
    var folio: String?,
    var orderTypeId: Int?,
    var orderType: String?
){}