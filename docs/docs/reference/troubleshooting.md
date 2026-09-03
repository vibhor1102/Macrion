# Troubleshooting & FAQ

Below are solutions to common questions and Android device quirks encountered when running automation tools.

---

## 1. Accessibility Service Keeps Disabling Itself

Many Android phone manufacturers (notably Samsung, Xiaomi, Huawei, OnePlus, and Oppo) include aggressive battery management systems that forcibly kill background Accessibility services to save battery.

### Solutions:
1. **Exclude Macrion from Battery Optimization**:
   - Go to Android **Settings** ➔ **Apps** ➔ **Macrion** ➔ **Battery**.
   - Select **Unrestricted** (or "Don't optimize").
2. **Lock Macrion in Recent Apps**:
   - Open your device's Recent Apps overview.
   - Long-press the Macrion app card or tap the menu icon above it.
   - Tap the **Lock** icon to prevent the system from clearing Macrion when closing apps.
3. **Check "Don't Kill My App"**:
   - Visit [dontkillmyapp.com](https://dontkillmyapp.com) for manufacturer-specific instructions on disabling OEM app killers.

---

## 2. "Screen Capture" Prompts Every Time

Starting in Android 14, Android requires user confirmation each time an app requests access to capture screen contents:
- When prompted, check **"Entire Screen"** and tap **"Start now"**.
- Keep Macrion active in the background to avoid re-prompting within the same session.

---

## 3. Touches Are Not Registering in My Game

If Macrion's target marker is tapping, but the game is ignoring the taps:
- **Increase Press Duration**: Some games intentionally ignore taps that are faster than human fingers (under 20ms). Try increasing **Press Duration** to `50 ms` or `80 ms`.
- **Add Pre/Post Delays**: If the game has transitions or animations, ensure you have configured adequate post-delays (`300 ms` to `800 ms`) so the game engine is ready to accept touch inputs.

---

## 4. Frequently Asked Questions (FAQ)

### Does Macrion require Root?
**No.** Macrion operates completely without root access. It uses Android's standard Accessibility and MediaProjection APIs.

### Will Macrion drain my battery?
In **Simple Mode**, battery consumption is negligible. In **Smart Mode**, screen capture and image processing use CPU cycles. You can optimize this by setting the detection rate to **1–5 FPS** and restricting detection areas to small bounding boxes.

### Can Macrion automate games running in Landscape mode?
**Yes.** Macrion automatically detects screen orientation changes and rotates touch coordinates and overlay bars accordingly.
