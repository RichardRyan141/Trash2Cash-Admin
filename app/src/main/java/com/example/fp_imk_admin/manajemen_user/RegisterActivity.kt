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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
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
import com.example.fp_imk_admin.LoadingScreen
import com.example.fp_imk_admin.LocationSessionManager
import com.example.fp_imk_admin.R
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.Location
import com.example.fp_imk_admin.data.User
import com.example.fp_imk_admin.data.dummyLocs
import com.example.fp_imk_admin.manajemen_transaksi.LocationDropdown
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    RegisterScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(employeeRole: String, selectedRole: String, onRoleSelected: (String) -> Unit) {
    if(employeeRole == "admin") {
        var roles = listOf("user", "karyawan", "admin")
        var roleDisplay = listOf("User", "Karyawan", "Admin")
        var textDisp by remember { mutableStateOf("User") }
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.offset(y = -15.dp).wrapContentSize(Alignment.TopStart)
        ) {
            TextField(
                value = textDisp,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                textStyle = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .menuAnchor(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Gray
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.zip(roleDisplay).forEach { (role, disp) ->
                    DropdownMenuItem(
                        text = { Text(disp) },
                        onClick = {
                            onRoleSelected(role)
                            textDisp = disp
                            expanded = false
                        }
                    )
                }
            }
        }
    } else {
        Text(
            text = "User",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        onRoleSelected("user")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen() {
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }

    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    var user by remember {mutableStateOf<User?>(null)}

    LaunchedEffect(uid) {
        if (uid != null) {
            UserSessionManager.getUserData(uid) { fetchedUser ->
                if (fetchedUser != null) {
                    user = fetchedUser
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        LocationSessionManager.getAllLocations(
            onSuccess = { fetchedLocations ->
                locations = if (fetchedLocations.isNotEmpty()) fetchedLocations else dummyLocs
                if (fetchedLocations.isNotEmpty()) {
                    selectedLocation = locations[0]
                }
            },
            onError = {
            }
        )
    }

    if(locations.isEmpty() || user == null) {
        LoadingScreen()
    } else {
        RegisterScreenContent(user!!.role, locations)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(user_role: String, locations: List<Location>) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var noTelp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("user") }

    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    if (user_role == "karyawan") selectedLocation = locations[0]

    val scrollState = rememberScrollState()

    fun isFormValid(): Boolean {
        val passwordValid = password.length >= 8 && password.any { it.isDigit() }
        val fieldsFilled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        val passwordsMatch = password == confirmPassword
        val phoneValid = noTelp.all { it.isDigit() } && noTelp.length >= 10
        return fieldsFilled && phoneValid && passwordsMatch && passwordValid
    }

    fun registerUser() {
        val user = User(
            username = username,
            email = email,
            noTelp = noTelp,
            role = role,
            lokasiID = selectedLocation?.id ?: ""
        )
        UserSessionManager.registerUser(user = user, password = password) { success, message ->
            if (success) {
                Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, HomepageActivity::class.java))
            } else {
                Toast.makeText(context, message ?: "Registrasi gagal", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1EBEB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Pengguna",
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "Buat ",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                RoleDropdown(
                    employeeRole = user_role,
                    selectedRole = role,
                    onRoleSelected = { role = it }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (role == "karyawan") {
                LocationDropdown(
                    locations = locations,
                    selectedLocation = selectedLocation,
                    onLocationSelected = { selectedLocation = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

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

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Masukkan Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
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
            if (password.length < 8 || !password.any { it.isDigit() }) {
                Text(
                    text = "Password harus minimal 8 karakter dan mengandung angka",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable { confirmPasswordVisible = !confirmPasswordVisible }
                            .padding(end = 8.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            if (password != confirmPassword) {
                Text(
                    text = "Password harus sama",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    registerUser()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid()
            ) {
                Text("Buat ${role.replaceFirstChar { it.uppercaseChar() }}")
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
}