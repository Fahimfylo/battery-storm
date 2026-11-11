package com.ghost.batterystorm.utils

object PlatformUtils {
    fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("windows")

    val windowsVersion: String = "${System.getProperty("os.name")} ${System.getProperty("os.version")}"
    fun is64Bit(): Boolean = System.getProperty("sun.arch.data.model") == "64"
}