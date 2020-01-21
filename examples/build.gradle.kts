val javaccJar by configurations.creating

dependencies {
    javaccJar(project(":"))
}

// Print info logging from Ant by default
ant.lifecycleLogLevel = AntBuilder.AntMessagePriority.INFO

// Note: this is intentionally different from :test:prepareForAntTests
// It avoids conflict when both tasks would try to overwrite the same file
val prepareForAntTests by tasks.registering(Copy::class) {
    into("$buildDir/libs")
    from(javaccJar) {
        rename { "javacc.jar" }
    }
}

val antTest by tasks.registering {
    group = "Tests: Ant-based"
    description = "Execute all Ant-based tests"
    dependsOn(prepareForAntTests)
    doFirst {
        ant.withGroovyBuilder {
            "ant"("antfile" to file("build.xml"))
        }
    }
}

val test by tasks.registering {
    dependsOn(antTest)
}

tasks.check {
    dependsOn(test)
}
