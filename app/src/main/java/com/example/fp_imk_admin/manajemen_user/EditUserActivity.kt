package com.example.fp_imk_admin.manajemen_user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.HomepageActivity
import com.example.fp_imk_admin.R
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditUserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userId = intent.getStringExtra("USER_ID")?: "QbosNpcHMsVpCcg2js6kELvwS7Y2"
            EditUserScreen(userId)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditUserPreview() {
    EditUserScreen("QbosNpcHMsVpCcg2js6kELvwS7Y2")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(userId: String) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var noTelp by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("User") }
    var balance by remember { mutableStateOf(0) }

    var user_role = ""
    val userDataState = UserSessionManager.userData.collectAsState()
    val userData = userDataState.value

    LaunchedEffect(Unit) {
        UserSessionManager.getUserData(uid = userId)
    }

    username = userData?.username ?: ""
    email = userData?.email ?: ""
    noTelp = userData?.noTelp ?: ""
    user_role = userData?.role ?: "employee"
    balance = userData?.balance ?: 0

    fun isFormValid(): Boolean {
        val fieldsFilled = username.isNotBlank() && email.isNotBlank()
        val phoneValid = noTelp.all { it.isDigit() } && noTelp.length >= 10
        return fieldsFilled && phoneValid
    }

    fun editUser() {
        UserSessionManager.editUser(
            uid = userId,
            username = username,
            email = email,
            noTelp = noTelp,
            role = user_role,
            balance = balance
        ) { success, message ->
            if (success) {
                Toast.makeText(context, "User berhasil diupdate", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, UserListActivity::class.java))
            } else {
                Toast.makeText(context, message ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(50.dp))

        Row {
            Text(
                text = "Edit ${user_role.replaceFirstChar { it.uppercaseChar() }}",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Masukkan Username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
        if (!username.isNotBlank()) {
            Text(
                text = "Masukkan username",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Masukkan Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
        if (!email.isNotBlank()) {
            Text(
                text = "Masukkan Email",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = noTelp,
            onValueChange = { noTelp = it },
            label = { Text("Masukkan Nomor Telepon") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
        if (!noTelp.isNotBlank() || noTelp.length < 10) {
            Text(
                text = "Masukkan Nomor Telepon (min. 10 digit)",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                editUser()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid()
        ) {
            Text("Edit ${user_role.replaceFirstChar { it.uppercaseChar() }}")
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { (context as? Activity)?.finish() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali")
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}