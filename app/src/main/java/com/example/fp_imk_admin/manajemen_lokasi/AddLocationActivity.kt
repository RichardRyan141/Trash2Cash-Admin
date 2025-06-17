package com.example.fp_imk_admin.manajemen_lokasi

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.LocationSessionManager
import com.example.fp_imk_admin.data.Location

class AddLocationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AddLocationScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddLocationPreview() {
    AddLocationScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen() {
    val context = LocalContext.current
    var namaLokasi by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var long by remember { mutableStateOf("") }

    fun validCoor(coor: String, range: Double): Boolean {
        val value = coor.toDoubleOrNull()
        val valid = value != null && value in -range..range
        return valid
    }

    fun isFormValid(): Boolean {
        val fieldsFilled = namaLokasi.isNotBlank() && alamat.isNotBlank() && lat.isNotBlank() && long.isNotBlank()
        val latValid = validCoor(lat, 90.0)
        val longValid = validCoor(long, 180.0)

        return fieldsFilled && latValid && longValid
    }

    fun buatLokasi() {
        val location = Location(
            namaLokasi = namaLokasi.trim(),
            alamat = alamat.trim(),
            lat = lat.toDouble(),
            long = long.toDouble()
        )
        LocationSessionManager.createLocation(location) { success, message ->
            if (success) {
                Toast.makeText(context, "Lokasi berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                (context as? ComponentActivity)?.finish()
            } else {
                Toast.makeText(context, message ?: "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
        }
        (context as? Activity)?.finish()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1EBEB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Lokasi",
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity?)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = namaLokasi,
                onValueChange = { namaLokasi = it },
                label = { Text("Masukkan Nama Lokasi") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            if (!namaLokasi.isNotBlank()) {
                Text(
                    text = "Nama lokasi harus terisi",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = alamat,
                onValueChange = { alamat = it },
                label = { Text("Masukkan Alamat Lokasi") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            if (!alamat.isNotBlank()) {
                Text(
                    text = "Alamat harus terisi",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = lat,
                onValueChange = { lat = it },
                label = { Text("Masukkan Latitude") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            if (!lat.isNotBlank()) {
                Text(
                    text = "Latitude harus terisi",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if(!validCoor(lat, 90.0)) {
                Text(
                    text = "Koordinat tidak valid",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = long,
                onValueChange = { long = it },
                label = { Text("Masukkan Longtitude") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            if (!long.isNotBlank()) {
                Text(
                    text = "Longtitude harus terisi",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if(!validCoor(long, 180.0)) {
                Text(
                    text = "Koordinat tidak valid",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    buatLokasi()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid()
            ) {
                Text("Tambah Lokasi")
            }
        }
    }
}
