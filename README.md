# Lab 3 – SE Fundamentals
**Variant: 450768**

## 🔧 Stack

- Java 17+
- Apache Ant
- CMake
- JUnit 5
- SVN & Git
- SCP / SSH
- native2ascii

## 📦 Structure

- `build.xml`, `build.properties` – Ant build system
- `CMakeLists.txt`, `variables.cmake` – CMake configuration
- `scripts/` – shell scripts to run workflows
- `src/` – source code
- `test/` – JUnit tests
- `resources/` – config and localization


## 🚀 Usage

### 🔨 Ant Build (`scripts/ant.sh`)

Runs a full Ant workflow:
- Cleans build artifacts
- Compiles code (or falls back to last working Git state)
- Builds the JAR
- Runs tests
- Commits test report if successful
- Optionally SCPs result and diffs SVN
- Runs with custom environments

Run it:
```bash
./scripts/ant.sh
```

### ⚙️ CMake Build (`scripts/cmake.sh`)

Performs the same flow using CMake:

- Sets up the build directory
- Compiles (with fallback)
- Builds the JAR
- Runs tests
- Saves report
- Optionally deploys and commits to SVN
- Launches with custom env settings

Run it:
```bash
./scripts/cmake.sh
