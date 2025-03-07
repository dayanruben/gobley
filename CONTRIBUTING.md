# Contributing to Gobley

Thank you for taking the time to contribute to Gobley. We welcome all types of
contributions: including pull requests, bug reports, and feature requests.

Please read the guide below to get started. If you encounter any difficulties
setting up the project, please file a 'Help Wanted' issue.

## Project setup

To build this project locally, you'll need:

- Rust
- Zig 0.13 (for Linux cross-compilation)
- MinGW (GCC 13)
- OpenJDK 17
- Android SDK 35 with CMake (CMake is used by `:tests:gradle:android-linking`)
- Android NDK
- Perl (Used to build OpenSSL by `:examples:tokio-blake3-app`)
- Chrome (Used by WASM/JS tests in `:tests:uniffi:coverall`)
- Visual C++ x64 & ARM64 (Windows)
- Xcode (macOS)

See [`.github/workflows/dependency-image.Dockerfile`](.github/workflows/dependency-image.Dockerfile)
for more details.

For faster development, you can disable some unit tests and examples. Use the
following Gradle properties to choose tests and examples to turn on and off.

| Gradle property name          | Projects                                   |
|-------------------------------|--------------------------------------------|
| `gobley.projects.gradleTests` | `:tests:gradle`                            |
| `gobley.projects.uniffiTests` | `:tests:uniffi` & `:examples:custom-types` |
| `gobley.projects.examples`    | `:examples`                                |

These following properties are already in `gradle.properties`. Simply replace
`=true` to `=false` to turn them off.

## Build and use the Gradle plugins locally

### Option 1 - Dynamically include the plugins in your project

Clone this repository and reference it from your project. Configure
`dependencySubstitution` to use the local plugin version.

```kotlin
// settings.gradle.kts
pluginManagement {
    // ..
    includeBuild("../gobley/build-logic")
    // ...
    plugins {
        // comment out id("dev.gobley.<plugin name>") if you have it here
    }
}
// ...
includeBuild("../gobley/build-logic") {
    dependencySubstitution {
        val projectNames = arrayOf(
            "gobley-gradle",
            "gobley-gradle-cargo",
            "gobley-gradle-rust",
            "gobley-gradle-uniffi",
        )
        for (projectName in projectNames) {
            substitute(module("dev.gobley.uniffi:$projectName"))
                .using(project(":$projectName"))
        }
    }
}
```

Add the Gradle plugins to the Gradle build file.

```kotlin
// build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("dev.gobley.cargo")
    id("dev.gobley.uniffi")
    id("dev.gobley.rust")
    // ...
}
```

This will automatically re-build the plugin if you make any local changes but
also increase the build time of your project.

### Option 2 - Publish the plugins locally

Clone the repository and build it.

Run the following to publish the plugins locally:

```shell
./gradlew :build-logic:gobley-gradle:publishToMavenLocal
./gradlew :build-logic:gobley-gradle-cargo:publishToMavenLocal
./gradlew :build-logic:gobley-gradle-rust:publishToMavenLocal
./gradlew :build-logic:gobley-gradle-uniffi:publishToMavenLocal
```

This publishes the plugins to `~/.m2`. If you want to remove the local versions,
open that folder.

Add `mavenLocal()` to your project's `settings.gradle.kts` to use the local
version:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        // ...
    }
}
```

This won't re-build the plugin even if you make changes but also won't affect
the build time of your project.

## Use the modified version of the bindgen

You can use the modified version of the bindgen with the unmodified version of
the Gradle plugins of Gobley. Use `bindgenFromPath()` or `bindgenFromGit*()`
to make the UniFFI plugin reference your modified version.

```kotlin
uniffi {
    // ...
    bindgenFromPath("<path-to-our-bindgen>")
    // or
    bindgenFromGitTag("https://github.com/your-username/gobley", "v0.1.0")
}
```

## Coding Convention

Since Gobley is in its early stages, we don't have an established coding
convention. However, we ask you not to modify the unrelated source code just for
style changes. If you want to introduce multiple changes, please split them
into multiple PRs so we can easily track them.
