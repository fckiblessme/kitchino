package com.kitchino.app.audit

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object FrameCodec {

    const val LENGTH_PREFIX_BYTES: Int = 2
    const val MAX_FRAME_BYTES: Int = 1024

    fun encode(message: String): ByteArray {
        val body = message.toByteArray(Charsets.UTF_8)
        require(body.isNotEmpty()) { "Кадр не может быть пустым" }
        require(body.size <= MAX_FRAME_BYTES) {
            "Кадр ${body.size} байт превышает предел $MAX_FRAME_BYTES"
        }
        val frame = ByteArray(LENGTH_PREFIX_BYTES + body.size)
        frame[0] = (body.size shr 8).toByte()
        frame[1] = body.size.toByte()
        body.copyInto(frame, LENGTH_PREFIX_BYTES)
        return frame
    }

    fun writeFrame(output: OutputStream, message: String) {
        output.write(encode(message))
        output.flush()
    }
}

class FrameException(val kind: Kind, message: String) : IOException(message) {
    enum class Kind {
        EMPTY_FRAME,
        OVERSIZED_FRAME,
        EOF_MID_FRAME
    }
}

class AuditFrameReader(private val input: InputStream) {

    private val lengthBuffer = ByteArray(FrameCodec.LENGTH_PREFIX_BYTES)

    fun readFrame(): String? {
        var received = 0
        while (received < lengthBuffer.size) {
            val read = input.read(lengthBuffer, received, lengthBuffer.size - received)
            if (read < 0) {
                if (received == 0) return null
                throw FrameException(FrameException.Kind.EOF_MID_FRAME, "Обрыв внутри префикса длины")
            }
            received += read
        }
        val length = ((lengthBuffer[0].toInt() and 0xFF) shl 8) or (lengthBuffer[1].toInt() and 0xFF)
        if (length == 0) {
            throw FrameException(FrameException.Kind.EMPTY_FRAME, "Пустой кадр запрещён контрактом")
        }
        if (length > FrameCodec.MAX_FRAME_BYTES) {
            throw FrameException(
                FrameException.Kind.OVERSIZED_FRAME,
                "Кадр $length байт превышает предел ${FrameCodec.MAX_FRAME_BYTES}"
            )
        }
        val body = ByteArray(length)
        var done = 0
        while (done < length) {
            val read = input.read(body, done, length - done)
            if (read < 0) {
                throw FrameException(FrameException.Kind.EOF_MID_FRAME, "Обрыв внутри кадра")
            }
            done += read
        }
        return String(body, Charsets.UTF_8)
    }
}
