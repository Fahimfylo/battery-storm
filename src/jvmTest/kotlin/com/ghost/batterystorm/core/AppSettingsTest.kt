package com.ghost.batterystorm.core

import com.ghost.batterystorm.model.SimulationMode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AppSettingsTest {

    @Test
    fun `default settings are valid`() {
        val settings = AppSettings()
        assertEquals(50, settings.simulatedPercentage)
        assertEquals(SimulationMode.Manual, settings.simulationMode)
        assertEquals(1.0, settings.dischargeRate)
        assertFalse(settings.startWithWindows)
        assertFalse(settings.launchMinimized)
        assertTrue(settings.minimizeToTray)
        assertEquals("dark", settings.theme)
    }

    @Test
    fun `withPercentage validates range`() {
        val settings = AppSettings()
        val updated = settings.withPercentage(75)
        assertEquals(75, updated.simulatedPercentage)
        assertEquals(75, updated.lastSimulatedPercentage)
    }

    @Test
    fun `withPercentage rejects invalid values`() {
        val settings = AppSettings()
        assertThrows(IllegalArgumentException::class.java) {
            settings.withPercentage(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            settings.withPercentage(101)
        }
    }

    @Test
    fun `isValidPercentage checks range`() {
        val settings = AppSettings()
        assertTrue(settings.isValidPercentage(0))
        assertTrue(settings.isValidPercentage(50))
        assertTrue(settings.isValidPercentage(100))
        assertFalse(settings.isValidPercentage(-1))
        assertFalse(settings.isValidPercentage(101))
    }
}
