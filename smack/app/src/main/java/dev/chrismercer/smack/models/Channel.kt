package dev.chrismercer.smack.models

class Channel (val name: String, val description: String, val id: String){
    override fun toString(): String {
        return "#${name}"
    }
}