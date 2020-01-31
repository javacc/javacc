package org.javacc.test.simpleFuzzer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class SimpleFuzzerParserTest {
//    @Test
//    internal fun helloWorld() {
//        val input = "hello world"
//        val ids = SimpleFuzzer(input.reader()).Ids()
//        assertEquals("[hello, world]", ids.toString()) { "input: $input" }
//    }

    @Test
    internal fun generateSamples() {
        val r = Random()
        r.setSeed(42L)
        val fuzzer = SimpleFuzzerFuzzyParser(r)
        for (i in 1..10) {
            println(fuzzer.Select())
        }
    }
}
