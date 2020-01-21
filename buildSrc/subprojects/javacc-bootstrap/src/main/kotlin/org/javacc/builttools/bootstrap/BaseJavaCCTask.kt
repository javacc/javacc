package org.javacc.builttools.bootstrap

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import java.io.File
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.process.JavaExecSpec

abstract class BaseJavaCCTask(
    folderName: String,
    objectFactory: ObjectFactory
) : DefaultTask() {
    @Classpath
    val javaCCClasspath = objectFactory.property<Configuration>()
        .convention(project.configurations.named(JavaCCPlugin.JAVACC_CLASSPATH_CONFIGURATION_NAME))

    @InputFile
    val inputFile = objectFactory.property<File>()

    @OutputDirectory
    val output = objectFactory.directoryProperty()
        .convention(project.layout.buildDirectory.dir("$folderName/$name"))

    @Internal
    val allGeneratedDirectory = objectFactory.directoryProperty()
        .convention(project.layout.buildDirectory.dir("$folderName/$name-all-generated"))

    @Input
    val sourceSetName = objectFactory.property<String>()
        .convention("main")

    @Input
    val packageName = objectFactory.property<String>()

    abstract fun JavaExecSpec.configureJava()

    @TaskAction
    fun run() {
        // Remove existing files in the output directories
        project.delete(output.asFileTree)
        project.delete(allGeneratedDirectory.asFileTree)
        // Generate sources
        project.javaexec {
            classpath = javaCCClasspath.get()
            configureJava()
        }
        // Copy files to the output directory except those that already exist in src/main/java
        // It enables to "override" the generated files
        // Note: the full set of generated files is still kept in -all-generated folder.
        val sourceSets: SourceSetContainer by project
        val srcDirs = sourceSets[sourceSetName.get()].allJava.srcDirs
        project.copy {
            into(output)
            from(allGeneratedDirectory) {
                include {
                    it.isDirectory || !srcDirs.any { srcDir ->
                        it.relativePath.getFile(srcDir).isFile
                    }
                }
            }
        }
    }
}
