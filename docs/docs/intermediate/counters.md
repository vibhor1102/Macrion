# Counters & Loop Controls

When automating gameplay loops or repetitive data workflows, you often want an action to occur a specific number of times before doing something else (or stopping entirely). 

Macrion provides built-in **Counters** to track repetitions.

---

## What is a Counter?

A Counter is a named numeric variable within your scenario that can:
- Start at a specific number (usually `0`).
- Increment by 1 each time a specific action triggers.
- Act as a requirement in conditions (e.g., *"Only run this if Counter A is less than 10"*).
- Automatically reset after reaching a target value.

---

## Common Use Cases

### 1. Repeat a Dungeon 10 Times
- You want your scenario to tap "Replay" 10 times, and on the 10th time tap "Return to City".
- **Rule 1**: When "Victory Screen" is detected AND `Counter < 10`:
  - Action: Tap "Replay" and increment `Counter` by 1.
- **Rule 2**: When "Victory Screen" is detected AND `Counter >= 10`:
  - Action: Tap "Return to City" and stop scenario.

### 2. Safeguard Against Infinite Loops
- If an unexpected error dialog keeps reappearing, an unconstrained auto-clicker might click it thousands of times.
- Adding a counter limit ensures automation gracefully halts after a threshold is reached.

---

## Configuring a Counter

1. Open your scenario editor in Macrion.
2. Under the scenario settings or action details, locate **Counters**.
3. Set your **Initial Value** (default: `0`) and **Maximum Limit**.
4. In your Action configuration, enable **Increment Counter** upon execution.
5. In your Condition configuration, add a **Counter Condition** rule (`<`, `<=`, `=`, `!=`, `>=`, `>`).
