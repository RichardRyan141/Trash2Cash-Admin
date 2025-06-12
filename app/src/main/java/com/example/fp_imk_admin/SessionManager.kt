package com.example.fp_imk_admin

import android.util.Log
import com.example.fp_imk_admin.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object UserSessionManager {
    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    fun getUserData(uid: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                Log.d("FirebaseUser", "User: $user")
                _userData.value = user
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load user data", error.toException())
            }
        })
    }

    fun getAllUsers(onResult: (List<User>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.get()
            .addOnSuccessListener { snapshot ->
                val usersList = mutableListOf<User>()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    val userId = child.key
                    if (user != null && userId != null) {
                        user.id = userId
                        usersList.add(user)
                    }
                }
                onResult(usersList)
            }
            .addOnFailureListener {
                Log.e("Firebase", "Failed to load users: ${it.message}")
                onResult(emptyList())
            }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    if (uid == null) {
                        onResult(false, "User ID not found")
                        return@addOnCompleteListener
                    }

                    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                    dbRef.get()
                        .addOnSuccessListener { snapshot ->
                            val role = snapshot.child("role").getValue(String::class.java)
                            if (role != "user") {
                                onResult(true, null)
                            } else {
                                auth.signOut()
                                onResult(false, "Login tidak diizinkan untuk user di sini")
                            }
                        }
                        .addOnFailureListener {
                            auth.signOut()
                            onResult(false, "Gagal memverifikasi role pengguna")
                        }
                } else {
                    onResult(false, "Login gagal: Email atau password salah")
                }
            }
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        noTelp: String,
        role: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val auth = FirebaseAuth.getInstance()
        val usersRef = database.getReference("users")

        usersRef.orderByChild("noTelp").equalTo(noTelp).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onResult(false, "Nomor telepon sudah digunakan")
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                val uid = firebaseUser?.uid ?: return@addOnCompleteListener
                                val user = User(
                                    username = username,
                                    email = email,
                                    noTelp = noTelp,
                                    balance = 0,
                                    role = role
                                )

                                usersRef.child(uid).setValue(user)
                                    .addOnSuccessListener {
                                        onResult(true, null)
                                    }
                                    .addOnFailureListener {
                                        onResult(false, "Gagal menyimpan data user")
                                    }
                            } else {
                                onResult(false, "Registrasi gagal: ${task.exception?.message}")
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Gagal memeriksa nomor telepon: ${e.message}")
            }
    }

    fun editUser(
        uid: String,
        username: String,
        email: String,
        noTelp: String,
        role: String,
        balance: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val updatedUser = mapOf(
            "username" to username,
            "email" to email,
            "noTelp" to noTelp,
            "role" to role,
            "balance" to balance
        )

        usersRef.updateChildren(updatedUser)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, "Gagal mengupdate data: ${exception.message}")
            }
    }


    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}
