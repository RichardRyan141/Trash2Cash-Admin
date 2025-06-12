package com.example.fp_imk_admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.data.User
import com.example.fp_imk_admin.manajemen_kategori.BuatKategoriActivity
import com.example.fp_imk_admin.manajemen_kategori.DaftarKategoriActivity
import com.example.fp_imk_admin.manajemen_user.RegisterActivity
import com.example.fp_imk_admin.manajemen_user.UserListActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState

class HomepageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val uid = user?.uid
    var user_role = ""
    val userDataState = UserSessionManager.userData.collectAsState()
    val userData = userDataState.value

    LaunchedEffect(uid) {
        if (uid != null) {
            UserSessionManager.getUserData(uid = uid)
        }
    }

    user_role = userData?.role ?: "employee"

    Column(modifier = Modifier.fillMaxSize()) {
        header(userData)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            item {
                if (user_role != "employee") {
                    manajemen_user("superadmin")
                    laporan()
                    manajemen_kategori()
                } else {
                    Spacer(modifier = Modifier.height(40.dp))
                }

                manajemen_transaksi()
            }
        }
    }
}

@Composable
fun header(userData: User?) {
    var context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFADD0A2),
                            Color(0xFF7AE982),
                            Color(0xFF41C10A)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = userData?.username ?: "username",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = userData?.email ?: "user@email.com",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            IconButton(onClick = {
                UserSessionManager.logout()
                context.startActivity(Intent(context, MainActivity::class.java))
            }) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }
        }
    }
}

@Composable
fun manajemen_user(user_role: String) {
    val items = mutableListOf(
        Triple(Color.Black, "Buat Pengguna", RegisterActivity::class.java),
        Triple(Color.Green, "Daftar Pengguna", UserListActivity::class.java),
    )

    val arrangement = if (items.size == 2) {
        Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally)
    } else {
        Arrangement.SpaceBetween
    }

    Surface(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 25.dp)
            .fillMaxWidth(),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Manajemen User",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = arrangement,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (color, label, target) ->
                    manajemen_user_item(color, label, target)
                }
            }
        }
    }
}

@Composable
fun manajemen_user_item(image_color: Color, text: String, target: Class<*>) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                context.startActivity(Intent(context, target))
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = text,
            tint = image_color,
            modifier = Modifier.size(48.dp).padding(bottom = 10.dp)
        )
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Composable
fun laporan() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .padding(start = 16.dp, end=16.dp, top=20.dp, bottom=25.dp)
            .fillMaxWidth(),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Laporan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                laporan_item(Color.Black, "Laporan Pembelian Sampah")
                laporan_item(Color.Green, "Laporan Transfer")
            }
        }
    }
}

@Composable
fun laporan_item(image_color: Color, text: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = text,
            tint = image_color,
            modifier = Modifier.size(48.dp).padding(bottom = 10.dp)
        )
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun manajemen_kategori() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .padding(start = 16.dp, end=16.dp, top=20.dp, bottom=25.dp)
            .fillMaxWidth(),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Manajemen Kategori",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                manajemen_kategori_item(Color.Black, "Daftar Kategori", DaftarKategoriActivity::class.java)
                manajemen_kategori_item(Color.Green, "Buat Kategori Baru", BuatKategoriActivity::class.java)
            }
        }
    }
}

@Composable
fun manajemen_kategori_item(image_color: Color, text: String, menuClass: Class<*>) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                context.startActivity(Intent(context, menuClass))
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = text,
            tint = image_color,
            modifier = Modifier.size(48.dp).padding(bottom = 10.dp)
        )
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun manajemen_transaksi() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .padding(start = 16.dp, end=16.dp, top=20.dp, bottom=25.dp)
            .fillMaxWidth(),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Manajemen Transaksi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical=10.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                manajemen_transaksi_item(Color.Black, "Buat Transaksi Baru")
                manajemen_transaksi_item(Color.Green, "Daftar Transaksi Pending")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                manajemen_transaksi_item(Color.Blue, "Daftar Transaksi Selesai")
            }
        }
    }
}

@Composable
fun manajemen_transaksi_item(image_color: Color, text: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = text,
            tint = image_color,
            modifier = Modifier.size(48.dp).padding(bottom = 10.dp)
        )
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}
