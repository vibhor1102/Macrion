# Detection Modes & Performance Tuning

Screen-aware automation requires analyzing pixels, which can consume CPU power and battery if not tuned properly. Macrion gives you fine-grained controls to strike the right balance between lightning-fast reaction speed and minimal battery usage.

---

## 1. Frame Rate & Detection Frequency

Under Scenario Settings, you can configure the **Detection Rate** (checks per second):

- **1 to 2 FPS (Eco Mode)**: Best for slow-paced games, turn-based games, or waiting on occasional modal dialogs. Dramatically reduces CPU load and keeps your device cool.
- **5 FPS (Balanced - Recommended)**: The sweet spot for almost all scenarios. Reactions feel instant without running your processor hot.
- **10+ FPS (High Speed)**: Reserved for fast-action games where milliseconds matter. Use with smaller search regions to avoid throttling.

---

## 2. Restrict Detection Search Regions

By default, template matching checks the whole screen. Searching 1080x2400 pixels takes roughly 5 to 10 times longer than searching a 200x200 pixel box.

- Always set a **Bounding Box** around where you expect your button or icon to appear.
- Doing this allows Macrion to run at high frequencies while consuming negligible CPU resources.

---

## 3. Color & Saturation Tolerance

Some games employ dynamic lighting, day/night cycles, or subtle particle effects over UI buttons:

- If a button changes shade in different environments, standard grayscale matching might score lower.
- Macrion provides **Color Saturation** and variance checks to ensure buttons are identified correctly even if the background lighting changes slightly.
- If matches fail unexpectedly under different lighting, try lowering the confidence threshold from `80%` to `70%`.
