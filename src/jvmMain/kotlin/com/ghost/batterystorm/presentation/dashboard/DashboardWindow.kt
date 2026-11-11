package com.ghost.batterystorm.presentation.dashboard

import com.ghost.batterystorm.core.AppSettings
import com.ghost.batterystorm.core.BatterySimulator
import com.ghost.batterystorm.model.BatteryState
import com.ghost.batterystorm.model.SimulationMode
import com.ghost.batterystorm.model.SimulationStatus
import com.ghost.batterystorm.tray.TrayIconState
import com.ghost.batterystorm.tray.WindowsTrayManager
import com.ghost.batterystorm.viewmodel.DashboardViewModel
import com.ghost.batterystorm.utils.Logger
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Main dashboard window for Battery Storm.
 *
 * Swing-based UI showing simulated battery status, controls, and settings.
 * Integrates with WindowsTrayManager for tray icon visibility.
 */
class DashboardWindow(
    private val viewModel: DashboardViewModel,
    private val simulator: BatterySimulator,
    private val trayManager: WindowsTrayManager
) : JFrame("Battery Storm") {

    private val percentageLabel = JLabel("50%", SwingConstants.CENTER)
    private val statusLabel = JLabel("Stopped", SwingConstants.CENTER)
    private val actualLabel = JLabel("Actual: --%", SwingConstants.CENTER)
    private val durationLabel = JLabel("Duration: 00:00:00", SwingConstants.CENTER)
    private val modeLabel = JLabel("Mode: Manual", SwingConstants.CENTER)

    private val startButton = JButton("Start")
    private val pauseButton = JButton("Pause")
    private val resumeButton = JButton("Resume")
    private val stopButton = JButton("Stop")
    private val resetButton = JButton("Reset")
    private val reset100Button = JButton("Reset to 100%")

    private val percentageSlider = JSlider(0, 100, 50)
    private val rateSlider = JSlider(1, 100, 10)
    private val autoDischargeCheckbox = JCheckBox("Automatic Discharge")

    private val batteryPanel = BatteryIconPanel()

    init {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onClose()
            }
        })
        setSize(700, 600)
        setLocationRelativeTo(null)
        setLayout(BorderLayout(16, 16))

        val content = JPanel(BorderLayout(16, 16))
        content.border = EmptyBorder(24, 24, 24, 24)
        content.background = Color(0x1A1A1A)
        setContentPane(content)

        // Header
        content.add(createHeaderPanel(), BorderLayout.NORTH)

        // Center scrollable area
        val centerPanel = JPanel()
        centerPanel.layout = BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        centerPanel.background = Color(0x1A1A1A)

        centerPanel.add(createBatteryOverviewPanel())
        centerPanel.add(Box.createRigidArea(Dimension(0, 16)))
        centerPanel.add(createSimulationControlsPanel())
        centerPanel.add(Box.createRigidArea(Dimension(0, 16)))
        centerPanel.add(createSimulationConfigPanel())
        centerPanel.add(Box.createRigidArea(Dimension(0, 16)))
        centerPanel.add(createActivityInfoPanel())
        centerPanel.add(Box.createRigidArea(Dimension(0, 16)))
        centerPanel.add(createSettingsPanel())

        val scrollPane = JScrollPane(centerPanel)
        scrollPane.border = BorderFactory.createEmptyBorder()
        scrollPane.background = Color(0x1A1A1A)
        scrollPane.getViewport().background = Color(0x1A1A1A)
        content.add(scrollPane, BorderLayout.CENTER)

        // Wire up events
        wireEvents()

        updateUI()

        // Observe simulation state changes
        observeSimulation()
    }

    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = Color(0x1A1A1A)

        val titleLabel = JLabel("BATTERY STORM")
        titleLabel.font = Font(Font.SANS_SERIF, Font.BOLD, 24)
        titleLabel.foreground = Color.WHITE

        val statusPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        statusPanel.background = Color(0x1A1A1A)
        statusLabel.font = Font(Font.SANS_SERIF, Font.BOLD, 14)
        statusLabel.foreground = Color(0x00FF88)
        statusPanel.add(statusLabel)

        panel.add(titleLabel, BorderLayout.WEST)
        panel.add(statusPanel, BorderLayout.EAST)

        return panel
    }

    private fun createBatteryOverviewPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = Color(0x2A2A2A)
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(0x3A3A3A), 1),
            EmptyBorder(20, 20, 20, 20)
        )

        val batteryWrapper = JPanel(FlowLayout(FlowLayout.CENTER))
        batteryWrapper.background = Color(0x2A2A2A)
        batteryPanel.preferredSize = Dimension(120, 120)
        batteryWrapper.add(batteryPanel)

        percentageLabel.font = Font(Font.SANS_SERIF, Font.BOLD, 48)
        percentageLabel.foreground = Color.WHITE
        percentageLabel.alignmentX = Component.CENTER_ALIGNMENT

        val simLabel = JLabel("Simulated Battery Level")
        simLabel.font = Font(Font.SANS_SERIF, Font.PLAIN, 14)
        simLabel.foreground = Color(0xAAAAAA)
        simLabel.alignmentX = Component.CENTER_ALIGNMENT

        val infoRow = JPanel(FlowLayout(FlowLayout.CENTER, 24, 0))
        infoRow.background = Color(0x2A2A2A)
        infoRow.add(actualLabel)
        infoRow.add(modeLabel)
        infoRow.add(durationLabel)
        actualLabel.foreground = Color(0xAAAAAA)
        modeLabel.foreground = Color(0xAAAAAA)
        durationLabel.foreground = Color(0xAAAAAA)
        listOf(actualLabel, modeLabel, durationLabel).forEach { it.font = Font(Font.SANS_SERIF, Font.PLAIN, 14) }

        panel.add(batteryWrapper)
        panel.add(Box.createRigidArea(Dimension(0, 12)))
        panel.add(percentageLabel)
        panel.add(simLabel)
        panel.add(Box.createRigidArea(Dimension(0, 20)))
        panel.add(infoRow)

        return panel
    }

    private fun createSimulationControlsPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = Color(0x2A2A2A)
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(0x3A3A3A), 1),
            EmptyBorder(20, 20, 20, 20)
        )

        val title = JLabel("Simulation Controls")
        title.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        title.foreground = Color.WHITE
        title.alignmentX = Component.LEFT_ALIGNMENT

        val buttonRow = JPanel(FlowLayout(FlowLayout.LEFT, 12, 12))
        buttonRow.background = Color(0x2A2A2A)

        listOf(startButton, pauseButton, resumeButton, stopButton, resetButton, reset100Button).forEach { btn ->
            btn.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
            btn.setFocusPainted(false)
            buttonRow.add(btn)
        }

        panel.add(title)
        panel.add(Box.createRigidArea(Dimension(0, 12)))
        panel.add(buttonRow)

        return panel
    }

    private fun createSimulationConfigPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = Color(0x2A2A2A)
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(0x3A3A3A), 1),
            EmptyBorder(20, 20, 20, 20)
        )

        val title = JLabel("Simulation Configuration")
        title.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        title.foreground = Color.WHITE
        title.alignmentX = Component.LEFT_ALIGNMENT

        // Manual percentage
        val pctRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        pctRow.background = Color(0x2A2A2A)
        pctRow.add(JLabel("Simulated Percentage:"))
        pctRow.add(Box.createRigidArea(Dimension(8, 0)))
        percentageSlider.majorTickSpacing = 10
        percentageSlider.minorTickSpacing = 1
        percentageSlider.setPaintTicks(true)
        percentageSlider.setPaintLabels(true)
        percentageSlider.preferredSize = Dimension(300, 50)
        pctRow.add(percentageSlider)
        val pctValueLabel = JLabel("50%")
        pctValueLabel.foreground = Color.WHITE
        pctRow.add(Box.createRigidArea(Dimension(8, 0)))
        pctRow.add(pctValueLabel)
        percentageSlider.addChangeListener { pctValueLabel.text = "${percentageSlider.value}%" }

        // Auto discharge toggle
        val autoRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        autoRow.background = Color(0x2A2A2A)
        autoDischargeCheckbox.background = Color(0x2A2A2A)
        autoDischargeCheckbox.foreground = Color.WHITE
        autoDischargeCheckbox.font = Font(Font.SANS_SERIF, Font.PLAIN, 14)
        autoRow.add(autoDischargeCheckbox)

        // Discharge rate
        val rateRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        rateRow.background = Color(0x2A2A2A)
        rateRow.add(JLabel("Discharge Rate (%/sec):"))
        rateRow.add(Box.createRigidArea(Dimension(8, 0)))
        rateSlider.majorTickSpacing = 10
        rateSlider.setPaintTicks(true)
        rateSlider.preferredSize = Dimension(200, 50)
        rateRow.add(rateSlider)
        val rateValueLabel = JLabel("1.0")
        rateValueLabel.foreground = Color.WHITE
        rateRow.add(Box.createRigidArea(Dimension(8, 0)))
        rateRow.add(rateValueLabel)
        rateSlider.addChangeListener {
            val v = rateSlider.value / 10.0
            rateValueLabel.text = "%.1f".format(v)
        }

        panel.add(title)
        panel.add(Box.createRigidArea(Dimension(0, 16)))
        panel.add(pctRow)
        panel.add(Box.createRigidArea(Dimension(0, 16)))
        panel.add(autoRow)
        panel.add(Box.createRigidArea(Dimension(0, 16)))
        panel.add(rateRow)

        return panel
    }

    private fun createActivityInfoPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = Color(0x2A2A2A)
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(0x3A3A3A), 1),
            EmptyBorder(20, 20, 20, 20)
        )

        val title = JLabel("Activity Information")
        title.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        title.foreground = Color.WHITE
        title.alignmentX = Component.LEFT_ALIGNMENT

        val infoPanel = JPanel(GridLayout(4, 2, 16, 8))
        infoPanel.background = Color(0x2A2A2A)

        val labels = listOf(
            Pair("Simulated Percentage", percentageLabel),
            Pair("Session Duration", durationLabel),
            Pair("Simulation State", statusLabel),
            Pair("Discharge Mode", modeLabel)
        )

        for ((labelText, valueLabel) in labels) {
            val keyLabel = JLabel(labelText)
            keyLabel.foreground = Color(0xAAAAAA)
            keyLabel.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
            infoPanel.add(keyLabel)
            infoPanel.add(valueLabel)
        }

        panel.add(title)
        panel.add(Box.createRigidArea(Dimension(0, 12)))
        panel.add(infoPanel)

        return panel
    }

    private fun createSettingsPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = Color(0x2A2A2A)
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(0x3A3A3A), 1),
            EmptyBorder(20, 20, 20, 20)
        )

        val title = JLabel("Settings")
        title.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        title.foreground = Color.WHITE
        title.alignmentX = Component.LEFT_ALIGNMENT

        val startWithWindows = JCheckBox("Start with Windows")
        val launchMinimized = JCheckBox("Launch minimized")
        val minimizeToTray = JCheckBox("Minimize to tray")
        listOf(startWithWindows, launchMinimized, minimizeToTray).forEach {
            it.background = Color(0x2A2A2A)
            it.foreground = Color.WHITE
            it.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        }

        panel.add(title)
        panel.add(Box.createRigidArea(Dimension(0, 12)))
        panel.add(startWithWindows)
        panel.add(launchMinimized)
        panel.add(minimizeToTray)

        return panel
    }

    private fun wireEvents() {
        startButton.addActionListener {
            val rate = rateSlider.value / 10.0
            val mode = if (autoDischargeCheckbox.isSelected) SimulationMode.Automatic else SimulationMode.Manual
            viewModel.startSimulation(mode, rate)
        }
        pauseButton.addActionListener { viewModel.pauseSimulation() }
        resumeButton.addActionListener { viewModel.resumeSimulation() }
        stopButton.addActionListener { viewModel.stopSimulation() }
        resetButton.addActionListener { viewModel.setSimulatedPercentage(50) }
        reset100Button.addActionListener { viewModel.setSimulatedPercentage(100) }

        percentageSlider.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                viewModel.setSimulatedPercentage(percentageSlider.value)
            }
        })

        autoDischargeCheckbox.addActionListener {
            val mode = if (autoDischargeCheckbox.isSelected) SimulationMode.Automatic else SimulationMode.Manual
            val rate = rateSlider.value / 10.0
            viewModel.startSimulation(mode, rate)
        }

        rateSlider.addChangeListener {
            val v = rateSlider.value / 10.0
            viewModel.setDischargeRate(v)
        }

        // Tray callbacks
        trayManager.onTrayLeftClick = {
            EventQueue.invokeLater {
                if (isVisible) {
                    toFront()
                } else {
                    setVisible(true)
                }
            }
        }
        trayManager.onTrayRightClick = {
            EventQueue.invokeLater {
                showTrayMenu()
            }
        }
    }

    private fun showTrayMenu() {
        val popup = JPopupMenu()
        val showItem = JMenuItem("Show Dashboard")
        val exitItem = JMenuItem("Exit")
        showItem.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        exitItem.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        showItem.addActionListener { setVisible(true); toFront() }
        exitItem.addActionListener { onClose() }
        popup.add(showItem)
        popup.addSeparator()
        popup.add(exitItem)

        val location = getLocationOnScreen()
        popup.show(this, location.x, location.y)
    }

    private fun observeSimulation() {
        val timer = javax.swing.Timer(500) { _ ->
            updateUI()
        }
        timer.setRepeats(true)
        timer.start()
    }

    private fun updateUI() {
        val state = simulator.state.value

        EventQueue.invokeLater {
            percentageLabel.text = "${state.simulatedPercentage}%"
            statusLabel.text = state.status.toString()
            statusLabel.foreground = when (state.status) {
                SimulationStatus.Running -> Color(0x00FF88)
                SimulationStatus.Paused -> Color(0xFFCC00)
                SimulationStatus.Stopped -> Color(0xFF5555)
            }
            actualLabel.text = "Actual: ${state.actualPercentage?.toString() ?: "--"}%"
            modeLabel.text = "Mode: ${state.mode}"
            durationLabel.text = "Duration: ${formatDuration(state.simulationDuration)}"

            batteryPanel.percentage = state.simulatedPercentage
            batteryPanel.repaint()

            percentageSlider.value = state.simulatedPercentage
            rateSlider.value = (state.dischargeRate * 10).toInt()
            autoDischargeCheckbox.isSelected = state.mode == SimulationMode.Automatic

            when (state.status) {
                SimulationStatus.Stopped -> {
                    startButton.isEnabled = true
                    pauseButton.isEnabled = false
                    resumeButton.isEnabled = false
                    stopButton.isEnabled = false
                }
                SimulationStatus.Running -> {
                    startButton.isEnabled = false
                    pauseButton.isEnabled = true
                    resumeButton.isEnabled = false
                    stopButton.isEnabled = true
                }
                SimulationStatus.Paused -> {
                    startButton.isEnabled = false
                    pauseButton.isEnabled = false
                    resumeButton.isEnabled = true
                    stopButton.isEnabled = true
                }
            }
        }
    }

    private fun onClose() {
        Logger.info("Application closing")
        trayManager.cleanup()
        dispose()
        System.exit(0)
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}

