package dev.chrismercer.smack.services

import dev.chrismercer.smack.utils.SOCKET_EVENT_NEW_CHANNEL
import dev.chrismercer.smack.utils.SOCKET_URL
import io.socket.client.IO

object ChatServerService {
    private val socket = IO.socket(SOCKET_URL)

    fun connect() {
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
        socket.emit(SOCKET_EVENT_NEW_CHANNEL, channelName, channelDesc)
    }

    object Channels {

    }

    object Messages {

    }
}