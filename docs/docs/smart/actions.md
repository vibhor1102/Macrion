# Actions & Gestures

Once a condition evaluates to true, Macrion executes the **Action** attached to it. An action defines what gesture to perform, where to perform it, and what timing rules to follow.

---

## Action Types

Macrion supports multiple action types:

### 1. Click (Tap)
- **Click on Condition Location**: Macrion automatically taps the exact center of where the target image was found on screen. If the button moves, Macrion follows it!
- **Click on Custom Position**: Macrion taps a fixed coordinate (X, Y) regardless of where the detected image was located. (Useful for clicking a "Next" button after detecting a "Success" banner elsewhere on screen).
- **Press Duration**: Milliseconds the tap is held (standard: `40 ms` to `60 ms`).

### 2. Long Press
- A prolonged touch at the specified coordinates.
- Configure duration from `500 ms` up to several seconds.

### 3. Swipe / Drag
- Requires a **Start Point** and an **End Point**.
- **Duration**: Controls the speed of the swipe (e.g., `200 ms` for a fast flick, `1200 ms` for a slow scroll or item drag).

### 4. Pause / Wait
- Performs no physical gesture on the screen, but pauses execution for a designated duration before resuming detection.

---

## Action Delays & Timing

Smooth automation requires timing control to let game animations settle:

- **Pre-Delay**: How long Macrion waits *before* executing the gesture after detecting the match.
- **Post-Delay**: How long Macrion waits *after* executing the gesture before it begins inspecting the screen again. Setting an adequate post-delay (e.g., `500 ms` to `1000 ms`) prevents the same button from being tapped twice before the screen has a chance to change.

---

## Action Repeat Limits

Under the action settings, you can define how many times the action is allowed to run:
- **Unlimited**: Executes whenever the condition matches.
- **Run $N$ Times**: Disables itself or halts after triggering a set number of times.
