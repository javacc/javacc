package org.javacc.fuzzer

import org.javacc.parser.NormalProduction

class JavaCCConfig(
    val packageName: String,
    val parserClassName: String,
    val bnfproductions: List<NormalProduction>
)
