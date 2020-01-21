val javaccJar by configurations.creating

dependencies {
    javaccJar(project(":"))
}

val prepareForAntTests by tasks.registering(Copy::class) {
    into("$rootDir/target")
    from(javaccJar) {
        rename { "javacc.jar" }
    }
}

// Print info logging from Ant by default
ant.lifecycleLogLevel = AntBuilder.AntMessagePriority.INFO

val antTest by tasks.registering {
    group = "Tests: Ant-based"
    description = "Execute all Ant-based tests"
}

val test by tasks.registering {
    dependsOn(antTest)
}

tasks.check {
    dependsOn(test)
}

val antTestOther by tasks.registering {
    dependsOn(prepareForAntTests)

    doFirst {
        ant.withGroovyBuilder {
            "ant"(
                "antfile" to file("build.xml"),
                "dir" to projectDir,
                "target" to "tests-without-build-xml-files"
            )
        }
    }
}

antTest {
    dependsOn(antTestOther)
}

// Below creates antTest$name Gradle task for each Ant-based test
projectDir.listFiles { f: File -> f.isDirectory }
    ?.filter { File(it, "build.xml").exists() }
    ?.forEach { dir ->
        val task = tasks.register("antTest" + dir.name.capitalize()) {
            group = "Tests: Ant-based"
            description = "Execute ${dir.name}"
            dependsOn(prepareForAntTests)
            // This is equivalent to <ant antfile="..." dir="..">
            onlyIf {
                val buildXml = File(dir, "build.xml")
                val javaVersion = JavaVersion.current()
                if (javaVersion <= JavaVersion.VERSION_1_8 ||
                    !buildXml.readText().contains("""source="1.5""")) {
                    true
                } else {
                    println("Build file $buildXml uses source=1.5 which is not compatible with " +
                            "the current Java version $javaVersion. The test will be skipped")
                    false
                }
            }
            doFirst {
                ant.withGroovyBuilder {
                    "ant"(
                        "antfile" to File(dir, "build.xml"),
                        "dir" to dir,
                        "target" to "clean"
                    )
                    // Execute the default target
                    "ant"(
                        "antfile" to File(dir, "build.xml"),
                        "dir" to dir
                    ) {
                        "property"(
                            "name" to "javacc",
                            "value" to prepareForAntTests.get().destinationDir.resolve("javacc.jar")
                        )
                    }
                }
            }
        }
        antTest {
            dependsOn(task)
        }
    }
