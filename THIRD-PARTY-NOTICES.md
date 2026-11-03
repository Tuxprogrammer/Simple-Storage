# Third-party notices

Simple Storage includes code and build setup from the projects below. Each keeps its own license notice here. The mod's own license is MIT, Copyright (c) 2026 Spencer Callicott, in `LICENSE`.

## Tom's Simple Storage

- Source: https://github.com/tom5454/Toms-Storage
- Baseline: branch `26.2`, commit `95bb61d3d9fe9ec39c6525bfc7b24a13afe05129` ([0001](docs/design/0001-port-policy-and-licensing.md))
- Used: Java code ported under the ported-code gate (each file names its source)
- License: MIT

```
MIT License

Copyright (c) 2020 tom5454

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## GTNH ExampleMod buildscript

- Source: https://github.com/GTNewHorizons/ExampleMod1.7.10 (through the fork https://github.com/Tuxprogrammer/ExampleMod1.7.10)
- Used: the build setup this repository started from: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `dependencies.gradle`, `repositories.gradle`, `gtnhShared/`, `.github/`, `jitpack.yml`, the docs templates and the `.claude/` skills
- License: MIT, Copyright (c) 2021 Johann Bernhardt

The MIT license text is the same as above, with that copyright line.

## Gradle wrapper

- Files: `gradlew`, `gradlew.bat`, `gradle/wrapper/`
- License: Apache License 2.0, Copyright the original authors (Gradle, Inc.); see https://www.apache.org/licenses/LICENSE-2.0
