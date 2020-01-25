import com.github.vlsi.gradle.properties.dsl.props
import org.javacc.builttools.bootstrap.BaseJavaCCTask
import org.javacc.builttools.bootstrap.JavaCCTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `javacc-bootstrap`
    id("com.github.vlsi.ide")
}

// integration-tests sources use JavaCC generated in the current build
// If the build is broken (e.g. a core file does not work), then build import to the IDE
// would fail because IDE would try to generate
val skipGenerateSourcesOnIdeImport by props()

val currentJavaCC by configurations.creating

dependencies {
    currentJavaCC(project(":"))
}

val generateTestSources by tasks.registering {
    if (!skipGenerateSourcesOnIdeImport) {
        dependsOn(tasks.withType<BaseJavaCCTask>())
    }
}

tasks.named("compileTestKotlin") {
    dependsOn(generateTestSources)
}

ide {
    // Note: below assumes only test code would use javacc-generated parsers
    tasks.withType<BaseJavaCCTask> {
        // This adds generateTestSources to the IDE "after sync" list of tasks
        // The problem with adding individual tasks is there's no clear way to remove
        // the IDE configuration if the task no longer exists (e.g. test/javacc/... was removed)
        // So we use a dummy `generateTestSources` task, which executes all the rest.
        // See https://github.com/JetBrains/gradle-idea-ext-plugin/issues/98
        generatedJavaSources(generateTestSources, output.get().asFile, sourceSets.named("test"))
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = "unused"
    targetCompatibility = "unused"
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

file("src/test/javacc").listFiles { f: File -> f.isDirectory }?.forEach { dir ->
    val parserFile = dir.listFiles { _, name -> name.endsWith(".jj") }?.firstOrNull()
    if (parserFile == null) {
        logger.warn("No parser (*.jj) found in $dir")
        return@forEach
    }
    tasks.register<JavaCCTask>("testGenerate${dir.name}") {
        sourceSetName.set("test")
        javaCCClasspath.set(currentJavaCC)
        inputFile.set(parserFile)
        packageName.set("org.javacc.test.${dir.name}")
    }
}
