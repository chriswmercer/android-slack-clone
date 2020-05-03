package dev.chrismercer.smack.controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.chrismercer.smack.R
import dev.chrismercer.smack.services.AuthService
import dev.chrismercer.smack.utils.BROADCAST_USER_DATA_CHANGE
import dev.chrismercer.smack.utils.iosColourToAndroid
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
    }

    private val userDataChangeReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUserDetails()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginLogoutButtonNavHeaderClicked(view: View) {
        if(!AuthService.User.isLoggedIn) {
            Log.d("LOGGER", "LoginLogout Pressed")
            var loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        } else {
            AuthService.logoutUser(this)
        }
    }

    fun addChannelButtonNavHeaderClicked(view: View) {
        Log.d("LOGGER","Add Channel Pressed")
    }

    fun sendMessageMain(view: View) {
        Log.d("LOGGER", "Send Clicked")
    }

    private fun updateUserDetails() {
        if (AuthService.User.isLoggedIn) {
            usernameNavHeader.text = AuthService.User.name
            emailNavHeader.text = AuthService.User.email
            val resourceId = resources.getIdentifier(AuthService.User.avatar, "drawable", packageName)
            profileImageNavHeader.setImageResource(resourceId)
            profileImageNavHeader.setBackgroundColor(iosColourToAndroid(AuthService.User.colour))
            loginButtonNavHeader.text = "Logout"
        } else {
            usernameNavHeader.text = "Please Login"
            emailNavHeader.text = ""
            val resourceId = resources.getIdentifier("profiledefault", "drawable", packageName)
            profileImageNavHeader.setImageResource(resourceId)
            profileImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginButtonNavHeader.text = "Login"
        }
    }
}
