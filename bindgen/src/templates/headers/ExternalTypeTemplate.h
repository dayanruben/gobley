{%- let fully_qualified_rustbuffer_name = "{}_RustBuffer"|format(namespace) %}
{%- let local_rustbuffer_name = "RustBuffer{}"|format(name) %}

typedef RustBuffer {{ local_rustbuffer_name }};
