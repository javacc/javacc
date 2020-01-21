package org.javacc.builttools.bootstrap

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

open class JavaCCPlugin : Plugin<Project> {
    companion object {
        const val JAVACC_CLASSPATH_CONFIGURATION_NAME = "javaccBoostrapClaspath"
        const val GENERATE_SOURCES_TASK_NAME = "generateSources"
    }

    override fun apply(target: Project) {
        target.configureJavaCC()
    }

    fun Project.configureJavaCC() {
        configurations.create(JAVACC_CLASSPATH_CONFIGURATION_NAME) {
            isCanBeConsumed = false
        }.defaultDependencies {
            // TODO: use previous version here?
            // add(dependencies.create("net.java.dev.javacc:javacc:4.0"))
            add(dependencies.create(files("$rootDir/bootstrap/javacc.jar")))
        }

        tasks.register(GENERATE_SOURCES_TASK_NAME) {
            description = "Generates sources (e.g. JavaCC)"
            dependsOn(tasks.withType<JavaCCTask>())
        }
    }
}
