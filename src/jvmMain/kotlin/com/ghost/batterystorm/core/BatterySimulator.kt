package com.ghost.batterystorm.core

import com.ghost.batterystorm.model.BatteryState
import com.ghost.batterystorm.model.SimulationMode
import com.ghost.batterystorm.model.SimulationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds

class BatterySimulator private constructor(
    private val initialState: BatteryState
) : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<BatteryState> = _state.asStateFlow()

    private var simulationJob: Job? = null
    private var lastDischargeTime: Long = System.currentTimeMillis()

    constructor() : this(BatteryState())

    fun start(mode: SimulationMode = SimulationMode.Automatic, dischargeRate: Double = 1.0) {
        simulationJob?.cancel()
        lastDischargeTime = System.currentTimeMillis()
        _state.value = _state.value.copy(status = SimulationStatus.Running, mode = mode, dischargeRate = dischargeRate)
        simulationJob = launch {
            dischargeLoop()
        }
    }

    fun pause() {
        simulationJob?.cancel()
        simulationJob = null
        _state.value = _state.value.copy(status = SimulationStatus.Paused)
    }

    fun resume() {
        if (_state.value.status == SimulationStatus.Paused) {
            lastDischargeTime = System.currentTimeMillis()
            _state.value = _state.value.copy(status = SimulationStatus.Running)
            simulationJob = launch {
                dischargeLoop()
            }
        }
    }

    fun stop() {
        simulationJob?.cancel()
        simulationJob = null
        val now = System.currentTimeMillis()
        val elapsed = now - lastDischargeTime
        _state.value = _state.value.copy(status = SimulationStatus.Stopped, simulationDuration = _state.value.simulationDuration + elapsed)
    }

    fun reset() {
        stop()
        _state.value = _state.value.copy(simulatedPercentage = 50, actualPercentage = null, simulationDuration = 0)
        lastDischargeTime = System.currentTimeMillis()
    }

    fun setPercentage(percentage: Int) {
        require(percentage in 0..100) { "Percentage must be between 0 and 100, got $percentage" }
        simulationJob?.cancel()
        simulationJob = null
        val now = System.currentTimeMillis()
        val elapsed = now - lastDischargeTime
        _state.value = _state.value.copy(
            simulatedPercentage = percentage,
            status = SimulationStatus.Stopped,
            simulationDuration = _state.value.simulationDuration + elapsed,
            lastUpdated = now
        )
        lastDischargeTime = now
    }

    fun setActualBatteryPercentage(percentage: Int?) {
        _state.value = _state.value.copy(actualPercentage = percentage)
    }

    fun setDischargeRate(rate: Double) {
        require(rate > 0.0) { "Discharge rate must be positive, got $rate" }
        _state.value = _state.value.copy(dischargeRate = rate)
    }

    private suspend fun dischargeLoop() {
        while (simulationJob?.isActive == true) {
            val current = _state.value
            if (current.status != SimulationStatus.Running) {
                return
            }
            if (current.simulatedPercentage <= 0) {
                stop()
                return
            }
            val now = System.currentTimeMillis()
            val elapsedSeconds = (now - lastDischargeTime) / 1000.0
            lastDischargeTime = now
            if (elapsedSeconds > 0 && current.mode == SimulationMode.Automatic) {
                val decrement = (elapsedSeconds * current.dischargeRate).toInt()
                if (decrement > 0) {
                    val newPercentage = (current.simulatedPercentage - decrement).coerceAtLeast(0)
                    val elapsedMs = now - lastDischargeTime + (elapsedSeconds * 1000).toLong()
                    _state.value = _state.value.copy(
                        simulatedPercentage = newPercentage,
                        simulationDuration = _state.value.simulationDuration + elapsedMs,
                        lastUpdated = now
                    )
                    if (newPercentage <= 0) {
                        stop()
                        return
                    }
                }
            }
            withContext(Dispatchers.Default) {
                delay(1.seconds)
            }
        }
    }
}