package com.grosner.kpoet

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import kotlin.reflect.KClass

typealias JavaFileMethod = JavaFile.Builder.() -> Unit

fun javaFile(packageName: String, imports: JavaFileMethod = { },
             function: () -> TypeSpec) = JavaFile.builder(packageName, function()).apply(imports).build()!!

fun JavaFile.Builder.`import static`(kClass: KClass<*>, vararg names: String) = addStaticImport(kClass.java, *names)

fun JavaFile.Builder.`import static`(className: ClassName, vararg names: String) = addStaticImport(className, *names)

fun JavaFile.Builder.`import static`(enum: Enum<*>) = addStaticImport(enum)

val JavaFile.typeName: TypeName get() = ClassName.get(packageName, typeSpec.name)
