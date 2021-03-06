package dev.chrismercer.smack.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.chrismercer.smack.R
import dev.chrismercer.smack.models.Message
import dev.chrismercer.smack.utils.iosColourToAndroid
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageAdapter(val context: Context, val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val userImage = itemView?.findViewById<ImageView>(R.id.messageImage)
        val timeStamp = itemView?.findViewById<TextView>(R.id.messageDate)
        val userName = itemView?.findViewById<TextView>(R.id.messageUserName)
        val messageBody = itemView?.findViewById<TextView>(R.id.messageBodyText)

        fun bindMessage(context: Context, message: Message) {
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable", context.packageName)
            userImage?.setImageResource(resourceId)
            userImage?.setBackgroundColor(iosColourToAndroid(message.avatarColour))
            userName?.text = message.username
            timeStamp?.text = niceDateString(message.timeStamp)
            messageBody?.text = message.body
        }

        fun niceDateString(isoString: String) : String {
            val isof = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isof.timeZone = TimeZone.getTimeZone("UTC")
            var convertedDate = Date()

            try {
                convertedDate = isof.parse(isoString)
            } catch (e: ParseException) {
                Log.d("PARSE", "Cannot parse date")
            }

            var dateString = SimpleDateFormat("EEE, d MMM HH:mm", Locale.getDefault())
            return dateString.format(convertedDate)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindMessage(context, messages[position])
    }
}