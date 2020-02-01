package com.grosner.kpoet

import com.squareup.javapoet.*

typealias StringTemplateMethod = StringTemplate.() -> String

class StringTemplate {
    val args = mutableListOf<Any?>()

    private fun arg(mask: String, v: Any?) = args.run {
        add(v)
        "$$size$mask"
    }

    val TypeName.T: String get() = arg("T", this)
    val FieldSpec.T: String get() = arg("T", this.type)
    val MethodSpec.T: String get() = arg("T", this.returnType)

    val FieldSpec.N: String get() = arg("N", this)
    val MethodSpec.N: String get() = arg("N", this)
    val TypeSpec.N: String get() = arg("N", this)

    val String.N: String get() = arg("N", this)

    val Any?.L: String get() = arg("L", this)
    val Any?.S: String get() = arg("S", this)
}

inline fun MethodSpec.Builder.addCode(template: StringTemplate.() -> String) = run {
    val t = StringTemplate()
    val format = t.template()
    addCode(format, *t.args.toTypedArray())!!
}

inline fun CodeBlock.Builder.add(template: StringTemplate.() -> String) = run {
    val t = StringTemplate()
    val format = t.template()
    add(format, *t.args.toTypedArray())!!
}

inline fun codeBlock(template: StringTemplate.() -> String) =
    CodeBlock.builder().add(template).build()!!
