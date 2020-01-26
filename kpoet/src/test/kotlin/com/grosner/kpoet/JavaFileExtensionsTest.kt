package com.grosner.kpoet

import com.squareup.javapoet.ClassName
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals
import java.util.*

class JavaFileExtensionsTest : Spek({
    describe("Java file extensions") {
        on("Can create standard java file with static imports") {
            val file = javaFile("com.grosner", {
                `import static`(Collections::class, "*")
                `import static`(ClassName.get(String::class.java), "*")
            }) {
                `class`("HelloWorld") {
                    this
                }
            }

            file.writeTo(System.out)

            it("should print proper code") {
                assertEquals(
                        "package com.grosner;\n\n" +
                        "import static java.lang.String.*;\n" +
                        "import static java.util.Collections.*;\n\n" +

                        "class HelloWorld {\n" +
                        "}\n",
                        file.toString())
            }
        }
    }

})