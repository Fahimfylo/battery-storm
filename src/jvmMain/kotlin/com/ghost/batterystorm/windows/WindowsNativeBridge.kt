package com.ghost.batterystorm.windows

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.win32.StdCallLibrary

/**
 * JNA bindings for Windows Shell_NotifyIcon and related APIs.
 *
 * This isolates all native Windows declarations from the UI layer.
 * No protected processes, DLL patching, or battery telemetry modification is performed.
 */
object WindowsNativeBridge {

    const val WM_USER = 0x0400
    const val NIM_ADD = 0x00000000
    const val NIM_MODIFY = 0x00000001
    const val NIM_DELETE = 0x00000002

    const val NIF_MESSAGE = 0x00000001
    const val NIF_ICON = 0x00000002
    const val NIF_TIP = 0x00000004
    const val NIF_STATE = 0x00000008
    const val NIF_INFO = 0x00000010
    const val NIF_GUID = 0x00000020

    const val WM_LBUTTONUP = 0x0202
    const val WM_RBUTTONUP = 0x0205
    const val WM_DESTROY = 0x0002

    const val SW_HIDE = 0
    const val WS_OVERLAPPED = 0x00000000
    const val CW_USEDEFAULT = -2147483648

    const val COLOR_WINDOW = 5

    interface User32 : StdCallLibrary {
        fun RegisterClassW(lpWndClass: Structure): Short
        fun CreateWindowExW(
            dwExStyle: Int,
            lpClassName: Pointer,
            lpWindowName: Pointer,
            dwStyle: Int,
            X: Int,
            Y: Int,
            nWidth: Int,
            nHeight: Int,
            hWndParent: Pointer,
            hMenu: Pointer,
            hInstance: Pointer,
            lpParam: Pointer
        ): Pointer
        fun DefWindowProcW(hWnd: Pointer, uMsg: Int, wParam: Long, lParam: Long): Long
        fun GetMessageW(lpMsg: Structure, hWnd: Pointer, wMsgFilterMin: Int, wMsgFilterMax: Int): Int
        fun TranslateMessage(lpMsg: Structure): Boolean
        fun DispatchMessageW(lpMsg: Structure): Long
        fun PostQuitMessage(nExitCode: Int)
        fun DestroyWindow(hWnd: Pointer): Boolean
        fun UnregisterClassW(lpClassName: Pointer, hInstance: Pointer): Boolean
        fun ShowWindow(hWnd: Pointer, nCmdShow: Int): Boolean
        fun UpdateWindow(hWnd: Pointer): Boolean
        fun GetDC(hWnd: Pointer): Pointer
        fun ReleaseDC(hWnd: Pointer, hDC: Pointer): Int
        fun CreateIconIndirect(piconinfo: Structure): Pointer
        fun DestroyIcon(hIcon: Pointer): Boolean
    }

    interface Shell32 : StdCallLibrary {
        fun Shell_NotifyIconW(dwMessage: Int, lpData: Structure): Boolean
    }

    interface Kernel32 : StdCallLibrary {
        fun GetSystemPowerStatus(lpSystemPowerStatus: Structure): Boolean
        fun GetModuleHandleW(lpModuleName: Pointer?): Pointer
    }

    interface Gdi32 : StdCallLibrary {
        fun CreateCompatibleDC(hdc: Pointer): Pointer
        fun CreateCompatibleBitmap(hdc: Pointer, nWidth: Int, nHeight: Int): Pointer
        fun SelectObject(hdc: Pointer, hgdiobj: Pointer): Pointer
        fun DeleteDC(hdc: Pointer): Boolean
        fun DeleteObject(hObject: Pointer): Boolean
    }

    class MSG : Structure() {
        @JvmField var hwnd: Pointer = Pointer.createConstant(0)
        @JvmField var message: Int = 0
        @JvmField var wParam: Long = 0
        @JvmField var lParam: Long = 0
        @JvmField var time: Int = 0
        @JvmField var ptX: Int = 0
        @JvmField var ptY: Int = 0

        override fun getFieldOrder(): List<String> =
            listOf("hwnd", "message", "wParam", "lParam", "time", "ptX", "ptY")
    }

