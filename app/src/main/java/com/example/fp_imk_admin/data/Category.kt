package com.example.fp_imk_admin.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Category (
    val namaKategori: String = "",
    val hargaPerKg: Int = 0,
    val id: String? = null
)

fun getCategoryList(
    onSuccess: (List<Category>) -> Unit,
    onError: (DatabaseError) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("categories")

    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Category>()
                for (child in snapshot.children) {
                    val category = child.getValue(Category::class.java)
                    if (category != null) {
                        list.add(category.copy(id = child.key))
                    }
                }
                onSuccess(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
}

fun getCategoryData(
    uid: String,
    onSuccess: (Category?) -> Unit,
    onError: (Exception) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("categories").child(uid)
    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val category = snapshot.getValue(Category::class.java)
            onSuccess(category)
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.toException())
        }
    })
}
