package com.grosner.kpoet

import com.squareup.javapoet.TypeName
import org.jetbrains.annotations.Nullable
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals

class ParameterExtensionsTest : Spek({
    describe("tests parameter extension methods and sample their features") {
        on("generate java file") {
            val file = javaFile("com.grosner") {
                `class`("Destroyer") {
                    `public`(TypeName.VOID, "welcomeOverlords",
                            param(`@`(Nullable::class, { this["value"] = "".S }), String::class, "robot"),
                            `final param`(`@`(Nullable::class), String::class, "android"),
                            param(`@`(TestAnnotation::class, {
                                this["name"] = "Some Kind of Member".S
                                this["purpose"] = "Some Purpose we have".S
                            }), Int::class, "purposeFul"))
                }
            }

            file.writeTo(System.out)

            it("should produce correct output") {
                assertEquals("package com.grosner;\n\n" +

                        "import com.grosner.kpoet.TestAnnotation;\n" +
                        "import java.lang.String;\n" +
                        "import org.jetbrains.annotations.Nullable;\n\n" +

                        "class Destroyer {\n" +
                        "  public void welcomeOverlords(@Nullable(\"\") String robot, @Nullable final String android," +
                        "\n      @TestAnnotation(name = \"Some Kind of Member\", " +
                        "purpose = \"Some Purpose we have\") int purposeFul) {\n" +
                        "  }\n" +
                        "}\n", file.toString())
            }
        }

    }
})

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class TestAnnotation(val name: String = "", val purpose: Int = 0)
