package dev.chrismercer.smack.controllers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import dev.chrismercer.smack.R
import dev.chrismercer.smack.services.AuthService
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
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    fun loginLogoutButtonNavHeaderClicked(view: View) {
        Log.d("LOGGER", "LoginLogout Pressed")
        var loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    fun addChannelButtonNavHeaderClicked(view: View) {
        Log.d("LOGGER","Add Channel Pressed")
    }

    fun sendMessageMain(view: View) {
        Log.d("LOGGER", "Send Clicked")
    }

    override fun onRestart() {
        super.onRestart()
        if (AuthService.User.isLoggedIn) {
            usernameNavHeader.text = AuthService.User.name
            emailNavHeader.text = AuthService.User.email

            val resourceId = resources.getIdentifier(AuthService.User.avatar, "drawable", packageName)
            profileImageNavHeader.setImageResource(resourceId)

        }
    }
}
