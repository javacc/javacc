package org.javacc.test.idlist

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class IdListTest {
    @ParameterizedTest
    @ValueSource(strings = ["hello", "hello world"])
    internal fun validInputs(value: String) {
        IdList(value.reader()).Input()
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    internal fun throwsParseException(value: String) {
        assertThrows<ParseException>({ -> "IdList($value).Input()" }) {
            IdList(value.reader()).Input()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "3sdf", "hello #hello"])
    internal fun throwsTokenMgrError(value: String) {
        assertThrows<TokenMgrError>({ -> "IdList($value).Input()" }) {
            IdList(value.reader()).Input()
        }
    }
}
