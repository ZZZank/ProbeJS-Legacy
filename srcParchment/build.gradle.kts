plugins {
    java
    `project-common`
}

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org/") {
        name = "ParchmentMC"
    }
}

dependencies {
    implementation(project(":srcCore"))
    testImplementation(testFixtures(project(":srcCore")))

    // The whole process of re-process Parchment mapping is at `test`, because I'm too lazy to make another subproject
    val featherVersion = "1.1.0"
    testImplementation("org.parchmentmc:feather:${featherVersion}")
    testImplementation("org.parchmentmc.feather:io-gson:${featherVersion}")
    testImplementation("org.ow2.asm:asm:9.6")
}

tasks.test {
    useJUnitPlatform()
}
