package com.grosner.kpoet

import com.squareup.javapoet.TypeName
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals

class MethodExtensionsTest : Spek({
    describe("method extensions") {
        on("can create switch statements") {

            it("generates switch break") {
                val method = `public`(TypeName.VOID, "handleAction", param(String::class, "action")) {
                    switch("action") {
                        case("bonus".S) {
                            // str -> "\$S", "bonus"
                            statement("this.name = ${"BONUS".S}")
                            `break`()
                        }
                        default {
                            statement("this.name = ${"NO BONUS".S}")
                            `break`()
                        }
                    }
                }

                println(method.toString())
                assertEquals("public void handleAction(java.lang.String action) {\n" +
                        "  switch (action) {\n" +
                        "    case \"bonus\": {\n" +
                        "      this.name = \"BONUS\";\n" +
                        "      break;\n" +
                        "    }\n" +
                        "    default: {\n" +
                        "      this.name = \"NO BONUS\";\n" +
                        "      break;\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n", method.toString())
            }

            it("generates switch return") {
                val method = `public`(TypeName.VOID, "handleAction",
                        param(String::class, "action")) {
                    switch("action") {
                        case("bonus".S) {
                            `return`("BONUS".S)
                        }
                        default {
                            `return`("NO BONUS".S)
                        }
                    }
                }

                println(method.toString())
                assertEquals("public void handleAction(java.lang.String action) {\n" +
                        "  switch (action) {\n" +
                        "    case \"bonus\": {\n" +
                        "      return \"BONUS\";\n" +
                        "    }\n" +
                        "    default: {\n" +
                        "      return \"NO BONUS\";\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n", method.toString())
            }
        }

        on("print for loops") {
            val method = `public`(TypeName.VOID, "forLoop") {
                statement("\$T j = 0", TypeName.INT)
                `for`("\$T i = 0; i < size; i++", TypeName.INT) {
                    `if`("i > 0") {
                        `continue`()
                    }.`else if`("i < 0") {
                        statement("j++")
                    }.end()
                }
            }

            println(method.toString())
            assertEquals("" +
                    "public void forLoop() {\n" +
                    "  int j = 0;\n" +
                    "  for (int i = 0; i < size; i++) {\n" +
                    "    if (i > 0) {\n" +
                    "      continue;\n" +
                    "    } else if (i < 0) {\n" +
                    "      j++;\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n", method.toString())
        }

        on("do while loops") {
            val method = `private`(TypeName.VOID, "doWhile") {
                `do` {
                    statement("i++")
                    `if`("i == 5") {
                        `break`()
                    }.end()
                }.`while`("i < 0")
            }
            println(method.toString())
            assertEquals("private void doWhile() {\n" +
                    "  do {\n" +
                    "    i++;\n" +
                    "    if (i == 5) {\n" +
                    "      break;\n" +
                    "    }\n" +
                    "  } while (i < 0);\n" +
                    "}\n", method.toString())
        }
    }
})