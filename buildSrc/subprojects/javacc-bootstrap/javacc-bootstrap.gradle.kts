gradlePlugin {
    plugins {
        register("javacc-bootstrap") {
            id = "javacc-bootstrap"
            implementationClass = "org.javacc.builttools.bootstrap.JavaCCPlugin"
        }
    }
}
