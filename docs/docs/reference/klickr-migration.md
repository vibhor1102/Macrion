# Klick'r Migration & Compatibility

Macrion is derived from [Klick'r / Smart AutoClicker](https://github.com/Nain57/Smart-AutoClicker). If you have existing scenarios created in Klick'r, you can seamlessly migrate them to Macrion.

---

## Importing Klick'r Backups into Macrion

Macrion natively understands the Klick'r scenario file format:

1. Open Klick'r on your device and export your scenarios to a backup ZIP file.
2. Open **Macrion** ➔ **Backup & Restore**.
3. Tap **Import Scenarios** and select your Klick'r `.zip` backup.
4. Macrion will read the scenarios, migrate the internal database formats, and add them to your scenario library.

---

## Exporting Klick'r-Compatible Backups

If you need to share a scenario with someone who still uses Klick'r:

1. Open Macrion ➔ **Backup & Restore** ➔ **Export**.
2. Check the option **Klick'r-Compatible Export**.
3. Macrion will inspect your scenario for any Macrion-exclusive features (such as advanced condition thresholds, new trigger types, or updated schema fields).
4. If incompatible features exist, Macrion displays an impact summary explaining what will be omitted or adapted to ensure Klick'r can open the file without errors.
5. Tap **Export Compatibility Copy**.

::: warning Always Keep a Macrion Backup
The compatibility export strips or adapts newer features so older apps can read the file. Always keep an ordinary Macrion backup as your master copy!
:::
