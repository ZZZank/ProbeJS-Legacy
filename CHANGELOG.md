# ProbeJS Legacy 6.0.4 -> 6.0.5

- [1.20.1] fix recipe shortcuts like `event.shaped(...)`
- Fix some rare beaning name generation issue. Example: `.getRGB()` will now generate `.RGB` beaning instead of `.rGB`
- Convertible type declaration with no modification is now moved to `shared`. This can greatly decrease the amount of script-type specific files
- The param name in setter is now the same as original method
- [1.20.1] remove invalid & duplicated assignment, better `TemporalAmount` & `Item` & `ItemStack` support
- JClass is now a type instead of class
- More precise check for functional interface
- Allow generic param in lambda. Do you know that `<K, V>(k: K, v: V) => void` is a valid type declaration?

---
