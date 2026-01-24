# ProbeJS Legacy 6.0.0 -> 6.0.1

Skip anonymous class

- Anonymous classes will be skipped when scanning classes in Forge data and mod Jar
- fix some NPE issue
    - NPE when there's no `java(...)`/`Java.loadClass(...)` usage in a script type
    - NPE when a mod in `Mods with forced Full Scanning` config provides no jar manifest
- [1.16.5] make `BuiltinEventRecord.RECORDS` a mutable map, to allow adding custom event record
- [1.20.1] add type doc for `ReplacementMatch`
- [1.16.5] add `JSMemberType.Builder.literalMember(...)` back for backward compat (mostly KesseractJS compat)
- [1.16.5] remove duplicated event record
- [1.16.5] make `AccessClassData` a static util class

---

# ProbeJS Legacy 5.2.4 -> 6.0.0

The Simplicity Update

## Breaking Changes for script developer

Breaking changes for script developer:

1. TS style import ("packages/xxx/xxxx/x/X") is removed from ProbeJS Legacy

This means using `require("packages/...")`, `java("packages/...")` or `Java.loadClass("packages/...")` is no longer supported by ProbeJS Legacy. PJSL provides typing matching for the raw class name, and you should always use that as best practice.

Example:
- 5.2.4 for 1.16.5: `java("packages/net/minecraft/world/item/$ItemStack")`
- 6.0.0 for 1.16.5: `java("net.minecraft.item.ItemStack")`
- 5.2.4 for 1.20.1: `Java.loadClass("packages/net/minecraft/world/item/$ItemStack")`
- 6.0.0 for 1.20.1: `Java.loadClass("net.minecraft.world.item.ItemStack")`

Tip: If you are too lazy to manually replace the class name string, you can just delete that and import again

```js
// delete this line
const $ItemStack = java("packages/net/minecraft/world/item/$ItemStack")

// write import name and IDE will provide auto-import
$ItemStac

// select the approriate import
const { $ItemStack } = require("net.minecraft.item.ItemStack")

// and optionally, replace "require"
const $ItemStack = java("net.minecraft.item.ItemStack")
```

2. Global `$SomeType_` is removed, use `Internal.SomeType_` and `Internal.SomeType` instead

## Notable fix/improvement

- Support for java-like raw usage of generic class
  - Example: for `List<E>`, you can use `List` directly, and ProbeJS will now infer the variable `E` as type boundary
- [1.20.1] Fix recipe event support
- `LoadClass<T>` implementation is changed to prevent constructing large union types for class matching, this can improve IDE performance
- Integration for ProbeJS VSCode extension is removed
- More accurate and more performant mixin class check for ModJarClassScanner
- [1.20.1] fix lang data reading
- [1.20.1] fix loot table reading
- Simplify code for reading some Minecraft/KubeJS data, to be less invasive and more performant
- [1.16.5] disable Registry type assignment because it's simply not a thing on 1.16.5
- [1.20.1] fix false positive type assignment for some registry
- [1.20.1] fix `$RegistryEventJS<$Object>` using denied `$Object`
- Added "old typing removed" message for shared dump
- [1.16.5] add predefined KubeJS event data, allowing users to see most events without manually triggering it

## Notable changes of ProbeJS Legacy internal

- You can add you own child dump, via `ProbeJSPlugin.addChildDump(...)`
- ProbeJS Legacy now uses a "unified" project layout, with support for 1.16.5 and 1.20.1 provided in a single git branch, sharing game-independent code infrastructure
- As a result of this, some methods/classes are moved
- `ClassPath.toClazz` removed
- `Types.typeMaybeGeneric` removed, you don't need it anymore (see "Support for java-like raw usage of generic class")
- Generated type hint classes now all use `zzzank.probejs.generated` package
- `JSMemberType.Builder.literalMember(...)` -> `rawNameMember`
  - This will break KesseractJS, but is there anyone actually using it?
- Codec usages are moved. For 1.16.5, ProbeJS Legacy will use Gson instead of Codec to read/write EventJSInfo

---

# ProbeJS Legacy 5.2.3 -> 5.2.4

fix recipe doc

- Fix recipe doc crashing dumping process when KubeJS Create is NOT installed

---

# ProbeJS Legacy 5.2.2 -> 5.2.3

fix config data & edge case for script transform

