package com.example.fp_imk_admin.manajemen_laporan

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.ChartSection
import com.example.fp_imk_admin.LoadingScreen
import com.example.fp_imk_admin.LocationSessionManager
import com.example.fp_imk_admin.TransactionSessionManager
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.Location
import com.example.fp_imk_admin.data.Transaction
import com.example.fp_imk_admin.data.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LaporanTransferActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaporanTransferScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun LaporanTransferPreview() {
    LaporanTransferScreen()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaporanTransferScreen() {
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        LocationSessionManager.getAllLocations(
            onSuccess = { fetchedLocations ->
                locations = fetchedLocations
                Log.d("LaporanTransferScreen", "Locations: $locations")
            },
            onError = { error ->
                Log.e("LaporanTransferScreen", "Database error: ${error.message}")
            }
        )
        TransactionSessionManager.getAllTransactions(
            onResult = { fetchedTransactions ->
                transactions = fetchedTransactions
                Log.d("LaporanTransferScreen", "Transactions: $transactions")
            }
        )
        UserSessionManager.getAllUsers(
            onResult = { fetchedUsers ->
                users = fetchedUsers
                Log.d("LaporanTransferScreen", "Users: $users")
            }
        )
    }

    if (locations.isEmpty() || transactions.isEmpty() || users.isEmpty()) {
        LoadingScreen()
    } else {
        LaporanTransferScreenContent(locations, transactions, users)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaporanTransferScreenContent(locList: List<Location>, transList: List<Transaction>, userList: List<User>) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }

    var startDateObj by remember { mutableStateOf<Date?>(null) }
    var endDateObj by remember { mutableStateOf<Date?>(null) }

    val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val inputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val waktuFormatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
    val groupMonthFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val startDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val selected = "$dayOfMonth/${month + 1}/$year"
            startDateText = displayFormatter.format(inputFormatter.parse(selected)!!)
            startDateObj = inputFormatter.parse(selected)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val selected = "$dayOfMonth/${month + 1}/$year"
            endDateText = displayFormatter.format(inputFormatter.parse(selected)!!)
            endDateObj = inputFormatter.parse(selected)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val filtered = remember(transList, startDateObj, endDateObj) {
        transList.filter { transaction ->
            val waktuDate = try {
                waktuFormatter.parse(transaction.waktu)
            } catch (e: Exception) {
                Log.d("ErrorWaktu", "${e.message}")
                null
            }
            waktuDate != null &&
                    (startDateObj == null || !waktuDate.before(startDateObj)) &&
                    (endDateObj == null || !waktuDate.after(endDateObj)) &&
                    transaction.masuk == false
        }.sortedByDescending {
            waktuFormatter.parse(it.waktu)
        }
    }
    Log.d("AllTransactions", "Transactions: $transList")
    Log.d("FilteredTransactions", "Transactions: $filtered")

    val perWallet = filtered.groupBy { it.tujuan.substringBefore(" ") }
        .mapValues { it.value.sumOf { t -> t.nominal } }

    val perBulan = transList
        .filter {
            it.masuk == false
        }
        .groupBy {
            val date = waktuFormatter.parse(it.waktu)
            groupMonthFormatter.format(date!!)
        }.mapValues {
            it.value.sumOf { t -> t.nominal }
        }

    Log.d("PerBulan", "$perBulan")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Transfer Saldo",
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    TextButton(onClick = { startDatePickerDialog.show() }) {
                        Text(
                            text = if (startDateText.isEmpty()) "Pilih Tanggal Awal" else "Awal: $startDateText",
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    TextButton(onClick = { endDatePickerDialog.show() }) {
                        Text(
                            text = if (endDateText.isEmpty()) "Pilih Tanggal Akhir" else "Akhir: $endDateText",
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                ChartSection(
                    "Laporan Per Lokasi",
                    perWallet,
                    "Pie"
                )
//                ChartSection(
//                    "Laporan Per User",
//                    perUser.mapKeys { id -> userList.find { it.id == id.key }?.username ?: id.key },
//                    "Pie"
//                )
                ChartSection("Laporan Per Bulan", perBulan, "VertBar")

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}