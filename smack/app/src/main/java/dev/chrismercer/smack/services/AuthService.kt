package dev.chrismercer.smack.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import dev.chrismercer.smack.utils.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    fun registerUser(context: Context, username: String, email: String, password: String, avatar: String, colour: String, complete: (Boolean) -> Unit) {
        registerUserInternal(context, email, password) { complete ->
            if (!complete) {
                Log.d("AUTH", "Could not register user - error in registerUser internal")
                complete(false)
            } else {
                loginUserInternal(context, email, password) { loggedIn ->
                    if (!loggedIn) {
                        Log.d("AUTH", "Could not register user - error in log in")
                        complete(false)
                    } else {
                        createUserInternal(context, User.authToken, username, email, avatar, colour) { created ->
                            if (created) {
                                userDataChange(context)
                                complete(true)
                            } else {
                                logoutUser(context)
                                complete(false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        loginUserInternal(context, email, password) { loggedIn ->
            if (!loggedIn) {
                logoutUser(context)
                Log.d("AUTH", "Could not register user - error in log in")
                complete(false)
            } else {
                findUserByEmail(context, email) { found ->
                    if (found) {
                        Log.d("AUTH", "Logged in ok")
                        userDataChange(context)
                        complete(true)
                    } else {
                        Log.d("AUTH", "Could not log in")
                        logoutUser(context)
                        complete(false)
                    }
                }
            }
        }
    }

    fun logoutUser(context: Context) {
        User.id = ""
        User.name = ""
        User.email = ""
        User.authToken = ""
        User.avatar = ""
        User.colour = ""
        User.isLoggedIn = false
        userDataChange(context)
    }

    private fun registerUserInternal(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object: StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            Log.d("DEBUG", response)
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not register user: ${error.printStackTrace()}")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(registerRequest)
    }

    private fun loginUserInternal(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object: JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->
            Log.d("DEBUG", response.toString())
            try {
                User.email = email
                User.authToken = response.getString(JSONPROP_TOKEN)
                User.isLoggedIn = true
                complete(true)
            } catch(e: JSONException) {
                Log.d("JSON", "Could not log in: ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not login user: ${error.printStackTrace()}")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(loginRequest)
    }

    private fun createUserInternal(context: Context, token: String, name: String, email: String, avatar: String, colour: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatar)
        jsonBody.put("avatarColor", colour)
        val requestBody = jsonBody.toString()

        val createRequest = object : JsonObjectRequest(Method.POST, URL_CREATE, null, Response.Listener { response ->
            Log.d("DEBUG", response.toString())
            User.name = name
            User.avatar = avatar
            User.colour = colour
            try {
                User.id = response.getString("_id")
            } catch (e: JSONException) {
                logoutUser(context)
                Log.d("ERROR", "Could not read json on create user: ${e.localizedMessage}")
                complete(false)
            }
            complete(true)
        }, Response.ErrorListener { error ->
            logoutUser(context)
            Log.d("ERROR", "Could not create user: ${error.printStackTrace()}")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                var headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${User.authToken}")
                return headers
            }
        }

        Volley.newRequestQueue(context).add(createRequest)
    }

    private fun findUserByEmail(context: Context, email: String, complete: (Boolean) -> Unit) {
        val finalUrl = "${URL_FIND_USER}${email}"

        val findUserRequest = object : JsonObjectRequest(Method.GET, finalUrl, null, Response.Listener { response ->
            Log.d("DEBUG", response.toString())
            try {
                User.id = response.getString("_id")
                User.name = response.getString("name")
                User.avatar = response.getString("avatarName")
                User.colour = response.getString("avatarColor")
            } catch (e: JSONException) {
                logoutUser(context)
                Log.d("ERROR", "Could not read json on create user: ${e.localizedMessage}")
                complete(false)
            }
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not find user ${email}: ${error.printStackTrace()}")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                var headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${User.authToken}")
                return headers
            }
        }

        Volley.newRequestQueue(context).add(findUserRequest)
    }

    private fun userDataChange(context: Context) {
        val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
        LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
    }

    object User {
        var id = ""
        var name = ""
        var email = ""
        var authToken = ""
        var avatar = ""
        var colour = ""
        var isLoggedIn = false
    }
}