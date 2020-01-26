package com.grosner.kpoet

import com.squareup.javapoet.*
import java.lang.reflect.Type
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

fun TypeSpec.Builder.extends(typeName: TypeName) = superclass(typeName)!!

fun TypeSpec.Builder.extends(type: Type) = superclass(type)!!

fun TypeSpec.Builder.implements(vararg typeName: TypeName) = addSuperinterfaces(typeName.toList())!!

fun TypeSpec.Builder.implements(vararg type: Type) = apply { type.forEach { addSuperinterface(it) } }

fun TypeSpec.Builder.modifiers(vararg modifier: Modifier) = addModifiers(*modifier)!!

fun TypeSpec.Builder.modifiers(vararg modifiers: List<Modifier>)
        = apply { modifiers.forEach { addModifiers(*it.toTypedArray()) } }

infix fun TypeSpec.Builder.annotation(type: KClass<*>) = addAnnotation(type.java)!!

infix fun TypeSpec.Builder.annotation(type: ClassName) = addAnnotation(type)!!

inline fun TypeSpec.Builder.annotation(className: ClassName,
                                       function: AnnotationSpec.Builder.() -> AnnotationSpec.Builder)
        = addAnnotation(AnnotationSpec.builder(className).function().build())!!

inline fun TypeSpec.Builder.annotation(className: KClass<*>,
                                       function: AnnotationSpec.Builder.() -> AnnotationSpec.Builder)
        = addAnnotation(AnnotationSpec.builder(className.java).function().build())!!

inline fun `class`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.classBuilder(className).typeSpecFunc().build()!!

inline fun `interface`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.interfaceBuilder(className).typeSpecFunc().build()!!

inline fun `abstract class`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.classBuilder(className).typeSpecFunc().modifiers(abstract).build()!!

inline fun `public abstract class`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.classBuilder(className).typeSpecFunc().modifiers(public, abstract).build()!!

inline fun `public class`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.classBuilder(className).typeSpecFunc().modifiers(public).build()!!

inline fun `public final class`(className: String, typeSpecFunc: TypeMethod)
        = typeSpecFunc(TypeSpec.classBuilder(className)).modifiers(public, final).build()!!

inline fun `final class`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.classBuilder(className).typeSpecFunc().modifiers(final).build()!!

inline fun `enum`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.enumBuilder(className).typeSpecFunc().build()!!

inline fun `public enum`(className: String, typeSpecFunc: TypeMethod)
        = TypeSpec.enumBuilder(className).typeSpecFunc().modifiers(public).build()!!

inline fun `anonymous class`(typeArgumentsFormat: String, vararg args: Any?,
                             typeSpecFunc: TypeMethod)
        = typeSpecFunc(TypeSpec.anonymousClassBuilder(typeArgumentsFormat, *args)).build()!!

inline fun `@interface`(className: String, typeSpecFunc: TypeMethod)
        = typeSpecFunc(TypeSpec.annotationBuilder(className)).build()!!

inline fun `public @interface`(className: String, typeSpecFunc: TypeMethod)
        = typeSpecFunc(TypeSpec.annotationBuilder(className)).modifiers(public).build()!!

fun TypeSpec.Builder.constructor(vararg parameters: ParameterSpec.Builder,
                                 methodSpecFunction: MethodMethod = { this })
        = addMethod(methodSpecFunction(MethodSpec.constructorBuilder()).addParameters(parameters.map { it.build() }
        .toMutableList()).build())!!

fun TypeSpec.Builder.abstract(returnClass: ClassName, name: String,
                              vararg parameters: ParameterSpec.Builder,
                              methodSpecFunction: MethodMethod = { this })
        = addMethod(methodSpecFunction(MethodSpec.methodBuilder(name))
        .addModifiers(Modifier.ABSTRACT)
        .addParameters(parameters.map { it.build() }
                .toMutableList())
        .returns(returnClass)
        .build())!!

fun TypeSpec.Builder.abstract(returnType: KClass<*>, name: String,
                              vararg parameters: ParameterSpec.Builder,
                              methodSpecFunction: MethodMethod = { this })
        = addMethod(methodSpecFunction(MethodSpec.methodBuilder(name))
        .addModifiers(Modifier.ABSTRACT)
        .addParameters(parameters.map { it.build() }
                .toMutableList())
        .returns(returnType)
        .build())!!

fun TypeSpec.Builder.case(name: String) = addEnumConstant(name)!!

fun TypeSpec.Builder.case(name: String, function: TypeMethod) = addEnumConstant(name, function().build())!!

fun TypeSpec.Builder.case(name: String, parameter: String, vararg args: Any?, function: TypeMethod = { this })
        = addEnumConstant(name, TypeSpec.anonymousClassBuilder(parameter, *args).function().build())!!

fun TypeSpec.Builder.javadoc(doc: String, vararg args: Any?) = addJavadoc(doc, args)

fun TypeSpec.Builder.javadoc(codeBlock: CodeBlock) = addJavadoc(codeBlock)


