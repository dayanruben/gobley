plugins {
    id("uniffi-tests")
}

uniffi {
    generateFromLibrary {
        namespace = "imported_types_lib"
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.http)
                api(project(":examples:custom-types"))
                api(project(":tests:uniffi:ext-types:custom-types"))
                api(project(":tests:uniffi:ext-types:uniffi-one"))
                api(project(":tests:uniffi:ext-types:sub-lib"))
            }
        }
    }
}

tasks.withType<Test> {
    val dependencyNamespaceNames = arrayOf(
        "uniffi_kmm_example_custom_types",
        "kmm_ext_types_custom",
        "kmm_uniffi_one",
        "sub_lib",
    )
    for (dependencyNamespace in dependencyNamespaceNames) {
        systemProperties["uniffi.component.$dependencyNamespace.libraryOverride"] = "uniffi_kmm_fixture_ext_types"
    }
}