package com.kitchino.app.warehouse.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ConsumptionReportWriterTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun row() = ReportRow(ingredient = "Картофель", amount = 200.0, timestamp = 123456789L)


    @Test
    fun successPath_fileWrittenAndClosed() {
        val dir = tmp.newFolder("reports")
        val writer = ConsumptionReportWriter(dir)

        val result = writer.write(listOf(row()))

        assertTrue(result is ReportWriteResult.Ok)
        val file = File((result as ReportWriteResult.Ok).path)
        assertTrue(file.exists())
        val text = file.readText()
        assertTrue(text.startsWith("ingredient;amount;timestamp"))
        assertTrue(text.contains("Картофель;200.0"))
    }


    @Test
    fun acquisitionError_noResourceOpened() {
        val blocking = tmp.newFile("blocking")
        val writer = ConsumptionReportWriter(blocking)

        val result = writer.write(listOf(row()))

        assertTrue(result is ReportWriteResult.Error)
        assertFalse(blocking.isDirectory)
    }


    @Test
    fun workError_noPartialFileLeft() {
        val dir = tmp.newFolder("reports")
        val writer = ConsumptionReportWriter(
            reportDir = dir,
            openWriter = { file ->
                object : java.io.Writer() {
                    private val delegate = file.bufferedWriter()

                    override fun write(cbuf: CharArray, off: Int, len: Int) {
                        delegate.write(cbuf, off, minOf(len, 8))
                        throw java.io.IOException("смоделированный отказ записи")
                    }

                    override fun flush() {
                        delegate.flush()
                    }

                    override fun close() {
                        delegate.close()
                    }
                }
            }
        )

        val result = writer.write(listOf(row()))

        assertTrue(result is ReportWriteResult.Error)
        assertEquals(0, dir.listFiles()?.size ?: 0)
    }


    @Test
    fun earlyExit_noFileCreated() {
        val dir = tmp.newFolder("empty")
        val writer = ConsumptionReportWriter(dir)

        val result = writer.write(emptyList())

        assertTrue(result is ReportWriteResult.Error)
        assertEquals(0, dir.listFiles()?.size ?: 0)
    }
}