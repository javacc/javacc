package com.grosner.kpoet

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.*
import kotlin.reflect.KClass

val packagePrivate
    get() = listOf<Modifier>()

val public = PUBLIC

val private = PRIVATE

val protected = PROTECTED

val abstract = ABSTRACT

val static = STATIC

val final = FINAL

fun `fun`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
          codeMethod: MethodMethod = { })
        = applyParams(packagePrivate, type, name, params = *params, function = codeMethod)

fun `fun`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
          codeMethod: MethodMethod = { })
        = applyParams(packagePrivate, type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`fun`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                           codeMethod: MethodMethod = { })
        = addMethod(applyParams(packagePrivate, type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`fun`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                           codeMethod: MethodMethod = { })
        = addMethod(applyParams(packagePrivate, type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(packagePrivate, type, name, codeMethod).also { addField(it) }

fun TypeSpec.Builder.`field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(packagePrivate, type, name, codeMethod).also { addField(it) }

fun `private`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
              codeMethod: MethodMethod = { })
        = applyParams(listOf(private), type, name, params = *params, function = codeMethod)

fun `private`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
              codeMethod: MethodMethod = { })
        = applyParams(listOf(private), type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`private`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                               codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(private), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`private`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                               codeMethod: MethodMethod = { })
        = applyParams(listOf(private), type, name, params = *params, function = codeMethod).also { addMethod(it) }

fun TypeSpec.Builder.`private field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(private), type, name, codeMethod).also { addField(it) }

fun TypeSpec.Builder.`private field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(private), type, name, codeMethod).also { addField(it) }

fun `private final`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                    codeMethod: MethodMethod = { })
        = applyParams(listOf(private, final), type, name, params = *params, function = codeMethod)

fun `private final`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                    codeMethod: MethodMethod = { })
        = applyParams(listOf(private, final), type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`private final`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                                     codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(private, final), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`private final`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                                     codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(private, final), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`private final field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(private, final), type, name, codeMethod).also { addField(it) }

fun TypeSpec.Builder.`private final field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(private, final), type, name, codeMethod).also { addField(it) }

fun `private static final`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                           codeMethod: MethodMethod = { })
        = applyParams(listOf(private, static, final), type, name, params = *params, function = codeMethod)

fun `private static final`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                           codeMethod: MethodMethod = { })
        = applyParams(listOf(private, static, final), type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`private static final`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                                            codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(private, static, final), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`private static final`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                                            codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(private, static, final), type, name, params = *params, function = codeMethod))!!


fun TypeSpec.Builder.`private static final field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = addField(applyFieldParams(listOf(private, static, final), type, name, codeMethod))!!

fun TypeSpec.Builder.`private static final field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = addField(applyFieldParams(listOf(private, static, final), type, name, codeMethod))!!

fun `public static`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                    codeMethod: MethodMethod = { })
        = applyParams(listOf(public, static), type, name, params = *params, function = codeMethod)

fun `public static`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                    codeMethod: MethodMethod = { })
        = applyParams(listOf(public, static), type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`public static`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                                     codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(public, static), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`public static`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                                     codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(public, static), type, name, params = *params, function = codeMethod))!!


fun TypeSpec.Builder.`public static field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = addField(applyFieldParams(listOf(public, static), type, name, codeMethod))!!

fun TypeSpec.Builder.`public static field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = addField(applyFieldParams(listOf(public, static), type, name, codeMethod))!!

fun `public static final`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                          codeMethod: MethodMethod = { })
        = applyParams(listOf(public, static, final), type, name, params = *params, function = codeMethod)

fun `public static final`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                          codeMethod: MethodMethod = { })
        = applyParams(listOf(public, static, final), type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`public static final`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                                           codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(public, static, final), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`public static final`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                                           codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(public, static, final), type, name, params = *params, function = codeMethod))!!


fun TypeSpec.Builder.`public static final field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(public, static, final), type, name, codeMethod).also { addField(it) }

fun TypeSpec.Builder.`public static final field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(public, static, final), type, name, codeMethod).also { addField(it) }

fun `public`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
             codeMethod: MethodMethod = { })
        = applyParams(listOf(public), type, name, params = *params, function = codeMethod)

fun `public`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
             codeMethod: MethodMethod = { })
        = applyParams(listOf(public), type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`public`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                              codeMethod: MethodMethod = { })
        = applyParams(listOf(public), type, name, params = *params, function = codeMethod).also { addMethod(it) }

fun TypeSpec.Builder.`public`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                              codeMethod: MethodMethod = { })
        = applyParams(listOf(public), type, name, params = *params, function = codeMethod).also { addMethod(it) }

// VLSI: new
fun TypeSpec.Builder.`public`(
    type: TypeName, name: String, opaqueParams: CodeBlock,
    codeMethod: MethodMethod = { }
) = addMethod(applyParams(listOf(public), type, name) {
    addOpaqueParameters(opaqueParams)
    codeMethod()
})!!

fun TypeSpec.Builder.`public field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(public), type, name, codeMethod).also { addField(it) }

fun TypeSpec.Builder.`public field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(public), type, name, codeMethod).also { addField(it) }

fun `protected`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                codeMethod: MethodMethod = { })
        = applyParams(listOf(protected), type, name, params = *params, function = codeMethod)

fun `protected`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                codeMethod: MethodMethod = { })
        = applyParams(listOf(protected), type, name, params = *params, function = codeMethod)

fun TypeSpec.Builder.`protected`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                                 codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(protected), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`protected`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                                 codeMethod: MethodMethod = { })
        = addMethod(applyParams(listOf(protected), type, name, params = *params, function = codeMethod))!!

fun TypeSpec.Builder.`protected field`(type: KClass<*>, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(protected), type, name, codeMethod).also { addField(it) }

fun TypeSpec.Builder.`protected field`(type: TypeName, name: String, codeMethod: FieldMethod = { })
        = applyFieldParams(listOf(protected), type, name, codeMethod).also { addField(it) }

private fun applyParams(modifiers: Collection<Modifier>,
                        type: TypeName,
                        name: String,
                        vararg params: ParameterSpec.Builder,
                        function: MethodMethod = { })
        = MethodSpec.methodBuilder(name).addModifiers(*modifiers.toTypedArray())
        .returns(type).addParameters(params.map { it.build() }.toList())
        .apply(function).build()!!

private fun applyParams(modifiers: Collection<Modifier>,
                        kClass: KClass<*>,
                        name: String,
                        vararg params: ParameterSpec.Builder,
                        function: MethodMethod = { })
        = MethodSpec.methodBuilder(name).addModifiers(*modifiers.toTypedArray())
        .returns(kClass).addParameters(params.map { it.build() }.toList())
        .apply(function).build()!!

private fun applyFieldParams(modifiers: Collection<Modifier>,
                             type: TypeName,
                             name: String,
                             function: FieldMethod = { })
        = FieldSpec.builder(type, name).addModifiers(*modifiers.toTypedArray())
        .apply(function).build()!!

private fun applyFieldParams(modifiers: Collection<Modifier>,
                             kClass: KClass<*>,
                             name: String,
                             function: FieldMethod = { })
        = FieldSpec.builder(kClass.java, name).addModifiers(*modifiers.toTypedArray())
        .apply(function).build()!!

