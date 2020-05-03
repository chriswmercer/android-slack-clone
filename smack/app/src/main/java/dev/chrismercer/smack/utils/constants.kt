package dev.chrismercer.smack.utils

import android.graphics.Color
import java.util.*

//urls
const val URL_BASE = "https://chriswm-chatter.herokuapp.com/v1/"
const val URL_REGISTER = "${URL_BASE}account/register"
const val URL_LOGIN = "${URL_BASE}account/login"
const val URL_CREATE = "${URL_BASE}user/add"

//json props
const val JSONPROP_TOKEN = "token"

//extra values
const val EXTRA_LOGIN_EMAIL = "EXTRA_LOGIN_EMAIL"

//intents
const val BROADCAST_USER_DATA_CHANGE = "userdatachanged"

fun iosColourToAndroid(iosColour: String) : Int {
    val strippedColour = iosColour.replace("[", "").replace("]", "").replace(",", "")

    var r = 0
    var g = 0
    var b = 0

    val scanner = Scanner(strippedColour)
    if(scanner.hasNext()) {
        r = (scanner.nextDouble() * 255).toInt()
        g = (scanner.nextDouble() * 255).toInt()
        b = (scanner.nextDouble() * 255).toInt()
    }

    return Color.rgb(r, g ,b)
}