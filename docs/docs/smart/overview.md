# How Smart Detection Works

Traditional auto-clickers fail whenever an unexpected event occurs — a network lag popup, an unexpected ad, or a slow loading animation. 

**Smart Automation** solves this problem by giving Macrion "eyes." Instead of blindly tapping, Macrion continuously inspects your screen for visual cues and takes action only when appropriate.

---

## The Scenario Model

A Smart Scenario in Macrion consists of **Conditions** and **Actions**:

```
[Screen Inspection] ──▶ Matches Target Condition?
                               │
                ┌──────────────┴──────────────┐
               YES                            NO
                ▼                             ▼
       [Execute Action]               [Wait / Next Check]
       (Tap, Swipe, etc.)
```

1. **Condition**: *"Is the 'Claim Daily Reward' button visible on screen right now?"*
2. **Action**: *"Tap the button, wait 500 milliseconds, and increment counter by 1."*

---

## Scenario Architecture

Below is an overview of how Smart Scenarios are structured inside Macrion:

![Scenario Structure Diagram](/diagrams/scenario_structure.png)

A single scenario can hold multiple independent condition-action pairs. Macrion evaluates these conditions on each captured frame and executes the matching actions.

---

## Scenario Processing Lifecycle

How does Macrion evaluate your rules while running?

![Scenario Processing Diagram](/diagrams/scenario_processing.png)

1. **Capture Frame**: Macrion grabs the latest screen frame via Android's MediaProjection API.
2. **Evaluate Conditions**: The engine compares cropped template images against the screen.
3. **Trigger Matching Action**: If a condition matches within your set confidence threshold, Macrion triggers its associated gesture.
4. **Throttle & Repeat**: Macrion respects your configured frame rate and delays, ensuring minimal battery impact while waiting for the next event.

---

## Next Steps

- Learn how to crop targets and configure match thresholds in [Setting Up Conditions](./conditions).
- Explore gestures, click offsets, and pauses in [Actions & Gestures](./actions).
