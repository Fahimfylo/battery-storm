# ROLE & PERSONA

Act as a Senior Software Engineer, Senior Kotlin Developer, Windows Desktop Application Engineer, Software Architect, and Expert UI/UX Designer.

You are responsible for designing, implementing, testing, debugging, and delivering a complete, reliable Windows desktop application.

Think like an experienced engineer. Do not rush into coding. Inspect the environment, understand technical constraints, plan the implementation, and then execute the work systematically.

Do not produce superficial implementations, placeholder functionality, fake integrations, or unnecessary abstractions.

# PROJECT IDENTITY

Project Name: BATTERY STORM

Application Type: Windows Battery Simulation & Taskbar Indicator Utility

Primary Platform: Windows 11 (64-bit)

Target Device: Windows 11 laptop, including HP ProBook 445 G7.

Primary Language: Kotlin

Desktop UI: Compose Multiplatform Desktop

Build System: Gradle Kotlin DSL

Architecture: MVVM with clean separation of responsibilities.

Package Name: com.ghost.batterystorm

Application Goal:

Build a lightweight Windows desktop utility that allows users to simulate a battery percentage and display it through a custom Windows notification-area indicator styled to closely resemble the native Windows 11 battery indicator.

IMPORTANT TECHNICAL CONSTRAINT:

Windows does not provide a supported public API for replacing or overriding its built-in battery percentage in the Windows 11 taskbar.

Do not attempt to modify protected Windows processes, inject code into Explorer, patch system DLLs, manipulate Windows battery drivers, or falsify actual battery telemetry.

Instead, implement a custom notification-area icon with a simulated battery percentage. Clearly identify the indicator as a simulation in its tooltip and application settings.

The real Windows battery status must remain untouched.

# 1. FIRST: ENVIRONMENT INSPECTION

Before writing code:

1. Inspect the current workspace.
2. Check whether a project already exists.
3. Inspect existing Gradle configuration and source files.
4. Identify the installed JDK version.
5. Identify Kotlin and Gradle versions if configured.
6. Check the Windows version and architecture.
7. Verify whether the Windows notification-area APIs needed for the project are accessible.
8. Identify dependency compatibility issues.

Do not overwrite existing project files without inspecting them.

If no project exists, initialize a clean Kotlin Compose Desktop project.

Prefer JDK 21 and mutually compatible stable Kotlin, Compose Multiplatform, Gradle, and JNA versions. Verify actual compatibility rather than assuming versions.

# 2. TECHNOLOGY STACK

Use:

- Kotlin
- Compose Multiplatform Desktop
- Gradle Kotlin DSL
- JDK 21
- Kotlin Coroutines
- MVVM architecture
- JNA for Windows native integration
- Windows Shell notification-area APIs
- Java AWT SystemTray as an initial fallback if native integration is unavailable
- kotlinx.serialization for configuration persistence
- SQLite only if session history genuinely requires it

Avoid unnecessary dependencies.

Do not introduce React, Electron, Node.js, or Android-specific libraries.

# 3. PRIMARY FEATURE: CUSTOM TASKBAR BATTERY INDICATOR

This is the most important feature.

Create a custom notification-area battery indicator that visually resembles the Windows 11 native battery icon.

Requirements:

1. Display a custom battery icon in the Windows notification area.
2. Display a simulated percentage in the tooltip and custom popup.
3. Support percentage values from 0 to 100.
4. Support manual percentage adjustment.
5. Support simulated automatic discharge.
6. Support pause and resume.
7. Support reset.
8. Support customizable simulation speed.
9. Update the icon when the simulated percentage changes.
10. Respond correctly to left-click and right-click.
11. Support Windows notification-area overflow.
12. Provide a context menu.
13. Support application exit and cleanup.
14. Avoid creating duplicate tray icons.
15. Recover correctly when Explorer.exe restarts, where supported.

Visual requirements:

- Match Windows 11 system-tray visual proportions.
- Use appropriate light and dark icon variants.
- Use a battery silhouette with a percentage indicator.
- Use clean vector or raster assets at appropriate Windows DPI sizes.
- Support high-DPI displays.
- Keep the indicator compact.
- Do not use emojis as the final battery icon.
- Do not use a generic Java coffee-cup tray icon.

Investigate Windows Shell_NotifyIcon and NOTIFYICONDATA through JNA for native tray integration.

Use a proper Windows message window and callback handling if required.

Do not claim that the custom icon replaces the built-in Windows battery indicator.

# 4. CUSTOM POPUP

When the custom tray indicator is clicked, display a compact Windows 11-inspired popup.

Design characteristics:

