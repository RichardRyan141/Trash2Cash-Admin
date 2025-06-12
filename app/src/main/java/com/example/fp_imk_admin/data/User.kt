package com.example.fp_imk_admin.data

data class User(
    var id: String = "",
    val username: String = "",
    val email: String = "",
    val noTelp: String = "",
    val balance: Int = 0,
    val role: String = "user"
)