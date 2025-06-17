package com.example.fp_imk_admin.manajemen_transaksi

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.example.fp_imk_admin.LoadingScreen
import com.example.fp_imk_admin.LocationSessionManager
import com.example.fp_imk_admin.TransactionSessionManager
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.Location
import com.example.fp_imk_admin.data.Transaction
import com.example.fp_imk_admin.data.User
import com.example.fp_imk_admin.data.dummyLocs
import com.google.firebase.auth.FirebaseAuth

class CreateTransactionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreateTransactionScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTransactionPreview() {
    CreateTransactionScreen()
}

@Composable
fun CreateTransactionScreen(role: String = "admin") {
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }

    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(uid) {
        uid?.let { userId ->
            UserSessionManager.getUserData(userId) { fetchedUser ->
                fetchedUser?.let { userData ->
                    user = userData
                    LocationSessionManager.getAllLocations(
                        onSuccess = { fetchedLocations ->
                            val locs =
                                if (fetchedLocations.isNotEmpty()) fetchedLocations else dummyLocs
                            val userLocId = userData.lokasiID
                            selectedLocation = locs.firstOrNull()

                            if (userLocId.isNotEmpty()) {
                                val match = locs.find { it.id == userLocId }
                                match?.let {
                                    selectedLocation = it
                                    locations = listOf(it)
                                } ?: run {
                                    locations = locs
                                }
                            } else {
                                locations = locs
                            }
                        },
                        onError = {
                        }
                    )
                }
            }
        }
    }

    if (user == null || locations.isEmpty()) {
        LoadingScreen()
    } else {
        CreateTransactionScreenContent(user!!, locations)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreenContent(employee: User, locations: List<Location>) {
    val context = LocalContext.current
    var noTelp by remember { mutableStateOf("") }
    var userFound by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<Location>(locations.first()) }
    var user by remember { mutableStateOf<User?>(null) }

    fun buatTransaksi() {
        selectedLocation?.let { loc ->
            val trans = Transaction(
                masuk = true,
                sumber = loc.namaLokasi,
                tujuan = user!!.id,
                loc_id = loc.id ?: ""
            )

            TransactionSessionManager.addTransaction(trans) { success, message ->
                if (success) {
                    Toast.makeText(context, "Transaksi berhasil dibuat", Toast.LENGTH_SHORT).show()
                    (context as? Activity)?.finish()
                } else {
                    Toast.makeText(context, message ?: "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                }
            }
        } ?: run {
            Toast.makeText(context, "Lokasi belum dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1EBEB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Transaksi",
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp)
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
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .fillMaxWidth()
        ) {
            if (employee.role == "admin") {
                Text(
                    text = "Lokasi",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LocationDropdown(
                    locations = locations,
                    selectedLocation = selectedLocation,
                    onLocationSelected = { selectedLocation = it }
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Data User",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = noTelp,
                    onValueChange = { noTelp = it },
                    label = { Text("Nomor Telepon User") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    UserSessionManager.getUserDataFromPhone(noTelp) { fetchedUser ->
                        if (fetchedUser != null) {
                            if (fetchedUser.role != "user") {
                                Toast.makeText(context, "Pengguna bukan user", Toast.LENGTH_SHORT).show()
                                userFound = false
                            } else {
                                user = fetchedUser
                                userFound = true
                            }
                        } else {
                            userFound = false
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
            if (userFound) {
                OutlinedTextField(
                    value = user!!.username,
                    onValueChange = {},
                    label = { Text("Nama User") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledLabelColor = Color.DarkGray,
                        disabledBorderColor = Color.Gray,
                        disabledLeadingIconColor = Color.Black
                    )
                )
            } else {
                if (noTelp.isNotEmpty()) {
                    Text(
                        text = "User tidak ditemukan",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Button(
                onClick = {
                    buatTransaksi()
                },
                modifier = Modifier.fillMaxWidth().padding(vertical=24.dp),
                enabled = userFound
            ) {
                Text("Buat Transaksi")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    locations: List<Location>,
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedLocation?.namaLokasi ?: "Pilih lokasi",
            onValueChange = {},
            readOnly = true,
            label = { Text("Lokasi") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.namaLokasi) },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    }
                )
            }
        }
    }
}
