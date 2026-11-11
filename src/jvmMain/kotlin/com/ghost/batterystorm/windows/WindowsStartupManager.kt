package com.ghost.batterystorm.windows

import com.sun.jna.Pointer
import com.sun.jna.win32.StdCallLibrary

/**
 * Manages Windows startup registry (Run key) for "Start with Windows" setting.
 * Uses HKCU\Software\Microsoft\Windows\CurrentVersion\Run — no admin required.
 */
class WindowsStartupManager {

    private val advapi32: StdCallLibrary = com.sun.jna.Native.load("advapi32", StdCallLibrary::class.java)

    private companion object {
        const val HKEY_CURRENT_USER = 0x80000001
        const val RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"
        const val APP_NAME = "BatteryStorm"
    }

    fun isStartupEnabled(): Boolean {
        // Simplified implementation using reg query via ProcessBuilder
        try {
            val process = ProcessBuilder("reg", "query", "HKCU\\${RUN_KEY}", "/v", APP_NAME)
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            return exitCode == 0
        } catch (e: Exception) {
            System.err.println("BatteryStorm: Failed to check startup registry: ${e.message}")
            return false
        }
    }

    fun setStartupEnabled(enabled: Boolean) {
        try {
            if (enabled) {
                val javaHome = System.getProperty("java.home")
                val exePath = "$javaHome\\bin\\javaw.exe"
                val process = ProcessBuilder("reg", "add", "HKCU\\${RUN_KEY}",
                    "/v", APP_NAME, "/t", "REG_SZ", "/d", exePath, "/f")
                    .redirectErrorStream(true)
                    .start()
                process.waitFor()
            } else {
                val process = ProcessBuilder("reg", "delete", "HKCU\\${RUN_KEY}",
                    "/v", APP_NAME, "/f")
                    .redirectErrorStream(true)
                    .start()
                process.waitFor()
            }
        } catch (e: Exception) {
            System.err.println("BatteryStorm: Failed to update startup registry: ${e.message}")
        }
    }
}