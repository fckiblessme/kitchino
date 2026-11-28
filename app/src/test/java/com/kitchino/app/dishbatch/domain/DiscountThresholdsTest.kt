package com.kitchino.app.dishbatch.domain

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DiscountThresholdsTest {

    @Test
    fun defaults_areValid() {
        assertTrue(DiscountThresholds.DEFAULT.isValid())
    }

    @Test
    fun thresholdsOutOfOrder_areInvalid() {
        assertFalse(DiscountThresholds(0.20, 0.05, 0.50).isValid())
    }

    @Test
    fun equalThresholds_areInvalid() {
        assertFalse(DiscountThresholds(0.20, 0.20, 0.50).isValid())
    }

    @Test
    fun valueAboveOne_isInvalid() {
        assertFalse(DiscountThresholds(0.05, 0.20, 1.50).isValid())
    }

    @Test
    fun negativeValue_isInvalid() {
        assertFalse(DiscountThresholds(-0.05, 0.20, 0.50).isValid())
    }

    @Test
    fun nanValue_isInvalid() {
        assertFalse(DiscountThresholds(Double.NaN, 0.20, 0.50).isValid())
    }

    @Test
    fun percentText_wholeValues_printWithoutFraction() {
        assertEquals("5", DiscountThresholds.DEFAULT.writeoffPercentText())
        assertEquals("20", DiscountThresholds.DEFAULT.heavyPercentText())
        assertEquals("50", DiscountThresholds.DEFAULT.lightPercentText())
    }

    @Test
    fun percentText_fractionalValue_printsWithOneDecimal() {
        assertEquals("12.5", DiscountThresholds(0.125, 0.30, 0.50).writeoffPercentText())
    }
}
