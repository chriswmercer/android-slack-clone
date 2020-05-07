package dev.chrismercer.smack.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import dev.chrismercer.smack.controllers.App
import dev.chrismercer.smack.models.Channel
import dev.chrismercer.smack.utils.*
import io.socket.client.IO
import org.json.JSONException
import java.lang.Exception

object ChatServerService {

    private val socket = IO.socket(SOCKET_URL)
    private lateinit var context: Context

    var channels = ArrayList<Channel>()

    fun connect(context: Context) {
        this.context = context
        if(!socket.connected()) {
            socket.connect()
        }

        setupChannelListener()
    }

    fun disconnect() {
        if(socket.connected()) {
            socket.disconnect()
        }
    }

    private fun checkConnection() {
        if(!socket.connected()) throw Exception("Service must be connected")
    }

    fun newChannel(channelName: String, channelDesc: String) {
        checkConnection()
        socket.emit(SOCKET_EVENT_NEW_CHANNEL, channelName, channelDesc)
    }

    fun getChannels(complete: (Boolean) -> Unit) {
        checkConnection()
        if (!AuthService.User.isLoggedIn) complete(false)

        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->
            channels.clear()

            try {
                for (index in 0 until response.length()) {
                    val channelObject = response.getJSONObject(index)
                    val name = channelObject.getString("name")
                    val desc = channelObject.getString("description")
                    val id = channelObject.getString("_id")
                    val channel = Channel(name, desc, id)
                    this.channels.add(channel)
                }
                BROADCAST_CHANNEL_DATA_CHANGE.dataChange()
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "Could not get channels: ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not get channels: ${error.printStackTrace()}")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${AuthService.User.authToken}"
                return headers
            }
        }

        App.sharedPreferences.requestQueue.add(channelsRequest)
    }

    fun clear() {
        channels.clear()
        BROADCAST_CHANNEL_DATA_CHANGE.dataChange()
    }

    private fun setupChannelListener() {
        socket.on(SOCKET_EVENT_CHANNEL_CREATED) { data ->
            if (data?.size == 3) {
                val channelName = data[0] as String
                val channelDesc = data[1] as String
                val channelId = data[2] as String

                val channel = Channel(channelName, channelDesc, channelId)
                channels.add(channel)
                BROADCAST_CHANNEL_DATA_CHANGE.dataChange()
            } else {
                Log.d("CHATSERVERSERVICE", "Did not get expected data")
            }
        }
    }

    private fun String.dataChange() {
        if(AuthService.User.isLoggedIn) {
            val userDataChange = Intent(this)
            LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
        }
    }

}