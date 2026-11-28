package com.kitchino.app.audit

import java.io.ByteArrayInputStream
import java.io.InputStream
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class FrameCodecTest {

    @Test
    fun severalFrames_inOneStream_readAsSeparateMessages() {
        val stream = ByteArrayInputStream(
            FrameCodec.encode("{\"batchId\":1}") +
                FrameCodec.encode("{\"batchId\":2}") +
                FrameCodec.encode("{\"batchId\":3}")
        )
        val reader = AuditFrameReader(stream)
        assertEquals("{\"batchId\":1}", reader.readFrame())
        assertEquals("{\"batchId\":2}", reader.readFrame())
        assertEquals("{\"batchId\":3}", reader.readFrame())
        assertNull(reader.readFrame())
    }

    @Test
    fun frameSplitAcrossSingleByteReads_assembledToFullFrame() {
        val bytes = FrameCodec.encode("{\"batchId\":42,\"reason\":\"Срок истек\"}")
        val singleByteInput = object : InputStream() {
            private var position = 0
            override fun read(): Int =
                if (position < bytes.size) bytes[position++].toInt() else -1
        }
        val reader = AuditFrameReader(singleByteInput)
        assertEquals("{\"batchId\":42,\"reason\":\"Срок истек\"}", reader.readFrame())
        assertNull(reader.readFrame())
    }

    @Test
    fun oversizedFrame_rejectedBeforeBodyAllocation() {
        val oversized = byteArrayOf(0x7F.toByte(), 0x7F.toByte(), 1, 2, 3)
        val reader = AuditFrameReader(ByteArrayInputStream(oversized))
        val exception = assertThrows(FrameException::class.java) { reader.readFrame() }
        assertEquals(FrameException.Kind.OVERSIZED_FRAME, exception.kind)
    }

    @Test
    fun eofInsideFrame_reportedAsError_notAsCleanEof() {
        val prefix = byteArrayOf(0x00, 0x08.toByte())
        val partialBody = "batch".toByteArray()
        val reader = AuditFrameReader(ByteArrayInputStream(prefix + partialBody))
        val exception = assertThrows(FrameException::class.java) { reader.readFrame() }
        assertEquals(FrameException.Kind.EOF_MID_FRAME, exception.kind)
    }

    @Test
    fun eofInsideLengthPrefix_reportedAsError() {
        val reader = AuditFrameReader(ByteArrayInputStream(byteArrayOf(0x01)))
        val exception = assertThrows(FrameException::class.java) { reader.readFrame() }
        assertEquals(FrameException.Kind.EOF_MID_FRAME, exception.kind)
    }

    @Test
    fun emptyFrame_rejected() {
        val reader = AuditFrameReader(ByteArrayInputStream(byteArrayOf(0x00, 0x00)))
        val exception = assertThrows(FrameException::class.java) { reader.readFrame() }
        assertEquals(FrameException.Kind.EMPTY_FRAME, exception.kind)
    }

    @Test
    fun encode_rejectsBodyOverLimit() {
        val tooLong = "x".repeat(FrameCodec.MAX_FRAME_BYTES + 1)
        assertThrows(IllegalArgumentException::class.java) { FrameCodec.encode(tooLong) }
    }

    @Test
    fun frameAtLimit_encodesAndReadsBack() {
        val atLimit = "x".repeat(FrameCodec.MAX_FRAME_BYTES)
        val reader = AuditFrameReader(ByteArrayInputStream(FrameCodec.encode(atLimit)))
        assertEquals(atLimit, reader.readFrame())
    }
}
