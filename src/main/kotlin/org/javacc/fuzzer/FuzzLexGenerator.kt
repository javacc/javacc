package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import org.javacc.parser.*
import java.util.*
import java.util.concurrent.Callable
import javax.tools.JavaFileObject

class FuzzLexGenerator(
    private val inputFiles: List<JavaFileObject>,
    private val outputFiles: MutableList<JavaFileObject>,
    private val config: JavaCCConfig
) : Callable<Boolean> {
    private val fuzzLexerClassName = config.parserClassName + "FuzzyLexer"

    val typeName = ClassName.get(config.packageName, fuzzLexerClassName)!!
    val generateMethodName = "jj_generate"

    private val fillTokenMethodName = "jj_fillToken"

    private lateinit var jj_random: FieldSpec
    private lateinit var jjmatchedKind: FieldSpec
    private lateinit var jjimage: FieldSpec
    private lateinit var image: FieldSpec
    private lateinit var curTokenImage: FieldSpec

    private val nameAllocator = NameAllocator()

    private val RegularExpression.unwrap: RegularExpression get() {
        var r = this
        while (r is RJustName) {
            r = r.regexpr
        }
        return r
    }

    private fun MethodSpec.Builder.implement(re: RegularExpression) {
        when (val r = re.unwrap) {
            is RStringLiteral -> addStatement("$N.append($S)", jjimage, r.image);
            is RCharacterList -> implement(r)
            is RSequence -> implement(r)
            is RChoice -> implement(r)
            is RRepetitionRange -> implementLoop(r.regexpr, r.min, if (r.hasMax) r.max else 5.coerceAtLeast(r.min + 5))
            is RZeroOrMore -> implementLoop(r.regexpr, 0, 5)
            is RZeroOrOne -> implementLoop(r.regexpr, 0, 1)
            is ROneOrMore -> implementLoop(r.regexpr, 1, 5)
            is REndOfFile -> comment("EOF")
            else -> TODO("Unsupported regexp: ${r}")
        }
    }

    private fun MethodSpec.Builder.implement(re: RSequence) {
        for (r in re.units) {
            implement(r as RegularExpression)
        }
    }

    private fun MethodSpec.Builder.implement(re: RChoice) {
        switch("$N.nextInt($L)", jj_random, re.choices.size) {
            for ((index, c) in re.choices.withIndex()) {
                case(L, index) {
                    implement(c as RegularExpression)
                    `break`()
                }
            }
        }
    }

    private fun MethodSpec.Builder.implementLoop(re: RegularExpression, min: Int, max: Int) {
        nameAllocator.allocate("jj_cnt") { jj_cnt ->
            `for`("int $1N = $2L + $3N.nextInt($4L); $1N > 0; $1N--", jj_cnt, min, jj_random, max) {
                implement(re)
            }
        }
    }

    private fun MethodSpec.Builder.implement(re: RCharacterList) {
        if (re.negated_list) {
            comment("negated")
        }
        if (re.descriptors.size == 1) {
            implementCharacter(re.descriptors.first())
        } else {
            switch("$N.nextInt($L)", jj_random, re.descriptors.size) {
                for ((index, descriptor) in re.descriptors.withIndex()) {
                    case(L, index) {
                        implementCharacter(descriptor)
                        `break`()
                    }
                }
            }
        }
    }

    private fun MethodSpec.Builder.implementCharacter(descriptor: Any?) {
        when (descriptor) {
            is SingleCharacter ->
                addStatement(
                    "$N.append('$L')",
                    jjimage,
                    descriptor.ch.characterLiteralWithoutSingleQuotes()
                )
            is CharacterRange ->
                addStatement(
                    "$1N.append(/* $2L..$3L */ (char) ('$2L' + $4N.nextInt($5L)))",
                    jjimage,
                    descriptor.left.characterLiteralWithoutSingleQuotes(),
                    descriptor.right.characterLiteralWithoutSingleQuotes(),
                    jj_random,
                    descriptor.right - descriptor.left + 1
                )
            else -> TODO(descriptor.toString())
        }
    }

    /**
     * See [https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6]
     */
    fun Char.characterLiteralWithoutSingleQuotes(): String = when (this) {
        '\b' -> "\\b" /* \u0008: backspace (BS) */
        '\t' -> "\\t" /* \u0009: horizontal tab (HT) */
        '\n' -> "\\n" /* \u000a: linefeed (LF) */
        '\r' -> "\\r" /* \u000d: carriage return (CR) */
        '\"' -> "\"" /* \u0022: double quote (") */
        '\'' -> "\\'" /* \u0027: single quote (') */
        '\\' -> "\\\\" /* \u005c: backslash (\) */
        else -> if (Character.isISOControl(this)) String
            .format("\\u%04x", toInt()) else this.toString()
    }

    override fun call(): Boolean {
        val fuzzyLexerFile = javaFile(config.packageName) {
            `class`(fuzzLexerClassName) {
                jj_random = `protected field`(Random::class, "jj_random")
                field(Int::class, "curLexState")
                field(Int::class, "defaultLexState")
                field(Int::class, "jjnewStateCnt")
                field(Int::class, "jjround")
                field(Int::class, "jjmatchedPos")
                jjmatchedKind = field(Int::class, "jjmatchedKind")
                jjimage = `private final field`(StringBuilder::class, "jjimage") { `=`("new $T()", StringBuilder::class.java) }
                image = `private final field`(StringBuilder::class, "image") { `=`(N, jjimage) }
                curTokenImage = `private field`(String::class, "curTokenImage")
                `private field`(Int::class, "jjimageLen");
                `private field`(Int::class, "lengthOfMatch");
                `protected field`(Int::class, "curChar");

                constructor(param(Random::class, "random")) {
                    addStatement("this.$N = random", jj_random)
                }

                val fillToken = private(config.tokenTypeName, fillTokenMethodName, param(config.tokenKindTypeName, "kind")) {
                    code {
                        val tokenType = config.tokenTypeName.T
                        """
                        ${jjmatchedKind.N} = kind;
                        ${curTokenImage.N} = ${jjimage.N}.toString();
                        $tokenType t = $tokenType.newToken(${jjmatchedKind.N}, ${curTokenImage.N});

                        """.trimIndent()
                    }
                    comment("TODO: fill position")
                    `return`("t")
                }

                val fillTokenLiteral = private(config.tokenTypeName, fillTokenMethodName,
                    param(config.tokenKindTypeName, "kind"), param(String::class, "image")) {
                    code {
                        """
                        ${jjimage.N}.setLength(0);
                        ${jjimage.N}.append(image);
                        return ${fillToken.N}(kind);
                        """.trimIndent()
                    }
                }

                val generatorNames = mutableMapOf<RegularExpression, String>()
                for (tp in JavaCCGlobals.rexprlist) {
                    for (regexpSpec in tp.respecs) {
                        val r = regexpSpec.rexp.unwrap
                        if (r is RStringLiteral) {
                            // String literals do not need their own functions
                            generatorNames[r] = r.image
                            continue
                        }
                        val label = r.label?.capitalize() ?: ""
                        val gen = public(config.tokenTypeName, "${generateMethodName}_${label}_${r.ordinal}") {
                            addStatement("$N.setLength(0)", jjimage)
                            implement(r)
                            `return`("$N($L)", fillToken, r.ordinal)
                        }
                        generatorNames[r] = gen.name
                    }
                }

                public(config.tokenTypeName, generateMethodName, param(config.tokenKindTypeName, "kind")) {
                    switch("kind") {
                        for((re , generator) in generatorNames) {
                            case(L, re.ordinal) {
                                val r = re.unwrap
                                `return` {
                                    if (r is RStringLiteral) {
                                        "${fillTokenLiteral.N}(kind, ${r.image.S})"
                                    } else {
                                        "${generator.N}()"
                                    }
                                }
                            }
                        }
                        default {
                            `throw new`(IllegalArgumentException::class, "\"Unexpected token kind \" + kind")
                        }
                    }
                }
            }
        }
        outputFiles += fuzzyLexerFile.toJavaFileObject()
        return true
    }

}
