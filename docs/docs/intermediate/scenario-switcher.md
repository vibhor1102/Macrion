# Scenario Switcher

Complex automation tasks often consist of separate stages. For example:
- **Phase 1**: Navigate menus and purchase inventory items.
- **Phase 2**: Farm a level or battle monsters.
- **Phase 3**: Clean inventory and claim achievements.

Putting all of this into a single giant scenario can become messy. Macrion's **Scenario Switcher** allows scenarios to transition seamlessly into one another or let you switch them on the fly from the floating overlay.

---

## 1. Switching Scenarios via the Floating Overlay

While any scenario is loaded in the floating overlay:
1. Tap the **Scenario Switcher (🔀)** icon on the toolbar.
2. A popup menu lists all your saved scenarios.
3. Tap the scenario you want to activate.
4. The overlay immediately loads the new scenario without closing or disrupting your app.

---

## 2. Automated Scenario Chaining

You can configure an action in Scenario A that automatically loads and starts Scenario B:

1. In Scenario A, add an action.
2. Under Action Type, choose **Switch Scenario**.
3. Select the target scenario from your list.
4. When this action triggers, Macrion smoothly stops Scenario A and begins running Scenario B.

::: tip Modular Scenarios
Chaining scenarios keeps your automations clean, modular, and easy to maintain. If one phase of a game changes in an update, you only have to edit that one modular scenario rather than re-testing everything.
:::
