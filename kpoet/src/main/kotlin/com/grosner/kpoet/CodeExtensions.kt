package com.grosner.kpoet

import com.squareup.javapoet.CodeBlock

const val L = "\$L"
const val N = "\$N"
const val S = "\$S"
const val T = "\$T"

inline fun CodeBlock.Builder.case(statement: String, vararg args: Any?, function: CodeMethod) =
    add("case ").add(statement, *args).beginControlFlow(":")
        .apply(function).endControlFlow()!!

inline fun CodeBlock.Builder.case(statement: StringTemplateMethod, function: CodeMethod) =
    add("case ").add(statement).beginControlFlow(":")
        .apply(function).endControlFlow()!!

inline fun CodeBlock.Builder.default(function: CodeMethod)
        = beginControlFlow("default:").apply(function).endControlFlow()!!

inline fun CodeBlock.Builder.code(codeMethod: CodeMethod)
        = apply(codeMethod)

inline fun code(codeMethod: CodeMethod) = CodeBlock.builder().apply(codeMethod).build()!!

fun CodeBlock.Builder.statement(code: String, vararg args: Any?) = addStatement(code, *args)!!

inline fun CodeBlock.Builder.statement(codeMethod: StringTemplateMethod) =
    addStatement(codeBlock(codeMethod))!!

fun CodeBlock.Builder.end(statement: String = "", vararg args: Any?) =
    (if (statement.isBlank().not()) endControlFlow(statement, *args) else endControlFlow())!!

fun CodeBlock.Builder.end(statement: StringTemplateMethod) =
    endControlFlow(L, codeBlock(statement))!!

// control flow extensions
inline fun CodeBlock.Builder.`if`(statement: String, vararg args: Any?,
                                  function: CodeMethod)
        = beginControl("if", statement = statement, args = *args, function = function)

inline fun CodeBlock.Builder.`do`(function: CodeMethod)
        = beginControl("do", function = function)

fun CodeBlock.Builder.`while`(statement: String, vararg args: Any?) = endControl("while", statement = statement, args = *args)

inline infix fun CodeBlock.Builder.`else`(function: CodeMethod)
        = nextControl("else", function = function).end()

inline fun CodeBlock.Builder.`else if`(statement: String, vararg args: Any?,
                                       function: CodeMethod)
        = nextControl("else if", statement = statement, args = *args, function = function)

inline fun CodeBlock.Builder.`for`(statement: String, vararg args: Any?,
                                   function: CodeMethod)
        = beginControl("for", statement = statement, args = *args, function = function).endControlFlow()!!

inline fun CodeBlock.Builder.`for`(
    statement: StringTemplateMethod,
    function: CodeMethod
) =
    beginControl("for", statement = statement, function = function).endControlFlow()!!

inline fun CodeBlock.Builder.`switch`(statement: String, vararg args: Any?,
                                      function: CodeMethod)
        = beginControl("switch", statement = statement, args = *args, function = function).endControlFlow()!!

inline fun CodeBlock.Builder.`switch`(
    statement: StringTemplateMethod,
    function: CodeMethod
) =
    beginControl("switch", statement = statement, function = function).endControlFlow()!!

fun CodeBlock.Builder.`return`(statement: String, vararg args: Any?) = addStatement("return $statement", *args)!!

fun CodeBlock.Builder.`break`() = addStatement("break")!!

fun CodeBlock.Builder.`continue`() = addStatement("continue")!!

inline fun CodeBlock.Builder.nextControl(name: String, statement: String = "", vararg args: Any?,
                                         function: CodeMethod)
        = nextControlFlow("$name${if (statement.isEmpty()) "" else " ($statement)"}", *args)
    .add(CodeBlock.builder().apply(function).build())!!

inline fun CodeBlock.Builder.beginControl(
    name: String,
    statement: StringTemplateMethod,
    function: CodeMethod
) = add(L, name)
    .beginControlFlow(" ($L)", codeBlock(statement))
    .apply(function)!!

inline fun CodeBlock.Builder.beginControl(
    name: String, statement: String = "", vararg args: Any?,
    function: CodeMethod
) = add(L, name)
    .beginControlFlow(if (statement.isEmpty()) "" else " ($statement)", *args)
    .apply(function)!!

inline fun CodeBlock.Builder.endControl(name: String, statement: String = "", vararg args: Any?) =
    endControlFlow("$name${if (statement.isEmpty()) "" else " ($statement)"}", *args)!!
