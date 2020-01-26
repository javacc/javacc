package com.grosner.kpoet

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import kotlin.reflect.KClass

fun AnnotationSpec.Builder.member(name: String, format: String, vararg args: Any?) = addMember(name, format, *args)

fun AnnotationSpec.Builder.member(name: String, codeBlock: CodeBlock) = addMember(name, codeBlock)

fun AnnotationSpec.Builder.member(name: String, codeBlockFunc: CodeBlock.Builder.() -> CodeBlock.Builder)
        = addMember(name, CodeBlock.builder().codeBlockFunc().build())


fun `@`(kClass: KClass<*>, mapFunc: MutableMap<String, String>.() -> Unit = { })
        = AnnotationSpec.builder(kClass.java)
        .apply {
            mutableMapOf<String, String>().apply { mapFunc(this) }
                    .forEach { key, value -> addMember(key, value) }
        }


fun `@`(className: ClassName, mapFunc: MutableMap<String, String>.() -> Unit = {})
        = AnnotationSpec.builder(className)
        .apply {
            mutableMapOf<String, String>().apply { mapFunc(this) }
                    .forEach { key, value -> addMember(key, value) }
        }