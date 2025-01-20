
// Define FFI callback types

{%- for type_ in ci.iter_types() %}
{%- let ffi_converter_name = type_|ffi_converter_name %}
   // {{ ffi_converter_name }} TEST
{% endfor %}

{%- for def in ci.ffi_definitions() %}
{%- match def %}
{%- when FfiDefinition::CallbackFunction(callback) %}
internal typealias {{ callback.name()|ffi_callback_name }} = {{ ci.namespace() }}.cinterop.{{ callback.name()|ffi_callback_name }}
{%- when FfiDefinition::Struct(ffi_struct) %}
internal actual typealias {{ ffi_struct.name()|ffi_struct_name }} = CPointer<{{ ci.namespace() }}.cinterop.{{ ffi_struct.name()|ffi_struct_name }}>
{% for field in ffi_struct.fields() %}
internal actual var {{ ffi_struct.name()|ffi_struct_name }}.{{ field.name()|var_name }}: {{ field.type_().borrow()|ffi_type_name_for_ffi_struct }}
{% let type_name = field.type_().borrow()|ffi_type_name_for_ffi_struct -%}
    get() = pointed.{{ field.name()|var_name }} {%  if type_name.contains("ByValue") %}.readValue() {%- else -%}/* test  {{ type_name }} */{% endif %}
    set(value) {
        {%- match field.type_() %}
        {%- when FfiType::RustBuffer(_) %}
        value.write(pointed.{{ field.name()|var_name }}.rawPtr)
        {%- when FfiType::RustCallStatus %}
        value.write(pointed.{{ field.name()|var_name }}.rawPtr)
        {%- when _ %}
        pointed.{{ field.name()|var_name }} = value as {{ field.type_().borrow()|ffi_type_name_for_ffi_struct_inner }}
        {%- endmatch %}
    }
{% endfor %}

internal actual fun {{ ffi_struct.name()|ffi_struct_name }}.uniffiSetValue(other: {{ ffi_struct.name()|ffi_struct_name }}) {
    {%- for field in ffi_struct.fields() %}
    {{ field.name()|var_name }} = other.{{ field.name()|var_name }}
    {%- endfor %}
}
internal actual fun {{ ffi_struct.name()|ffi_struct_name }}.uniffiSetValue(other: {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue) {
    {%- for field in ffi_struct.fields() %}
    {{ field.name()|var_name }} = other.{{ field.name()|var_name }}
    {%- endfor %}
}

internal actual typealias {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue = CValue<{{ ci.namespace() }}.cinterop.{{ ffi_struct.name()|ffi_struct_name }}>
fun {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue(
    {% for field in ffi_struct.fields() %}
    {{ field.name()|var_name }}: {{ field.type_().borrow()|ffi_type_name_for_ffi_struct }},
    {% endfor %}
): {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue {
    return cValue<{{ ci.namespace() }}.cinterop.{{ ffi_struct.name()|ffi_struct_name }}> {
        {% for field in ffi_struct.fields() %}
        {%- match field.type_() %}
        {%- when FfiType::RustBuffer(_) %}
        {{ field.name()|var_name }}.write(this.{{ field.name()|var_name }}.rawPtr)
        {%- when FfiType::RustCallStatus %}
        {{ field.name()|var_name }}.write(this.{{ field.name()|var_name }}.rawPtr)
        {%- when _ %}
        this.{{ field.name()|var_name }} = {{ field.name()|var_name }} as {{ field.type_().borrow()|ffi_type_name_for_ffi_struct_inner }}
        {%- endmatch %}
        {% endfor %}
    }
}

{% for field in ffi_struct.fields() %}
internal actual var {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue.{{ field.name()|var_name }}: {{ field.type_().borrow()|ffi_type_name_for_ffi_struct }}
{% let type_name = field.type_().borrow()|ffi_type_name_for_ffi_struct -%}
    get() = useContents { {{ field.name()|var_name }} {%  if type_name.contains("ByValue") %}.readValue() {%- else -%}/* test  {{ type_name }} */{% endif %} }
    set(value) { TODO("Not implemented") }
{% endfor %}

internal actual fun {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue.uniffiSetValue(other: {{ ffi_struct.name()|ffi_struct_name }}) {
    {%- for field in ffi_struct.fields() %}
    {{ field.name()|var_name }} = other.{{ field.name()|var_name }}
    {%- endfor %}
}
internal actual fun {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue.uniffiSetValue(other: {{ ffi_struct.name()|ffi_struct_name }}UniffiByValue) {
    {%- for field in ffi_struct.fields() %}
    {{ field.name()|var_name }} = other.{{ field.name()|var_name }}
    {%- endfor %}
}
{%- when FfiDefinition::Function(_) %}
{# functions are handled below #}
{%- endmatch %}
{%- endfor %}


internal actual interface UniffiLib {
    actual companion object {
        internal actual val INSTANCE: UniffiLib by lazy {
            UniffiLibInstance().also { lib ->
             {% for fn in self.initialization_fns() -%}
                {{ fn }}(lib)
             {% endfor -%}
             }
        }
        {% if ci.contains_object_types() %}
        // The Cleaner for the whole library
        internal actual val CLEANER: UniffiCleaner by lazy {
            UniffiCleaner.create()
        }
        {%- endif %}
    }

    {% for func in ci.iter_ffi_function_definitions() -%}
    actual fun {{ func.name() }}(
        {%- call kt::arg_list_ffi_decl_for_ffi_function(func) %}
    ): {% match func.return_type() %}{% when Some with (return_type) %}{{ return_type.borrow()|ffi_type_name_for_ffi_function }}{% when None %}Unit{% endmatch %}
    {% endfor %}
}

internal class UniffiLibInstance: UniffiLib {
    {% for func in ci.iter_ffi_function_definitions() -%}
    override fun {{ func.name() }}(
        {%- call kt::arg_list_ffi_decl_for_ffi_function(func) %}
    ): {% match func.return_type() %}{% when Some with (return_type) %}{{ return_type.borrow()|ffi_type_name_for_ffi_function }}{% when None %}Unit{% endmatch %}
        = {{ ci.namespace() }}.cinterop.{{ func.name() }}({%- call kt::arg_list_ffi_call(func) %})
    {% endfor %}
}
