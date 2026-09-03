# Introduction to Macrion

Macrion is an open-source Android automation tool that lets your phone perform actions automatically based on what you see on the screen.

Whether you need to click a single button repeatedly or build an intelligent workflow that waits for a specific confirmation dialog to appear, Macrion gives you the tools to do so without requiring coding knowledge.

---

## The Core Philosophy

Most Android auto-clickers work blindly: they tap predetermined coordinates on your screen at fixed time intervals, regardless of whether the app has loaded, a pop-up has appeared, or a connection error occurred.

Macrion combines two distinct approaches:

1. **Position-Based ("Simple") Mode**: Fast, lightweight clicking at designated screen positions. Perfect for games or repetitive tasks where the layout never changes.
2. **Vision-Aware ("Smart") Mode**: Macrion inspects your screen in real time. It can recognize buttons, icons, or specific colors, and only trigger your configured actions when those visual conditions are satisfied.

---

## Privacy & Security

Automation apps require elevated Android permissions to interact with other apps. Because of this, privacy is a fundamental pillar of Macrion:

- **100% On-Device**: All screen capture, image recognition, and automation logic runs locally on your phone's processor.
- **Zero Telemetry**: Macrion does not send analytics, crash reports, or personal data to any external server.
- **No Account Required**: You do not need to register, sign in, or connect any cloud services.
- **Open Source**: The complete source code is transparent and published under the GNU General Public License v3.0 (GPLv3).

---

## History & Independence

Macrion is derived from the open-source project [Klick'r / Smart AutoClicker](https://github.com/Nain57/Smart-AutoClicker), originally created by Kevin Buzeau. 

While inspired by Klick'r, Macrion is developed independently with its own application ID, updated architecture, modernized UI, and focused release cycle. Scenarios exported from Klick'r can also be imported directly into Macrion.

---

## Next Steps

Ready to get started? Head to [Installation & Setup](./installation) to download and install Macrion on your device.
