package dev.chrismercer.smack.utils

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.toolbox.Volley

class SharedPrefs(context: Context) {
    val PREFS_FILE_NAME = "prefs"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, 0)

    val IS_LOGGED_IN_KEY = "isLoggedIn"
    val AUTH_TOKEN_KEY = "authToken"
    val USER_EMAIL_KEY = "userEmail"

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN_KEY, false)
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN_KEY, value).apply()

    var authToken: String
        get() = prefs.getString(AUTH_TOKEN_KEY, "").toString()
        set(value) = prefs.edit().putString(AUTH_TOKEN_KEY, value).apply()

    var userEmail: String
        get() = prefs.getString(USER_EMAIL_KEY, "").toString()
        set(value) = prefs.edit().putString(USER_EMAIL_KEY, value).apply()

    val requestQueue = Volley.newRequestQueue(context)
}