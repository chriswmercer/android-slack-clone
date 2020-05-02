package dev.chrismercer.smack.controllers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import dev.chrismercer.smack.utils.EXTRA_LOGIN_EMAIL
import dev.chrismercer.smack.R
import dev.chrismercer.smack.services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginLoginButtonClicked(view: View) {
        AuthService.loginUser(this, loginEmailText.text.toString(), loginPasswordText.text.toString()) { complete ->
            if(complete && AuthService.User.isLoggedIn) {
                finish()
            } else {
                Toast.makeText(this, "Username or password not recognised", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loginCreateUserButtonClicked(view: View) {
        val createIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createIntent)
    }
}
