package org.javacc.builttools.bootstrap

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import org.gradle.process.JavaExecSpec
import javax.inject.Inject

open class JavaCCTask @Inject constructor(
    objectFactory: ObjectFactory
) : BaseJavaCCTask("javacc", objectFactory) {

    @Input
    val lookAhead = objectFactory.property<Int>().convention(1)

    @Input
    val static = objectFactory.property<Boolean>().convention(false)

    override fun JavaExecSpec.configureJava() {
        // The class is in the top-level package
        main = "javacc"
        args("-STATIC=${static.get()}")
        args("-LOOKAHEAD:${lookAhead.get()}")
        args("-OUTPUT_DIRECTORY:${allGeneratedDirectory.get()}/${packageName.get().replace('.', '/')}")
        args(inputFile.get())
    }
}
