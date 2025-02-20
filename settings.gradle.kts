pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "uniffi-kotlin-multiplatform-bindings"

include(":tests:gradle:android-linking")
include(":tests:gradle:cargo-only")
include(":tests:gradle:no-uniffi-block")

include(":tests:uniffi:callbacks")
include(":tests:uniffi:chronological")
include(":tests:uniffi:coverall")
include(":tests:uniffi:docstring")
include(":tests:uniffi:docstring-proc-macro")
include(":tests:uniffi:enum-types")
include(":tests:uniffi:error-types")
include(":tests:uniffi:ext-types:custom-types")
include(":tests:uniffi:ext-types:ext-types")
include(":tests:uniffi:ext-types:ext-types-proc-macro")
include(":tests:uniffi:ext-types:http-headermap")
include(":tests:uniffi:ext-types:sub-lib")
include(":tests:uniffi:ext-types:uniffi-one")
include(":tests:uniffi:futures")
include(":tests:uniffi:keywords")
include(":tests:uniffi:proc-macro")
include(":tests:uniffi:simple-fns")
include(":tests:uniffi:simple-iface")
include(":tests:uniffi:struct-default-values")
include(":tests:uniffi:trait-methods")
include(":tests:uniffi:type-limits")

include(":examples:app")
include(":examples:arithmetic-procmacro")
include(":examples:audio-cpp-app")
include(":examples:custom-types")
include(":examples:todolist")
include(":examples:tokio-blake3-app")
