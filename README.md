## ProbeJS Legacy

is an unofficial continuation of ProbeJS for KubeJS versions abandoned by official ProbeJS.

## Usage

1.  You need to install ProbeJS Legacy, obviously.
2.  Enter a singleplayer world. Use `/probejs dump` to export datas gathered by ProbeJS Legacy. For first-time users, this will be done automatically when you join the world.
3.  Open your game instance folder using VSCode.
4.  Enjoy!

Note0: type `@item` or `@block_tag` to see snippets generated.

Note1: game instance folder is where you can see the `kubejs` folder directly.

Note2: Other IDEs with support for TypeScript typing (.d.ts) should also work.

## New Features

(Compared to ProbeJS)

- Simulated `Internal`
   - ProbeJS Legacy will now generate a `Internal` namespace, allowing users to migrate from ProbeJS 6 or ProbeJS Legacy 3 easily.
   - Example: `Internal.ItemStack` and `Internal.ItemStack_`

- Better Param Naming
   - Param names that conflict with TypeScript will now be renamed in a better way.
   - Param names in Method/Constructor, if in `arg123` format, will be renamed to a new name generated from its type.

Example:

```typescript
"mayUseItemAt"(arg0: $BlockPos$$Type, arg1: $Direction$$Type, arg2: $ItemStack$$Type): boolean
```
will be renamed to

```typescript
"mayUseItemAt"(blockPos0: $BlockPos$$Type, direction1: $Direction$$Type, itemStack2: $ItemStack$$Type): boolean
```

- Better `$Class<T>`
   - ProbeJS will transform `$Class<T>` into `typeof T`, but ignoring many conditions where this should not be done. ProbeJS Legacy makes it more correct.
   - Some basic classes, for example `java.lang.String` and `java.lang.Character`. can now be dumped.
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

- Typing files sharing
   - By extracting common parts in dumped classes, the total typing files size can be much smaller than before.
   - Another benefits is that TS server can now reuse common typings between 3 script types, improving IDE performance.

- Better comment formatting
   - blank lines at the front/back will now be ignored
   - comments will be formatted as one line if there's only one valid line

- Side only methods (marked by `@OnlyIn`) will have comments telling you that it's server-only or client-only.

- `xxx.d.ts` file with too many modules in it will now be split into smaller files automatically, providing better IDE performance

- Method/Field in declaration files are now naturally sorted.

- `gloabl` resolving: values you put into `global` can be resolved now. Configure resolving depth via `'global' Resolving Depth` config.

- And many internal changes. This Readme will certainly be too long if I write them all down.

## Collecting data

### KubeJS Event

For 1.16.5, ProbeJS Legacy will record all posted event. If you're unable to find some common events, try triggering it before dumping data. Example: `block.place` event is triggered by placing a block.

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
- Classes read from cache file. The cache file is saved to `<your instance folder>/.probe/classes.txt`.
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
