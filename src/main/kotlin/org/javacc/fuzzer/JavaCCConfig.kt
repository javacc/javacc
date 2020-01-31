package org.javacc.fuzzer

import com.squareup.javapoet.ClassName
import org.javacc.parser.NormalProduction
import org.javacc.parser.Token

class JavaCCConfig(
    val packageName: String,
    val parserClassName: String,
    val parserImports: List<List<Token>>,
    val parserStaticImports: List<List<Token>>,
    val bnfproductions: List<NormalProduction>
) {
    val tokenTypeName = ClassName.get(packageName, "Token")
}