- Fix a config problem which will cause config data ingame to be invalid before first dump, which will then cause auto dump to trigger at each game play
- Fix ScriptTransformer causing inconsistent line content and accidentally got invalidated when there's trailing comment

---

# ProbeJS Legacy 5.2.1 -> 5.2.2

fix StackOverflow, again

- Turns out the previous fix is just deferring the StackOverflowError to the first usage

---

# ProbeJS Legacy 5.2.0 -> 5.2.1

fix StackOverflow, I mean the error, not website

- Fix `java.lang.StackOverflowError` when resolving some method type variables

---

# ProbeJS Legacy 5.1.1 -> 5.2.0

isolated scope

- new type replacement system that can replace variable more thoroughly
- `isolatedScope` is now enabled by default
- using `require(...)` without scope isolation will now output an error instead of a warning
- fixed some generic classes having no default type param in converted type
- minor optimization for converting class type

---

# ProbeJS Legacy 5.0.0 -> 5.1.1

instanceof

- `instanceof` support for `JClass`, aka the class loaded via `java(...)` (1.16.5) or `Java.loadClass(...)` (1.20.1)
- fix typing for some binding via interface class
- better method filtering for interface method
- fix type assignment for `$Class`, this should fix `onForgeEvent(...)` in 1.16.5 and `ForgeEvents.onEvent(...)` in 1.20.1

---

# ProbeJS Legacy 4.8.2 -> 5.0.0

Typing files sharing

- Common files in three script types will now be extracted to `shared` folder, making total typing folder size much smaller
- Options in `jsconfig.json` are tweaked to make typing loading faster
- `TypedDynamicFunction` support is removed, there doesn't seem to be anyone using it
- better filtering for "methods from superclass"
- better filtering for fields inherited from superclass
- namespace generation for interface will be skipped when possible
- typing support for `ItemType`, used by item builder
- simplify js object formatting in `global`
- fix some method in MethodDecl.Builder not returning `this`
- mod jar scanning can now handle mod with no file properly
- (internal) seal most registry info access
- invalid classes will now be skipped in auto param renaming
- (internal) config system rewritten, will not affect actual config file
- `TSUtilityTypes` are exposed to JS event

---

# ProbeJS Legacy 4.8.1 -> 4.8.2

fix setter name

- fixed setters generated from methods having wrong name
- config will be automatically refreshed on each dump request
- ProbeJS will now warn users when using `require` with `isolatedScopes` disabled

---

# ProbeJS Legacy 4.8.0 -> 4.8.1

fix hidden setters

- fixed getters hiding setters with the same name
- hide some stub methods in Rhizo and KesseractJS

---

# ProbeJS Legacy 4.7.2 -> 4.8.0

Better Beaning

- Beaning (setter and getter) generation is rewritten
    - fixed setter being identified as getter
    - fixed some generated getter being invalid and not actually present
    - fields can now be converted to getter and setter automatically, toggle via `Field As Beaning` config
- Rhizo 3.6.5 support
    - Rhizo 3.6.5 removed explicit class wrapping, and added `ClassWrapper`, ProbeJS Legacy adapts to both
- Type assignment improvement
    - many invalid or redundant type assignment docs are removed
    - added type assignment doc for RandomIntGenerator and MaterialJS
    - `$Class` type will not be automatically converted to `JClass` now
- and some internal changes

---

# ProbeJS Legacy 4.7.1 -> 4.7.2

fixing & info++

- fix type inferring in `java(...)`
- rewrite ClassRegistry class walking to skip invalid class
- classes that are server-only will not show up in `java(...)` for client scripts, the same apply to server scripts
- fix ImportInfos providing for TypeDecl
- fix loading vanilla classes from cache
- add error message when there are exceptions in class cache loading
- fix `@JSParams` handling
- add info for `@OnlyIn` side only marker

---

# ProbeJS Legacy 4.7.0 -> 4.7.1

fix type declaration

- fix type declaration having broken imports, this is especially significant in simulated `Internal`
- fix param of `recipeEvent.custom(...)`

---

# ProbeJS Legacy 4.6.3 -> 4.7.0

`@ReturnsSelf` support & better comment formatting

- add Rhizo ReturnsSelf annotation support
- better comment formatting
    - blank lines at the front/back will now be ignored
    - comments will be formatted as one line if there's only one valid line
