package com.kitchino.app.audit

import android.app.Service
import android.content.Intent
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.os.IBinder
import android.os.Process
import android.util.Log
import org.json.JSONException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class AuditLogService : Service() {

    private val serverRunning = AtomicBoolean(false)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (serverRunning.compareAndSet(false, true)) {
            thread(name = THREAD_NAME) { runServerLoop() }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun runServerLoop() {
        try {
            LocalServerSocket(AuditIpc.SOCKET_NAME).use { server ->
                Log.i(TAG, "Журнал: pid=${Process.myPid()}, приём на ${AuditIpc.SOCKET_NAME}")
                while (true) {
                    val client = server.accept()
                    handleConnection(client)
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Журнал: сервер обмена остановлен: ${e.message}")
        } finally {
            serverRunning.set(false)
        }
    }

    private fun handleConnection(client: LocalSocket) {
        try {
            client.use { socket ->
                val reader = AuditFrameReader(socket.inputStream)
                while (true) {
                    val frame = reader.readFrame() ?: break
                    appendEvent(frame)
                }
            }
        } catch (e: FrameException) {
            Log.e(TAG, "Журнал: кадр отвергнут (${e.kind}): ${e.message}")
        } catch (e: JSONException) {
            Log.e(TAG, "Журнал: тело кадра не является событием, кадр не записан: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "Журнал: обрыв соединения: ${e.message}")
        }
    }

    private fun appendEvent(frame: String) {
        val event = auditEventFromJson(frame)
        OutputStreamWriter(
            FileOutputStream(File(filesDir, AuditIpc.LOG_FILE_NAME), true),
            Charsets.UTF_8
        ).use { writer ->
            writer.write("${System.currentTimeMillis()} $frame\n")
        }
        Log.i(TAG, "Журнал: batch=${event.batchId} ${event.action}")
    }

    private companion object {
        const val TAG = "AuditIpc"
        const val THREAD_NAME = "audit-ipc-server"
    }
}
