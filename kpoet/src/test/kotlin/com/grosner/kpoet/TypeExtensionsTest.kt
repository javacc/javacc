package com.grosner.kpoet

import com.squareup.javapoet.TypeName
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.Serializable


/**
 * Description:
 */
@RunWith(JUnitPlatform::class)
class TypeExtensionsTest : Spek({
    describe("type extensions") {
        on("can create a class with a method that sums two numbers with if else branches") {
            val typeSpec = `class`("TestClass") {
                modifiers(public, final)

                `public`(String::class, "doGood") {
                    statement("\$T a = 1", TypeName.INT)
                    statement("\$T b = 2", TypeName.INT)
                    statement("\$T sum = a + b", TypeName.INT)

                    `if`("sum > 3") {
                        `return`("Large Sum".S)
                    }.`else if`("sum < 3") {
                        `return`("Small Sum".S)
                    } `else` {
                        `return`("Three is the Sum".S)
                    }
                }
            }

            it("should generate proper class file") {
                assertEquals("public final class TestClass {\n" +
                        "  public java.lang.String doGood() {\n" +
                        "    int a = 1;\n" +
                        "    int b = 2;\n" +
                        "    int sum = a + b;\n" +
                        "    if (sum > 3) {\n" +
                        "      return \"Large Sum\";\n" +
                        "    } else if (sum < 3) {\n" +
                        "      return \"Small Sum\";\n" +
                        "    } else {\n" +
                        "      return \"Three is the Sum\";\n" +
                        "    }\n" +
                        "  }\n", typeSpec.toString())
            }
        }

        on("can create a class with fields") {
            val isReady = "isReady"
            val typeSpec = `abstract class`("TestClass") {
                modifiers(public)
                field(TypeName.BOOLEAN, isReady) { `=`(false.L) }
                field(String::class, isReady) { `=`("SomeName".S) }

                constructor(param(TypeName.BOOLEAN, isReady)) {
                    statement("this.$isReady = $isReady")
                }
            }

            it("should generate proper class file") {
                assertEquals("public abstract class TestClass {\n" +
                        "  boolean isReady = false;\n\n" +
                        "  java.lang.String isReady = \"SomeName\";\n\n" +
                        "  TestClass(boolean isReady) {\n" +
                        "    this.isReady = isReady;\n" +
                        "  }\n" +
                        "}\n", typeSpec.toString())
            }
        }

        on("can create subclass with overridden methods") {
            val typeSpec = `class`("TestClass") {
                extends(parameterized<String>(List::class))
                implements(parameterized<String>(Comparable::class), Serializable::class.typeName)

                `public`(Int::class, "compareTo", param(String::class, "other")) {
                    annotation(Override::class)
                    `return`(0.L)
                }
            }

            it("should generate proper class file") {
                assertEquals("class TestClass extends java.util.List<java.lang.String> implements java.lang" +
                        ".Comparable<java.lang.String>, java.io.Serializable {\n" +
                        "  @java.lang.Override\n" +
                        "  public int compareTo(java.lang.String other) {\n" +
                        "    return 0;\n" +
                        "  }\n" +
                        "}\n", typeSpec.toString())
            }
        }

        on("can create enum class") {
            val typeSpec = `enum`("Roshambo") {
                modifiers(public)
                case("ROCK", "fist".S) {
                    `public`(String::class, "toString") {
                        `@`(Override::class)
                        `return`("avalanche!".S)
                    }
                }
                case("SCISSORS", "peace".S)
                case("PAPER", "flat".S)

                `private final field`(String::class, "handsign")

                `constructor`(param(String::class, "handsign")) {
                    statement("this.handsign = handsign")
                }
            }

            it("should generate proper class file") {
                println(typeSpec.toString())
                assertEquals(
                        "public enum Roshambo {\n" +
                                "  ROCK(\"fist\") {\n" +
                                "    @java.lang.Override\n" +
                                "    public java.lang.String toString() {\n" +
                                "      return \"avalanche!\";\n" +
                                "    }\n" +
                                "  },\n\n" +

                                "  SCISSORS(\"peace\"),\n\n" +

                                "  PAPER(\"flat\");\n\n" +

                                "  private final java.lang.String handsign;\n\n" +

                                "  Roshambo(java.lang.String handsign) {\n" +
                                "    this.handsign = handsign;\n" +
                                "  }\n" +
                                "}\n", typeSpec.toString())
            }
        }
    }
})


