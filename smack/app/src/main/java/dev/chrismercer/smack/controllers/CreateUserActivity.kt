package dev.chrismercer.smack.controllers

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.chrismercer.smack.R
import dev.chrismercer.smack.services.AuthService
import dev.chrismercer.smack.utils.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlin.random.Random

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE
    }

    fun onUserCreateAvatarClick(view: View?) {
        val random = java.util.Random()
        val color = random.nextInt(2) //upper bound not included
        val avatarNumber = random.nextInt(28)

        userAvatar = if (color == 0) {
            "light$avatarNumber"
        } else {
            "dark$avatarNumber"
        }

        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createUserAvatar.setImageResource(resourceId)
    }

    fun onUserCreateGenerateBackgrondColourClick(view: View?) {
        val random = java.util.Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)
        createUserAvatar.setBackgroundColor(Color.rgb(r, g, b))
        avatarColor = "[${r.toDouble() / 255}, ${g.toDouble() / 255}, ${b.toDouble() / 255}, 1]"
    }

    fun onCreateUserClick(view: View?) {
        var usernameStr = createUserName.text.toString()
        val emailStr = createUserEmail.text.toString()
        val passwordStr = createUserPassword.text.toString()

        if(usernameStr == "" || emailStr == "" || passwordStr == "") {
            Toast.makeText(this, "Enter all the information", Toast.LENGTH_SHORT).show()
            return
        }

        enableSpinner(true)

        AuthService.registerUser(this, usernameStr, emailStr, passwordStr, userAvatar, avatarColor) { created ->
            if(created) {
                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                enableSpinner(false)
                finish()
            } else {
                enableSpinner(false)
                Toast.makeText(this, "Could not create user. Try again please!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enableSpinner(enable: Boolean) {
        if(enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }

        createUserAvatar.isEnabled = !enable
        createUserBackgroundChange.isEnabled = !enable
        createUserCreateUser.isEnabled = !enable
    }
}