- Rounded corners.
- Dark and light theme support.
- Native-looking spacing.
- Subtle shadows.
- Clean typography.
- Minimal controls.
- Smooth but restrained transitions.

Popup content:

BATTERY STORM

SIMULATION MODE

Battery icon

Simulated battery percentage

Actual battery percentage (read-only, if available)

Simulation status:

- Running
- Paused
- Stopped

Controls:

- Start
- Pause
- Resume
- Reset
- Open Dashboard
- Exit

Clearly distinguish actual battery telemetry from simulated values.

# 5. SIMULATION ENGINE

Implement a dedicated BatterySimulator class.

Responsibilities:

- Maintain simulated battery percentage.
- Maintain simulation status.
- Support manual percentage changes.
- Support automatic discharge.
- Support configurable discharge speed.
- Support start, pause, resume and reset.
- Notify UI and tray components when state changes.
- Persist user settings.
- Handle application lifecycle correctly.

Suggested state model:

BatteryState:
- simulatedPercentage: Int
- actualPercentage: Int?
- isRunning: Boolean
- isPaused: Boolean
- dischargeRate: Double
- simulationDuration: Long
- simulationMode: SimulationMode

Simulation modes:

- Manual
- Automatic

Use Kotlin StateFlow for observable state.

Use structured Kotlin coroutines.

Never create unmanaged infinite loops or unbounded threads.

When the percentage reaches zero, stop the simulation automatically.

Do not shut down the computer, disable Windows protections, or alter the real battery.

# 6. DASHBOARD

Create a professional dashboard using Compose Desktop.

Design direction:

Windows 11-inspired minimalism.

Prefer:
- Modern dark interface.
- Optional light theme.
- Neutral surfaces.
- Subtle borders.
- Rounded cards.
- Clean icons.
- Good spacing.
- Responsive layouts.
- Clear visual hierarchy.

Dashboard sections:

A. Header
- BATTERY STORM logo/title
- Simulation status
- Settings button
- Minimize-to-tray action

B. Battery overview
- Large battery visualization
- Simulated percentage
- Actual percentage
- Simulation duration
- Current simulation state

C. Simulation controls
- Start button
- Pause button
- Resume button
- Stop button
- Reset button

D. Simulation configuration
- Manual percentage input
- Automatic discharge toggle
- Discharge speed slider
- Simulation duration

E. Activity information
- Current simulated percentage
- Previous percentage
- Session duration
- Simulation history

F. Settings
- Start with Windows
- Launch minimized
- Minimize to tray
- Theme selection
- Tray tooltip options
- Simulation defaults

# 7. WINDOWS INTEGRATION

Create a dedicated Windows integration layer.

Suggested classes:

WindowsTrayManager.kt
WindowsNativeBridge.kt
WindowsBatteryReader.kt
WindowsStartupManager.kt

Requirements:

- Use Windows APIs only where necessary.
- Keep native code isolated from the UI.
- Handle native resource cleanup.
- Handle missing native capabilities gracefully.
- Avoid requiring administrator permissions for normal operation.
- Use supported Windows APIs.
- Read actual battery information without modifying it.
- Support Windows 11 64-bit.

For actual battery monitoring, use appropriate Windows APIs such as GetSystemPowerStatus.

Do not assume that GetSystemPowerStatus provides exact discharge power or battery temperature.

Do not invent unsupported hardware readings.

# 8. ARCHITECTURE

Use a clean modular structure.

Suggested structure:

src/
└── jvmMain/
    └── kotlin/
        └── com/ghost/batterystorm/
            ├── Main.kt
            │
            ├── core/
            │   ├── BatterySimulator.kt
            │   ├── SimulationController.kt
            │   └── AppSettings.kt
            │
            ├── model/
            │   ├── BatteryState.kt
            │   ├── SimulationMode.kt
            │   └── SimulationStatus.kt
            │
            ├── presentation/
            │   ├── dashboard/
            │   ├── components/
            │   ├── settings/
            │   └── popup/
            │
            ├── tray/
            │   ├── WindowsTrayManager.kt
            │   ├── TrayIconRenderer.kt
            │   └── TrayMenu.kt
            │
            ├── windows/
            │   ├── WindowsNativeBridge.kt
            │   ├── WindowsBatteryReader.kt
            │   └── WindowsStartupManager.kt
            │
            ├── data/
            │   ├── SettingsRepository.kt
            │   └── SessionRepository.kt
            │
            ├── viewmodel/
            │   └── DashboardViewModel.kt
            │
            └── utils/
                ├── Logger.kt
                └── PlatformUtils.kt

Adapt the structure when technically justified.

Do not create empty files just to match the proposed structure.

# 9. RELIABILITY AND ERROR HANDLING

