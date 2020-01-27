package org.javacc.fuzzer

import org.javacc.parser.JavaCCParserConstants
import org.javacc.parser.Token

class CodeEmitter {
    private val sb = StringBuilder()
    private var line: Int = 0
    private var pos: Int = 0
    var addWhitespace: Boolean = false

    private fun addWhitespace(t: Token) {
        if (!addWhitespace) {
            addWhitespace = true
        } else {
            while (line < t.beginLine) {
                sb.append('\n')
                line += 1
                pos = 1
            }
            while (pos < t.beginColumn) {
                sb.append(" ")
                pos += 1
            }
        }
    }

    /**
     * See [org.javacc.parser.CodeGenerator.getStringForTokenOnly]
     */
    fun appendSingle(t: Token) = apply {
        addWhitespace(t)

        if (t.kind != JavaCCParserConstants.STRING_LITERAL &&
            t.kind != JavaCCParserConstants.CHARACTER_LITERAL) {
            sb.append(t.image)
        } else {
            for (ch in t.image) {
                if (ch < 0x20.toChar() || ch > 0x7e.toChar()) {
                    sb.append("\\u")
                    sb.append(Integer.toHexString(ch.toInt()).padStart(4, '0'))
                } else {
                    sb.append(ch)
                }
            }
        }
        line = t.endLine
        pos = t.endColumn + 1
        if (t.image.endsWith('\n') || t.image.endsWith('\r')) {
            line += 1
            pos = 1
        }
    }

    fun appendSingleWithComments(t: Token) = apply {
        var comment = t.firstComment
        while (comment != null) {
            appendSingle(comment)
            comment = comment.next
        }
        appendSingle(t)
    }

    fun appendWithComments(t: Token) = apply {
        var next: Token? = t
        while (next != null) {
            appendSingleWithComments(next)
            next = next.next
        }
    }

    override fun toString() = sb.toString()
}
