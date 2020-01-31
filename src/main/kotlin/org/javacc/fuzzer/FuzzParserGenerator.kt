package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.*
import org.javacc.parser.*
import java.lang.Error
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Callable
import javax.tools.JavaFileObject

class FuzzParserGenerator(
    val inputFiles: List<JavaFileObject>,
    val outputFiles: MutableList<JavaFileObject>,
    val config: JavaCCConfig,
    val lexer: FuzzLexGenerator
) : Callable<Boolean> {
    private val fuzzParserClassName = config.parserClassName + "FuzzyParser"

    lateinit var tokenSource: FieldSpec
    lateinit var jj_random: FieldSpec

    private fun MethodSpec.Builder.phase1ExpansionGen(sequence: Sequence) {
        for (i in 1 until sequence.units.size) {
            phase1ExpansionGen(sequence.units[i] as Expansion)
        }
    }

    private fun MethodSpec.Builder.phase1ExpansionGen(action: Action) {
        addCode("$[$L\n$]", action.actionTokens.asString())
    }

    private fun MethodSpec.Builder.phase1ExpansionGen(regex: RegularExpression) {
        if (regex.ordinal == 0) {
            comment("EOF")
            return
        }
        addCode("$[")
        regex.lhsTokens.takeIf { it.isNotEmpty() }?.let {
            addCode("$L = ", it.asString())
        }
        // val label = regex.label.ifEmpty {
        //     JavaCCGlobals.names_of_tokens[regex.ordinal] ?: regex.ordinal.toString()
        // }
        addCode(
            "$N.$N()$L;\n$]",
            tokenSource,
            lexer.generatorNames[regex.ordinal],
            regex.rhsToken?.let { "." + it.asString } ?: ""
        )
    }

    private fun MethodSpec.Builder.phase1ExpansionGenLoop(
        nested: Expansion,
        min: Int,
        max: Int
    ) {
        val lookahead = if (nested is Sequence) {
            nested.lookahead
        } else {
            Lookahead().apply {
                amount = Options.getLookahead()
                laExpansion = nested
            }
        }
        `for`("int jj_cnt = $L + $N.nextInt($L); jj_cnt > 0; jj_cnt--", min, jj_random, max - min) {
            phase1ExpansionGen(nested)
            comment("TODO: implement Lookahead check for $L", lookahead)
        }
    }

    /**
     * See [org.javacc.parser.ParseEngine.phase1ExpansionGen]
     */
    private fun MethodSpec.Builder.phase1ExpansionGen(expansion: Expansion) {
        when (expansion) {
            is RegularExpression -> phase1ExpansionGen(expansion)
            is NonTerminal -> `return`(S, "NonTerminal: $expansion")
            is Action -> phase1ExpansionGen(expansion)
            is Choice -> `return`(S, "Choice: $expansion")
            is Sequence -> phase1ExpansionGen(expansion)
            is OneOrMore -> phase1ExpansionGenLoop(expansion.expansion, 1, 5)
            is ZeroOrMore -> phase1ExpansionGenLoop(expansion.expansion, 0, 5)
            is TryBlock -> `return`(S, "TryBlock: $expansion")
        }
    }

    /**
     * See [org.javacc.parser.ParseEngine.buildPhase1Routine]
     */
    private fun TypeSpec.Builder.buildPhase1Routine(p: BNFProduction) {
        val returnType = p.returnTypeTokens.asString()
        val params = CodeBlock.of(L, p.parameterListTokens.asString())
        `public`(returnType.T, name = p.lhs, opaqueParams = params) {
            addCode("$[$L\n$]", p.declarationTokens.asString())
            phase1ExpansionGen(p.expansion)
            `throw new`(Error::class, "Missing return statement")
        }
    }

    override fun call(): Boolean {
        val fuzzerFile = javaFile(
            config.packageName,
            imports = {
                for (import in config.parserImports) {
                    addImport(import.asString())
                }
                for (import in config.parserStaticImports) {
                    addStaticImport(import.asString())
                }
            }
        ) {
            `class`(fuzzParserClassName) {
                modifiers(public)
                jj_random = `private field`(Random::class, "jj_random")
                tokenSource = `private field`(lexer.typeName, "tokenSource")
                val token = `public field`(config.tokenTypeName, "token")

                constructor(param(Random::class, "random")) {
                    addStatement("this.$N = random", jj_random)
                    addStatement("this.$N = new $T(random)", tokenSource, lexer.typeName)
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
