package com.ghost.batterystorm.core

import com.ghost.batterystorm.model.SimulationMode
import com.ghost.batterystorm.model.SimulationStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class BatterySimulatorTest {

    @Test
    fun `initial simulated percentage is 50`() {
        val sim = BatterySimulator()
        assertEquals(50, sim.state.value.simulatedPercentage)
    }

    @Test
    fun `initial status is stopped`() {
        val sim = BatterySimulator()
        assertEquals(SimulationStatus.Stopped, sim.state.value.status)
    }

    @Test
    fun `manual percentage change updates state`() {
        val sim = BatterySimulator()
        sim.setPercentage(75)
        assertEquals(75, sim.state.value.simulatedPercentage)
        assertEquals(SimulationStatus.Stopped, sim.state.value.status)
    }

    @Test
    fun `invalid percentage throws exception`() {
        val sim = BatterySimulator()
        assertThrows(IllegalArgumentException::class.java) {
            sim.setPercentage(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            sim.setPercentage(101)
        }
    }

    @Test
    fun `start and stop simulation`() {
        val sim = BatterySimulator()
        sim.start(SimulationMode.Automatic, 1.0)
        assertEquals(SimulationStatus.Running, sim.state.value.status)
        sim.stop()
        assertEquals(SimulationStatus.Stopped, sim.state.value.status)
    }

    @Test
    fun `pause and resume simulation`() {
        val sim = BatterySimulator()
        sim.start(SimulationMode.Automatic, 1.0)
        sim.pause()
        assertEquals(SimulationStatus.Paused, sim.state.value.status)
        sim.resume()
        assertEquals(SimulationStatus.Running, sim.state.value.status)
        sim.stop()
    }

    @Test
    fun `reset returns to default`() {
        val sim = BatterySimulator()
        sim.setPercentage(25)
        sim.start(SimulationMode.Automatic, 5.0)
        sim.reset()
        assertEquals(50, sim.state.value.simulatedPercentage)
        assertEquals(SimulationStatus.Stopped, sim.state.value.status)
        assertEquals(0L, sim.state.value.simulationDuration)
    }

    @Test
    fun `discharge rate must be positive`() {
        val sim = BatterySimulator()
        assertThrows(IllegalArgumentException::class.java) {
            sim.setDischargeRate(0.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            sim.setDischargeRate(-1.0)
        }
    }

    @Test
    fun `actual battery percentage can be set`() {
        val sim = BatterySimulator()
        sim.setActualBatteryPercentage(80)
        assertEquals(80, sim.state.value.actualPercentage)
        sim.setActualBatteryPercentage(null)
        assertNull(sim.state.value.actualPercentage)
    }
}
