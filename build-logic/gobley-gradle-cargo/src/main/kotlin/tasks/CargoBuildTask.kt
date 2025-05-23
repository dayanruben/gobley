/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gobley.gradle.cargo.tasks

import gobley.gradle.InternalGobleyGradleApi
import gobley.gradle.cargo.CargoMessage
import gobley.gradle.cargo.profiles.CargoProfile
import gobley.gradle.rust.CrateType
import gobley.gradle.rust.targets.RustNativeTarget
import gobley.gradle.rust.targets.RustTarget
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

@Suppress("LeakingThis")
@CacheableTask
abstract class CargoBuildTask : CargoPackageTask() {
    @get:Input
    abstract val profile: Property<CargoProfile>

    @get:Input
    abstract val target: Property<RustTarget>

    @get:Input
    abstract val features: SetProperty<String>

    @get:Input
    abstract val extraArguments: ListProperty<String>

    @OutputFiles
    val libraryFileByCrateType: Provider<Map<CrateType, RegularFile>> =
        profile.zip(target, ::Pair).zip(cargoPackage) { (profile, target), cargoPackage ->
            cargoPackage.libraryCrateTypes.mapNotNull { crateType ->
                crateType to cargoPackage.outputDirectory(profile, target).file(
                    target.outputFileName(cargoPackage.libraryCrateName, crateType)
                        ?: return@mapNotNull null
                )
            }.toMap()
        }

    @get:OutputFile
    @get:Optional
    abstract val nativeStaticLibsDefFile: RegularFileProperty

    @TaskAction
    @OptIn(InternalGobleyGradleApi::class)
    fun build() {
        val profile = profile.get()
        val target = target.get()
        val result = cargo("rustc") {
            arguments("--profile", profile.profileName)
            arguments("--target", target.rustTriple)
            if (features.isPresent) {
                if (features.get().isNotEmpty()) {
                    arguments("--features", features.get().joinToString(","))
                }
            }
            arguments("--lib")
            arguments("--message-format", "json")
            for (extraArgument in extraArguments.get()) {
                arguments(extraArgument)
            }
            arguments("--")
            if (nativeStaticLibsDefFile.isPresent) {
                arguments("--print", "native-static-libs")
            }
            suppressXcodeIosToolchains()
            if (nativeStaticLibsDefFile.isPresent) {
                captureStandardOutput()
            }
        }.get().apply {
            assertNormalExitValue()
        }

        if (nativeStaticLibsDefFile.isPresent) {
            val nativeStaticLibsDefFile = nativeStaticLibsDefFile.get().asFile.apply {
                parentFile?.mkdirs()
            }
            val messages = result.standardOutput!!.split('\n')
                .mapNotNull { runCatching { CargoMessage(it) }.getOrNull() }

            val librarySearchPaths = mutableListOf<String>()
            var staticLibraries: String? = null
            for (message in messages) {
                when (message) {
                    is CargoMessage.BuildScriptExecuted -> {
                        val buildScriptOutput = File(message.outDir).parentFile?.resolve("output")
                            ?.readLines(Charsets.UTF_8)
                        if (buildScriptOutput != null) {
                            for (line in buildScriptOutput) {
                                val searchPath = line.substringAfter("cargo:", "").trim(':')
                                    .substringAfter("rustc-link-search=", "")
                                    .takeIf(String::isNotEmpty)?.split('=')
                                when (searchPath?.size) {
                                    1 -> librarySearchPaths.add(searchPath[0])
                                    2 -> if (searchPath[1] != "crate" && searchPath[1] != "dependency") {
                                        librarySearchPaths.add(searchPath[1])
                                    }
                                }
                            }
                        }

                    }

                    is CargoMessage.CompilerMessage -> {
                        if (staticLibraries == null) {
                            val note = message.message.rendered?.trim()
                            staticLibraries =
                                note?.trim()?.substringAfter("note: native-static-libs: ", "")
                                    ?.takeIf(String::isNotEmpty)
                        }
                    }

                    else -> {}
                }
            }

            val linkerOptName = if (target is RustNativeTarget) {
                "linkerOpts.${target.cinteropName}"
            } else {
                "linkerOpts"
            }
            val linkerFlag = StringBuilder().apply {
                if (librarySearchPaths.isNotEmpty()) {
                    append(librarySearchPaths.joinToString(" ") { "-L$it" })
                    if (staticLibraries != null) {
                        append(' ')
                    }
                }
                if (staticLibraries != null) {
                    append(staticLibraries)
                }
            }
            nativeStaticLibsDefFile.writeText(StringBuilder().apply {
                if (linkerFlag.isNotEmpty()) {
                    append("$linkerOptName = $linkerFlag\n")
                }
                val libraryFile = libraryFileByCrateType.get()[CrateType.SystemStaticLibrary]
                if (libraryFile != null) {
                    append("staticLibraries = ${libraryFile.asFile.name}")
                }
            }.toString())
        }
    }
}
