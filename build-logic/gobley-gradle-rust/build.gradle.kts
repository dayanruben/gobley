plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
}

configureGobleyGradleProject(
    description = "A Gradle plugin for configuring Rust toolchain and linking Rust libraries to Kotlin projects.",
)

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.kotlin.multiplatform))
    api(project(":gobley-gradle"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    vcsUrl.set("https://github.com/gobley/gobley")
    website.set(vcsUrl)

    plugins {
        create("gobley-gradle-rust") {
            id = "dev.gobley.rust"
            displayName = "Gobley Rust Gradle Plugin"
            implementationClass = "gobley.gradle.rust.RustPlugin"
            description = project.description
            tags.addAll("rust", "kotlin", "kotlin-multiplatform")
        }
    }
}