Implement:

- Proper exception handling.
- Structured logging.
- Coroutine cancellation.
- Thread-safe state management.
- Native resource cleanup.
- Graceful shutdown.
- Settings persistence.
- Validation of percentage values.
- Protection against duplicate tray initialization.
- Fallback UI if native integration fails.

Do not silently swallow exceptions.

Provide useful error messages.

# 10. TESTING

Create tests for:

1. Initial simulated percentage.
2. Manual percentage changes.
3. Automatic discharge.
4. Pause and resume.
5. Reset.
6. Zero-percent behavior.
7. Invalid percentage values.
8. Settings persistence.
9. Application lifecycle.
10. Tray integration initialization and cleanup.

Use unit tests for the simulation engine.

Use Windows integration testing for the native notification-area component.

Verify behavior on an actual Windows desktop session.

Do not claim that native tray integration works unless it has been tested.

# 11. PERFORMANCE

The application must be lightweight.

Requirements:

- Low idle CPU usage.
- Minimal background memory consumption.
- No unnecessary polling.
- Efficient tray icon updates.
- Avoid excessive UI recompositions.
- Avoid excessive disk writes.
- Proper coroutine lifecycle.
- No memory leaks.

The simulation must not consume actual battery power intentionally.

# 12. BUILD AND RELEASE

Configure Gradle tasks for:

- Development run.
- Unit tests.
- Windows packaging.
- Executable distribution.
- Installer generation.

Provide a Windows-compatible release.

Prefer an installer and a portable distribution where technically supported.

The application should not require Android Studio or an Android emulator.

# 13. DEVELOPMENT WORKFLOW

Follow this exact workflow:

PHASE 1: Inspect the environment and workspace.

PHASE 2: Present a concise architecture and implementation plan.

PHASE 3: Initialize or repair the project configuration.

PHASE 4: Implement the simulation engine and state management.

PHASE 5: Implement the Compose Desktop dashboard.

PHASE 6: Implement native Windows notification-area integration.

PHASE 7: Implement the custom battery popup and menu.

PHASE 8: Implement settings persistence and application lifecycle.

PHASE 9: Add tests and fix all discovered issues.

PHASE 10: Build and verify the Windows distribution.

After each phase:

- Explain what was implemented.
- Identify modified files.
- Run relevant tests.
- Fix errors before proceeding.
- Report anything that could not be verified.

Do not stop after producing an architecture document.

Do not ask for permission after every small implementation step.

If a critical technical limitation prevents implementation, explain it and propose the best supported alternative.

# 14. ENGINEERING RULES

- Write maintainable, production-quality Kotlin.
- Prefer simple solutions over unnecessary complexity.
- Follow SOLID principles where useful.
- Avoid massive files.
- Use meaningful class and function names.
- Avoid hardcoded configuration.
- Keep UI, business logic, and native integration separate.
- Do not use deprecated APIs without justification.
- Do not fabricate test results.
- Do not leave core features as TODOs.
- Do not introduce unsafe Windows modifications.
- Do not compromise real battery telemetry.

# 15. FINAL DELIVERABLE

At completion, provide:

1. Complete source code.
2. Final project architecture.
3. Dependency list.
4. Windows build instructions.
5. Executable or installer location.
6. Test results.
7. Known limitations.
8. Instructions for configuring the tray indicator.
9. Instructions for starting and stopping simulation.

# SUCCESS CRITERIA

The project is successful when:

- The application launches on Windows 11.
- The dashboard works.
- The simulation engine works.
- The custom tray icon appears in the Windows notification area.
- The simulated percentage updates correctly.
- The popup and context menu work.
- The real Windows battery status remains unchanged.
- The application can minimize to tray.
- The application exits cleanly.
- The Windows distribution builds successfully.

Begin with environment inspection. Then present the implementation plan and start development.

You are responsible for delivering a working application, not merely generating code snippets or documentation.

Continue working on BATTERY STORM.

The highest-priority unfinished feature is the native Windows notification-area integration.

Do not replace it with a normal Compose window or a generic Java tray icon without explaining the limitation.

Investigate Windows Shell_NotifyIcon, NOTIFYICONDATA, notification-area callbacks, and native Windows message handling using JNA.

Implement the custom simulated battery indicator with proper icon rendering, tooltip updates, click handling, context menu, lifecycle cleanup, and Explorer restart recovery where supported.

The icon must visually resemble the Windows 11 native battery indicator, but remain clearly identifiable as a simulated indicator.

Preserve the actual Windows battery status.

First inspect the existing implementation, identify the missing functionality, and then implement and test it. Do not rewrite working modules unnecessarily.