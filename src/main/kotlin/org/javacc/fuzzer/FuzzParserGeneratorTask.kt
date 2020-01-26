package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.javacc.parser.BNFProduction
import org.javacc.parser.CodeProduction
import org.javacc.parser.ParseEngine
import java.nio.charset.Charset
import java.util.concurrent.Callable
import javax.tools.JavaFileObject

class FuzzParserGeneratorTask(
    val inputFiles: List<JavaFileObject>,
    val outputFiles: MutableList<JavaFileObject>,
    val config: JavaCCConfig,
    val encoding: Charset = Charsets.UTF_8
) : Callable<Boolean> {
    private val fuzzParserClassName = config.parserClassName + "FuzzyParser"

    private val fuzzLexerClassName = config.parserClassName + "FuzzyLezer"

    /**
     * See [ParseEngine.buildPhase1Routine]
     */
    fun TypeSpec.Builder.buildPhase1Routine(p: BNFProduction) {
        println(p.returnTypeTokens)
        val returnType = StringBuilder().apply {
            for(t in p.returnTypeTokens) {
                appendSingleWithComments(t)
            }
        }.toString()
//        `public`(returnType.T, name = p.lhs, params = arrayOf(param())) {
//            this
//        }
    }

    override fun call(): Boolean {
        val fuzzerFile = javaFile(config.packageName) {
            `class`(fuzzParserClassName) {
                modifiers(public)

                for (bnfproduction in config.bnfproductions) {
                    when (bnfproduction) {
                        is BNFProduction -> buildPhase1Routine(bnfproduction)
                        else -> throw IllegalArgumentException("Unsupported production: $bnfproduction")
                    }
                    println(bnfproduction)
//                    `public`(String::class, bnfproduction.ty)
//                    bnfproduction.
                }
//                `public`(String::class, "getStatus", param(TypeName.BOOLEAN, "isReady")) {
//                    `if`("isReady") {
//                        `return`("BONUS".S) // if we don't use .S, it's outputted as a literal.
//                    } `else` {
//                        `return`("NO BONUS".S)
//                    }
//                }
                this
            }
        }

        outputFiles += fuzzerFile.toJavaFileObject()

        val fuzzyLexerFile = javaFile(config.packageName) {
            `class`(fuzzLexerClassName) {

                this
            }
        }

        return true
    }
}
