# Live Debug Panel & Performance Reports

When a complex scenario isn't behaving as expected—perhaps a condition isn't triggering or an action is clicking the wrong spot—Macrion provides powerful diagnostic tools to show you exactly what the engine sees.

---

## 1. The Live Debug Panel

You can enable the **Live Debug Panel** from the floating overlay settings:

- **Bounding Box Visualizer**: Outlines your configured detection areas directly on top of your app with bright colored rectangles.
- **Real-Time Match Scores**: Displays the current similarity score (e.g., `87% / 80% Threshold`) in real time next to each condition.
  - If you see a score fluctuating around `78%` while your threshold is `80%`, you immediately know you just need to lower the threshold slightly.
- **Match Indicator**: Highlights with a green checkmark the moment a condition matches.
- **Touch Region Passthrough**: Lets you touch through the overlay while the debug panel remains visible.

---

## 2. Generating Debug Reports

If you encounter persistent detection issues or performance drops, you can generate a comprehensive **Debug Report**:

1. Open Macrion Settings ➔ **Debug & Diagnostics**.
2. Run your scenario for a few cycles.
3. Tap **Generate Condition Performance Report**.
4. Macrion packages:
   - Average frame processing times (in milliseconds).
   - Match failure vs. match success ratios.
   - Detection frequency statistics.
5. You can export or share this report when seeking help in GitHub issues or fine-tuning your detection parameters.
