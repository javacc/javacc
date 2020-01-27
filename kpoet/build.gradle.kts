dependencies {
    api(project(":javacc-javapoet"))

    testImplementation("org.jetbrains.spek:spek-api:1.1.0")
    testRuntimeOnly("org.jetbrains.spek:spek-junit-platform-engine:1.1.0")
//    testRuntimeOnly("org.junit.platform:junit-platform-runner:1.0.0-M3")
}
