package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.javacc.parser.*
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
    lateinit var token: FieldSpec
    lateinit var jj_consumeToken: MethodSpec

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
        val label = regex.label.ifEmpty {
            JavaCCGlobals.names_of_tokens[regex.ordinal] ?: regex.ordinal.toString()
        }
        addCode(
            "$N($N)$L;\n$]",
            jj_consumeToken,
            label,
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
        `for`("int jj_cnt = $L + $N.nextInt($L); jj_cnt > 0; jj_cnt--", min, jj_random, max - min + 1) {
            phase1ExpansionGen(nested)
            comment("TODO: implement Lookahead check for $L", lookahead)
        }
    }

    /**
     * See [org.javacc.parser.ParseEngine.phase1ExpansionGen]
     */
    private fun MethodSpec.Builder.phase1ExpansionGen(p: NonTerminal) {
        addCode("$[")
        p.lhsTokens.takeIf { it.isNotEmpty() }?.let {
            addCode("$L = ", it.asString())
        }
        addCode(
            "$N($L);\n$]",
            p.name,
            p.argumentTokens.asString()
        )
    }

    /**
     * See [org.javacc.parser.ParseEngine.phase1ExpansionGen]
     */
    private fun MethodSpec.Builder.phase1ExpansionGen(p: Choice) {
        // TODO: support lookahead
        switch("$N.nextInt($L)", jj_random, p.choices.size) {
            for ((index, choice) in p.choices.withIndex()) {
                case(L, index) {
                    phase1ExpansionGen(choice as Expansion)
                    `break`()
                }
            }
        }
    }

    /**
     * See [org.javacc.parser.ParseEngine.phase1ExpansionGen]
     */
    private fun MethodSpec.Builder.phase1ExpansionGen(expansion: Expansion) {
        when (expansion) {
            is RegularExpression -> phase1ExpansionGen(expansion)
            is NonTerminal -> phase1ExpansionGen(expansion)
            is Action -> phase1ExpansionGen(expansion)
            is Choice -> phase1ExpansionGen(expansion)
            is Sequence -> phase1ExpansionGen(expansion)
            is OneOrMore -> phase1ExpansionGenLoop(expansion.expansion, 1, 5)
            is ZeroOrOne -> phase1ExpansionGenLoop(expansion.expansion, 0, 1)
            is ZeroOrMore -> phase1ExpansionGenLoop(expansion.expansion, 0, 5)
            // is TryBlock -> phase1ExpansionGenLoop(expansion)
            else -> TODO(expansion.toString())
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
            `throw new`(Error::class, S, "Missing return statement")
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
                addStaticImport(config.parserConstantsClassName, "*")
            }
        ) {
            `class`(fuzzParserClassName) {
                modifiers(public)
                jj_random = `private field`(Random::class, "jj_random")
                tokenSource = `private field`(lexer.typeName, "tokenSource")
                token = `public field`(config.tokenTypeName, "token") {
                    `=`("new $T()", config.tokenTypeName)
                }

                constructor(param(Random::class, "random")) {
                    addStatement("this.$N = random", jj_random)
                    addStatement("this.$N = new $T(random)", tokenSource, lexer.typeName)
                }

                jj_consumeToken = protected(config.tokenTypeName, "jj_consumeToken", param(config.tokenKindTypeName, "kind")) {
                    addJavadoc(
                        """
                            Generates the next token with given kind.
                            @throws IllegalArgumentException when the current token kind does not match the provided argument
                        """.trimIndent()
                    )
                    addStatement("$T oldToken = $N", config.tokenTypeName, token)
                    addStatement("$T nextToken = oldToken.next", config.tokenTypeName)
                    `if`("nextToken == null") {
                        addStatement("nextToken = $N.$N(kind)", tokenSource, lexer.generateMethodName)
                    }.`end if`()
                    addStatement("$N = nextToken", token)
                    addStatement("oldToken.next = null; // Stop nepotism")
                    `return`("nextToken")
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
