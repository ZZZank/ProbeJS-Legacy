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

    val featherVersion = "1.1.0"
    implementation("org.parchmentmc:feather:${featherVersion}")
    implementation("org.parchmentmc.feather:io-gson:${featherVersion}")

    compileOnly("org.ow2.asm:asm:9.6")
    testImplementation("org.ow2.asm:asm:9.6")
}

tasks.test {
    useJUnitPlatform()
}
