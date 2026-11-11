package com.ghost.batterystorm.core

import com.ghost.batterystorm.model.SimulationMode
import com.ghost.batterystorm.model.SimulationStatus
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val simulatedPercentage: Int = 50,
    val simulationMode: SimulationMode = SimulationMode.Manual,
    val dischargeRate: Double = 1.0,
    val startWithWindows: Boolean = false,
    val launchMinimized: Boolean = false,
    val minimizeToTray: Boolean = true,
    val theme: String = "dark",
    val showTooltip: Boolean = true,
    val showActualBattery: Boolean = true,
    val simulationDuration: Long = 0,
    val lastSimulatedPercentage: Int = 50
) {
    fun isValidPercentage(percentage: Int): Boolean = percentage in 0..100

    fun withPercentage(percentage: Int): AppSettings {
        require(percentage in 0..100) { "Percentage must be between 0 and 100, got $percentage" }
        return copy(simulatedPercentage = percentage, lastSimulatedPercentage = percentage)
    }
}