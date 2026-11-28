package com.kitchino.app.dishbatch.data

import com.kitchino.app.dishbatch.domain.DiscountThresholds
import com.kitchino.app.dishbatch.domain.ExpiryDiscountDecision
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DiscountConfigFileSourceTest {

    private fun fileWith(content: String): File {
        val file = File.createTempFile("discounts", ".conf")
        file.writeText(content)
        file.deleteOnExit()
        return file
    }

    @Test
    fun validFile_thresholdsAppliedFromFile_withoutWarning() {
        val config = DiscountConfigFileSource(
            fileWith("writeoff=0.10\nheavy=0.30\nlight=0.60\n")
        ).load()
        assertTrue(config.appliedFromFile)
        assertNull(config.warning)
        assertEquals(0.10, config.thresholds.writeoff)
        assertEquals(0.30, config.thresholds.heavy)
        assertEquals(0.60, config.thresholds.light)
    }

    @Test
    fun missingFile_defaults_withWarning() {
        val config = DiscountConfigFileSource(
            File.createTempFile("dir", ".tmp").parentFile.resolve("absent-${System.nanoTime()}.conf")
        ).load()
        assertFalse(config.appliedFromFile)
        assertEquals(DiscountThresholds.DEFAULT, config.thresholds)
        assertNotNull(config.warning)
    }

    @Test
    fun damagedLine_reportedWithLineNumber() {
        val config = DiscountConfigFileSource(
            fileWith("writeoff=0.10\nbroken line\nheavy=0.30\nlight=0.60\n")
        ).load()
        assertFalse(config.appliedFromFile)
        assertEquals(DiscountThresholds.DEFAULT, config.thresholds)
        assertTrue(config.warning!!.contains("Строка 2"))
    }

    @Test
    fun notANumber_damaged() {
        val config = DiscountConfigFileSource(
            fileWith("writeoff=abc\nheavy=0.30\nlight=0.60\n")
        ).load()
        assertFalse(config.appliedFromFile)
        assertEquals(DiscountThresholds.DEFAULT, config.thresholds)
    }

    @Test
    fun orderViolated_damaged() {
        val config = DiscountConfigFileSource(
            fileWith("writeoff=0.60\nheavy=0.30\nlight=0.10\n")
        ).load()
        assertFalse(config.appliedFromFile)
        assertEquals(DiscountThresholds.DEFAULT, config.thresholds)
        assertTrue(config.warning!!.contains("повреждён"))
    }

    @Test
    fun notAllKeys_damaged() {
        val config = DiscountConfigFileSource(
            fileWith("writeoff=0.10\nheavy=0.30\n")
        ).load()
        assertFalse(config.appliedFromFile)
        assertEquals(DiscountThresholds.DEFAULT, config.thresholds)
    }

    @Test
    fun unknownKey_lineDamaged() {
        val config = DiscountConfigFileSource(
            fileWith("writeoff=0.10\nother=0.20\nheavy=0.30\nlight=0.60\n")
        ).load()
        assertFalse(config.appliedFromFile)
        assertTrue(config.warning!!.contains("Строка 2"))
    }

    @Test
    fun commentsAndBlankLines_skipped() {
        val config = DiscountConfigFileSource(
            fileWith("# пороги\n\nwriteoff=0.10\nheavy=0.30\nlight=0.60\n")
        ).load()
        assertTrue(config.appliedFromFile)
        assertNull(config.warning)
    }

    @Test
    fun unreadableFile_defaults_withWarning() {
        val file = fileWith("writeoff=0.10\nheavy=0.30\nlight=0.60\n")
        assertTrue(file.setReadable(false, false))
        val config = DiscountConfigFileSource(file).load()
        assertFalse(config.appliedFromFile)
        assertEquals(DiscountThresholds.DEFAULT, config.thresholds)
        assertNotNull(config.warning)
        file.setReadable(true, false)
    }

    @Test
    fun customThresholds_changeDecisionReasons() {
        val decision = ExpiryDiscountDecision(DiscountThresholds(0.30, 0.40, 0.50))
            .recommendAction(
                com.kitchino.app.dishbatch.data.DishBatchEntity(
                    idDish = 1L, idEmployee = 1L, quantity = 1,
                    madeAt = 0, expiresAt = 100, status = com.kitchino.app.dishbatch.data.Status.ACTIVE
                ),
                currentTime = 75
            )
        assertEquals(com.kitchino.app.dishbatch.data.DecisionType.WRITE_OFF, decision.decisionType)
        assertTrue(decision.discountReason.contains("Менее 30%"))
    }
}
