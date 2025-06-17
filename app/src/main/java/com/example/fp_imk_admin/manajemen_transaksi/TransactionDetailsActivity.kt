package com.example.fp_imk_admin.manajemen_transaksi

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.fp_imk_admin.CategorySessionManager
import com.example.fp_imk_admin.LocationSessionManager
import com.example.fp_imk_admin.TransactionSessionManager
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.Category
import com.example.fp_imk_admin.data.Location
import com.example.fp_imk_admin.data.Transaction
import com.example.fp_imk_admin.data.TransactionDetail
import com.example.fp_imk_admin.data.User
import com.example.fp_imk_admin.data.transactionDetailList
import kotlin.math.round

class TransactionDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trans_id = intent.getStringExtra("TRANS_ID") ?: "-OSvl4VBfbfMdjtY8Php"
        enableEdgeToEdge()
        setContent {
            TransactionDetailsScreen(trans_id)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailsPreview() {
    TransactionDetailsScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(transID: String = "-OSvl4VBfbfMdjtY8Php") {
    var transaction by remember { mutableStateOf<Transaction?>(null) }
    var allCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var addedDetails by remember { mutableStateOf<List<TransactionDetail>>(emptyList()) }
    var user by remember { mutableStateOf<User?>(null)}
    var loc by remember {mutableStateOf<Location?>(null)}
    var totalNominal by remember { mutableIntStateOf(0) }

    LaunchedEffect(transID) {
        TransactionSessionManager.getTransactionByID (
            transID,
            onResult = { transaction = it },
            onError = { error -> Log.e("FirebaseError", "Error fetching transaction", error.toException()) }
        )
        CategorySessionManager.getCategoryList (
            onSuccess = { allCategories = it },
            onError = { error -> Log.e("FirebaseError", "Error fetching categories", error.toException())}
        )
        TransactionSessionManager.getDetailsFor(transID) {
            addedDetails = it + transactionDetailList
            for(item in addedDetails) {
                totalNominal += round(item.berat * item.hargaPerKg).toInt()
            }
        }
    }

    LaunchedEffect(transaction) {
        transaction?.let {
            UserSessionManager.getUserData(it.tujuan) { fetchedUser ->
                user = fetchedUser
            }
            LocationSessionManager.getLocationByID (
                it.loc_id,
                onResult = { fetchedLocation ->
                    loc = fetchedLocation
                },
                onError = { error ->
                    Log.e("TransactionDetail", "Error retrieving location data ${error.message}")
                }
            )
        }
    }

    if (transaction == null || user == null || loc == null) {
        Text("Loading…")
    } else {
        ListTransactionDetailsContent(
            user = user!!,
            location = loc!!,
            transaction = transaction!!,
            categoryList = allCategories,
            detailsList = addedDetails,
            total = totalNominal,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTransactionDetailsContent(
    user: User,
    location: Location,
    transaction: Transaction,
    categoryList: List<Category>,
    detailsList: List<TransactionDetail>,
    total: Int,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1EBEB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Transaksi",
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
        Column(modifier = Modifier.padding(16.dp).padding(innerPadding)) {
            TransDetail(user, location, transaction, total)

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Text("Details", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = "Kategori", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(text = "Berat (kg)", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(text = "Harga per kg (Rp)", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(text = "Subtotal (Rp)", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                }
                detailsList.forEach { detail ->
                    val catName =
                        categoryList.find { it.id == detail.category_id }?.namaKategori ?: "Unknown"
                    val subtotal = detail.berat * detail.hargaPerKg
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(text = catName, modifier = Modifier.weight(1f))
                        Text(text = "${detail.berat} kg", modifier = Modifier.weight(1f))
                        Text(text = detail.hargaPerKg.toString(), modifier = Modifier.weight(1f))
                        Text(text = "%.2f".format(subtotal), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}