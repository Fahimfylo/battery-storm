package com.ghost.batterystorm

import com.ghost.batterystorm.core.BatterySimulator
import com.ghost.batterystorm.data.SettingsRepository
import com.ghost.batterystorm.presentation.dashboard.DashboardWindow
import com.ghost.batterystorm.tray.WindowsTrayManager
import com.ghost.batterystorm.tray.TrayIconState
import com.ghost.batterystorm.utils.Logger
import com.ghost.batterystorm.viewmodel.DashboardViewModel
import java.awt.EventQueue
import java.io.File

/**
 * Application entry point for Battery Storm.
 *
 * Initializes the simulation engine, settings repository, and Windows tray manager,
 * then launches the Swing-based dashboard window.
 *
 * The real Windows battery status is never modified — only a simulated tray icon is shown.
 */
fun main() {
    Logger.info("Battery Storm starting...")

    val settingsDir = File(System.getProperty("user.home"), ".batterystorm")
    val settingsRepository = SettingsRepository(settingsDir)
    val simulator = BatterySimulator()
    val viewModel = DashboardViewModel(simulator, settingsRepository)

    val trayManager = WindowsTrayManager()

    // Initialize tray icon with current simulated percentage
    val initialState = simulator.state.value
    trayManager.init(
        TrayIconState(
            simulatedPercentage = initialState.simulatedPercentage,
            status = initialState.status,
            tooltip = "Battery Storm - Simulated: ${initialState.simulatedPercentage}%"
        )
    )

    EventQueue.invokeLater {
        val window = DashboardWindow(viewModel, simulator, trayManager)
        window.isVisible = true
    }

    Logger.info("Battery Storm initialized")
}