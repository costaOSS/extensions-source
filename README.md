# Keiyoushi Extensions — MegaApp Fork

### Please give the repo a :star:

| Build | Need Help? |
|-------|------------|
| [![CI](https://github.com/keiyoushi/extensions-source/actions/workflows/build_push.yml/badge.svg)](https://github.com/keiyoushi/extensions-source/actions/workflows/build_push.yml) | [![Discord](https://img.shields.io/discord/1193460528052453448.svg?label=discord&labelColor=7289da&color=2c2f33&style=flat)](https://discord.gg/3FbCpdKbdY) |

---

## 🚀 MegaApp — All Extensions in One APK

This fork adds **MegaApp**: a single Android APK that bundles all 1,300+ Keiyoushi extensions directly into one app, with **no separate extension installs required**.

### Why MegaApp?

Normally Mihon/Tachiyomi extensions are installed as separate APKs (one per source). MegaApp compiles every extension source directly into one application, so:

- ✅ **No extension APK installation needed** — works without sideloading extension APKs
- ✅ **No PC or server required** — self-contained, runs entirely on-device
- ✅ **Works with Kotatsu-Redo** — compatible as a source provider without external dependency
- ✅ **1,336+ manga/manhwa/manhua sources** across all languages
- ✅ **Shared libraries included** — randomua, textinterceptor, cryptoaes, cookieinterceptor, and more

---

## 🏗️ Building MegaApp

### Prerequisites

- JDK 17+
- Android SDK (API 36)
- Gradle 9.6+

### Build Steps

```bash
# 1. Clone the repo
git clone https://github.com/your-user/extensions-source
cd extensions-source

# 2. Generate the mega-app source registry
#    (scans all ~1,450 extension sources and generates SourceRegistry.kt)
./gradlew generateMegaApp

# 3. Build the release APK
./gradlew :mega-app:assembleRelease
```

The signed APK will be at:
```
mega-app/build/outputs/apk/release/mega-app-release.apk
```

### Signing

Set these environment variables before building release:
```bash
export KEY_STORE_PASSWORD=your_keystore_password
export ALIAS=your_key_alias
export KEY_PASSWORD=your_key_password
```

Place your keystore at `signingkey.jks` in the project root.  
If no keystore is found, the debug signing key is used automatically.

---

## 🔧 MegaApp Architecture

```
extensions-source/
├── mega-app/                      # The single APK module
│   ├── build.gradle.kts           # App build config (compileSdk 36, minSdk 21)
│   ├── mega-sources-list.txt      # Auto-generated list of all source directories
│   ├── mega-sources.gradle        # Auto-generated Groovy script (sets sourceSets)
│   └── mega-excluded-sources.txt  # Manually maintained exclusion list
│
├── mega-plugin/                   # Gradle plugin that generates the mega-app
│   └── src/.../megaplugin/
│       ├── MegaPlugin.kt               # Plugin entry point
│       ├── RepositoryScanner.kt        # Scans src/ for all extensions
│       ├── SourceSetGenerator.kt       # Generates mega-sources-list.txt + .gradle
│       ├── SourceRegistryGenerator.kt  # Generates SourceRegistry.kt
│       └── DependencyResolver.kt       # Resolves shared libraries
│
├── src/                           # All individual extension sources (~1,450 dirs)
├── lib/                           # Shared libraries (cryptoaes, randomua, etc.)
└── lib-multisrc/                  # Multi-source theme libraries
```

### How It Works

1. **`generateMegaApp`** task (run by `mega-plugin`) scans all extensions under `src/`
2. For each extension it finds the main class and generates a thin wrapper in `mega-app/src/main/java/.../generated/`
3. All extension source directories are added to the app's `sourceSets` via `mega-sources.gradle`
4. A `SourceRegistry.kt` is generated that instantiates all 1,336 source classes
5. The app is compiled as a normal Android application with all sources inlined

### Excluded Extensions

Some extensions cannot compile in the flat mega build and are listed in [`mega-app/mega-excluded-sources.txt`](mega-app/mega-excluded-sources.txt):

| Extension | Reason |
|-----------|--------|
| `ko/wolfdotcom` | Requires build-time Gradle task that fetches domain number from network |
| `zh/baozimanhua` | Requires JitPack dep `com.github.stevenyomi:baozibanner` |
| `pt/manhastro` | Requires OkHttp brotli interceptor |
| `fr/lesporoiniens` | kotlinx.serialization incompatibility |
| `ja/ciaoplus` | `Bitmap.Config?` nullable type mismatch |
| `en/vizshonenjump` | Requires `com.drewnoakes:metadata-extractor` JitPack dep |

To exclude additional extensions, add their path (relative to project root) to `mega-app/mega-excluded-sources.txt`.

---

## 📖 Original Keiyoushi Usage

**If you are new to repository/extensions, please read the [Keiyoushi Getting Started guide](https://keiyoushi.github.io/docs/guides/getting-started#adding-the-extension-repo) first.**

- Add the repo by visiting the [Keiyoushi Website](https://keiyoushi.github.io/add-repo)
- Or copy & paste: `https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json`

## Requests

To request a new source or bug fix, [create an issue](https://github.com/keiyoushi/extensions-source/issues/new/choose).

## Contributing

Contributions are welcome! Check out the repo's [issue backlog](https://github.com/keiyoushi/extensions-source/issues) for source requests and bug reports.

## License

    Copyright 2015 Javier Tomás

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Disclaimer

This project does not have any affiliation with the content providers available.

This project is not affiliated with Mihon/Tachiyomi. Don't ask for help about these extensions at the
official support means of Mihon/Tachiyomi. All credits to the codebase goes to the original contributors.
