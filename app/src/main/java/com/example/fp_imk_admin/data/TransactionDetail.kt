package com.example.fp_imk_admin.data

data class TransactionDetail(
    val trans_id: String = "",
    val category_id: String = "",
    val berat: Double = 0.0,
    val hargaPerKg: Int = 0
)

var transactionDetailList = mutableListOf<TransactionDetail>()