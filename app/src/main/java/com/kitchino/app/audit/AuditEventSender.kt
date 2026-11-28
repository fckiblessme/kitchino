package com.kitchino.app.audit

import android.content.Context
import android.content.Intent
import android.net.LocalSocket
import android.net.LocalSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object AuditIpc {
    const val SOCKET_NAME = "kitchino.audit"
    const val CONNECT_ATTEMPTS = 10
    const val CONNECT_RETRY_DELAY_MILLIS = 100L
    const val LOG_FILE_NAME = "audit_events.log"
}

class AuditEventSender(private val context: Context) {

    suspend fun send(events: List<AuditEvent>): Boolean = withContext(Dispatchers.IO) {
        if (events.isEmpty()) return@withContext true

        context.startService(Intent(context, AuditLogService::class.java))

        try {
            LocalSocket().use { socket ->
                connectWithRetry(socket)
                val output = socket.outputStream
                for (event in events) {
                    FrameCodec.writeFrame(output, event.toJson())
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun connectWithRetry(socket: LocalSocket) {
        val address = LocalSocketAddress(
            AuditIpc.SOCKET_NAME,
            LocalSocketAddress.Namespace.ABSTRACT
        )
        var lastError: IOException? = null
        repeat(AuditIpc.CONNECT_ATTEMPTS) {
            try {
                socket.connect(address)
                return
            } catch (e: IOException) {
                lastError = e
                Thread.sleep(AuditIpc.CONNECT_RETRY_DELAY_MILLIS)
            }
        }
        throw lastError ?: IOException("Служба журнала не отвечает: ${AuditIpc.SOCKET_NAME}")
    }
}
