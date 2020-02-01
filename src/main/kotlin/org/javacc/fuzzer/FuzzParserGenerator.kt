package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.*
import org.javacc.parser.*
import java.io.InputStream
import java.io.Reader
import java.lang.UnsupportedOperationException
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer
import javax.tools.JavaFileObject

class FuzzParserGenerator(
    val inputFiles: List<JavaFileObject>,
    val outputFiles: MutableList<JavaFileObject>,
    val config: JavaCCConfig,
    val lexer: FuzzLexGenerator
) : Callable<Boolean> {
    private val fuzzParserClassName = config.parserClassName + "FuzzyParser"

    private val jj_depth = "jj_depth"

    lateinit var tokenSource: FieldSpec
    lateinit var jj_random: FieldSpec
    lateinit var token: FieldSpec
    lateinit var tokenOutput: FieldSpec
    lateinit var jj_consumeToken: MethodSpec
    lateinit var getToken: MethodSpec

    private val nameAllocator = NameAllocator()

    private val depthVars = mutableMapOf<MethodSpec.Builder, FieldSpec>()

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
        nameAllocator.allocate("jj_cnt") { jj_cnt ->
            `for`({
                val i = jj_cnt.N
                "int $i = ${min.L} + ${jj_random.N}.nextInt(${(max - min + 1).L})/${jj_depth.N}; $i > 0; $i--"
            }) {
                phase1ExpansionGen(nested)
                comment("TODO: implement Lookahead check for $L", lookahead)
            }
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
            is OneOrMore -> phase1ExpansionGenLoop(expansion.expansion, 1, 3)
            is ZeroOrOne -> phase1ExpansionGenLoop(expansion.expansion, 0, 1)
            is ZeroOrMore -> phase1ExpansionGenLoop(expansion.expansion, 0, 3)
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
        val depth = `private field`(Int::class, "${jj_depth}_${p.lhs}")
        `public`(returnType.T, name = p.lhs, opaqueParams = params) {
            addException(config.parseExceptionTypeName)
            depthVars[this] = depth
            p.declarationTokens.takeIf { it.isNotEmpty() }?.also {
                addCode("$L\n", it.asString())
            }
            statement { "int ${jj_depth.N} = ++${depth.N}" }
            `try` {
                phase1ExpansionGen(p.expansion)
            } `finally` {
                addStatement("$N--", depth)
            }
            if (p.returnTypeTokens.first().kind != JavaCCParserConstants.VOID) {
                `throw new`(Error::class, S, "Missing return statement")
            }
            depthVars.remove(this)
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
                val parserBody = JavaCCGlobals.cu_to_insertion_point_2
                    .asSequence()
                    .dropWhile { it.image != "{" }
                    .drop(1)
                    .asIterable()
                    .asString()
                addBody("$L\n", parserBody)
                jj_random = `private field`(Random::class, "jj_random")
                tokenSource = `private field`(lexer.typeName, "tokenSource")
                token = `public field`(config.tokenTypeName, "token") {
                    `=`("new $T()", config.tokenTypeName)
                }
                tokenOutput = `private field`(Consumer::class.parameterized(config.tokenTypeName), "tokenOutput")

                constructor(param(Random::class, "random")) {
                    addStatement("this.$N = random", jj_random)
                    addStatement("this.$N = new $T(random)", tokenSource, lexer.typeName)
                }

                public(TypeName.VOID, "ReInit", param(Reader::class, "reader")) {
                    `throw new`(UnsupportedOperationException::class, S, "Not implemented yet")
                }

                public(TypeName.VOID, "set${tokenOutput.name.capitalize()}", param(tokenOutput.type, "value")) {
                    addStatement("this.$N = value", tokenOutput)
                }

                public(tokenOutput.type, "get${tokenOutput.name.capitalize()}") {
                    `return`(N, tokenOutput)
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
                        `if`("$N != null", tokenOutput) {
                            addStatement("$N.accept(nextToken)", tokenOutput)
                        }.`end if`()
                    }.`end if`()
                    addStatement("$N = nextToken", token)
                    addStatement("oldToken.next = null; // Stop nepotism")
                    `return`("nextToken")
                }

                getToken = public(config.tokenTypeName, "getToken", param(Int::class, "offset")) {
                    addModifiers(final)
                    code {
                        """
                        ${config.tokenTypeName.T} t = token;
                        for(int i = 0; i < offset; i++) {
                          if (t.next != null) {
                            t = t.next;
                          } else {
                            throw new IllegalArgumentException("Lookahead is not implemented yet");
                          }
                        }
                        return t;
                        """.trimIndent()
                    }
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
