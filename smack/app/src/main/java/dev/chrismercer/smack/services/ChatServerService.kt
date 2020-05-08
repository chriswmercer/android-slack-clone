package dev.chrismercer.smack.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import dev.chrismercer.smack.controllers.App
import dev.chrismercer.smack.models.Channel
import dev.chrismercer.smack.models.Message
import dev.chrismercer.smack.utils.*
import io.socket.client.IO
import org.json.JSONException
import java.lang.Exception

object ChatServerService {

    private val socket = IO.socket(SOCKET_URL)
    private lateinit var context: Context

    var channels = ArrayList<Channel>()
    var messages = ArrayList<Message>()

    var selectedChannel : Channel? = null
        private set

    fun connect(context: Context) {
        this.context = context
        if(!socket.connected()) {
            socket.connect()
        }

        setupChannelListener()
        setupMessageListener()
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

    fun sendMessage(message: String, complete: (Boolean) -> Unit) {
        checkConnection()
        if (!AuthService.User.isLoggedIn || selectedChannel == null) complete(false)

        val userId = AuthService.User.id
        val channelId = selectedChannel!!.id
        socket.emit(SOCKET_EVENT_NEW_MESSAGE_SEND, message, userId, channelId, AuthService.User.name, AuthService.User.avatar, AuthService.User.colour)
        complete(true)
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

                if (channels.count() > 0) {
                    setChannel(0) { ok ->
                        if (ok) BROADCAST_MESSAGE_DATA_CHANGE.dataChange()
                    }
                }

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

    private fun getMessages(forChannel: Channel, complete: (Boolean) -> Unit) {
        checkConnection()
        if (!AuthService.User.isLoggedIn) complete(false)

        val url = "${URL_GET_MESSAGES}${forChannel.id}"

        val messagesRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
            messages.clear()

            try {
                for (index in 0 until response.length()) {
                    val messageObject = response.getJSONObject(index)
                    val messageBody = messageObject.getString("messageBody")
                    val userId = messageObject.getString("userId")
                    val channelId = messageObject.getString("channelId")
                    val userName = messageObject.getString("userName")
                    val userAvatar = messageObject.getString("userAvatar")
                    val userAvatarColor = messageObject.getString("userAvatarColor")
                    val timeStamp = messageObject.getString("timeStamp")

                    val message = Message(messageBody, userName, channelId, userAvatar, userAvatarColor, userId, timeStamp)
                    this.messages.add(message)
                }
                BROADCAST_MESSAGE_DATA_CHANGE.dataChange()
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "Could not get messages: ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not get messages: ${error.printStackTrace()}")
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

        App.sharedPreferences.requestQueue.add(messagesRequest)
    }

    fun setChannel(indexInChannels: Int, complete: (Boolean) -> Unit) {
        if (indexInChannels >= channels.count()) complete(false)
        selectedChannel = channels[indexInChannels]
        if (selectedChannel != null) getMessages(selectedChannel!!) { gotChannels ->
            complete(gotChannels)
        }
    }

    fun clearAll() {
        channels.clear()
        messages.clear()
        BROADCAST_CHANNEL_DATA_CHANGE.dataChange()
        BROADCAST_MESSAGE_DATA_CHANGE.dataChange()
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
                Log.d("CHATSERVERSERVICE", "Did not get expected channel data")
            }
        }
    }

    private fun setupMessageListener() {
        socket.on(SOCKET_EVENT_NEW_MESSAGE) { data ->
            if (data?.size == 8) {
                val channelId = data[2] as String

                if (channelId == selectedChannel?.id) {
                    val messageBody = data[0] as String
                    val userName = data[3] as String
                    val userAvatar = data[4] as String
                    val avatarColor = data[5] as String
                    val id = data[6] as String
                    val timeStamp = data[7] as String

                    val message = Message(
                        messageBody,
                        userName,
                        channelId,
                        userAvatar,
                        avatarColor,
                        id,
                        timeStamp
                    )
                    messages.add(message)
                    BROADCAST_MESSAGE_DATA_CHANGE.dataChange()
                }
            } else {
                Log.d("CHATSEVERSERVICE", "Did not get expected message data")
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