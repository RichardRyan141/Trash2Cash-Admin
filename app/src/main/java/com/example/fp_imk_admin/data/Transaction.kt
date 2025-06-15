package com.example.fp_imk_admin.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(
    val masuk: Boolean = false,
    val waktu: String = getCurrentTimestamp(),
    val sumber: String = "",
    val tujuan: String = "",
    val nominal: Int = 0,
    val pesan: String = "",
    val user_id: String = "",
    val noRef: String = "",
    val loc_id: String = ""
)

fun getCurrentTimestamp(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}