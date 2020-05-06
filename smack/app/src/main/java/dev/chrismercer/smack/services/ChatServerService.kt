package dev.chrismercer.smack.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.chrismercer.smack.models.Channel
import dev.chrismercer.smack.utils.*
import io.socket.client.IO
import io.socket.emitter.Emitter
import java.lang.Exception

object ChatServerService {

    private val socket = IO.socket(SOCKET_URL)
    private lateinit var context: Context

    var channels = ArrayList<Channel>()

    fun connect(context: Context) {
        this.context = context
        setupChannelListener()

        if(!socket.connected()) {
            socket.connect()
        }
    }

    fun disconnect() {
        if(socket.connected()) {
            socket.disconnect()
        }
    }

    fun newChannel(channelName: String, channelDesc: String) {
        if(!socket.connected()) throw Exception("Service must be connected")
        socket.emit(SOCKET_EVENT_NEW_CHANNEL, channelName, channelDesc)
    }

    private fun setupChannelListener() {
        socket.on(SOCKET_EVENT_CHANNEL_CREATED, Emitter.Listener { data ->
            if (data?.size == 3) {
                val channelName = data[0] as String
                val channelDesc = data[1] as String
                val channelId = data[2] as String

                val channel = Channel(channelName, channelDesc, channelId)
                channels.add(channel)
                dataChange(BROADCAST_CHANNEL_DATA_CHANGE)
            } else {
                Log.d("CHATSERVERSERVICE", "Did not get expected data")
            }
        })
    }

    private fun dataChange(dataId: String) {
        val userDataChange = Intent(dataId)
        LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
    }

}