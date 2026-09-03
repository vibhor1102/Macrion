# Simple (Position-Based) Clicking

**Simple Mode** (often called *Position-Based Clicking*) is the classic auto-clicking mode. It tells Macrion: *"Tap at coordinate (X, Y) every few milliseconds, without inspecting the screen."*

Use Simple Mode when your buttons remain in fixed locations, when you don't need condition logic, or when you need ultra-low battery consumption.

---

## Single Target vs. Multi-Target

Macrion supports two primary types of simple scenarios:

### 1. Single-Target Clicking
Ideal for clicking games or single buttons:
- **Click Interval**: How often to tap. You can set this in milliseconds (ms), seconds, or minutes (e.g., `100 ms` = 10 clicks per second).
- **Hold Duration**: The amount of time the touch remains pressed down (usually `20 ms` to `50 ms` for a standard tap).
- **Stop Condition**: You can configure Macrion to:
  - Run indefinitely until you manually press Stop.
  - Stop after a specific amount of time (e.g., run for 15 minutes).
  - Stop after a total number of clicks (e.g., exactly 500 clicks).

### 2. Multi-Target Sequences
If your task requires tapping several different buttons in sequence:
- Add multiple click points (`Target 1`, `Target 2`, `Target 3`).
- Reorder them easily via drag-and-drop.
- Configure individual delays between each tap. For example:
  - Tap `Target 1` (Menu button)
  - Wait `1500 ms` for menu animation
  - Tap `Target 2` (Collect Reward button)
  - Wait `500 ms`
  - Tap `Target 3` (Confirm button)

---

## Swipes and Long Presses

Simple Mode isn't limited to short taps:

- **Long Press**: Increase the **Hold Duration** (for example, `1000 ms` for a 1-second hold).
- **Swipes**: Set a **Start Position** and an **End Position**, along with a **Swipe Duration** (e.g., `300 ms` for a swift flick, or `1000 ms` for a smooth scroll).

---

## Randomization (Anti-Detection)

To prevent games and apps from detecting mechanical, robotic tapping patterns:

- **Random Delay Variance**: Adds a slight jitter to your tap intervals (e.g., `1000 ms ± 50 ms`), so taps happen naturally between 950 ms and 1050 ms.
- **Random Coordinate Radius**: Slightly randomizes the exact pixel of the touch within a tiny circle (e.g., within 5 pixels of the target), mimicking human fingers.
