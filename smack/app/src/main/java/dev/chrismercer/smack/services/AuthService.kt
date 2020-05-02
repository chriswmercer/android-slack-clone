package dev.chrismercer.smack.services

import android.app.DownloadManager
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import dev.chrismercer.smack.utils.URL_REGISTER
import org.json.JSONObject

object AuthService {

    private fun doCall(context: Context, url: String, method: Int, body: String, success: (Boolean) -> Unit) {

        val registerRequest = object : StringRequest(method, url, Response.Listener {
            Log.d("RegisterRequest", it)
            success(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could no register user: $error")
            success(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return body.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(registerRequest)
    }

    fun registerUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        var requestBody = jsonBody.toString()

        doCall(context, URL_REGISTER, Request.Method.POST, requestBody) { success ->
            complete(success)
        }
    }
}