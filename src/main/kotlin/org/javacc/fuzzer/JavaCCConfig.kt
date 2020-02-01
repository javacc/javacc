package org.javacc.fuzzer

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
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
    val parserConstantsClassName = ClassName.get(packageName, parserClassName + "Constants")
    val parseExceptionTypeName = ClassName.get(packageName, "ParseException")
    val tokenKindTypeName = TypeName.get(Int::class.java)
}
