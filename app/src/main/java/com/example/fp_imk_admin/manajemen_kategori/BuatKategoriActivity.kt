package com.example.fp_imk_admin.manajemen_kategori

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.HomepageActivity
import com.example.fp_imk_admin.data.Category
import com.example.fp_imk_admin.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BuatKategoriActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuatKategoriScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuatKategoriScreen() {
    val context = LocalContext.current
    var namaKategori by remember { mutableStateOf("") }
    var hargaPerKg by remember { mutableStateOf("") }

    fun isFormValid(): Boolean {
        val hargaValid = hargaPerKg.toIntOrNull() != null && hargaPerKg.toInt() > 0
        val fieldsFilled = namaKategori.isNotBlank() && hargaPerKg.isNotBlank()
        return fieldsFilled && hargaValid
    }

    fun buatKategori() {
        val database = FirebaseDatabase.getInstance()
        val categoriesRef = database.getReference("categories")

        if (!isFormValid()) {
            Toast.makeText(context, "Lengkapi semua data dan ceklis persetujuan", Toast.LENGTH_SHORT).show()
            return
        }

        val hargaInt = hargaPerKg.toInt()

        categoriesRef.orderByChild("namaKategori").equalTo(namaKategori).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(context, "Nama kategori sudah digunakan", Toast.LENGTH_SHORT).show()
                } else {
                    val newRef = categoriesRef.push()
                    val category = Category(namaKategori = namaKategori, hargaPerKg = hargaInt)
                    newRef.setValue(category)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Kategori berhasil dibuat", Toast.LENGTH_SHORT).show()
                            (context as? ComponentActivity)?.finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Gagal menyimpan kategori: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseQuery", "Query failed", e)
                Toast.makeText(context, "Gagal mengecek nama kategori: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Buat Kategori",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    (context as? ComponentActivity)?.finish()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = namaKategori,
                onValueChange = { namaKategori = it },
                label = { Text("Masukkan Nama Kategori") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = hargaPerKg,
                onValueChange = { hargaPerKg = it },
                label = { Text("Masukkan Harga per Kg") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    buatKategori()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Buat Kategori")
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kembali")
            }
        }
    }
}