package com.ghost.batterystorm.windows

/**
 * Reads actual Windows battery status via GetSystemPowerStatus.
 * Read-only — never modifies battery telemetry.
 */
class WindowsBatteryReader {

    fun readBatteryPercentage(): Int? {
        val status = WindowsNativeBridge.SYSTEM_POWER_STATUS()
        val success = WindowsNativeBridge.kernel32.GetSystemPowerStatus(status)
        if (!success) return null
        status.read()
        return when {
            status.BatteryFlag.toInt() == 0xFF -> null
            status.BatteryLifePercent.toInt() == 255 -> null
            else -> status.BatteryLifePercent.toInt()
        }
    }

    fun isOnACPower(): Boolean? {
        val status = WindowsNativeBridge.SYSTEM_POWER_STATUS()
        val success = WindowsNativeBridge.kernel32.GetSystemPowerStatus(status)
        if (!success) return null
        status.read()
        return status.ACLineStatus.toInt() == 1
    }
}