- rejected classes will not be included in simulated `Internal`
- fixed SimulateOldTyping cannot to toggle on/off unless the game is restarted
- fix create sequenced assembly recipe doc
- skip reading read-only configs

---

# ProbeJS Legacy 4.6.1 -> 4.6.3

Fix again & Mod Scanner

- Fixed problems when there are too many classes in a single package.
- a new config entry `Mods with forced Full Scanning`, where you can force ProbeJS Legacy to perform full scan on
certain mods by adding its modid.
- `.gitignore` modification will now make Git not ignore ProbeJS Legacy config

---

# ProbeJS Legacy 4.6.0 -> 4.6.1

Fix `java(...)`

- Fixed `java(...)` broken in 4.6.0
- ProbeJS Legacy will now print a bit more error message when some classloading fails
- `xxx.d.ts` file with too many modules in it will now be split into smaller files for better performance

---

# ProbeJS Legacy 4.5.0 -> 4.6.0

Simulated `Internal`

- ProbeJS Legacy will now generate a `Internal` namespace, allowing ProbeJS Legacy 3 users to migrate to ProbeJS Legacy 4
more easily
- also, instead of having to import a precise type to actually use it in your jsdoc, for example `$ItemStackJS`, you can
now use `Internal` namespace to access it, for example `Internal.ItemStackJS`
- two unused config entries are disabled
- Fixed a strange type generic bug

---

# ProbeJS Legacy 4.4.2 -> 4.5.0

Auto Param Renaming

- Better Param Naming
    - param name that conflicts with TypeScript will now be renamed in a better way
    - param names in method/constructor, if in `arg123` format, will be renamed to a new name generated from its type
    - example: `"mayUseItemAt"(arg0: $BlockPos$$Type, arg1: $Direction$$Type, arg2: $ItemStack$$Type): boolean` will be renamed to
      `"mayUseItemAt"(blockPos0: $BlockPos$$Type, direction1: $Direction$$Type, itemStack2: $ItemStack$$Type): boolean`
