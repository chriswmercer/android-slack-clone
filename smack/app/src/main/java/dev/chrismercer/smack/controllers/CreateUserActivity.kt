package dev.chrismercer.smack.controllers

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import dev.chrismercer.smack.R
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlin.random.Random

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
    }

    fun onUserCreateAvatarClick(view: View?) {
        val random = java.util.Random()
        val color = random.nextInt(2) //upper bound not included
        val avatarNumber = random.nextInt(28)

        if (color == 0) {
            userAvatar = "light$avatarNumber"
        } else {
            userAvatar = "dark$avatarNumber"
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

    }
}
