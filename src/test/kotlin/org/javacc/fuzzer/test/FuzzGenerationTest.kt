package org.javacc.fuzzer.test

import com.grosner.kpoet.codeBlock
import com.grosner.kpoet.typeName
import com.squareup.javapoet.FieldSpec
import org.javacc.parser.Main
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Paths

class FuzzGenerationTest {

@Test
internal fun name() {
    val i = FieldSpec.builder(Int::class.typeName, "i").build()
    val min = FieldSpec.builder(Int::class.typeName, "min").build()
    val max = FieldSpec.builder(Int::class.typeName, "max").build()

    val result = codeBlock {
        """
        ${i.T} ${i.N} = 42;
        if (${i.N} < ${min.N}) {
            ${i.N} = ${min.N}
        }
        """.trimIndent()
    }

val hello = """Hello, "world", \n test"""
val j = FieldSpec.builder(Int::class.typeName, "j").build()
val codeBlock = codeBlock { "System.out.println(${hello.S} + ${j.N})" }

println(codeBlock.toString())
}

    @Test
    internal fun simpleFuzzer() {
        generateParser("SimpleFuzzer")
    }

//    @Test
//    internal fun javaCC() {
//        generateParser("javacc", File("src/main/javacc/JavaCC.jj"))
//    }

    private fun generateParser(
        parserName: String,
        file: File? = null
    ) {
        val location = file ?: (javaClass.getResource("$parserName.jj")
            ?: throw IllegalArgumentException("$parserName does not exist")).asFile!!

        val workDir = File("build/tests/$parserName")
        // Clean output directories (e.g. in case they contain previous test outputs)
        if (workDir.exists()) {
            workDir.deleteRecursively()
        }

        val javaDir = File(workDir, "java").apply { mkdirs() }
        // val classesDir = File(workDir, "classes").apply { mkdirs() }
        Main.mainProgram(
            arrayOf(
                "-STATIC=false",
                "-OUTPUT_DIRECTORY:$javaDir",
                location.absolutePath
            )
        )
    }

    private val URL.asFile
        get(): File? {
            if (protocol != "file") {
                return null
            }
            val uri = try {
                toURI()
            } catch (e: URISyntaxException) {
                throw IllegalArgumentException("Unable to convert URL $this to URI", e)
            }
            return if (uri.isOpaque) { // It is like file:test%20file.c++
                // getSchemeSpecificPart would return "test file.c++"
                File(uri.schemeSpecificPart)
            } else {
                // See https://stackoverflow.com/a/17870390/1261287
                Paths.get(uri).toFile()
            }
        }
}
