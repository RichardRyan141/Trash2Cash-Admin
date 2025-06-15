package com.example.fp_imk_admin.data

data class Location(
    val id: String? = "",
    val namaLokasi: String = "",
    val alamat: String = "",
    val lat : Double = 0.0,
    val long : Double = 0.0
)

val dummyLocs = listOf(
    Location("001", "Lokasi 1", "Alamat 1",),
    Location("002", "Lokasi 2", "Alamat 2"),
    Location("003", "Lokasi 3", "Alamat 3"),
    Location("004", "Lokasi 4", "Alamat 4"),
)