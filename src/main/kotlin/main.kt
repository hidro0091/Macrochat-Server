package com.leah

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.Duration.Companion.seconds

data class Message(
    val username: String,
    val time:     Long,
    val contents: String,
)

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}

fun messageStr(msg: Message): String {
    return "${convertLongToTime(msg.time)}  ${msg.username}: ${msg.contents}"
}

var messages = mutableListOf<Message>(Message("g", 32, "e"))

fun constructBlock(): String {
    var block = ""
    for (message in messages) {
        block += "${messageStr(message)}\n"
    }
    return block
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        install(WebSockets) {
            pingPeriod = 15.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        configureRouting()
    }.start(wait = true)
}