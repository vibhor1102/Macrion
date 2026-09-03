# Setting Up Conditions

A **Condition** is a visual rule that tells Macrion what to look for on your screen. When the visual rule evaluates to true, its linked action is executed.

---

## 1. Capturing a Target Image

To teach Macrion what to look for:

1. Open your target app or game to the exact screen containing the button, icon, or text you want to detect.
2. Open Macrion's floating overlay and select **Add Condition**.
3. A screen capture overlay will freeze your screen.
4. Drag a box tightly around the element you want to detect (for example, the "OK" button).
5. Tap **Confirm** to save the cropped template image.

::: tip Crop tightly and choose distinctive elements
Avoid including unnecessary background padding or changing elements (such as animated glows or shadows). The cleaner and more unique the cropped image is, the faster and more reliably Macrion will detect it.
:::

---

## 2. Detection Area (Search Region)

By default, Macrion can search the entire screen for your template. However, you can restrict detection to a specific **Search Region**:

- **Full Screen**: Great when an element can appear anywhere (e.g., floating bubbles, falling items).
- **Custom Region (Recommended)**: If you know a button always appears in the bottom right corner, restrict the detection area to that corner. 
  - Restricting the search area drastically speeds up detection.
  - It reduces CPU usage and prolongs battery life.
  - It eliminates false positives from other parts of the screen.

---

## 3. Threshold & Match Confidence

Image matching produces a similarity score between `0%` and `100%`:

- **Threshold (Default: ~80%)**: The minimum similarity percentage required for Macrion to consider the image a "match".
  - If a button slightly changes color or transparency, you can lower the threshold (e.g., to `70%`).
  - If Macrion is triggering by mistake on similar-looking buttons, raise the threshold (e.g., to `90%`).

---

## 4. Detection Modes: Presence vs. Absence

Most conditions check if an image **Appears**:
- **Appears (Present)**: Condition is true when the image is visible.

However, you can also invert conditions:
- **Disappears (Absent)**: Condition is true when the image is **NOT** visible.
  - *Example*: Wait until a "Loading..." spinner disappears, and only then proceed to the next step.
