package com.ghost.batterystorm.utils

/**
 * Simple structured logging for Battery Storm.
 * Outputs to stdout with timestamp and level.
 */
object Logger {
    private val isDebug = java.lang.Boolean.parseBoolean(System.getProperty("batterystorm.debug", "false"))

    fun debug(message: String, throwable: Throwable? = null) {
        if (isDebug) log("DEBUG", message, throwable)
    }

    fun info(message: String, throwable: Throwable? = null) {
        log("INFO ", message, throwable)
    }

    fun warn(message: String, throwable: Throwable? = null) {
        log("WARN ", message, throwable)
    }

    fun error(message: String, throwable: Throwable? = null) {
        log("ERROR", message, throwable)
    }

    private fun log(level: String, message: String, throwable: Throwable? = null) {
        val timestamp = java.time.Instant.now().toString()
        println("[$timestamp] [$level] $message")
        throwable?.printStackTrace()
    }
}