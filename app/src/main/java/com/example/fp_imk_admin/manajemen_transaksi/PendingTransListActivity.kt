package com.example.fp_imk_admin.manajemen_transaksi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.LoadingScreen
import com.example.fp_imk_admin.LocationSessionManager
import com.example.fp_imk_admin.TransactionSessionManager
import com.example.fp_imk_admin.UserSearchDropdown
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.Location
import com.example.fp_imk_admin.data.Transaction
import com.example.fp_imk_admin.data.User
import com.example.fp_imk_admin.data.dummyLocs
import com.google.firebase.auth.FirebaseAuth

class PendingTransListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var locationsState = mutableStateListOf<Location>()
        var isLocLoaded = mutableStateOf(false)
        var transactionsState = mutableStateListOf<Transaction>()
        var isTransLoaded = mutableStateOf(false)
        var usersState = mutableStateListOf<User>()
        var isUserLoaded = mutableStateOf(false)

        fun loadTransactions() {
            TransactionSessionManager.getAllTransactions { transactions ->
                transactionsState.clear()
                transactionsState.addAll(transactions)
                isTransLoaded.value = true
            }
            UserSessionManager.getAllUsers { users ->
                usersState.clear()
                usersState.addAll(users)
                isUserLoaded.value = true
            }
            LocationSessionManager.getAllLocations(
                onSuccess = { fetchedLocations ->
                    val allLocationsOption = Location(
                        id = "ALL",
                        namaLokasi = "Semua Lokasi"
                    )
                    locationsState.clear()
                    locationsState.add(allLocationsOption)
                    locationsState.addAll(fetchedLocations)
                    isLocLoaded.value = true
                },
                onError = { error ->
                    Log.e("LocationScreen", "Database error: ${error.message}")
                    isLocLoaded.value = true
                }
            )
        }

        loadTransactions()

        setContent {
            if (isLocLoaded.value && isTransLoaded.value && isUserLoaded.value && locationsState.isNotEmpty() && transactionsState.isNotEmpty() && usersState.isNotEmpty()) {
                PendingTransListScreen(locationsState, transactionsState, usersState, onRefresh = { loadTransactions() })
            } else {
                LoadingScreen()
            }
        }
        enableEdgeToEdge()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingTransListScreen(
    locationsState: SnapshotStateList<Location>,
    transactionsState: SnapshotStateList<Transaction>,
    usersState: SnapshotStateList<User>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val transactionList: List<Transaction> = transactionsState
    var userList by remember { mutableStateOf(usersState.toList()) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var locList by remember { mutableStateOf(locationsState.toList()) }
    var selectedLocation by remember { mutableStateOf<Location?>(locList[0]) }

    val currUser: User = userList.find { it.id == uid } ?: userList.first()

    if (currUser.role == "karyawan") {
        selectedLocation = locList.find { it.id == currUser.lokasiID }
    }

    var filteredTransactions = if (selectedUser != null) {
        transactionList.filter { it.tujuan == selectedUser!!.id }
    } else {
        transactionList
    }
    filteredTransactions = if (selectedLocation!!.id != "ALL") {
        filteredTransactions.filter { it.loc_id == selectedLocation!!.id }
    } else {
        filteredTransactions
    }
    filteredTransactions = filteredTransactions.filter { it.nominal == 0 }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1EBEB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Transaksi Pending",
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(Intent(context, CreateTransactionActivity::class.java))
                (context as Activity?)?.finish()
            }, containerColor = Color(0xFF00704A)) {
                Text(text = "Buat Transaksi", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(vertical=8.dp, horizontal=16.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (currUser.role == "admin") {
                Text(
                    text = "Filter Lokasi",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LocationDropdown(
                    locations = locList,
                    selectedLocation = selectedLocation,
                    onLocationSelected = { selectedLocation = it }
                )
            }

            Text(
                text = "Filter Pengguna",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical=8.dp)
            )
            UserSearchDropdown(
                userList = userList,
                onUserSelected = { user ->
                    selectedUser = user
                }
            )

            PendingTransactionTable(
                transactions = filteredTransactions,
                onDetails = { transaction ->
                    val intent = Intent(context, CreateTransactionDetailsActivity::class.java)
                    intent.putExtra("TRANS_ID", transaction.noRef)
                    context.startActivity(intent)
                    (context as Activity?)?.finish()
                },
                onDelete = { transaction ->
                    TransactionSessionManager.deleteTransaction(transaction) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
                            onRefresh()
                        } else {
                            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PendingTransactionTable(
    transactions: List<Transaction>,
    onDetails: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var locList by remember { mutableStateOf<List<Location>>(emptyList()) }
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect(Unit) {
        LocationSessionManager.getAllLocations(
            onSuccess = { fetchedLocations ->
                locList = if (fetchedLocations.isNotEmpty()) fetchedLocations else dummyLocs
            },
            onError = {
            }
        )
        UserSessionManager.getAllUsers { users ->
            userList = users
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Lokasi", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Nama User", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Actions", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 2.dp,
                color = Color.LightGray
            )
        }

        itemsIndexed(transactions) { index, trans ->
            val transLocation = locList.find { it.id == trans.loc_id } ?: locList.firstOrNull()
            val transUser = userList.find { it.id == trans.tujuan } ?: userList.firstOrNull()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transLocation?.namaLokasi ?: "Lokasi tidak ditemukan",
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = transUser?.username ?: "User tidak ditemukan",
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Button(
                        onClick = { onDetails(trans) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text(
                            text = "Tambah Detail",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { onDelete(trans) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(
                            text = "Delete",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 2.dp,
                color = Color.LightGray
            )
        }
    }
}
