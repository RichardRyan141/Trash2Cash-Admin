package com.example.fp_imk_admin

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.data.User


fun saveStringLocally(context: Context, tag: String, str: String) {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    prefs.edit().putString(tag, str).apply()
}

fun getStringLocally(context: Context, tag: String): String? {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return prefs.getString(tag, null)
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("Mengambil data...", fontSize = 20.sp)
    }
}

@Composable
fun UserSearchDropdown(
    userList: List<User>,
    onUserSelected: (User?) -> Unit
) {
    var queryValue by remember { mutableStateOf(TextFieldValue("")) }

    val filteredUsers = userList.filter {
        it.role == "user" && it.username.contains(queryValue.text, ignoreCase = true)
    }.take(5)

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = queryValue,
            onValueChange = { queryValue = it },
            label = { Text("Search Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (queryValue.text.isNotEmpty()) {
                    IconButton(onClick = {
                        queryValue = TextFieldValue("")
                        onUserSelected(null)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        )

        if (queryValue.text.isNotBlank() && filteredUsers.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .background(Color.White)
            ) {
                filteredUsers.forEach { user ->
                    Text(
                        text = user.username,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newText = user.username
                                queryValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newText.length)
                                )
                                onUserSelected(user)
                            }
                            .padding(12.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}