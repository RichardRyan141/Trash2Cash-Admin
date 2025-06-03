package com.example.fp_imk_admin

import android.content.Context


fun saveStringLocally(context: Context, tag: String, str: String) {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    prefs.edit().putString(tag, str).apply()
}

fun getStringLocally(context: Context, tag: String): String? {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return prefs.getString(tag, null)
}