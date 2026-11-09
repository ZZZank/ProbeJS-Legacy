## ProbeJS Legacy

is an unofficial continuation of ProbeJS for KubeJS versions abandoned by official ProbeJS.

## Usage

1.  You need to install ProbeJS Legacy, obviously.
2.  Enter a singleplayer world. Use `/probejs` to open the dump screen, then press the `Start Dump` button to export data gathered by ProbeJS Legacy.
3.  Open your game instance folder using VSCode.
4.  Enjoy!

Note0: type `@item` or `@block_tag` to see snippets generated. You can also use `#priority` and `#requires` to generate file headers.

Note1: game instance folder is where you can see the `kubejs` folder directly.

Note2: Other IDEs with support for TypeScript typing (.d.ts) should also work.

Note3: options can be edited in-game via `/probejs config` command, or the `Config` button in the dump screen.

## New Features

(Compared to ProbeJS)

- Simulated `Internal`
   - ProbeJS Legacy will now generate an `Internal` namespace, allowing users to migrate from ProbeJS 6 or ProbeJS Legacy 3 easily.
   - Example: `Internal.ItemStack` and `Internal.ItemStack_`

- Better Param Naming
   - Param names that conflict with TypeScript will now be renamed in a better way.
   - Param names in Method/Constructor, if in `arg123` format, will be renamed to a new name generated from its type.

Example:

```typescript
declare class Item {
    "mayUseItemAt"(arg0: $BlockPos_, arg1: $Direction_, arg2: $ItemStack_): boolean
}
```
will be renamed to

```typescript
declare class Item {
    "mayUseItemAt"(blockPos0: $BlockPos_, direction1: $Direction_, itemStack2: $ItemStack_): boolean
}
```

- Parchment mapping support
   - Real parameter names (e.g. `x`, `y`, `z` instead of `int2`, `int3`, `int4`) and javadoc from Parchment can be downloaded and applied on demand, providing better autocomplete experience.
   - The mapping is downloaded on demand and cached in `.probe/external`, so a network connection is required when it's not cached yet. Toggle it with the `Enable Parchment` checkbox in the dump screen.

- Better `$Class<T>`
   - ProbeJS will transform `$Class<T>` into `typeof T`, but ignoring many conditions where this should not be done. ProbeJS Legacy makes it more correct.
   - Some basic classes, for example `java.lang.String` and `java.lang.Character`, can now be dumped.
   - The `T` in `$Class<T>` is now actually usable for type hint.

- better `instanceof`
   - ProbeJS Legacy can now properly support `obj instanceof $SomeClass`, where `$SomeClass` is a class loaded via `java(...)` or `Java.loadClass(...)`.

Example:

```js
const $BoatItem = java("some.path.to.BoatItem")

let item = Item.of("boat").item; // here the type of `item` is `$Item`
if (item instanceof $BoatItem) {
    // here the type of `item` can be inferred to `$BoatItem`
}
```

- Field Beaning
   - Some fields will be transformed into getter+setter, in order to provide better support for TypeWrapper
   - Consider a `obj` with a field like this: `block: $Block`, and `obj.block = "minecraft:stone"` is a valid syntax in KubeJS. This had no typing support before, but now ProbeJS Legacy can support it properly.
   - Can be turned off via `Field As Beaning` config.

- Tree-like typing files
   - Typing files are organized as a tree grouped by Java package, and each package is exposed as a module named `java:<package path>`.
   - Common parts are extracted into a `shared` folder, which is shared by all script types. Typing files are smaller, and the TS server can reuse common typings between script types, improving IDE performance.
   - When using `require`, import like `const { $ArrayList } = require("java:java/util")`.

- Java-like raw usage of generic classes
   - For a generic class like `List<E>`, you can use `List` directly, and ProbeJS Legacy will infer `E` as its type boundary.

- Isolated script scopes
   - Preprocess scripts with import or export usage to match modern JavaScript behavior. Enabled by default, see `isolatedScopes` config.
   - `require(...)` requires this option; using `require` with isolation disabled will log an error.

- In-game config screen
   - `/probejs config` opens a config screen, so options like `Class Scanner`, `Mods with forced Full Scanning` and `Registry Object Filter` can be edited in-game.

- Dump screen
   - `/probejs` opens the dump screen, where each step of the dump process is displayed as an entry in a list.
   - `Clean Old Dumps` removes typing files that no longer exist, and `Enable Parchment` toggles Parchment mapping for this dump.

- Better comment formatting
   - blank lines at the front/back will now be ignored
   - comments will be formatted as one line if there's only one valid line

- Side only methods (marked by `@OnlyIn`) will have comments telling you that it's server-only or client-only.

- `xxx.d.ts` file with too many modules in it will now be split into smaller files automatically, providing better IDE performance

- Method/Field in declaration files are now naturally sorted.

- And many internal changes. This Readme will certainly be too long if I write them all down.

## Collecting KubeJS data

### KubeJS Event

For 1.16.5, common KubeJS events are predefined, so most of them can be found without triggering anything. Events that are not predefined are recorded when posted; trigger them once before dumping data. Example: `block.place` event is triggered by placing a block.

For 1.20.1, all registered KJS events will be dumped automatically, no need for triggering.

### Forge Event

ProbeJS Legacy will record all Forge events, regardless of whether they have been posted or not. This behaviour can be altered via `Class Scanner` config.

### Java Classes

ProbeJS Legacy will use multiple approaches for collecting Java classes.

These classes will be recorded:
- Classes loaded manually, aka those loaded via `java(...)` or `Java.loadClass(...)`
- Classes referenced by JS bindings, for example `AABB` and `Text`.
- Classes references by known classes, for example `Item.class` referenced by `ItemStack.class`.
- Registry objects, for example `BoatItem` for vanilla boats. By default this will skip non-vanilla objects, and can be altered via `Registry Object Filter` config.
- Classes from certain mods. See `Mods with forced Full Scanning` config.
- Classes found by class scanner. See `Class Scanner` config.

## Why

Initially, all I want to do is just find some sort of doc or helper for KubeJS script writing, so that I won't need to feel such pain in the arse when developing Enlightend6Expert([https://www.curseforge.com/minecraft/modpacks/enlightened-6-expert](https://www.curseforge.com/minecraft/modpacks/enlightened-6-expert)) .  
But after some research, I realized that 1.16.5, despite being one of the hottest modded versions, didn't even have a proper KubeJS helper mod.  
So there it is, a ProbeJS fork for 1.16.5.  
This is my first Java project and first Minecraft Mod project, and to be honest, ProbeJS is too tough for a coding noob like me. Thankfully I made it.

This project is licensed under LGPLv3, the same as the original ProbeJS.

## Showcase (Old)

Auto-completion snippets for Items, Blocks, Fluids, Entities and Tags:

![image](./examples/2.gif)

Auto-completion, type-hinting for most of the functions and classes:

![image](./examples/3.gif)
