package com.grosner.kpoet

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

val <T : Any> KClass<T>.typeName
    get() = TypeName.get(this.java)

val <T : Any> KClass<T>.className
    get() = ClassName.get(this.java)

val TypeMirror.typeName
    get() = TypeName.get(this)

fun KClass<*>.parameterized(vararg typeNames: KClass<*>) =
    this.className.parameterized(*typeNames.map { it.typeName }.toTypedArray())

fun KClass<*>.parameterized(vararg typeNames: TypeName) =
    this.className.parameterized(*typeNames)

fun ClassName.parameterized(vararg typeNames: TypeName) =
    ParameterizedTypeName.get(this, *typeNames)!!

inline fun <reified T : Any> KClass<*>.parameterized() =
    ParameterizedTypeName.get(java, T::class.java)!!

inline fun <reified T1 : Any, reified T2 : Any> KClass<*>.parameterized2() =
    ParameterizedTypeName.get(java, T1::class.java, T2::class.java)!!

inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any> KClass<*>.parameterized3() =
    ParameterizedTypeName.get(java, T1::class.java, T2::class.java, T3::class.java)!!
