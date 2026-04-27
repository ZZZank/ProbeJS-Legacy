# ProbeJS Legacy 6.0.2 -> 6.0.3

`probejs_legacy` as mod id

- Add an icon to ProbeJS Legacy mod file
- For 1.20.1, the main mod id was accidentally set to `probejs` during 6.0.0 update, which has now been changed back to `probejs_legacy`. For 1.16.5, you can use both `probejs` and `probejs_legacy` to check ProbeJS Legacy
- [1.20.1] Original ProbeJS will now be marked as incompatible
- [1.16.5] Fix KubeJS loading plugin multiple times when multiple mods are declared within one mod file
    - In theory this should be done by KubeJS itself, but KubeJS for 1.16 has long been dead
- improve filtering for methods that override methods in parent classes

---
