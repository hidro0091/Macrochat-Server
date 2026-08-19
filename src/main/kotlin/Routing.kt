package com.leah

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveParameters
import io.ktor.server.websocket.*
import io.ktor.websocket.Frame
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun Application.configureRouting() {
    routing {
        val messageResponseFlow = MutableSharedFlow<String>()
        post("/") {
            val formContent = call.receiveParameters()
            val params = Triple(
                formContent["username"] ?: "",
                formContent["time"] ?: "",
                formContent["contents"] ?: "",
            )

            if (params.toList().any {it.isEmpty()}) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            try {
                val username = formContent["username"] ?: ""
                val time = formContent["time"] ?: ""
                val contents = formContent["contents"] ?: ""
                messages.add(Message(username, time.toLong(), contents))
                messageResponseFlow.emit(constructBlock())

                call.respond(HttpStatusCode.OK)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (_: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        webSocket("/chat") {
            send(Frame.Text(constructBlock()))

            messageResponseFlow.collect { message ->
                send(Frame.Text(message))
            }
        }
    }
}