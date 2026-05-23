# ProbeJS Legacy 6.0.5 -> 6.1.0

- Fix import using `import { ... } from ...` instead of `const { ... } = require(...)`
- Formatted typing files can now be properly indented
- ProbeJS Legacy now provides a tree style output structure, just like ProbeJS 8. This can notably improve your KubeJS project init speed.
- Due to the new output structure, import path is also changed, but only if you use `require`: `const { $ArrayList } = require("java:java/util");`
  - This change will not affect JS users and plugin developers
  - KubeJS (1.16.5): `const $ArrayList = java("java.util.ArrayList");`
  - KubeJS (1.20.1): `const $ArrayList = Java.loadClass("java.util.ArrayList");`
  - Developer API keeps the same
- Interfaces will now be referred to as an abstract class, to make its behavior closer to Java. Interface class itself will use `$XXX$$Static` name, and only used in class `implements`
- New config format that uses simple JSON + JSON schema, for less confusion. Old config file can be automatically upgraded to new one.
- Fix some problem during mod scanning
- Add `checkJS` default value, for easier overwriting
- Static field/method now appear before non-static one
- [1.20.1] Fix deprecation doc for `require`
- Restore Mixin refmap for dev environment

---
