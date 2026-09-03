# External Automation (Tasker, MacroDroid & Intents)

Macrion is designed to play nicely with Android's wider automation ecosystem. You can launch, control, and stop scenarios using third-party apps like **Tasker**, **MacroDroid**, or **Automate**, as well as via standard Android broadcast intents.

---

## 1. Tasker & Locale Plugin Support

Macrion embeds standard **Tasker / Locale plugin** actions. When setting up a Task in Tasker or MacroDroid:

1. Add an action and navigate to **Plugin** ➔ **Macrion**.
2. Select the desired operation:
   - **Launch Scenario**: Choose a saved Smart or Simple scenario to start.
   - **Run Current**: Starts whatever scenario is currently active on the overlay.
   - **Stop**: Halts active automation and closes the overlay.
3. Save the task.

::: info Screen Capture for Smart Scenarios
Android security requires explicit user confirmation before granting screen recording permission. If Macrion does not already hold active capture permission, triggering a Smart scenario via Tasker will prompt a one-tap system authorization dialog.
:::

---

## 2. Quick Settings (QS) Tile

Macrion provides a **Quick Settings Tile** that you can add to your Android notification shade:
1. Swipe down twice from the top of your screen to open the Quick Settings panel.
2. Tap the edit (pencil) icon to customize tiles.
3. Locate **Macrion** and drag it into your active tiles.
4. Tapping this tile lets you quickly start your default scenario or toggle the overlay from anywhere.

---

## 3. Broadcast Intents (Advanced)

For custom scripts or terminal automations (e.g., via Termux or ADB):
Macrion listens for broadcast intents to control scenarios programmatically.

Example via ADB:
```bash
# Launch a specific scenario by ID
adb shell am broadcast -a io.github.vibhor1102.macrion.LAUNCH_SCENARIO --ei scenario_id 1

# Stop any currently running scenario
adb shell am broadcast -a io.github.vibhor1102.macrion.STOP_SCENARIO
```
*(Replace `io.github.vibhor1102.macrion` with `.debug` if using a local debug APK build).*
