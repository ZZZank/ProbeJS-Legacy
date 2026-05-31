# ProbeJS Legacy 6.1.0 -> 6.2.0

- Parchment mapping support!
  - Parchment is an open community-sourced modloader-neutral set of mappings of parameter names and javadocs, to augment the official names released by Mojang.
  - In short, Parchment provides better parameter name (`x, y, z` instead of `int2, int3, int4`) and builtin document for common classes
  - ProbeJS Legacy bundles a reprocessed mapping JSON file to provide Parchment mapping with no extra network requirement and minimum mod jar size increase
- fix null `ClassData.parent` causing NPE in dumping process
- generic param for class (e.g. `$FoodBuilder` in `$Consumer<$FoodBuilder>`) will now always be formatted as exact type
- fix recursive declaration of generic default value
- remove VSCode config addition, there's no need for it now
- fix convertible type not generated for those with no `.assignType()`
- AutoParamRename is now always on
- `Types.and()` for nothing now provides `any`

---
