package org.javacc.fuzzer

import org.javacc.parser.JavaCCGlobals
import org.javacc.parser.Options
import org.javacc.parser.Token
import java.io.File
import javax.tools.JavaFileObject

class FuzzGenerator(
    private val parserPackageName: List<Token>,
    private val parserClassName: String,
    private val parserImports: List<List<Token>>,
    private val parserStaticImports: List<List<Token>>
) {
    fun generateAndSave() {
        val outputDirectory = Options.getOutputDirectory()
        for (file in generate()) {
            // JavaCC saves files without package :(
            val nameWithoutPackage = File(file.name).name
            val f = File(outputDirectory, nameWithoutPackage)
            f.parentFile.mkdirs()
            f.writeText(file.getCharContent(true).toString())
        }
    }

    fun generate(): List<JavaFileObject> {
        val config = JavaCCConfig(
            packageName = parserPackageName.asStringWithoutComments().replace(Regex("\\s+"), ""),
            parserClassName = parserClassName,
            parserImports = parserImports,
            parserStaticImports = parserStaticImports,
            bnfproductions = JavaCCGlobals.bnfproductions
        )

        val outputFiles = mutableListOf<JavaFileObject>()
        val lexer = FuzzLexGenerator(
            listOf(),
            outputFiles,
            config
        )
        lexer.call()
        FuzzParserGenerator(
            listOf(),
            outputFiles,
            config,
            lexer
        ).call()

        for (file in outputFiles) {
            file.name
            println("FUZZ ${file.name}:\n")
            println(file.getCharContent(true))
        }
        return outputFiles
    }
}
