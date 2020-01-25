import com.github.vlsi.gradle.properties.dsl.props
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.javacc.builttools.bootstrap.BaseJavaCCTask
import org.javacc.builttools.bootstrap.JJTreeTask
import org.javacc.builttools.bootstrap.JavaCCTask

plugins {
    `java-library`
    `javacc-bootstrap`
    id("com.github.autostyle")
    id("com.gradle.plugin-publish") apply false
    id("com.github.vlsi.gradle-extensions")
    id("com.github.vlsi.ide")
    id("com.github.vlsi.stage-vote-release")
    kotlin("jvm") apply false
}

val String.v: String get() = rootProject.extra["$this.version"] as String

val buildVersion = "javacc".v + releaseParams.snapshotSuffix

description = "JavaCC is a parser/scanner generator for java"

println("Building JavaCC $buildVersion")

val enableGradleMetadata by props()
val skipJavadoc by props()
val skipAutostyle by props()
val slowSuiteLogThreshold by props(0L)
val slowTestLogThreshold by props(2000L)
val enablePmd by props()

releaseParams {
    tlp.set("javacc")
    organizationName.set("javacc")
    componentName.set("javacc")
    prefixForProperties.set("gh")
    svnDistEnabled.set(false)
    sitePreviewEnabled.set(false)
    nexus {
        mavenCentral()
    }
    voteText.set {
        """
        ${it.componentName} v${it.version}-rc${it.rc} is ready for preview.

        Git SHA: ${it.gitSha}
        Staging repository: ${it.nexusRepositoryUri}
        """.trimIndent()
    }
}

