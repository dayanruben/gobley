{%- let inner_type_name = inner_type|type_name %}

object {{ ffi_converter_name }}: FfiConverterRustBuffer<{{ inner_type_name }}?> {
    override fun read(source: NoCopySource): {{ inner_type_name }}? {
        if (source.readByte().toInt() == 0) {
            return null
        }
        return {{ inner_type|read_fn }}(source)
    }

    override fun allocationSize(value: {{ inner_type_name }}?): kotlin.Int {
        if (value == null) {
            return 1
        } else {
            return 1 + {{ inner_type|allocation_size_fn }}(value)
        }
    }

    override fun write(value: {{ inner_type_name }}?, buf: Buffer) {
        if (value == null) {
            buf.writeByte(0)
        } else {
            buf.writeByte(1)
            {{ inner_type|write_fn }}(value, buf)
        }
    }
}
