package com.ghost.batterystorm.tray

import com.ghost.batterystorm.model.SimulationStatus
import com.ghost.batterystorm.utils.Logger
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.swing.*

const val TRAY_ICON_ID = 1

data class TrayIconState(
    val simulatedPercentage: Int,
    val status: SimulationStatus,
    val tooltip: String = "Battery Storm - Simulated"
)

/**
 * Manages the Windows notification-area (system tray) battery icon.
 *
 * Uses java.awt.SystemTray with TrayIcon for reliable, visible tray integration.
 * The JNA Shell_NotifyIcon bindings in WindowsNativeBridge remain available
 * as a native implementation alternative if needed.
 *
 * The icon is drawn using Java 2D: a battery silhouette with a fill level
 * representing the simulated percentage. It is clearly a simulation indicator
 * (not the native Windows battery indicator).
 *
 * The real Windows battery status is never modified.
 */
class WindowsTrayManager {

    private var trayIcon: TrayIcon? = null
    private var tray: SystemTray? = null
    private var popupMenu: PopupMenu? = null

    var onTrayLeftClick: (() -> Unit)? = null
    var onTrayRightClick: (() -> Unit)? = null
    var onTrayMenuRequested: (() -> Unit)? = null

    private var currentState: TrayIconState? = null

    fun init(iconState: TrayIconState) {
        if (!SystemTray.isSupported()) {
            Logger.warn("System tray not supported on this platform")
            return
        }
        try {
            tray = SystemTray.getSystemTray()
            currentState = iconState
            val image = createBatteryIconImage(iconState.simulatedPercentage)
            trayIcon = TrayIcon(image, buildTooltip(iconState))

            // Left-click: show dashboard
            trayIcon!!.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e == null) return
                    when (e.button) {
                        MouseEvent.BUTTON1 -> onTrayLeftClick?.invoke()
                        MouseEvent.BUTTON3 -> onTrayRightClick?.invoke()
                    }
                }
            })

            tray!!.add(trayIcon)
            currentState = iconState
            Logger.info("Tray icon added to notification area")
        } catch (e: Exception) {
            Logger.error("Failed to initialize system tray icon", e)
        }
    }

    fun updateIcon(iconState: TrayIconState) {
        currentState = iconState
        trayIcon?.toolTip = buildTooltip(iconState)
        trayIcon?.image = createBatteryIconImage(iconState.simulatedPercentage)
    }

    fun removeTrayIcon() {
        tray?.let { t ->
            trayIcon?.let { ti ->
                try { t.remove(ti) } catch (e: Exception) { Logger.warn("Failed to remove tray icon", e) }
            }
        }
        trayIcon = null
    }

    fun cleanup() {
        removeTrayIcon()
        tray = null
    }

    fun showPopupMenu() {
        onTrayMenuRequested?.invoke()
    }

    private fun buildTooltip(state: TrayIconState): String {
        return "Battery Storm - Simulated: ${state.simulatedPercentage}% (${state.status})"
    }

    /**
     * Draws a battery icon image with the given fill percentage.
     * The battery silhouette is green (>50%), orange (20-50%), or red (<20%).
     * A small "SIM" marker in the corner indicates this is a simulation.
     */
    private fun createBatteryIconImage(percentage: Int): Image {
        val size = 32
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

            val cx = size / 2f
            val cy = size / 2f
            val bodyW = size * 0.65f
            val bodyH = size * 0.72f
            val tipW = size * 0.22f
            val tipH = size * 0.2f
            val bodyTop = cy - bodyH / 2f
            val bodyBottom = cy + bodyH / 2f
            val bodyLeft = cx - bodyW / 2f
            val bodyRight = cx + bodyW / 2f

            // Battery outline (gray)
            g.color = Color(0xCCCCCC)
            g.stroke = BasicStroke(2.5f)
            g.drawRoundRect(bodyLeft.toInt(), bodyTop.toInt(), bodyW.toInt(), bodyH.toInt(), 10, 10)

            // Battery tip (nipple)
            val tipTop = cy - tipH / 2f
            g.drawRoundRect(bodyRight.toInt(), tipTop.toInt(), tipW.toInt(), tipH.toInt(), 4, 4)

            // Fill level
            val fillH = (bodyH - 6) * (percentage / 100f)
            val fillTop = bodyBottom - 3 - fillH
            val fillColor = when {
                percentage > 50 -> Color(0x4CAF50) // Green
                percentage > 20 -> Color(0xFFA500) // Orange
                else -> Color(0xFF5252) // Red
            }
            g.color = fillColor
            g.fillRoundRect(
                (bodyLeft + 3).toInt(),
                fillTop.toInt(),
                (bodyW - 6).toInt(),
                fillH.toInt(),
                8, 8
            )

            // "SIM" marker to indicate simulation
            g.color = Color(0xFFFFFF)
            g.font = Font("SansSerif", Font.BOLD, 7)
            g.drawString("SIM", 2, size - 2)
        } finally {
            g.dispose()
        }
        return img
    }
}