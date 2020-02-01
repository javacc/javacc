package org.javacc.fuzzer

import com.grosner.kpoet.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import org.javacc.parser.*
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Callable
import javax.tools.JavaFileObject

class FuzzLexGenerator(
    private val inputFiles: List<JavaFileObject>,
    private val outputFiles: MutableList<JavaFileObject>,
    private val config: JavaCCConfig
) : Callable<Boolean> {
    private val fuzzLexerClassName = config.parserClassName + "FuzzyLexer"
    private val generatorNamesMutable = mutableMapOf<Int, String>()

    val typeName = ClassName.get(config.packageName, fuzzLexerClassName)!!
    val generateMethodName = "jj_generate"
    val generatorNames: Map<Int, String> = generatorNamesMutable

    private val fillTokenMethodName = "jj_fillToken"

    private lateinit var jj_random: FieldSpec
    private lateinit var jjmatchedKind: FieldSpec
    private lateinit var jjimage: FieldSpec
    private lateinit var image: FieldSpec
    private lateinit var curTokenImage: FieldSpec

    private fun MethodSpec.Builder.implement(re: RegularExpression) {
        var r = re
        while(r is RJustName) {
            r = r.regexpr
        }
        when(r) {
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
        `for`("int jj_cnt = $L + $N.nextInt($L); jj_cnt > 0; jj_cnt--", min, jj_random, max) {
            implement(re)
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
                addStatement("$N.append('$S')", jjimage, descriptor.ch.characterLiteralWithoutSingleQuotes())
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
                    addStatement("$N = kind", jjmatchedKind)
                    addStatement("$N = $N.toString()", curTokenImage, jjimage)
                    addStatement("$1T t = $1T.newToken($2N, $3N)", config.tokenTypeName, jjmatchedKind, curTokenImage)
                    comment("TODO: fill position")
                    `return`("t")
                }

                for (tp in JavaCCGlobals.rexprlist) {
                    for (regexpSpec in tp.respecs) {
                        val r = regexpSpec.rexp
                        val label = r.label?.capitalize() ?: ""
                        val gen = public(config.tokenTypeName, "${generateMethodName}_${label}_${r.ordinal}") {
                            addStatement("$N.setLength(0)", jjimage)
                            implement(r)
                            `return`("$N($L)", fillToken, r.ordinal)
                        }
                        generatorNamesMutable[r.ordinal] = gen.name
                    }
                }

                public(config.tokenTypeName, generateMethodName, param(config.tokenKindTypeName, "kind")) {
                    switch("kind") {
                        for((ordinal, generator) in generatorNames) {
                            case(L, ordinal) {
                                `return`("$N()", generator)
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
