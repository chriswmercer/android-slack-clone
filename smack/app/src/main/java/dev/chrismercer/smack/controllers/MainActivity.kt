package dev.chrismercer.smack.controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.chrismercer.smack.R
import dev.chrismercer.smack.models.Channel
import dev.chrismercer.smack.services.AuthService
import dev.chrismercer.smack.services.ChatServerService
import dev.chrismercer.smack.utils.BROADCAST_CHANNEL_DATA_CHANGE
import dev.chrismercer.smack.utils.BROADCAST_USER_DATA_CHANGE
import dev.chrismercer.smack.utils.SOCKET_EVENT_NEW_CHANNEL
import dev.chrismercer.smack.utils.iosColourToAndroid
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    lateinit var channelAdapter: ArrayAdapter<Channel>

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

        hideKeyboard()

        setupListeners()
        setupAdapters()
        ChatServerService.connect(this)

        if (AuthService.User.isLoggedIn) {
            AuthService.refreshLoginAfterReload(this) {}
        }
    }

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ChatServerService.channels)
        channel_list.adapter = channelAdapter
    }

    private fun setupListeners() {
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
        LocalBroadcastManager.getInstance(this).registerReceiver(channelDataChangeReceiver, IntentFilter(BROADCAST_CHANNEL_DATA_CHANGE))
    }

    private fun destroyListeners() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(channelDataChangeReceiver)
    }

    override fun onDestroy() {
        destroyListeners()
        ChatServerService.disconnect()
        super.onDestroy()
    }

    private val userDataChangeReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUserDetails()

            //update channels as the user is logged in
            context?.let {
                ChatServerService.getChannels(it) { complete ->
                    if (complete) {
                        updateChannels()
                    }
                }
            }
        }
    }

    private val channelDataChangeReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateChannels()
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
        if(AuthService.User.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView)
                .setPositiveButton("Add") { dialogInterface, i ->
                    //perform when clicked
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChanneelName)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDesc)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()

                    //create channel
                    ChatServerService.newChannel(channelName, channelDesc)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    //cancel and close
                }
                .show()
        }
    }

    fun sendMessageMain(view: View) {
        Log.d("LOGGER", "Send Clicked")
        hideKeyboard()
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
            usernameNavHeader.text = ""
            emailNavHeader.text = ""
            val resourceId = resources.getIdentifier("profiledefault", "drawable", packageName)
            profileImageNavHeader.setImageResource(resourceId)
            profileImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginButtonNavHeader.text = "Login"
        }
    }

    private fun updateChannels() {
        channelAdapter.notifyDataSetChanged()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}
