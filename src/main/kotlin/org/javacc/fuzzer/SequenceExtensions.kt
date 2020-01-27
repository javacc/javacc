package org.javacc.fuzzer

import org.javacc.parser.Lookahead
import org.javacc.parser.Sequence

val Sequence.lookahead: Lookahead get() = units.first() as Lookahead
