plugins {
    `java-library`
    `project-common`
    id("java-test-fixtures")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:25.0.0")
    compileOnlyApi("com.google.code.gson:gson:2.8.0")
    compileOnlyApi("org.apache.logging.log4j:log4j-api:2.15.0")
}
