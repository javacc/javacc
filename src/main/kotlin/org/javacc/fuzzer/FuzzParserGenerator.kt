package org.javacc.fuzzer

import org.javacc.parser.JavaCCGlobals
import java.nio.file.Path
import javax.tools.JavaFileObject

class FuzzParserGenerator {
    fun generate() {
        val config = JavaCCConfig(
            // TODO: get from .jj
            packageName = "org.javacc.test.simpleFuzzer",
            parserClassName = JavaCCGlobals.cu_name,
            bnfproductions = JavaCCGlobals.bnfproductions
        )

        val outputFiles = mutableListOf<JavaFileObject>()
        FuzzParserGeneratorTask(
            listOf(),
            outputFiles,
            config
        ).call()

        for (file in outputFiles) {
            println("FUZZ ${file.name}:\n")
            println(file.getCharContent(true))
        }
    }
}
