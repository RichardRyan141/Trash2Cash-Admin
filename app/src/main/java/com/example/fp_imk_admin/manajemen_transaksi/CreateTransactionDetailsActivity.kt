package com.example.fp_imk_admin.manajemen_transaksi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
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
import com.example.fp_imk_admin.manajemen_user.UserListActivity
import kotlin.math.round
import kotlin.math.roundToInt

class CreateTransactionDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trans_id = intent.getStringExtra("TRANS_ID") ?: "-OSvl4VBfbfMdjtY8Php"
        enableEdgeToEdge()
        setContent {
            CreateTransactionDetailsScreen(trans_id)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTransactionDetailsPreview() {
    CreateTransactionDetailsScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionDetailsScreen(transID: String = "-OSvl4VBfbfMdjtY8Php") {
    var context = LocalContext.current
    var transaction by remember { mutableStateOf<Transaction?>(null) }
    var allCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var addedDetails by remember { mutableStateOf<List<TransactionDetail>>(emptyList()) }
    var user by remember { mutableStateOf<User?>(null)}
    var loc by remember {mutableStateOf<Location?>(null)}
    var totalNominal by remember {mutableStateOf(0)}

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
        CreateTransactionDetailsContent(
            user = user!!,
            location = loc!!,
            transaction = transaction!!,
            categoryList = allCategories,
            detailsList = addedDetails,
            total = totalNominal,
            onAddDetail = { detail, categoryID, weight, pricePerKg ->
                val newDetail = TransactionDetail(transID, categoryID, weight, pricePerKg)
                transactionDetailList.add(newDetail)
                addedDetails = addedDetails + newDetail
                totalNominal += round(weight * pricePerKg).toInt()
                Log.d("DetailsList2", "$transactionDetailList")
            },
            onSaveDraft = {
                TransactionSessionManager.addDetail(transID, transactionDetailList) { success, errorMessage ->
                    if (success) {
                        Log.d("AddDetail", "Draft saved")
                        (context as? Activity)?.finish()
                    } else {
                        Log.e("AddDetail", "Error saving draft: $errorMessage")
                    }
                }
                transactionDetailList.clear()
            },
            onFinalize = {
                TransactionSessionManager.addDetail(transID, addedDetails) { success, errorMessage ->
                    if (success) {
                        val updated = transaction!!.copy(nominal = totalNominal)
                        TransactionSessionManager.updateTransactionNominal(transID, updated) { updateSuccess ->
                            if (updateSuccess) {
                                Log.d("Finalize", "Finalized successfully")

                                var updatedUser = user!!.copy(balance = user!!.balance + totalNominal)

                                UserSessionManager.editUser(updatedUser) { success, message ->
                                    if (success) {
                                        Log.d("Finalize", "User ${user!!.username} sudah dibayar")
                                        context.startActivity(Intent(context, PendingTransListActivity::class.java))
                                        (context as Activity?)?.finish()
                                    } else {
                                        Log.e("Finalize", "Gagal membayar ${user!!.username}")
                                    }
                                }
                            } else {
                                Log.e("Finalize", "Failed to update nominal")
                            }
                        }
                    } else {
                        Log.e("Finalize", "Error finalizing: $errorMessage")
                    }
                }
                transactionDetailList.clear()
            },
            onRemoveDetail = { detail ->
                transactionDetailList.remove(detail)
                addedDetails = addedDetails.filter { it != detail }
                totalNominal -= (detail.berat * detail.hargaPerKg).roundToInt()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionDetailsContent(
    user: User,
    location: Location,
    transaction: Transaction,
    categoryList: List<Category>,
    detailsList: List<TransactionDetail>,
    total: Int,
    onAddDetail: (TransactionDetail, String, Double, Int) -> Unit,
    onSaveDraft: () -> Unit,
    onFinalize: () -> Unit,
    onRemoveDetail: (TransactionDetail) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var weightInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val availableCategories = categoryList.filter { category ->
        detailsList.none { it.category_id == category.id }
    }
    Log.d("AvailableCategories", "${availableCategories}")
    Log.d("DetailsList", "$detailsList")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1EBEB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Detail Transaksi",
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as Activity?)?.finish()
                        transactionDetailList.clear()
                    }) {
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
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onSaveDraft,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save as Draft")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onFinalize,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // optional green
                ) {
                    Text("Save & Finalize")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(16.dp).padding(innerPadding)) {
            TransDetail(user, location, transaction, total)

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.namaKategori ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor() // required for proper positioning
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.namaKategori) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Berat (kg)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (weightInput.toDoubleOrNull() == null) {
                Text(
                    text = "Berat tidak valid",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val weight = weightInput.toDoubleOrNull()
                    val cat = selectedCategory
                    val detail = TransactionDetail(
                        trans_id = transaction.noRef,
                        category_id = cat!!.id,
                        berat = weight!!.toDouble(),
                        hargaPerKg = cat.hargaPerKg
                    )
                    onAddDetail(detail, cat.id, weight, cat.hargaPerKg)
                    weightInput = ""
                    selectedCategory = null
                },
                enabled = selectedCategory != null && weightInput.toDoubleOrNull() != null
            ) {
                Text("Add")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Details", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = "Kategori", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(text = "Berat (kg)", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(text = "Harga per kg (Rp)", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(text = "Subtotal (Rp)", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(text = "Action", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
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
                        IconButton(
                            onClick = { onRemoveDetail(detail) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransDetail(user: User, location: Location, transaction: Transaction, totalNominal: Int) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        TransDetailRow("User", user.username)
        TransDetailRow("Tanggal", transaction.waktu)
        TransDetailRow("Lokasi", location.namaLokasi)
        TransDetailRow("Nominal", totalNominal.toString())
    }
}

@Composable
fun TransDetailRow(key: String, content: String) {
    ConstraintLayout (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical=12.dp)
    ) {
        val (label, colon, value) = createRefs()

        Text(
            text = key,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(label) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            }
        )

        Text(
            text = ":",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(colon) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(label.top)
                bottom.linkTo(label.bottom)
            }
        )

        Text(
            text = content,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(value) {
                start.linkTo(colon.end, margin = 16.dp)
                end.linkTo(parent.end)
                top.linkTo(label.top)
                bottom.linkTo(label.bottom)
                width = Dimension.fillToConstraints
            }
        )
    }
}