package dev.chrismercer.smack.controllers

import android.app.Application
import dev.chrismercer.smack.utils.SharedPrefs

class App : Application() {

    companion object {
        lateinit var sharedPreferences: SharedPrefs
    }

    override fun onCreate() {
        sharedPreferences = SharedPrefs(applicationContext)
        super.onCreate()
    }
}