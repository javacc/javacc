package com.grosner.kpoet

import com.squareup.javapoet.CodeBlock

inline fun CodeBlock.Builder.case(statement: String, vararg args: Any, function: CodeMethod)
        = beginControlFlow("case $statement:", args).function().endControlFlow()!!

inline fun CodeBlock.Builder.default(function: CodeMethod)
        = beginControlFlow("default:").function().endControlFlow()!!

inline fun CodeBlock.Builder.code(codeMethod: CodeMethod)
        = add(CodeBlock.builder().codeMethod().build())!!

inline fun code(codeMethod: CodeMethod) = CodeBlock.builder().codeMethod().build()!!

inline fun CodeBlock.Builder.statement(codeMethod: CodeMethod)
        = addStatement(CodeBlock.builder().codeMethod().build().L)!!

fun CodeBlock.Builder.statement(code: String, vararg args: Any?) = addStatement(code, *args)!!

fun CodeBlock.Builder.end(statement: String = "", vararg args: Any?)
        = (if (statement.isNullOrBlank().not()) endControlFlow(statement, *args) else endControlFlow())!!


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

inline fun CodeBlock.Builder.`switch`(statement: String, vararg args: Any?,
                                      function: CodeMethod)
        = beginControl("switch", statement = statement, args = *args, function = function).endControlFlow()!!

fun CodeBlock.Builder.`return`(statement: String, vararg args: Any?) = addStatement("return $statement", *args)!!

fun CodeBlock.Builder.`break`() = addStatement("break")!!

fun CodeBlock.Builder.`continue`() = addStatement("continue")!!

inline fun CodeBlock.Builder.nextControl(name: String, statement: String = "", vararg args: Any?,
                                         function: CodeMethod)
        = nextControlFlow("$name${if (statement.isNullOrEmpty()) "" else " ($statement)"}", *args)
        .add(function(CodeBlock.builder()).build())!!

inline fun CodeBlock.Builder.beginControl(name: String, statement: String = "", vararg args: Any?,
                                          function: CodeMethod)
        = beginControlFlow("$name${if (statement.isNullOrEmpty()) "" else " ($statement)"}", *args)
        .add(function(CodeBlock.builder()).build())!!

inline fun CodeBlock.Builder.endControl(name: String, statement: String = "", vararg args: Any?)
        = endControlFlow("$name${if (statement.isNullOrEmpty()) "" else " ($statement)"}", *args)!!
