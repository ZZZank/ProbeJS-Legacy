plugins {
    `java-library`
    `project-common`
    id("java-test-fixtures")
    id("dev.architectury.loom-companion") version "1.17-SNAPSHOT"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:25.0.0")
    compileOnlyApi("com.google.code.gson:gson:2.8.0")
    compileOnlyApi("org.apache.logging.log4j:log4j-api:2.15.0")

    // Source: https://mvnrepository.com/artifact/com.demonwav.mcdev/annotations
    compileOnlyApi("com.demonwav.mcdev:annotations:2.1.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}