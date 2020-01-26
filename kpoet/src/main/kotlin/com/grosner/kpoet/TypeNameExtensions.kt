package com.grosner.kpoet

import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

val <T : Any> KClass<T>.typeName
    get() = TypeName.get(this.java)

val TypeMirror.typeName
    get() = TypeName.get(this)

inline fun <reified T : Any> parameterized(kClass: KClass<*>) = ParameterizedTypeName.get(kClass.java, T::class.java)!!

inline fun <reified T1 : Any, reified T2 : Any> parameterized2(kClass: KClass<*>)
        = ParameterizedTypeName.get(kClass.java, T1::class.java, T2::class.java)!!

inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any> parameterized3(kClass: KClass<*>)
        = ParameterizedTypeName.get(kClass.java, T1::class.java, T2::class.java, T3::class.java)!!