allprojects {
    group = "net.java.dev.javacc"
    version = buildVersion

    repositories {
        mavenCentral()
    }

    val javaMainUsed = file("src/main/java").isDirectory
    val javaTestUsed = file("src/test/java").isDirectory
    val javaUsed = javaMainUsed || javaTestUsed
    if (javaUsed) {
        apply(plugin = "java-library")
        dependencies {
            val compileOnly by configurations
            compileOnly("net.jcip:jcip-annotations:1.0")
            compileOnly("com.github.spotbugs:spotbugs-annotations:3.1.6")
            compileOnly("com.google.code.findbugs:jsr305:3.0.2")
        }
    }

    val kotlinMainUsed = file("src/main/kotlin").isDirectory
    val kotlinTestUsed = file("src/test/kotlin").isDirectory
    val kotlinUsed = kotlinMainUsed || kotlinTestUsed
    if (kotlinUsed) {
        apply(plugin = "java-library")
        apply(plugin = "org.jetbrains.kotlin.jvm")
        dependencies {
            add(if (kotlinMainUsed) "implementation" else "testImplementation", kotlin("stdlib"))
        }
    }

    if (javaUsed || kotlinUsed) {
        dependencies {
            val configurationName = if (javaMainUsed || kotlinMainUsed) {
                "implementation"
            } else {
                "testImplementation"
            }
            configurationName(platform(project(":javacc-dependencies-bom")))
        }
    }

    val hasTests = javaTestUsed || kotlinTestUsed
    if (hasTests) {
        // Add default tests dependencies
        dependencies {
            val testImplementation by configurations
            val testRuntimeOnly by configurations
            if (project.props.bool("junit3", default = true)) {
                // JUnit3 tests are still there
                testImplementation("junit:junit")
                testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
            }
            if (project.props.bool("junit5", default = true)) {
                // Enable to write tests with JUnit5 API
                testImplementation("org.junit.jupiter:junit-jupiter-api")
                testImplementation("org.junit.jupiter:junit-jupiter-params")
            }
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        // Ensure builds are reproducible
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        dirMode = "775".toInt(8)
        fileMode = "664".toInt(8)
    }

    if (!skipAutostyle) {
        apply(plugin = "com.github.autostyle")
        autostyle {
            kotlinGradle {
                ktlint()
            }
            format("configs") {
                filter {
                    include("**/*.sh", "**/*.bsh", "**/*.cmd", "**/*.bat")
                    include("**/*.properties", "**/*.yml")
                    include("**/*.xsd", "**/*.xsl", "**/*.xml")
                    // Autostyle does not support gitignore yet https://github.com/autostyle/autostyle/issues/13
                    exclude("out/**")
                    exclude("target/*")
                    // TODO: remove this when .gitattributes are merged to master
                    exclude("scripts/*.bat")
                    if (project == rootProject) {
                        exclude("gradlew*")
                    }
                }
                endWithNewline()
            }
            format("markdown") {
                filter.include("**/*.md")
                endWithNewline()
            }
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        if (!skipAutostyle) {
            autostyle {
                kotlin {}
            }
        }
    }

    plugins.withType<JavaPlugin> {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_1_7
            targetCompatibility = JavaVersion.VERSION_1_7
            withSourcesJar()
            if (!skipJavadoc) {
                withJavadocJar()
            }
        }

        apply(plugin = "maven-publish")

        if (enablePmd) {
            // It is disabled by default. It takes a while, and there are violations
            apply(plugin = "pmd")
            configure<PmdExtension> {
                ruleSetConfig = resources.text.fromFile("$rootDir/rulesets/java/maven-pmd-plugin-default.xml", "utf-8")
            }
        }

        if (!enableGradleMetadata) {
            tasks.withType<GenerateModuleMetadata> {
                enabled = false
            }
        }

        if (!skipAutostyle) {
            autostyle {
                java {
                    // TODO: a blank line between comments looks nice, however there are violations for now
                    // replaceRegex("side by side comments", "(\n\\s*+[*]*+/\n)(/[/*])", "\$1\n\$2")
                    // TODO: activate
                    // importOrder(
                    //     "static ",
                    //     "java.",
                    //     "org.javacc.",
                    //     ""
                    // )
                    // removeUnusedImports()
                    // TODO: there are lots of violations :(
                    // indentWithSpaces(2)
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }

        tasks {
            withType<JavaCompile>().configureEach {
                options.encoding = "UTF-8"
            }

            withType<Jar>().configureEach {
                manifest {
                    attributes["Bundle-License"] = "BSD-3-Clause"
                    attributes["Implementation-Title"] = "JavaCC"
                    attributes["Implementation-Version"] = project.version
                    attributes["Specification-Vendor"] = "JavaCC"
                    attributes["Specification-Version"] = project.version
                    attributes["Specification-Title"] = "JavaCC"
                    attributes["Implementation-Vendor"] = "JavaCC"
                    attributes["Implementation-Vendor-Id"] = "edu.berkeley.cs.jqf"
                }
            }
            withType<Javadoc>().configureEach {
                (options as StandardJavadocDocletOptions).apply {
                    noTimestamp.value = true
                    showFromProtected()
                    locale = "en"
                    docEncoding = "UTF-8"
                    charSet = "UTF-8"
                    encoding = "UTF-8"
                    docTitle = "JavaCC ${project.name} API"
                    windowTitle = "JavaCC ${project.name} API"
                    header = "<b>JavaCC</b>"
                    addBooleanOption("Xdoclint:none", true)
                    addStringOption("source", "7")
                    // TODO: compute lastEditYear
                    bottom =
                        "Copyright © 2006-???? Sun Microsystems, Inc, ????-2020 JavaCC development group"
                    if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
                        addBooleanOption("html5", true)
                        links("https://docs.oracle.com/javase/9/docs/api/")
                    } else {
                        links("https://docs.oracle.com/javase/7/docs/api/")
                    }
                }
            }
            withType<Test>().configureEach {
                useJUnitPlatform()
                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    showStandardStreams = true
                }
                // Pass the property to tests
                fun passProperty(name: String, default: String? = null) {
                    val value = System.getProperty(name) ?: default
                    value?.let { systemProperty(name, it) }
                }
                passProperty("junit.jupiter.execution.parallel.enabled", "true")
                passProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
                passProperty("junit.jupiter.execution.timeout.default", "5 m")
                // https://github.com/junit-team/junit5/issues/2041
                // Gradle does not print parameterized test names yet :(
                // Hopefully it will be fixed in Gradle 6.1
                fun String?.withDisplayName(displayName: String?, separator: String = ", "): String? = when {
                    displayName == null -> this
                    this == null -> displayName
                    endsWith(displayName) -> this
                    else -> "$this$separator$displayName"
                }
                fun printResult(descriptor: TestDescriptor, result: TestResult) {
                    val test = descriptor as org.gradle.api.internal.tasks.testing.TestDescriptorInternal
                    val classDisplayName = test.className.withDisplayName(test.classDisplayName)
                    val testDisplayName = test.name.withDisplayName(test.displayName)
                    val duration = "%5.1fsec".format((result.endTime - result.startTime) / 1000f)
                    val displayName = classDisplayName.withDisplayName(testDisplayName, " > ")
                    // Hide SUCCESS from output log, so FAILURE/SKIPPED are easier to spot
                    val resultType = result.resultType
                        .takeUnless { it == TestResult.ResultType.SUCCESS }
                        ?.toString()
                        ?: (if (result.skippedTestCount > 0 || result.testCount == 0L) "WARNING" else "       ")
                    if (!descriptor.isComposite) {
                        println("$resultType $duration, $displayName")
                    } else {
                        val completed = result.testCount.toString().padStart(4)
                        val failed = result.failedTestCount.toString().padStart(3)
                        val skipped = result.skippedTestCount.toString().padStart(3)
                        println("$resultType $duration, $completed completed, $failed failed, $skipped skipped, $displayName")
                    }
                }
                afterTest(KotlinClosure2<TestDescriptor, TestResult, Any>({ descriptor, result ->
                    // There are lots of skipped tests, so it is not clear how to log them
                    // without making build logs too verbose
                    if (result.resultType == TestResult.ResultType.FAILURE ||
                        result.endTime - result.startTime >= slowTestLogThreshold) {
                        printResult(descriptor, result)
                    }
                }))
                afterSuite(KotlinClosure2<TestDescriptor, TestResult, Any>({ descriptor, result ->
                    if (descriptor.name.startsWith("Gradle Test Executor")) {
                        return@KotlinClosure2
                    }
                    if (result.resultType == TestResult.ResultType.FAILURE ||
                        result.endTime - result.startTime >= slowSuiteLogThreshold) {
                        printResult(descriptor, result)
                    }
                }))
            }
            configure<PublishingExtension> {
                // TODO: move actual code (e.g. src/main) to the approrpriate module, and use
                //       the root project only for configurational purposes
                // if (project.path == ":") {
                //    // Do not publish "root" project. Java plugin is applied here for DSL purposes only
                //    return@configure
                // }
                if (project.path.startsWith(":javacc-examples") ||
                    project.path.startsWith(":javacc-release") ||
                    project.path.startsWith(":javacc-test")) {
                    // We don't publish examples to Maven Central
                    return@configure
                }
                publications {
                    // Gradle plugin is not yet in JavaCC tree, but it would require
                    // a slightly different publication
                    if (project.path != ":javacc-plugin-gradle") {
                        create<MavenPublication>(project.name) {
                            artifactId = project.name
                            version = rootProject.version.toString()
                            description = project.description
                            from(project.components.get("java"))
                        }
                    }
                    withType<MavenPublication> {
                        // if (!skipJavadoc) {
                        // Eager task creation is required due to
                        // https://github.com/gradle/gradle/issues/6246
                        //  artifact(sourcesJar.get())
                        //  artifact(javadocJar.get())
                        // }

                        // Use the resolved versions in pom.xml
                        // Gradle might have different resolution rules, so we set the versions
                        // that were used in Gradle build/test.
                        versionMapping {
                            usage(Usage.JAVA_RUNTIME) {
                                fromResolutionResult()
                            }
                            usage(Usage.JAVA_API) {
                                fromResolutionOf("runtimeClasspath")
                            }
                        }
                        pom {
                            withXml {
                                val sb = asString()
                                var s = sb.toString()
                                // <scope>compile</scope> is Maven default, so delete it
                                s = s.replace("<scope>compile</scope>", "")
                                // Cut <dependencyManagement> because all dependencies have the resolved versions
                                s = s.replace(
                                    Regex(
                                        "<dependencyManagement>.*?</dependencyManagement>",
                                        RegexOption.DOT_MATCHES_ALL
                                    ),
                                    ""
                                )
                                sb.setLength(0)
                                sb.append(s)
                                // Re-format the XML
                                asNode()
                            }
                            name.set(
                                (project.findProperty("artifact.name") as? String)
                                    ?: "JavaCC ${project.name.capitalize()}"
                            )
                            description.set(
                                project.description
                                    ?: "JavaCC ${project.name.capitalize()}"
                            )
                            inceptionYear.set("1996")
                            url.set("https://github.com/javacc/javacc")
                            licenses {
                                license {
                                    name.set("BSD-3-Clause")
                                    url.set("https://raw.githubusercontent.com/javacc/javacc/master/LICENSE")
                                    comments.set("BSD-3-Clause, Copyright (c) 2006, Sun Microsystems, Inc")
                                    distribution.set("repo")
                                }
                            }
                            issueManagement {
                                system.set("GitHub")
                                url.set("https://github.com/javacc/javacc/issues")
                            }
                            scm {
                                connection.set("scm:git:https://github.com/javacc/javacc.git")
                                developerConnection.set("scm:git:https://github.com/javacc/javacc.git")
                                url.set("https://github.com/javacc/javacc")
                                tag.set("HEAD")
                            }
                            organization {
                                name.set("javacc.org")
                                url.set("https://javacc.github.io/javacc/")
                            }
                            mailingLists {
                                mailingList {
                                    name.set("Commits")
                                    archive.set("https://javacc.org/mailing-list-archive/commits@javacc.java.net/")
                                }
                                mailingList {
                                    name.set("Users")
                                    archive.set("https://javacc.org/mailing-list-archive/users@javacc.java.net/")
                                }
                                mailingList {
                                    name.set("Developers")
                                    archive.set("https://javacc.org/mailing-list-archive/dev@javacc.java.net/")
                                }
                                mailingList {
                                    name.set("Issues")
                                    archive.set("https://javacc.dev.java.net/servlets/SummarizeList?listName=issues")
                                }
                            }
                            developers {
                                developer {
                                    name.set("Sreenivasa Viswanadha")
                                    id.set("sreeni")
                                    email.set("support@javacc.org")
                                    roles.add("Owner")
                                    timezone.set("0")
                                    url.set("http://www.kampbell.net")
                                    organization.set("javacc.org")
                                    organizationUrl.set("https://javacc.org")
                                }
                                developer {
                                    name.set("Chris Ainsley")
                                    id.set("ainsleyc")
                                    email.set("ainsleyc At dev.java.net")
                                    roles.add("Developer")
                                    timezone.set("0")
                                    organization.set("java.net")
                                    organizationUrl.set("http://www.java.net/")
                                }
                                developer {
                                    name.set("Tim Pizey")
                                    id.set("timp")
                                    email.set("timp AT paneris.org")
                                    roles.addAll("Maven maven", "Developer")
                                    timezone.set("0")
                                    url.set("http://paneris.org/~timp")
                                    organization.set("Context Computing")
                                    organizationUrl.set("http://www.context-computing.co.uk/")
                                }
                                developer {
                                    name.set("Caroline Lemieux")
                                    id.set("zosrothko")
                                    email.set("zosrothko AT orange.fr")
                                    roles.add("Developer")
                                    timezone.set("0")
                                    url.set("http://www.kampbell.net")
                                    organization.set("Kampbell")
                                    organizationUrl.set("https://github.com/Kampbell/")
                                }
                            }
                            contributors {
                                // TODO: load from $rootDir/contributors?
                                for (contributorName in listOf(
                                        "Markus Brigl",
                                        "Martin Swanson",
                                        "Anton Rybochkin",
                                        "Jean-Bernard DAMIANO",
                                        "Dusan Malusev",
                                        "Dave Benson",
                                        "Eric Spishak-Thomas",
                                        "Roman Leventov",
                                        "Philip Helger",
                                        "Eitan Adler",
                                        "Anton Rybochkin",
                                        "Marc Mazas")) {
                                    contributor {
                                        name.set(contributorName)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Configuration of the root project itself
// Note: it would be better to move parser to its own subfolder

val javaCC by tasks.registering(JavaCCTask::class) {
    description = "Generate the Java CC Main Parser"
    inputFile.set(file("src/main/javacc/JavaCC.jj"))
    packageName.set("org.javacc.parser")
}

val jjTreeParserDefinition by tasks.registering(JJTreeTask::class) {
    description = "Generate the JJTree Parser Definition (from the tree definition)"
    inputFile.set(file("src/main/jjtree/JJTree.jjt"))
    packageName.set("org.javacc.jjtree")
}

val jjTreeParser by tasks.registering(JavaCCTask::class) {
    description = "Generate the JJTree Parser"
    inputFile.set(
        jjTreeParserDefinition
            .map { it.output.get().file("org/javacc/jjtree/JJTree.jj").asFile }
    )
    packageName.set("org.javacc.jjtree")
}

val conditionParser by tasks.registering(JavaCCTask::class) {
    description = "Generate the Condition Parser"
    inputFile.set(file("src/main/javacc/ConditionParser.jj"))
    packageName.set("org.javacc.utils")
}

ide {
    tasks.withType<BaseJavaCCTask> {
        generatedJavaSources(this, output.get().asFile)
    }
}
