package com.grosner.kpoet

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

typealias CodeMethod = CodeBlock.Builder.() -> Unit
typealias MethodMethod = MethodSpec.Builder.() -> Unit
typealias FieldMethod = FieldSpec.Builder.() -> Unit
typealias ParamMethod = ParameterSpec.Builder.() -> Unit
typealias AnnotationMethod = AnnotationSpec.Builder.() -> Unit
typealias TypeMethod = TypeSpec.Builder.() -> Unit

fun MethodSpec.Builder.modifiers(vararg modifier: Modifier) = addModifiers(*modifier)!!

fun MethodSpec.Builder.modifiers(vararg modifiers: List<Modifier>) = apply { modifiers.forEach { addModifiers(it) } }

infix fun MethodSpec.Builder.returns(typeName: TypeName) = returns(typeName)!!

infix fun MethodSpec.Builder.returns(typeName: KClass<*>) = returns(typeName.java)!!

inline fun MethodSpec.Builder.code(codeMethod: StringTemplateMethod) = addCode(codeMethod)

inline fun MethodSpec.Builder.statement(codeMethod: StringTemplateMethod) =
    addStatement(L, codeBlock(codeMethod))!!

fun MethodSpec.Builder.statement(code: String, vararg args: Any?) = addStatement(code, *args)!!

infix fun MethodSpec.Builder.annotation(type: KClass<*>) = addAnnotation(type.java)!!

infix fun MethodSpec.Builder.annotation(type: ClassName) = addAnnotation(type)!!

inline fun MethodSpec.Builder.annotation(className: ClassName,
                                         function: AnnotationMethod)
        = addAnnotation(AnnotationSpec.builder(className).apply(function).build())!!

inline fun MethodSpec.Builder.annotation(className: KClass<*>,
                                         function: AnnotationMethod)
        = addAnnotation(AnnotationSpec.builder(className.java).apply(function).build())!!

// control flow extensions
inline fun MethodSpec.Builder.`if`(statement: String, vararg args: Any?,
                                   function: MethodMethod)
        = beginControl("if", statement = statement, args = *args, function = function)

inline fun MethodSpec.Builder.`do`(function: MethodMethod)
        = beginControl("do", function = function)

fun MethodSpec.Builder.`while`(statement: String, vararg args: Any?) = endControl("while", statement = statement, args = *args)

// VLSI: new
fun MethodSpec.Builder.`while`(statement: String, function: MethodMethod) =
    beginControl("while", statement = statement, function = function).endControlFlow()!!

infix inline fun MethodSpec.Builder.`else`(function: MethodMethod)
        = nextControl("else", function = function).end()

inline fun MethodSpec.Builder.`else if`(statement: String, vararg args: Any?,
                                        function: MethodMethod)
        = nextControl("else if", statement = statement, args = *args, function = function)

fun MethodSpec.Builder.`end if`() = endControlFlow()

fun MethodSpec.Builder.`try`(function: MethodMethod) =
    beginControl("try", function = function)

fun MethodSpec.Builder.`catch`(statement: String, vararg args: Any, function: MethodMethod) =
    nextControl("catch", statement = statement, args = *args, function = function)

inline infix fun MethodSpec.Builder.`finally`(function: MethodMethod) =
    nextControl("finally", function = function).end()

fun MethodSpec.Builder.`end try`() = end()

fun MethodSpec.Builder.end(statement: String = "", vararg args: Any?)
        = (if (statement.isBlank().not()) endControlFlow(statement, *args) else endControlFlow())!!

inline fun MethodSpec.Builder.`for`(statement: String, vararg args: Any?,
                                    function: MethodMethod)
        = beginControl("for", statement = statement, args = *args, function = function).endControlFlow()!!

inline fun MethodSpec.Builder.`for`(
    statement: StringTemplateMethod,
    function: MethodMethod
) =
    beginControl("for", statement = statement, function = function).endControlFlow()!!

inline fun MethodSpec.Builder.`switch`(statement: String, vararg args: Any?,
                                       function: MethodMethod)
        = beginControl("switch", statement = statement, args = *args, function = function).endControlFlow()!!

fun MethodSpec.Builder.`return`(statement: String, vararg args: Any?) = addStatement("return $statement", *args)!!

fun MethodSpec.Builder.`return`(statement: StringTemplateMethod) = addStatement("return $L", codeBlock(statement))!!

fun MethodSpec.Builder.`break`() = addStatement("break")!!

fun MethodSpec.Builder.`continue`() = addStatement("continue")!!

inline fun MethodSpec.Builder.case(statement: String, vararg args: Any, function: MethodMethod)
        = beginControlFlow("case $statement:", *args).apply(function).endControlFlow()!!

inline fun MethodSpec.Builder.default(function: MethodMethod)
        = beginControlFlow("default:").apply(function).endControlFlow()!!

fun MethodSpec.Builder.`throw new`(type: KClass<*>, statement: String, vararg arg: Any?)
        = addStatement("throw new \$T($statement)", type.java, *arg)!!

fun MethodSpec.Builder.`throw new`(type: ClassName, statement: String, vararg arg: Any?)
        = addStatement("throw new \$T($statement)", type, *arg)!!

fun MethodSpec.Builder.`@`(kClass: KClass<*>, annotationMethod: AnnotationMethod = { })
        = addAnnotation(AnnotationSpec.builder(kClass.java).apply(annotationMethod).build())!!

fun MethodSpec.Builder.`@`(className: ClassName, annotationMethod: AnnotationMethod = { })
        = addAnnotation(AnnotationSpec.builder(className).apply(annotationMethod).build())!!

inline fun MethodSpec.Builder.nextControl(name: String, statement: String = "", vararg args: Any?,
                                          function: MethodMethod)
        = nextControlFlow("$name${if (statement.isEmpty()) "" else " ($statement)"}", *args)
        .apply(function)

inline fun MethodSpec.Builder.beginControl(name: String, statement: String = "", vararg args: Any?,
                                           function: MethodMethod)
        = beginControlFlow("$name${if (statement.isEmpty()) "" else " ($statement)"}", *args)
        .apply(function)


inline fun MethodSpec.Builder.beginControl(
    name: String,
    statement: StringTemplateMethod,
    function: MethodMethod
) = addCode(L, name)
    .beginControlFlow(" ($L)", codeBlock(statement))
    .apply(function)!!

inline fun MethodSpec.Builder.endControl(name: String, statement: String = "", vararg args: Any?)
        = endControlFlow("$name${if (statement.isEmpty()) "" else " ($statement)"}", *args)!!

fun MethodSpec.Builder.javadoc(format: String, vararg args: Any?) = addJavadoc(format, *args)

fun MethodSpec.Builder.javadoc(codeBlock: CodeBlock) = addJavadoc(codeBlock)

fun MethodSpec.Builder.comment(format: String, vararg args: Any?) = addComment(format, *args)


