package org.javacc.fuzzer

import com.squareup.javapoet.ClassName
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

val Token.asString: String get() = StringBuilder().appendWithComments(this).toString()

val String.T: TypeName get() = ClassName.get("", this)

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

/**
 * See [org.javacc.parser.CodeGenerator.getStringForTokenOnly]
 */
fun Appendable.appendSingle(t: Token): Appendable {
    if (t.kind == JavaCCParserConstants.STRING_LITERAL ||
        t.kind == JavaCCParserConstants.CHARACTER_LITERAL
    ) {
        for (ch in t.image) {
            if (ch < 0x20.toChar() || ch > 0x7e.toChar()) {
                append("\\u")
                append(Integer.toHexString(ch.toInt()).padStart(4, '0'))
            } else {
                append(ch)
            }
        }
    } else {
        append(t.image)
    }
    return this
}

fun Appendable.appendSingleWithComments(t: Token): Appendable {
    var comment = t.firstComment
    while (comment != null) {
        appendSingle(comment)
        comment = comment.next
    }
    appendSingle(t)
    return this
}

fun Appendable.appendWithComments(t: Token): Appendable {
    var next: Token? = t
    while (next != null) {
        appendSingle(next)
        next = next.next
    }
    return this
}
