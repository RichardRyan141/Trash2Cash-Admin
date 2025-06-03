package com.example.fp_imk_admin.data

import android.widget.Toast
import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class User(
    val username: String = "",
    val email: String = "",
    val noTelp: String = "",
    val balance: Int = 0,
    val role: String = "user"
)

fun getUserData(
    uid: String,
    onSuccess: (User?) -> Unit,
    onError: (Exception) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val user = snapshot.getValue(User::class.java)
            onSuccess(user)
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.toException())
        }
    })
}