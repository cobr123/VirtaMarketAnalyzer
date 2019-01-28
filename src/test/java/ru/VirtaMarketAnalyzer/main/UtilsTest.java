package ru.VirtaMarketAnalyzer.main;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void toDoubleTestDotDecimal() {
        assertEquals(Utils.toDouble("4.90"), 4.90);
    }

    @Test
    void toDoubleTestCommaDecimal() {
        assertThrows(NumberFormatException.class, () -> {
            Utils.toDouble("4,90");
        });
    }
}