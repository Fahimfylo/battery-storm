package com.ghost.batterystorm.model

data class BatteryState(
    val simulatedPercentage: Int = 50,
    val actualPercentage: Int? = null,
    val status: SimulationStatus = SimulationStatus.Stopped,
    val mode: SimulationMode = SimulationMode.Manual,
    val dischargeRate: Double = 1.0,
    val simulationDuration: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun isValidPercentage(percentage: Int): Boolean = percentage in 0..100
}