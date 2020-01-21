package org.javacc.builttools.bootstrap

import org.gradle.api.model.ObjectFactory
import org.gradle.process.JavaExecSpec
import javax.inject.Inject

open class JJTreeTask @Inject constructor(
    objectFactory: ObjectFactory
) : BaseJavaCCTask("jjtree", objectFactory) {
    override fun JavaExecSpec.configureJava() {
        main = "jjtree"
        args("-OUTPUT_DIRECTORY:${allGeneratedDirectory.get()}/${packageName.get().replace('.', '/')}")
        args(inputFile.get())
    }
}
