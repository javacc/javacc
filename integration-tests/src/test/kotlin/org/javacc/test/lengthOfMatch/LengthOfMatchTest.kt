package org.javacc.test.lengthOfMatch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class LengthOfMatchTest {
    companion object {
        @JvmStatic
        @Suppress("unused")
        fun inputs() = listOf(
            arguments("abcdef", "[a-z=6]"),
            arguments("PLAIN_STRING", "[PLAIN_STRING=12]"),
            arguments("A", "[A|BC=1]"),
            arguments("BC", "[A|BC=2]"),
            arguments("A BC", "[A|BC=1, space=1, A|BC=2]"),
            arguments(
                "PLAIN_STRING  abc BC",
                "[PLAIN_STRING=12, space=1, space=1, a-z=3, space=1, A|BC=2]"
            )
        )
    }

    @ParameterizedTest
    @MethodSource("inputs")
    internal fun assertActions(input: String, expected: String) {
        val parser = Parser(input.reader())
        while (parser.nextToken.kind != Parser.EOF) {
            // parse input
        }
        assertEquals(expected, parser.lexicalActions.toString()) { "input: $input" }
    }
}
