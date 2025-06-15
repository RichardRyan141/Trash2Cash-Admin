package com.example.fp_imk_admin.manajemen_kategori

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.CategorySessionManager
import com.example.fp_imk_admin.data.Category
import java.text.NumberFormat
import java.util.Locale

class DaftarKategoriActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            val context = LocalContext.current

            val updateLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    CategorySessionManager.getCategoryList(
                        onSuccess = { fetchedCategories -> categories = fetchedCategories },
                        onError = { error ->
                            errorMessage = error.message
                            categories = emptyList()
                        }
                    )
                }
            }

            LaunchedEffect(Unit) {
                CategorySessionManager.getCategoryList(
                    onSuccess = { fetchedCategories -> categories = fetchedCategories },
                    onError = { error ->
                        errorMessage = error.message
                        categories = emptyList()
                    }
                )
            }

            DaftarKategoriScreen(
                categories = categories,
                errorMessage = errorMessage,
                onBack = { finish() },
                onEditCategory = { category ->
                    val intent = Intent(context, EditKategoriActivity::class.java).apply {
                        putExtra("kategori_id", category.id)
                        putExtra("namaKategori", category.namaKategori)
                        putExtra("hargaPerKg", category.hargaPerKg.toString())
                    }
                    updateLauncher.launch(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarKategoriScreen(
    categories: List<Category>,
    errorMessage: String?,
    onBack: () -> Unit,
    onEditCategory: (Category) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Daftar Kategori",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
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
                .padding(8.dp)
        ) {
            when {
                errorMessage != null -> {
                    Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                }

                categories.isEmpty() -> {
                    Text("No category available")
                }

                else -> {
                    CategoryTable(categories = categories, onEditCategory = onEditCategory)
                }
            }
        }
    }
}

@Composable
fun CategoryTable(
    categories: List<Category>,
    onEditCategory: (Category) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nama Kategori",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Harga per Kg",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Aksi",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(categories.size) { index ->
                val category = categories[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = category.namaKategori,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(category.hargaPerKg)}",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { onEditCategory(category) },
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}
