package com.ghost.batterystorm.viewmodel

import com.ghost.batterystorm.core.AppSettings
import com.ghost.batterystorm.core.BatterySimulator
import com.ghost.batterystorm.data.SettingsRepository
import com.ghost.batterystorm.model.BatteryState
import com.ghost.batterystorm.model.SimulationMode
import com.ghost.batterystorm.model.SimulationStatus
import com.ghost.batterystorm.tray.TrayIconState
import com.ghost.batterystorm.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel(
    private val simulator: BatterySimulator,
    private val settingsRepository: SettingsRepository
) : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val _settings = settingsRepository.load()
    val settings: AppSettings = _settings

    val state: StateFlow<BatteryState> = simulator.state

    private val _trayIconState = MutableStateFlow<TrayIconState>(
        TrayIconState(settings.simulatedPercentage, SimulationStatus.Stopped)
    )
    val trayIconState: StateFlow<TrayIconState> = _trayIconState.asStateFlow()

    private var stateJob: Job? = null

    init {
        stateJob = launch {
            simulator.state.collect { s ->
                _trayIconState.value = TrayIconState(
                    simulatedPercentage = s.simulatedPercentage,
                    status = s.status,
                    tooltip = "Battery Storm - Simulated: ${s.simulatedPercentage}%"
                )
            }
        }
    }

    fun startSimulation(mode: SimulationMode = SimulationMode.Automatic, rate: Double = settings.dischargeRate) {
        Logger.info("Starting simulation: mode=$mode rate=$rate")
        simulator.start(mode, rate)
        saveSettings()
    }

    fun pauseSimulation() {
        Logger.info("Pausing simulation")
        simulator.pause()
        saveSettings()
    }

    fun resumeSimulation() {
        Logger.info("Resuming simulation")
        simulator.resume()
        saveSettings()
    }

    fun stopSimulation() {
        Logger.info("Stopping simulation")
        simulator.stop()
        saveSettings()
    }

    fun resetSimulation() {
        Logger.info("Resetting simulation")
        simulator.reset()
        saveSettings()
    }

    fun setSimulatedPercentage(percentage: Int) {
        Logger.info("Setting simulated percentage: $percentage")
        simulator.setPercentage(percentage)
        saveSettings()
    }

    fun setDischargeRate(rate: Double) {
        Logger.info("Setting discharge rate: $rate")
        simulator.setDischargeRate(rate)
        saveSettings()
    }

    fun onCleared() {
        stateJob?.cancel()
    }

    private fun saveSettings() {
        val current = simulator.state.value
        val updated = settings.copy(
            simulatedPercentage = current.simulatedPercentage,
            simulationMode = current.mode,
            dischargeRate = current.dischargeRate,
            simulationDuration = current.simulationDuration,
            lastSimulatedPercentage = current.simulatedPercentage
        )
        settingsRepository.save(updated)
    }
}