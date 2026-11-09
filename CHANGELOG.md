# ProbeJS Legacy 6.2.0 -> 6.3.0

## New

- Parchment mapping is now optional, and can be downloaded on demand
- Config screen for editing options in-game.
  - Commands for toggling options have been removed, use `/probejs config` to open config screen
- New screen for visualizing dump progress
  - You can now start a dump by opening dump screen using `/probejs`
  - Dump progress will now be displayed as a list of entries, each visualizing one step
- Convertible type for interface now uses TS interface type, improving `JavaAdapter` support
- `$XXX$$Static` renamed to `$XXX$$Interface`

## Fix

- Field to bean conversion might break doc modifiers targeting field
- Optional fields in some recipe types having optional markers inside their names. E.g. `mod?` instead of `mod`
- `[x in K]: V` for map has been removed due to it being buggy
- [1.16.5] events cache not used
- `@Generics` on non-parameterized types gets ignored

---
