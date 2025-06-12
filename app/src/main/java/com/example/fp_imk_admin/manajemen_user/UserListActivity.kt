package com.example.fp_imk_admin.manajemen_user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.User

class UserListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserListScreen()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UserListPreview() {
    UserListScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen() {
    val context = LocalContext.current
    var dispUser by remember { mutableStateOf(true) }
    var dispEmpl by remember { mutableStateOf(true) }
    var dispAdmin by remember { mutableStateOf(true) }

    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        UserSessionManager.getAllUsers { users ->
            allUsers = users
        }
    }

    val filteredUsers = remember(dispUser, dispEmpl, dispAdmin, allUsers) {
        allUsers.filter {
            (dispUser && it.role == "user") ||
                    (dispEmpl && it.role == "employee") ||
                    (dispAdmin && it.role == "admin")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1EBEB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Pengguna",
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
                context.startActivity(Intent(context, RegisterActivity::class.java))
            }, containerColor = Color(0xFF00704A)) {
                Text(text = "Buat Pengguna", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(vertical=8.dp, horizontal=16.dp))
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
            Text(
                text = "Role",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "User",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal=8.dp)
                    )
                    Checkbox(
                        checked = dispUser,
                        onCheckedChange = { dispUser = it },
                        modifier = Modifier.scale(1.5f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Karyawan",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal=8.dp)
                    )
                    Checkbox(
                        checked = dispEmpl,
                        onCheckedChange = { dispEmpl = it },
                        modifier = Modifier.scale(1.5f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Admin",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal=8.dp)
                    )
                    Checkbox(
                        checked = dispAdmin,
                        onCheckedChange = { dispAdmin = it },
                        modifier = Modifier.scale(1.5f)
                    )
                }
            }

            UserTable(
                users = filteredUsers,
                onEdit = { user ->
                    val intent = Intent(context, EditUserActivity::class.java)
                    intent.putExtra("USER_ID", user.id)
                    context.startActivity(intent)
                },
                onDelete = {  }
            )
        }
    }
}

@Composable
fun UserTable(users: List<User>, onEdit: (User) -> Unit, onDelete: (User) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Role", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Username", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Actions", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 2.dp,
                color = Color.LightGray
            )
        }

        itemsIndexed(users) { index, user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(user.role, fontSize=20.sp, modifier = Modifier.weight(1f))
                Text(user.username, fontSize=20.sp, modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Button(
                        onClick = { onEdit(user) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text(
                            text = "Edit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { onDelete(user) },
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
