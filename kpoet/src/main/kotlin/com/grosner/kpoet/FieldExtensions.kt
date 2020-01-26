package com.grosner.kpoet

import com.squareup.javapoet.*
import kotlin.reflect.KClass

fun field(typeName: TypeName, name: String, fieldMethod: FieldMethod = { this })
        = FieldSpec.builder(typeName, name).fieldMethod()

fun field(kClass: KClass<*>, name: String, fieldMethod: FieldMethod = { this })
        = FieldSpec.builder(kClass.java, name).fieldMethod()

fun field(annotationSpec: AnnotationSpec.Builder, typeName: TypeName, name: String, fieldMethod: FieldMethod = { this })
        = FieldSpec.builder(typeName, name).addAnnotation(annotationSpec.build()).fieldMethod()

fun field(annotationSpec: AnnotationSpec.Builder, kClass: KClass<*>, name: String, fieldMethod: FieldMethod = { this })
        = FieldSpec.builder(kClass.java, name).addAnnotation(annotationSpec.build()).fieldMethod()

fun field(annotationFunction: FieldMethod, kClass: KClass<*>, name: String, fieldMethod: FieldMethod = { this })
        = FieldSpec.builder(kClass.java, name).annotationFunction().fieldMethod()

fun field(annotationFunction: FieldMethod, className: ClassName, name: String, fieldMethod: FieldMethod = { this })
        = FieldSpec.builder(className, name).annotationFunction().fieldMethod()

infix inline fun FieldSpec.Builder.`=`(codeFunc: CodeBlock.Builder.() -> CodeBlock.Builder)
        = initializer(codeFunc(CodeBlock.builder()).build())!!

fun FieldSpec.Builder.`=`(code: String, vararg args: Any?)
        = initializer(CodeBlock.builder().add(code, *args).build())!!

fun FieldSpec.Builder.`=`(codeFunc: CodeBlock.Builder.() -> CodeBlock.Builder,
                          fieldMethod: FieldMethod)
        = initializer(codeFunc(CodeBlock.builder()).build()).fieldMethod()

fun FieldSpec.Builder.`=`(code: String, vararg args: Any?, fieldMethod: FieldMethod)
        = initializer(CodeBlock.builder().add(code, *args).build()).fieldMethod()

fun FieldSpec.Builder.`@`(kClass: KClass<*>, annotationMethod: AnnotationMethod = { this })
        = addAnnotation(AnnotationSpec.builder(kClass.java).annotationMethod().build())!!

fun FieldSpec.Builder.`@`(className: ClassName, annotationMethod: AnnotationMethod = { this })
        = addAnnotation(AnnotationSpec.builder(className).annotationMethod().build())!!

fun FieldSpec.Builder.javadoc(doc: String, vararg args: Any?) = addJavadoc(doc, args)

fun FieldSpec.Builder.javadoc(codeBlock: CodeBlock) = addJavadoc(codeBlock)