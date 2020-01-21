import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.git.FindGitAttributes
import com.github.vlsi.gradle.git.dsl.gitignore

plugins {
    `java-library`
    id("com.github.vlsi.stage-vote-release")
}

val javaccJar by configurations.creating {
    // Just the jar, no transitive dependencies
    isTransitive = false
}

dependencies {
    // Project that builds javacc.jar
    javaccJar(project(":"))
}

// This task scans the project for gitignore / gitattributes, and that is reused for building
// source/binary artifacts with the appropriate eol/executable file flags
val gitProps by tasks.registering(FindGitAttributes::class) {
    // Scanning for .gitignore and .gitattributes files in a task avoids doing that
    // when distribution build is not required (e.g. code is just compiled)
    root.set(rootDir)
}

val baseFolder = "javacc-${rootProject.version}"

fun CrLfSpec.binaryLayout() = copySpec {
    includeEmptyDirs = false
    gitattributes(gitProps)
    into(baseFolder) {
        // Note: license content is taken from "/build/..", so gitignore should not be used
        // Note: this is a "license + third-party licenses", not just Apache-2.0
        // Note: files(...) adds both "files" and "dependency"
        from(rootDir) {
            gitignore(gitProps)
            include("LICENSE")
            include("docs/**")
            include("examples/**")
            exclude("examples/build.gradle.kts")
        }
        into("bin") {
            from("$rootDir/scripts") {
                gitignore(gitProps)
            }
        }
        into("target") {
            from(javaccJar) {
                rename { "javacc.jar" }
            }
        }
    }
}

for (archive in listOf(Zip::class, Tar::class)) {
    val taskName = "dist${archive.simpleName}"
    val archiveTask = tasks.register(taskName, archive) {
        val eol = if (archive == Tar::class) LineEndings.LF else LineEndings.CRLF
        group = "distribution"
        description = "Creates distribution with $eol line endings for text files"
        if (this is Tar) {
            compression = Compression.GZIP
            archiveExtension.set("tar.gz")
        }
        // Gradle does not track "filters" as archive/copy task dependencies,
        // So a mere change of a file attribute won't trigger re-execution of a task
        // So we add a custom property to re-execute the task in case attributes change
        inputs.property("gitproperties", gitProps.map { it.props.attrs.toString() })

        // Gradle defaults to the following pattern:
        // [baseName]-[appendix]-[version]-[classifier].[extension]
        archiveBaseName.set("javacc")

        CrLfSpec(eol).run {
            wa1191SetInputs(gitProps)
            with(binaryLayout())
        }
        doLast {
            logger.lifecycle("Distribution is created: ${archiveFile.get().asFile}")
        }
    }
    releaseArtifacts {
        artifact(archiveTask)
    }
}
