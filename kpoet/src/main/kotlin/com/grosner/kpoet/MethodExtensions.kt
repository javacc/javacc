package com.grosner.kpoet

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

typealias CodeMethod = CodeBlock.Builder.() -> CodeBlock.Builder
typealias MethodMethod = MethodSpec.Builder.() -> MethodSpec.Builder
typealias FieldMethod = FieldSpec.Builder.() -> FieldSpec.Builder
typealias ParamMethod = ParameterSpec.Builder.() -> ParameterSpec.Builder
typealias AnnotationMethod = AnnotationSpec.Builder.() -> AnnotationSpec.Builder
typealias TypeMethod = TypeSpec.Builder.() -> TypeSpec.Builder

fun MethodSpec.Builder.modifiers(vararg modifier: Modifier) = addModifiers(*modifier)!!

fun MethodSpec.Builder.modifiers(vararg modifiers: List<Modifier>) = apply { modifiers.forEach { addModifiers(it) } }

infix fun MethodSpec.Builder.returns(typeName: TypeName) = returns(typeName)!!

infix fun MethodSpec.Builder.returns(typeName: KClass<*>) = returns(typeName.java)!!

inline fun MethodSpec.Builder.code(codeMethod: CodeMethod) = addCode(codeMethod(CodeBlock
        .builder()).build())!!

inline fun MethodSpec.Builder.statement(codeMethod: CodeMethod)
        = addStatement("\$L", codeMethod(CodeBlock.builder()).build().toString())!!

fun MethodSpec.Builder.statement(code: String, vararg args: Any?) = addStatement(code, *args)!!

fun MethodSpec.Builder.comment(comment: String) = addComment(comment)!!

infix fun MethodSpec.Builder.annotation(type: KClass<*>) = addAnnotation(type.java)!!

infix fun MethodSpec.Builder.annotation(type: ClassName) = addAnnotation(type)!!

inline fun MethodSpec.Builder.annotation(className: ClassName,
                                         function: AnnotationSpec.Builder.() -> AnnotationSpec.Builder)
        = addAnnotation(AnnotationSpec.builder(className).function().build())!!

inline fun MethodSpec.Builder.annotation(className: KClass<*>,
                                         function: AnnotationSpec.Builder.() -> AnnotationSpec.Builder)
        = addAnnotation(AnnotationSpec.builder(className.java).function().build())!!

// control flow extensions
inline fun MethodSpec.Builder.`if`(statement: String, vararg args: Any?,
                                   function: MethodMethod)
        = beginControl("if", statement = statement, args = *args, function = function)

inline fun MethodSpec.Builder.`do`(function: MethodMethod)
        = beginControl("do", function = function)

fun MethodSpec.Builder.`while`(statement: String, vararg args: Any?) = endControl("while", statement = statement, args = *args)

infix inline fun MethodSpec.Builder.`else`(function: MethodMethod)
        = nextControl("else", function = function)

inline fun MethodSpec.Builder.`else if`(statement: String, vararg args: Any?,
                                        function: MethodMethod)
        = nextControl("else if", statement = statement, args = *args, function = function)

fun MethodSpec.Builder.end(statement: String = "", vararg args: Any?)
        = (if (statement.isNullOrBlank().not()) endControlFlow(statement, *args) else endControlFlow())!!

inline fun MethodSpec.Builder.`for`(statement: String, vararg args: Any?,
                                    function: MethodMethod)
        = beginControl("for", statement = statement, args = *args, function = function).endControlFlow()!!

inline fun MethodSpec.Builder.`switch`(statement: String, vararg args: Any?,
                                       function: MethodMethod)
        = beginControl("switch", statement = statement, args = *args, function = function).endControlFlow()!!

fun MethodSpec.Builder.`return`(statement: String, vararg args: Any?) = addStatement("return $statement", *args)!!

fun MethodSpec.Builder.`break`() = addStatement("break")!!

fun MethodSpec.Builder.`continue`() = addStatement("continue")!!

inline fun MethodSpec.Builder.case(statement: String, vararg args: Any, function: MethodMethod)
        = beginControlFlow("case $statement:", args).function().endControlFlow()!!

inline fun MethodSpec.Builder.default(function: MethodMethod)
        = beginControlFlow("default:").function().endControlFlow()!!

fun MethodSpec.Builder.`throw new`(type: KClass<*>, statement: String, vararg arg: Any?)
        = addStatement("throw new \$T(\"$statement\")", type.java, *arg)!!

fun MethodSpec.Builder.`throw new`(type: ClassName, statement: String, vararg arg: Any?)
        = addStatement("throw new \$T(\"$statement\")", type, *arg)!!

fun MethodSpec.Builder.`@`(kClass: KClass<*>, annotationMethod: AnnotationMethod = { this })
        = addAnnotation(AnnotationSpec.builder(kClass.java).annotationMethod().build())!!

fun MethodSpec.Builder.`@`(className: ClassName, annotationMethod: AnnotationMethod = { this })
        = addAnnotation(AnnotationSpec.builder(className).annotationMethod().build())!!

inline fun MethodSpec.Builder.nextControl(name: String, statement: String = "", vararg args: Any?,
                                          function: MethodMethod)
        = nextControlFlow("$name${if (statement.isNullOrEmpty()) "" else " ($statement)"}", *args)
        .function()

inline fun MethodSpec.Builder.beginControl(name: String, statement: String = "", vararg args: Any?,
                                           function: MethodMethod)
        = beginControlFlow("$name${if (statement.isNullOrEmpty()) "" else " ($statement)"}", *args)
        .function()

inline fun MethodSpec.Builder.endControl(name: String, statement: String = "", vararg args: Any?)
        = endControlFlow("$name${if (statement.isNullOrEmpty()) "" else " ($statement)"}", *args)!!

fun MethodSpec.Builder.javadoc(format: String, vararg args: Any?) = addJavadoc(format, args)

fun MethodSpec.Builder.javadoc(codeBlock: CodeBlock) = addJavadoc(codeBlock)

fun MethodSpec.Builder.comment(format: String, vararg args: Any?) = addComment(format, args)


