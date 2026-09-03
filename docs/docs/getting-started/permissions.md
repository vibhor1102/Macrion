# Android Permissions Explained

Android takes security and app isolation very seriously. Because Macrion acts on your behalf across other applications, it requires a few powerful system permissions.

Here is a plain-English explanation of every permission Macrion requests, why it is needed, and how it is used safely.

---

## Required Permissions

### 1. Accessibility Service
- **What Android says**: *"Macrion needs full control of your device."*
- **Why Macrion needs it**: This is the core engine that allows Macrion to perform physical touches on your screen. Without this permission, no app on Android can tap, long-press, or swipe outside its own window.
- **How Macrion uses it**: Macrion only generates touches at the specific coordinates you configure or when your smart scenario matches a target. It **never** reads keystrokes or inspects private text entries like passwords or messages.

---

### 2. Display Over Other Apps (Overlay)
- **What Android says**: *"Allow display over other apps."*
- **Why Macrion needs it**: Macrion provides a floating control bar that remains on screen while you are using your target game or app.
- **How Macrion uses it**: It draws a compact widget containing play, pause, record, and settings buttons so you can start or stop automation without constantly leaving your foreground app.

---

### 3. Screen Capture (MediaProjection)
- **What Android says**: *"Macrion will start capturing everything that's displayed on your screen."*
- **Why Macrion needs it**: This permission is required only for **Smart Scenarios**. To react to what is visible on screen, Macrion must take screen frames to detect your target images.
- **How Macrion uses it**: Frames are analyzed locally in device memory and immediately discarded. No screenshots or recordings are ever saved to disk or transmitted across the network.

::: tip Simple Mode Does Not Need Screen Capture
If you only use **Simple (Position-Based) Mode**, you do not need to grant screen capture permission at all, because position-based clicking does not inspect screen contents.
:::

---

### 4. Notifications
- **Why Macrion needs it**: On modern Android versions (Android 13+), apps that run background tasks must display an ongoing notification.
- **How Macrion uses it**: A persistent notification keeps Macrion's automation service alive so Android's battery manager doesn't abruptly kill your running scenario in the middle of execution. It also provides quick stop controls.

---

## How to Grant Permissions

Macrion guides you through each permission step-by-step when you launch the app:

1. Tap **Enable** next to the required permission.
2. Macrion will navigate you directly to the relevant system settings page.
3. Locate **Macrion** in the list and toggle the switch to **On**.
4. Confirm any system dialogs that appear.

Once the checklist is complete, you are ready to create your first automation! Head over to the [Quick Start Guide](./quick-start).