- method/fields in declaration files are now naturally sorted
- remove 'ignore Context as first arg' since rhino does not have it
- new JS side event: AddGlobalEventJS, allowing you to add global type declarations
- fixed function in binding being recognized as `$ArrowFunction`
- function in binding can now display its param count
- changed how brackets are added to types for simplicity
- added brackets to JSLambdaType to prevent TS syntax error
- some helpers in JS event provided by ProbeJS Legacy
- support enum wrapper of newest Rhizo (3.5.0)
- fixed static methods present in interface (where it shouldn't be) and not present in static class variant (where it should be)
- more debug message when error happens during typing generation
- fix RecipeEventJS#custom() reporting error when `checkJs` is on
- removed (some or all) invalid type assigning
- fixed trying to read a non-exist config file on first run
- skip `namespace` in config file if possible
- get rid of the extra space in some methods and fields
- config will be refreshed on joining world

---

# ProbeJS Legacy 4.4.1 -> 4.4.2

registry objects filter

- Registry Object Filter
    - a string regex used for filtering registry objects.
      Registry objects whose id matches this pattern will always be dumped by ProbeJS Legacy
    - by default it's set to only dump minecraft registry objects for performance
    - but noted that registry object classes might already be cached by our class cache file,
      you might need to delete `.probe/classes.txt` to actually see its effects
- added brackets to JSLambdaType to prevent TS syntax error
- mod performance threshold is decreased from 300 to 200
- add config path hint in some messages

---

# ProbeJS Legacy 4.4.0 -> 4.4.1

fix config

- fix config value set by user cannot be read by config
- builtin plugins are now always registered before any other plugins
- better function formatting in `global`
- reorganized file writing, it's now probably faster by reusing writer
- the values in `GLOBAL_CLASSES` are now again `typeof XXX` only, instead a new utility type `AttachJClass` is added to attach `JClass` to result of `java(...)`, this has no effect for users
- primitive types `charseq` and `character` will not create redundant codes (namespace, static class) now
- config parser for old config format is removed
- config `fullScan` is replaced by `Class Scanner` allowing you to choose class scanner

---

# ProbeJS Legacy 4.3.1 -> 4.4.0

better `$Class<T>` and recipe doc

### Event

- onForgeEvent() that uses string as its first arg will now have typing supports again
    - for example `onForgeEvent("net.minecraftforge.event.entity.ProjectileImpactEvent$Arrow", (event) => {}`
- now almost all elements in `recipes` in RecipeEvent will now be resolved, `recipes.minecraftCampfireCooking` for example
- by default some json-based recipe function is not dumped, since they're basically the same as `event.custom(...)`, a new config option is added that allows dumping them

### Class

- better `$Class` typing that prevents `typeof T` or `typeof any`
- `$Class` will now be redirected to `JClass` for clearer type hint
- primitive classes will not somehow be dumped now
- Java wrapper classes like `java.lang.String` can now be dumped again
- results of `java(...)` can now be used for generic matching that requires `$Class<T>`

### Others

- fixed registry names with same path and different namespace from conflicting
- support for KesseractJS
- ScriptableObject and NativeArray will be resolved to its JS type, for better `global` resolving
- cleanup imports of `GlobalClasses`

### Internal

- several tweaks to improve typing generation performance
- mixin targeting forge event bus is removed because we have class scanner, this improves forge event performance a bit
- probejs plugin can only be registered in Java code now
- make `ItemStackJS` assignable from `ItemStack`
- classes with simple package name will be dumped to `simple_package_classes.d.ts`

---

# ProbeJS Legacy 4.3.0 -> 4.3.1

fix recipe docs

- see 4.3.0 changelog for full changelog

---

# ProbeJS Legacy 4.2.0 -> 4.3.0

`java()` revive && native class scanner

- `java()` revive
    - performance issue of `java()` has been resolved, so it's enabled again
    - two paths are provided for each class that can be loaded by `java()`, one Java style, one TS style. It will be used by VSCode plugin in the future 
    - `SomeInterface$$Static` will now exported for Java interface `SomeInterface`, used for better `java()` support
    - `require()` is now redirected to `java()`, and is deprecated
    - you can use RegEx `^const\s?\{\s*(\S+)\s*\}\s*=\s*require\s?\((\S+)\)` to `let $1 = java($2)` to remove `require` usage
- now a filter will be applied to class scanner, to prevent crash due to scanning dangerous classes, and performance issue due to dumping too many classes
    - it's enabled by default, but you can also disable it in config fil: `probejs.fullScan`
- native class scanner via class data scanned by Forge
- a new command for refreshing config
- `Class<SomeClass>` will now be redirected to `typeof SomeClass`, allowing better type hint for `Class`
- bindings and recipes are now read directly instead of via a dummy event
    - a side effect of this is that it prevents spamming log file
    - binding reading will now read some more user defined global values, it's due to a design from KubeJS side
- ProbeJS now allows registering plugins independently, instead of relying on KubeJS(1.16)'s too-simple plugin management
- config file will not be saved during initialization, making IO performance a little bit better
- more config comments
- fix registry types' type assignment
    - this means that `Special.XXX` will not always be `(any) | (any) | ...`
- Context#jsToJava() is replaced by a context specific variant, should be more precise
- some more fail-safe and error logging

---

# ProbeJS Legacy 4.1.0 -> 4.2.0

"global" resolving & config rewrite

## What's new?

-   values added to `global` can now be scanned and exported to typings
    -   e.g. `global.jeiRuntime` (when JEI is installed) will now have typings provided
    -   this is enabled by default, you can also disable it in config file
-   config system is now rewritten. Now all available configs are written to `probe-settings.json`, allowing you to change them without in-game commands.
-   new config system will also provide more information in config file:
    -   current value, with `$value` as its key
    -   default value, with `$default` as its key
    -   comments, with `$comment` as its key
-   a new config option for not dumping non-public and not referenced classes, disabled by default
-   make event cache smaller by not storaging event id twice
-   ProbeJS Legacy will warn players when typing generations failed
-   recipe doc now also apply to startup events
-   binding `Types` is removed, import `$Types` instead
-   smarter scope wrapping, more in line with how CommonJS in VSCode works
-   fix an edge-case bug where `exportXXXX` will cause scope problem
-   fix `typeRoots` and `BaseUrl` in jsconfig.json

Note: ProbeJS Legacy 4 is very different from ProbeJS Legacy 3.x and older versions, old typing files are invalid in 4.x version, see [ProbeJS 7 release note](https://github.com/Prunoideae/ProbeJS/releases/tag/v7.0.0) and [another ProbeJS 7 release note](https://github.com/Prunoideae/ProbeJS/releases/tag/v7.0.0-1.21) for more details. If you are unable to use 4.x, or just don't like it, `3.11.0` is the only recommended 3.x version of PJS Legacy.

---

# ProbeJS Legacy 4.0.0 -> 4.1.0

Safer class scanning

## What's new?

-   Largely eliminates the chance of crashing the whole game due to loading critical classes too early
-   Better doc for `java()` function and `require()` function
    -   `java()` is now marked as deprecated, please use `require()` :)
-   And some internal cleanups

Note: ProbeJS Legacy 4 is very different from ProbeJS Legacy 3.x and older versions, old typing files are invalid in 4.x version, see [ProbeJS 7 release note](https://github.com/Prunoideae/ProbeJS/releases/tag/v7.0.0) and [another ProbeJS 7 release note](https://github.com/Prunoideae/ProbeJS/releases/tag/v7.0.0-1.21) for more details. If you are unable to use 4.x, or just don't like it, `3.11.0` is the only recommended 3.x version of PJS Legacy. 

---

# ProbeJS Legacy 3.10.0 -> 4.0.0

A huge update, see [ProbeJS 7 release note](https://github.com/Prunoideae/ProbeJS/releases/tag/v7.0.0) and [another ProbeJS 7 release note](https://github.com/Prunoideae/ProbeJS/releases/tag/v7.0.0-1.21) for more details.

But of course PJS Legacy 4 will not be a one to one copy of PJS 7, exact implementations vary a lot, and there can also be differences in features. 

---

# ProbeJS Legacy 3.10.0 -> 3.11.0

3.11.0 is a patch version that fixes Rhizo remapper compatibility, because Rhizo updated its remapper again after 3.0.

This version is marked as Beta only because it's not part of main-stream development, stability of this version is still pretty good.

---

# ProbeJS Legacy 3.9.9 -> 3.10.0

Rhizo 3.0 support

## What's new?

-   Use a new way of detecting Remapper, to properly support Rhizo 3.0.
-   ProbeJS Legacy will now warn users when Rhizo is not installed.
    -   not installing Rhizo can lead to problems like broken filtering for methods/fields.
-   Comment handling is further improved.
-   Excessive setter filtering is now removed, allowing more possible setter to be dumped.
-   Methods/fields additions from user docs will now show up at the front.
    -   this allows manually added methods/fields to have the highest priority when being parsed.
-   Internal cleanups for ProbeJS Legacy 4.0.0.
-   Doc type parsing for objects and tuples.

NOTE: 4.0.0 is still under development. If you're interested in it, you can see [dev note](https://github.com/ZZZank/ProbeJS-Forge/blob/1.16.5/dev_note.md) for the overall code structure of 4.0.0

---

# ProbeJS Legacy 3.2.0 -> 3.9.9

tag dumping++ & snippet rework

## What's new?

-   all available tag types will now be dumped
    -   e.g. slurry tag dumping when you have Mekanism installed: `type slurry = "mekanism:dirty"|"mekanism:clean";`
-   new snippet format
    -   e.g. `{some modid}.item.tag` -> `item_tag`
    -   the usage of `.` in snippet is removed because `.` will actually break snippets.
    -   snippet name is now much shorter, and with modid removed. Should be more accessible.
    -   `vanillaOrder` config is removed because of this.
-   fix multiple reference names for global class only have one applied
    -   an example: `Vec3d` and `Vec3` are linked to the same class, and now ProbeJS will provide type alias for them.
    -   `declare const Vec3d = Vec3`
    -   This was previously done by manually adding alias via raw docs, and was incomplete
-   better underscore handling for types
-   impl jsconfig merging
    -   config values added to jsconfig will be preserved after jsconfig dumping
    -   e.g. `"checkJs": true` in `"compilerOptions"` scope will not be removed after jsconfig dumping.
-   ProbeJS Legacy will now make JS type check targeting ES6 instead of ES5.
-   comment improvements
    -   the format of comments, actually. Comments marks at the front of each line will look much better.
-   some more fail-safe for horribly malformed json file
-   confusing "hybrid" type is removed
    -   previously used for FunctionalInterfaces type checking, but we have lambda type alias for it.
-   return type of onEvent/onForgeEvent is now explicitly marked as `void`
-   ProbeJS Legacy will now walk types in constructors
-   Some more type docs to support PonderJS better.

NOTE: Most features in this version are initially for 4.0.0, a huge rewrite. If you're interested in it, you can see [dev note](https://github.com/ZZZank/ProbeJS-Forge/blob/1.16.5/dev_note.md) for the overall code structure of 4.0.0

---

# ProbeJS Legacy 3.1.0 -> 3.2.0

Rhizo(not typo) support

## What's new?

-   Rhizo(not typo) support
    -   ProbeJS Legacy now supports Rhizo, which adds method/field remapper for 1.16.5
    -   With Rhizo, you can access methods/fields using readable MCP names like `getOpPermissionLevel()` instead of SRG
        names like `func_110455_j()`
    -   ProbeJS Legacy specially supports Rhizo's remapper, and can dump mapped MCP name for typing.
    -   If you're not using Rhizo, ProbeJS Legacy will automatically skip remapper accessing, so old Rhino is still
        compatible.
-   ProbeJS Legacy will now walk type parameters of superclass/interfaces more completely.
-   Some more type docs to support PonderJS better.

---

# ProbeJS Legacy 3.0.1 -> 3.1.0

better type recognizing

## What's new?

-   lambda style type alias for Functional Interface
-   recognize any interfaces with one and only one abstract method as Functional Interface
-   fix tag snippet compiling
-   improve error message, making it red, with links to Github issue page
-   OnJavaMixin
    -   result of calling `java()` will now be touched by ProbeJS, so that next time dumping is triggered, it can be dumped.
-   "implements" keyword support for class documents
-   better line recognizing for documents
-   special assignment of MaterialJS
    -   e.g. `type MaterialJS_ = "sponge" | "explosive" | "wool" | "ice" | "air" | "vegetable" | "clay" | "slime" | "rock" | "lava" | "leaves" | "web" | "grass" | "coral" | "cake" | "honey" | "plants" | "wood" | "dragon_egg" | "portal" | "organic" | "gourd" | "glass" | "metal" | "dirt" | "water" | "lantern" | "stone" | "sand" | "snow" | "plant" | "earth" | "iron" | "tnt" | "berry_bush" | MaterialJS;`
-   touch contents of pre-defined constants
    -   e.g. `global.jeiRuntime`
-   var arg support for method params
-   class touching for generics in superclass/interfaces
    -   type hint when using PonderJS should be better now
-   (try to) add underscore to documented type when possible
-   generics in superclass/interfaces will now be formatted
-   make method/constructor param typed

---

# ProbeJS Legacy 3.0.0 -> 3.0.1

Fix `Internal.ItemStackJS_`

## What's new?

-   fix class assignment for ItemStackJS.
    -   use MCP class name instead of Official mapping class name.
    -   i hate inconsistent mapping, so much trouble.
-   entries in `java.d.ts` are now naturally sorted.
-   js config is tweaked to avoid triggering probe typing when not in `kubejs/{...}_script` folder.

---

# ProbeJS Legacy 2.6.1 -> 3.0.0

Rich display info dumping

## What's new?

-   ProbeJS Legacy will now dump rich display informations about items, fluids, blocks, tags, and lang keys, providing rich display in VSCode when ProbeJS(VSCode Extension) is installed.
    -   for items, it can display:
        -   item id,
        -   localized name(or lang key if no localization is avaliable),
        -   whether it's block or item or crop, max stack size, and
        -   special infomations for tools and foods and etc.
        -   If you have icons exported via [Icon Exporter](https://www.curseforge.com/minecraft/mc-mods/iconexporter), icons for such item can also be displayed.
    -   for fluids, it can display
        -   id,
        -   localized name(or lang key if no localization is avaliable),
        -   whether it has a fluid block, and
        -   whether it has a fluid bucket item.
    -   for lang kays, it can display:
        -   all valid localization under similar locale, and `en_us`
    -   it's really hard to name them all, please try it by yourself.
-   config "disabled" is now replaced by "enabled", to make ProbeJS VSCode extension happy.
    -   Old config will be automatically upgraded.
-   `.vscode` now will be generated at `.minecraft/` folder, aka game folder, making workspace more useful.
-   Snippets of registries now support all avaliable registry types, e.g. potion and enchantment
-   ProbeJS now fetch registries and tags from Minecraft internals, instead of relying on an external json file.
    -   This change prevents reloading and exporting data when `/probejs dump` is triggered, which makes dumping much much FASTER, especially on a large modded instance.
    -   Also, `autoExport` config is removed because of this.
-   Use different method filtering approaches for classes and interfaces, so we should be able to filter out duplicated methods more completely.
-   Set default values for configs, so that first-time users will not get a all-disabled config
-   ProbeJS Legacy now dump registries into `special.d.ts` instead of `registries.d.ts`
-   More datas, including tags and platform data, will be dumped into `special.d.ts`
-   dumping messages are improved now, roughly showing stages of dumping.
-   class `DamageSource` will now show accepted assginments in its type. E.g. `type DamageSource_ = "inFire"|"lightningBolt"|"onFire"|"lava"|"hotFloor"|"inWall"|"cramming"|"drown"|"starve"|"cactus"|"fall"|"flyIntoWall"|"outOfWorld"|"generic"|"magic"|"wither"|"anvil"|"fallingBlock"|"dragonBreath"|"dryout"|"sweetBerryBush"|DamageSource;`

---

# ProbeJS Legacy 2.6.0 -> 2.6.1

Fix "optional" class dumping crash

## What's new?

-   ProbeJS will now catch error thrown when classes to be looked into is missing.
    -   This means that "Optional" classes, like classes intended for cross-mod compatibility, should be able to be dumped without crashing the whole game
    -   This bug actually exists ever since the very first release of ProbeJS Legacy, so updating to this version is highly recommended.

---

# ProbeJS Legacy 2.5.0 -> 2.6.0

Registry Dumping

## What's new?

-   Registry Dumping!
    -   Registries, whether from vanilla(e.g. items, blocks) or mods(e.g. Mekanism Slurries), can now be dumped into `registries.d.ts`.
    -   Registries will be resolved into types, showing every avaliable names under such registry. e.g. `type schedule = "minecraft:empty"|"minecraft:simple"|"minecraft:villager_baby"|"minecraft:villager_default";`
    -   Types that are registry entries will now take cooresponding registry as its assignables, like `type Attribute_ = Registry.minecraft.attribute | Attribute;`
    -   This means in some cases, like `item.enchant(..., 1)`, using string in `...` will no longer confuses your IDE, if you enable type checks.
-   Minor performance tweaks to allow event listening mixin runs a little bit faster. After this change, changing config `disabled` will actually require a game restart to take effects
-   move generalized representation of `onEvent` and `onForgeEvent` lower to prefer sepcialized ones

---

# ProbeJS Legacy 2.4.1 -> 2.5.0

RecipeFilter\_ & FunctionalInterfaces

## What's new?

-   Functional Interfaces(Interfaces that accept Lambda as their instances) can now also display their original type, thus accepting document
    -   e.g. `event.replaceInput(filter, toReplace, replaceWith)` in RecipeEvent, where `filter` used to be a only lambda function, but now accepts Lambda, original type, and objects like `{mod: "minecraft", type: "minecraft:blasting"}`
-   Detailed doc for `RecipeFilter`, `ItemStackJS`, `IngredientJS`, and much more
-   Event doc will now have a generalized variant displayed, to handle events that are not exported by ProbeJS yet

---

# ProbeJS Legacy 2.4.0 -> 2.4.1

Fix type casting error

## What's new?

-   fix error caused by type casting in formatScriptable()
-   doc for `AttachedData` and `CompoundNBT`. It can now act like a regular JS object(which means accessing members in formats like `data.some_member` or `data["some_member"]` will no longer confuse your IDE)

---

# ProbeJS Legacy 2.3.1 -> 2.4.0

Event dump ++

## What's new?

-   events dumped in `events.d.ts` are now naturally sorted(in alphabet order).
-   dumped events will have more info displayed, e.g. if such event is cancellabe or not.
    ```javascript
    /**
     * @cancellable No
     * @at startup, server
     */
    declare function onEvent(name: "item.crafted", handler: (event: Internal.ItemCraftedEventJS) => void);
    ```
-   rawTS doc will now have no namespace wrapped. Instead, there will be two comments marking start and end.
-   fix Raw TS doc being cleared even before they are used for doc gen, so Raw TS doc can actually get generated.
-   fix tag snippets not writing into files.
-   enable special formatter for rhino::Scriptable.
-   recipe doc for Thermal Series.
-   constructors for classes in `Internal` namespaces will now be showed, with comments clarifying that you needs `java()` to actually use it.
-   remove `haunting` in Create doc, since it's not a thing in MC1.16.

---

# ProbeJS Legacy 2.3.0 -> 2.3.1

Fix crash caused by ConcurrentModificationException

## What's new?

-   use `containsKey` + `put` instead of `computeIfAbsent` to avoid CME crash.
-   fix events with sub-id not actually having sub-id.

---

# ProbeJS Legacy 2.2.0 -> 2.3.0

Better document system

NOTE: Due to changes in event cache for sub-id support, previous cache will be invalid for this version.

## What's new?

-   Fix param fetching of documents.
    -   This is a bug that has existed since 2.0.0, because the original document system from ProbeJS for MC 1.18 is already problematic.
    -   Try `event.shaped()` or `event.shapeless()` in recipe event, its type hint should be normal now.
-   ProbeJS Legacy can now catch every fired KubeJS event, without the needs of `onEvent`.
-   ProbeJS Legacy can catch events with sub id, e.g. those from FTB Quest.
    -   Because of
-   Hand-written recipe doc and auto-gen recipe doc will now be combined together, providing more accurate parameter info.
-   Documents for class now support `extends` .
-   RawTS doc will now has no namespace wrapped.
-   ProbeJS Legacy can now properly get the index of outter bracket in method documents.
-   Better documents for builtin types, like `CompoundNBT` .
-   Several performance tweaks to improve performance, especially on heavily modded instances.

---

# ProbeJS Legacy 2.1.0 -> 2.2.0

Dump trimming!

## What's new?

-   Dump trimming: probeJS will now make use of inherited Class, and avoid dumping a method/field if such method/field is already avaliable through inheritance. This can greatly reduce the size of `global.d.ts`
    -   On a slightly modded instance, dump trimming makes dump size decrease from 8.02MB to 2.55MB, less than 1/3 of the original dump size.
-   Better documents for builtin types, like `Text` or `Map`
-   Java type `Object` will now be showed, but in namespace `Document`, and every "complex" type will be it's subclass(for better dump trimming)

---

# ProbeJS Legacy 2.0.0 -> 2.0.1

## What's new?

-   ProbeJS will now listen to EVERY Forge events, so that users don't need to painstakingly search for names of Forge Events (which are usually very long).
-   `/probejs dump` command is now restricted to Singleplayer, to prevent freezing servers by accident.
-   A slightly better documents for builtin types, like `ResourceLocation`

---

# ProbeJS Legacy 1.6.0 -> 2.0.0

A huge update, adding support for Document, Forge event listening, full config with autosave, and much much more

## What's new?

-   Collected class/methods/... will now be dumped to `kubejs/probe/` folder, instead of `kubejs/kubetypings/` folder.
-   ProbeJS can now dump more classes/methods into `global.d.ts`
    -   On a slightly modded 1.16.5 Forge instance, ProbeJS can now dump about 164000 lines, 4.4 times of previously 37700 lines.
-   ProbeJS can now dump java access related data better.
    -   On a slightly modded 1.16.5 Forge instance, ProbeJS can now recognize and dump about 2000 method presets, 33% more than previously 1500.
-   An actual config file will be generated at `kubejs/config/probe.json`, and will be automatically saved on change(through command).
-   Event dumping now supports `onForgeEvent()`
-   Correctly resolves types used by List/Map
-   Types will have `_` appended to prevent conflict
-   better support for RecipeSerializer
-   Remove `dump.js` generation, because it seems completely useless
-   Suopprt getters
-   and many small changes, for a better experience

---

# ProbeJS Legacy 1.?.? -> 1.6.0

The first release of ProbeJS 1.16.5 unofficial continuation.

## What's new?

-   ProbeJS now mixins into event listening, so you no longer need captureEvent to capture fired events.
-   Fix command arguments. /probejs previously will do what /probejs dump does, that's fixed now.
-   Add semi-comma in dump.json to handle malformed tag key, like per-viam-invenire:replace_vanilla_navigator, whose `-` will causes troubles
-   Respect kubeJS annotations.
    -   Ported from https://github.com/Prunoideae/ProbeJS-Forge/commit/549118a44d07b5e7a10f0ff71ac8ce5338a7066e
-   You can use `/probejs config xxxx` to access configs. Currently you can use `/probejs config dump_export` to toggle dump.json generating on or off
