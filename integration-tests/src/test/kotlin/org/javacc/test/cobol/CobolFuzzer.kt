package org.javacc.test.cobol

import org.javacc.test.plsql.PlSqlFuzzyParser
import org.junit.jupiter.api.Test
import java.util.*
import java.util.function.Consumer

class CobolFuzzer {
    @Test
    internal fun samples() {
        val r = Random()
        r.setSeed(42L)
        val fuzzer = CobolParserFuzzyParser(r)
        val res = mutableListOf<String>()
        fuzzer.tokenOutput = Consumer<Token> { res.add(it.image) }
        try {
            fuzzer.CompilationUnit()
        } finally {
            println(res.joinToString(" "))
        }
    }
}
