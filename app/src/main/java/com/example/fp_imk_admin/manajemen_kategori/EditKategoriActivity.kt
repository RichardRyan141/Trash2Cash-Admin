package com.example.fp_imk_admin.manajemen_kategori

import android.app.Activity
import android.os.Bundle
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
import com.example.fp_imk_admin.CategorySessionManager
import com.example.fp_imk_admin.data.Category

class EditKategoriActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val kategori_id = intent.getStringExtra("kategori_id") ?: ""
        val namaKategori = intent.getStringExtra("namaKategori") ?: ""
        val hargaPerKg = intent.getStringExtra("hargaPerKg") ?: ""
        enableEdgeToEdge()
        setContent {
            EditKategoriScreen(kategori_id, namaKategori, hargaPerKg)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditKategoriScreen(kategori_id: String, nama: String, harga: String) {
    val context = LocalContext.current
    var namaKategori by remember { mutableStateOf(nama) }
    var hargaPerKg by remember { mutableStateOf(harga) }

    fun isFormValid(): Boolean {
        val hargaValid = hargaPerKg.toIntOrNull() != null && hargaPerKg.toInt() > 0
        val fieldsFilled = namaKategori.isNotBlank() && hargaPerKg.isNotBlank()
        return fieldsFilled && hargaValid
    }

    fun editKategori() {
        val updatedCategory = Category(
            id = kategori_id,
            namaKategori = namaKategori,
            hargaPerKg = hargaPerKg.toInt()
        )

        CategorySessionManager.updateCategory(updatedCategory) { success, error ->
            if (success) {
                Toast.makeText(context, "Kategori berhasil diperbarui", Toast.LENGTH_SHORT).show()
                (context as? ComponentActivity)?.apply {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } else {
                Toast.makeText(context, "Gagal memperbarui kategori: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Edit Kategori",
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
                    editKategori()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid()
            ) {
                Text("Edit Kategori")
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