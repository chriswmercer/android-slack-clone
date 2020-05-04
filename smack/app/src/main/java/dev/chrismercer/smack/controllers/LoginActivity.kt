package dev.chrismercer.smack.controllers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import dev.chrismercer.smack.utils.EXTRA_LOGIN_EMAIL
import dev.chrismercer.smack.R
import dev.chrismercer.smack.services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSpinner.visibility = View.INVISIBLE
    }

    fun loginLoginButtonClicked(view: View) {
        enableSpinner(true)
        hideKeyboard()
        AuthService.loginUser(this, loginEmailText.text.toString(), loginPasswordText.text.toString()) { complete ->
            if(complete && AuthService.User.isLoggedIn) {
                enableSpinner(false)
                finish()
            } else {
                enableSpinner(false)
                Toast.makeText(this, "Username or password not recognised", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun loginCreateUserButtonClicked(view: View) {
        val createIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createIntent)
    }

    private fun enableSpinner(enable: Boolean) {
        if(enable) {
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }

        loginEmailText.isEnabled = !enable
        loginEmailText.isEnabled = !enable
        loginCreateUserButton.isEnabled = !enable
        loginLoginButton.isEnabled = !enable
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}
