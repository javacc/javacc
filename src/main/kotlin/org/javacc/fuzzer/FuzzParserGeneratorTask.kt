package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.*
import org.javacc.parser.*
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Callable
import javax.tools.JavaFileObject

private const val L = "\$L"
private const val N = "\$N"
private const val S = "\$S"
private const val T = "\$T"

class FuzzParserGeneratorTask(
    val inputFiles: List<JavaFileObject>,
    val outputFiles: MutableList<JavaFileObject>,
    val config: JavaCCConfig,
    val encoding: Charset = Charsets.UTF_8
) : Callable<Boolean> {
    private val fuzzParserClassName = config.parserClassName + "FuzzyParser"
    private val fuzzLexerClassName = config.parserClassName + "FuzzyLezer"
    private val tokenTypeName = ClassName.get(config.packageName, "Token")
    private val randomFieldName = "jj_random"
    private val generateTokenMethod = "jj_generateToken"
    private val lexerGenerateMethod = "jj_generate"

    private fun MethodSpec.Builder.phase1ExpansionGen(sequence: Sequence) {
        for (i in 1 until sequence.units.size) {
            phase1ExpansionGen(sequence.units[i] as Expansion)
        }
    }

    private fun MethodSpec.Builder.phase1ExpansionGen(action: Action) {
        addCode("$[$L\n$]", action.actionTokens.asString())
    }

    private fun MethodSpec.Builder.phase1ExpansionGen(regex: RegularExpression) {
        addCode("$[")
        regex.lhsTokens.takeIf { it.isNotEmpty() }?.let {
            addCode("$L = ", it.asString())
        }
        val label = regex.label.ifEmpty {
            JavaCCGlobals.names_of_tokens[regex.ordinal] ?: regex.ordinal.toString()
        }
        addCode(
            "\$N(\$N)$L;\n$]",
            generateTokenMethod,
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
        addStatement("int max = 1 + $N.nextInt(5);", randomFieldName)
        `for`("int i = 0; i < max; i++") {
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
            is OneOrMore -> phase1ExpansionGen(expansion)
            is ZeroOrMore -> `return`(S, "ZeroOrMore: $expansion")
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
        }
    }

    override fun call(): Boolean {
        val generators = mutableMapOf<RegularExpression, MethodSpec>()

        val fuzzyLexerFile = javaFile(config.packageName) {
            `class`(fuzzLexerClassName) {
                field(Int::class, "curLexState")
                field(Int::class, "defaultLexState")
                field(Int::class, "jjnewStateCnt")
                field(Int::class, "jjround")
                field(Int::class, "jjmatchedPos")
                val jjmatchedKind = field(Int::class, "jjmatchedKind")
                val jjimage = `private field`(java.lang.StringBuilder::class, "jjimage") { `=`("new $T()", java.lang.StringBuilder::class.java) }
                val image = `private field`(java.lang.StringBuilder::class, "image") { `=`(N, jjimage) }
                `private field`(Int::class, "jjimageLen");
                `private field`(Int::class, "lengthOfMatch");
                `protected field`(Int::class, "curChar");


                val fillToken = private(TypeName.VOID, "fillToken") {
                    addStatement("$1T t = $1T.newToken($2N, curTokenImage)", tokenTypeName, jjmatchedKind)
                    comment("TODO: fill position")
                    `return`("t")
                }

                public(tokenTypeName, lexerGenerateMethod, param(Int::class, "kind")) {
                    addStatement("$N = kind", jjmatchedKind)
                    `return`("$N()", fillToken)
                }

                for (tp in JavaCCGlobals.rexprlist) {
                    for (regexpSpec in tp.respecs) {
                        val r = regexpSpec.rexp
                        val label = r.label?.capitalize() ?: ""
                        val gen = private(TypeName.VOID, "jj_generate_${label}_${r.ordinal}") {
                            when(r) {
                                is RStringLiteral -> addStatement("$N.append($S)", jjimage, r.image);
                                else -> comment("${r}")
                            }
                        }
                    }
                }
            }
        }

        outputFiles += fuzzyLexerFile.toJavaFileObject()

        val fuzzerFile = javaFile(config.packageName) {
            `class`(fuzzParserClassName) {
                modifiers(public)

                `private field`(Random::class, randomFieldName) {
                    `=`("new \$T()", Random::class.java)
                }

                val tokenSource = `private field`(fuzzyLexerFile.typeName, "tokenSource") {
                    `=`("new \$T()", fuzzyLexerFile.typeName)
                }

                val token = `public field`(tokenTypeName, "token")

                `private`(tokenTypeName, generateTokenMethod, param(Int::class, "kind")) {
                    addStatement("$N = $N.$N(kind)", token, lexerGenerateMethod, tokenSource)
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
