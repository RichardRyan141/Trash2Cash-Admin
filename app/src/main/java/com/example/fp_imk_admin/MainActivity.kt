package com.example.fp_imk_admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.HomepageActivity
import com.example.fp_imk_admin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
//    var email by remember { mutableStateOf("admin1@gmail.com") }
    var email by remember { mutableStateOf("karya1@gmail.com") }
    var password by remember { mutableStateOf("pass1234") }
    var passwordVisible by remember { mutableStateOf(false) }


    fun isFormValid(): Boolean {
        val passwordValid = password.length >= 8 && password.any { it.isDigit() }
        val fieldsFilled = email.isNotBlank() && password.isNotBlank()
        return fieldsFilled && passwordValid
    }

    fun loginUser() {
        if (!isFormValid()) {
            Toast.makeText(context, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        UserSessionManager.login(email, password) { success, errorMessage ->
            if (success) {
                context.startActivity(Intent(context, HomepageActivity::class.java))
            } else {
                Toast.makeText(context, errorMessage ?: "Login gagal", Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.size(250.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Employee login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(100.dp))


        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Masukkan Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Masukkan Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .padding(end = 8.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                loginUser()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Spacer(modifier = Modifier.height(48.dp))
    }
}
