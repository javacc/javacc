package com.grosner.kpoet

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import kotlin.reflect.KClass

fun javaFile(packageName: String, imports: JavaFile.Builder.() -> JavaFile.Builder = { this },
             function: () -> TypeSpec) = JavaFile.builder(packageName, function()).imports().build()!!

fun JavaFile.Builder.`import static`(kClass: KClass<*>, vararg names: String) = addStaticImport(kClass.java, *names)

fun JavaFile.Builder.`import static`(className: ClassName, vararg names: String) = addStaticImport(className, *names)

fun JavaFile.Builder.`import static`(enum: Enum<*>) = addStaticImport(enum)


