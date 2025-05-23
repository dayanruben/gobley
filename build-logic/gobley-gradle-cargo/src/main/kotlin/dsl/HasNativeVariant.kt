/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gobley.gradle.cargo.dsl

import gobley.gradle.Variant
import org.gradle.api.provider.Property

interface HasNativeVariant {
    /**
     * The variant to use for platforms supporting Kotlin/Native. When unspecified, for Apple
     * platforms, when Gradle is invoked by Xcode, the plugin will read environment variables set by
     * Xcode and determine the variant automatically. For other platforms, defaults to
     * [Variant.Debug]. Setting this will override the `nativeVariant` properties in outer blocks.
     * For example, in the following DSL:
     * ```kotlin
     * cargo {
     *   nativeVariant = Variant.Release
     *   builds.linux {
     *     nativeVariant = Variant.Debug
     *   }
     * }
     * ```
     * while the Linux build will use a debug Rust library, other builds will use release Rust
     * libraries.
     *
     * **FOR LIBRARY DEVELOPERS**: Unlike [HasJvmVariant], [HasNativeVariant] does not distinguish
     * the publishing variant and the native variant. It is very likely that you are using
     * [Variant.Debug] for library publishing. Use Gradle properties or environment variables to
     * control this property, and set this to [Variant.Release] for publishing.
     * ```kotlin
     * cargo {
     *   nativeVariant = Variant(findProperty("my.project.native.variant"))
     * }
     * ```
     */
    val nativeVariant: Property<Variant>
}
