# Multi-Condition Logic & Priority

Real-world apps are rarely simple. Often, you want to perform an action only when multiple distinct things are true, or choose between two competing actions based on which is more urgent.

Macrion gives you precise control over condition combinations and priority.

---

## Combining Conditions (AND / OR)

In Macrion, an action can be linked to more than one condition:

- **ALL (AND Logic)**: The action triggers **only** when *every* attached condition matches simultaneously.
  - *Example*: Tap "Confirm" only if BOTH the "Item Selected" checkmark AND the "Sufficient Gold" badge are visible.
- **ANY (OR Logic)**: The action triggers if *at least one* of the attached conditions matches.
  - *Example*: Tap "Dismiss" if the "Close X" button OR the "Skip" button appears.

---

## Condition Priority

What happens if the screen matches Condition A *and* Condition B at the exact same moment, but they each demand a different tap?

Macrion evaluates actions according to their **Order in the Scenario List**:
1. Actions listed higher in the list have higher priority.
2. If multiple actions are eligible on the same frame, the highest-priority action executes first.
3. You can easily reorder actions using the drag handle on the left of each item in the editor.

::: tip Priority Tip
Place error handlers or modal dismissers (e.g., "Dismiss Server Error") near the top of your scenario list, so they are handled immediately before standard game loops.
:::
