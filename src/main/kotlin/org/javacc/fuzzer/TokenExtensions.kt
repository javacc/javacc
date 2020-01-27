package org.javacc.fuzzer

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import org.javacc.parser.JavaCCParserConstants
import org.javacc.parser.Token

/**
 * See [org.javacc.parser.CodeGenerator.getStringToPrint]
 */
val Token.firstComment: Token?
    get() {
        var comment = specialToken ?: return null
        while (comment.specialToken != null) {
            comment = comment.specialToken
        }
        return comment
    }

val Token.asString: String get() = CodeEmitter().appendWithComments(this).toString()

val String.T: TypeName get() = ClassName.get("", this)

val String.B: CodeBlock get() = CodeBlock.of("\$L", this)

operator fun Token.iterator() = object : Iterator<Token> {
    var token: Token? = this@iterator

    override fun hasNext(): Boolean = token != null

    override fun next(): Token {
        val res = token
        require(res != null)
        token = res.next
        return res
    }

}

fun Iterable<Token>.asString() = CodeEmitter().also { sb ->
    for(t in this@asString) {
        sb.appendSingleWithComments(t)
    }
}.toString()