/**
 * Custom panel that draws a battery icon visualization.
 */
class BatteryIconPanel : JPanel() {
    var percentage: Int = 50

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val w = width
        val h = height
        val cx = w / 2f
        val cy = h / 2f
        val bodyW = w * 0.6f
        val bodyH = h * 0.8f
        val tipW = w * 0.2f
        val tipH = h * 0.25f
        val bodyTop = cy - bodyH / 2f
        val bodyBottom = cy + bodyH / 2f
        val bodyLeft = cx - bodyW / 2f
        val bodyRight = cx + bodyW / 2f

        // Draw battery outline
        g2.color = Color(0xCCCCCC)
        g2.stroke = BasicStroke(4f)
        g2.drawRoundRect(bodyLeft.toInt(), bodyTop.toInt(), bodyW.toInt(), bodyH.toInt(), 16, 16)

        // Draw battery tip
        val tipTop = cy - tipH / 2f
        g2.drawRoundRect(bodyRight.toInt(), tipTop.toInt(), tipW.toInt(), tipH.toInt(), 4, 4)

        // Draw fill level
        val fillH = (bodyH - 8) * (percentage / 100f)
        val fillTop = bodyBottom - 4 - fillH
        val fillColor = when {
            percentage > 50 -> Color(0x4CAF50)
            percentage > 20 -> Color(0xFFA500)
            else -> Color(0xFF5252)
        }
        g2.color = fillColor
        g2.fillRoundRect(
            (bodyLeft + 4).toInt(),
            fillTop.toInt(),
            (bodyW - 8).toInt(),
            fillH.toInt(),
            8, 8
        )
    }
}