    class WNDCLASSW : Structure() {
        @JvmField var style: Int = 0
        @JvmField var lpfnWndProc: Pointer = Pointer.createConstant(0)
        @JvmField var cbClsExtra: Int = 0
        @JvmField var cbWndExtra: Int = 0
        @JvmField var hInstance: Pointer = Pointer.createConstant(0)
        @JvmField var hIcon: Pointer = Pointer.createConstant(0)
        @JvmField var hCursor: Pointer = Pointer.createConstant(0)
        @JvmField var hbrBackground: Pointer = Pointer.createConstant(0)
        @JvmField var lpszMenuName: Pointer = Pointer.createConstant(0)
        @JvmField var lpszClassName: Pointer = Pointer.createConstant(0)

        override fun getFieldOrder(): List<String> =
            listOf("style", "lpfnWndProc", "cbClsExtra", "cbWndExtra", "hInstance", "hIcon", "hCursor", "hbrBackground", "lpszMenuName", "lpszClassName")
    }

    class NOTIFYICONDATAW : Structure() {
        @JvmField var cbSize: Int = 0
        @JvmField var hWnd: Pointer = Pointer.createConstant(0)
        @JvmField var uID: Int = 0
        @JvmField var uFlags: Int = 0
        @JvmField var uCallbackMessage: Int = 0
        @JvmField var hIcon: Pointer = Pointer.createConstant(0)
        @JvmField var szTip: ShortArray = ShortArray(128)
        @JvmField var dwState: Int = 0
        @JvmField var dwStateMask: Int = 0
        @JvmField var szInfo: ShortArray = ShortArray(256)
        @JvmField var uTimeout: Int = 0
        @JvmField var szInfoTitle: ShortArray = ShortArray(64)
        @JvmField var dwInfoFlags: Int = 0
        @JvmField var guidItemData1: Int = 0
        @JvmField var guidItemData2: Short = 0
        @JvmField var guidItemData3: Short = 0
        @JvmField var guidItemData4: ByteArray = ByteArray(8)
        @JvmField var hBalloonIcon: Pointer = Pointer.createConstant(0)

        override fun getFieldOrder(): List<String> =
            listOf("cbSize", "hWnd", "uID", "uFlags", "uCallbackMessage", "hIcon", "szTip", "dwState", "dwStateMask", "szInfo", "uTimeout", "szInfoTitle", "dwInfoFlags", "guidItemData1", "guidItemData2", "guidItemData3", "guidItemData4", "hBalloonIcon")

        init {
            cbSize = size()
        }
    }

    class SYSTEM_POWER_STATUS : Structure() {
        @JvmField var ACLineStatus: Byte = 0
        @JvmField var BatteryFlag: Byte = 0
        @JvmField var BatteryLifePercent: Byte = 0
        @JvmField var Reserved1: Byte = 0
        @JvmField var BatteryLifeTime: Int = 0
        @JvmField var BatteryFullLifeTime: Int = 0

        override fun getFieldOrder(): List<String> =
            listOf("ACLineStatus", "BatteryFlag", "BatteryLifePercent", "Reserved1", "BatteryLifeTime", "BatteryFullLifeTime")
    }

    class ICONINFO : Structure() {
        @JvmField var fIcon: Int = 1
        @JvmField var xHotspot: Int = 0
        @JvmField var yHotspot: Int = 0
        @JvmField var hbmMask: Pointer = Pointer.createConstant(0)
        @JvmField var hbmColor: Pointer = Pointer.createConstant(0)

        override fun getFieldOrder(): List<String> = listOf("fIcon", "xHotspot", "yHotspot", "hbmMask", "hbmColor")
    }

    val user32: User32 = Native.load("user32", User32::class.java)
    val kernel32: Kernel32 = Native.load("kernel32", Kernel32::class.java)
    val gdi32: Gdi32 = Native.load("gdi32", Gdi32::class.java)
    val shell32: Shell32 = Native.load("shell32", Shell32::class.java)

    fun stringToWide(s: String): ShortArray {
        val buf = ShortArray(s.length + 1)
        for (i in s.indices) buf[i] = s.codePointAt(i).toShort()
        buf[s.length] = 0
        return buf
    }
}