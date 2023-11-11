// This file was autogenerated by some hot garbage in the `uniffi` crate.
// Trust me, you don't want to mess with it!

@file:Suppress("NAME_SHADOWING")

package {{ config.package_name() }}

//import kotlin.coroutines.cancellation.CancellationException
//import kotlin.coroutines.resumeWithException
//import kotlin.coroutines.suspendCoroutine
//import kotlinx.coroutines.coroutineScope

import okio.Buffer

{%- for req in self.imports() %}
{{ req.render() }}
{%- endfor %}

{% include "RustBufferTemplate.kt" %}
{% include "FfiConverterTemplate.kt" %}
{% include "Helpers.kt" %}

// Contains loading, initialization code,
// and the FFI Function declarations.
{% include "NamespaceLibraryTemplate.kt" %}

// Async support
{%- if ci.has_async_fns() %}
{% include "Async.kt" %}
{%- endif %}

// Public interface members begin here.
{{ type_helper_code }}

{%- for func in ci.function_definitions() %}
{%- include "TopLevelFunctionTemplate.kt" %}
{%- endfor %}

{% import "helpers.j2" as kt %}
