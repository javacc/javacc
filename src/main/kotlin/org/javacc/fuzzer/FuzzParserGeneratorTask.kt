package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.javacc.parser.*
import java.nio.charset.Charset
import java.util.*
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

    private fun MethodSpec.Builder.phase1ExpansionGen(sequence: Sequence) {
        for (i in 1 until sequence.units.size) {
            phase1ExpansionGen(sequence.units[i] as Expansion)
        }
    }

    private fun MethodSpec.Builder.phase1ExpansionGen(action: Action) {
        addCode("\$[\$L\n\$]", action.actionTokens.asString())
    }

    private fun MethodSpec.Builder.phase1ExpansionGen(regex: RegularExpression) {
        addCode("\$[")
        regex.lhsTokens.takeIf { it.isNotEmpty() }?.let {
            addCode("\$L = ", regex.lhsTokens.asString())
        }
        val label = regex.label.ifEmpty {
            JavaCCGlobals.names_of_tokens[regex.ordinal] ?: regex.ordinal.toString()
        }
        addCode(
            "generateRegexp(\$N)\$L;\n\$]",
            label,
            regex.rhsToken?.let { "." + it.asString } ?: ""
        )
    }

    private fun MethodSpec.Builder.phase1ExpansionGen(oneOrMore: OneOrMore) {
        val nested = oneOrMore.expansion
        val lookahead = if (nested is Sequence) {
            nested.lookahead
        } else {
            Lookahead().apply {
                amount = Options.getLookahead()
                laExpansion = nested
            }
        }
        `while`("true") {
            phase1ExpansionGen(nested)
            comment("TODO: implement Lookahead check for \$L", lookahead)
        }
    }

    /**
     * See [org.javacc.parser.ParseEngine.phase1ExpansionGen]
     */
    private fun MethodSpec.Builder.phase1ExpansionGen(expansion: Expansion) {
        when (expansion) {
            is RegularExpression -> phase1ExpansionGen(expansion)
            is NonTerminal -> `return`("\$S", "NonTerminal: $expansion")
            is Action -> phase1ExpansionGen(expansion)
            is Choice -> `return`("\$S", "Choice: $expansion")
            is Sequence -> phase1ExpansionGen(expansion)
            is OneOrMore -> phase1ExpansionGen(expansion)
            is ZeroOrMore -> `return`("\$S", "ZeroOrMore: $expansion")
            is TryBlock -> `return`("\$S", "TryBlock: $expansion")
        }
    }

    /**
     * See [org.javacc.parser.ParseEngine.buildPhase1Routine]
     */
    private fun TypeSpec.Builder.buildPhase1Routine(p: BNFProduction) {
        val returnType = p.returnTypeTokens.asString()
        val params = CodeBlock.of("\$L", p.parameterListTokens.asString())
        `public`(returnType.T, name = p.lhs, opaqueParams = params) {
            addCode("\$[\$L\n\$]", p.declarationTokens.asString())
            phase1ExpansionGen(p.expansion)
        }
    }

    override fun call(): Boolean {
        val fuzzyLexerFile = javaFile(config.packageName) {
            `class`(fuzzLexerClassName) {
            }
        }

        outputFiles += fuzzyLexerFile.toJavaFileObject()

        val fuzzerFile = javaFile(config.packageName) {
            `class`(fuzzParserClassName) {
                modifiers(public)

                `private field`(Random::class, "_random_") {
                    `=`("new \$T()", Random::class.java)
                }

                `private field`(fuzzyLexerFile.typeName, "tokenSource") {
                    `=`("new \$T()", fuzzyLexerFile.typeName)
                }

                for (production in config.bnfproductions) {
                    when (production) {
                        is BNFProduction -> buildPhase1Routine(production)
                        else -> throw IllegalArgumentException("Unsupported production: $production")
                    }
                }
            }
        }

        outputFiles += fuzzerFile.toJavaFileObject()

        return true
    }
}
