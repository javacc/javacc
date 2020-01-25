package org.javacc.test.simpleFuzzer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleFuzzerParserTest {
    @Test
    internal fun helloWorld() {
        val input = "hello world"
        val ids = SimpleFuzzer(input.reader()).Ids()
        assertEquals("[hello, world]", ids.toString()) { "input: $input" }
    }
}
