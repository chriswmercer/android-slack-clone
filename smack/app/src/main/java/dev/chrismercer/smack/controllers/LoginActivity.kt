package dev.chrismercer.smack.controllers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import dev.chrismercer.smack.utils.EXTRA_LOGIN_EMAIL
import dev.chrismercer.smack.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginLoginButtonClicked(view: View) {

    }

    fun loginCreateUserButtonClicked(view: View) {
        val createIntent = Intent(this, CreateUserActivity::class.java)
        if(!loginEmailText.text.equals("")) {
            createIntent.putExtra(EXTRA_LOGIN_EMAIL, loginEmailText.text.toString())
        }
        startActivity(createIntent)
    }
}
