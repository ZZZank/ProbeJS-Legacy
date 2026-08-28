plugins {
    `java-library`
    `repo-conventions`
    `project-common`
}

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        name = "SpongePowered"
    }
}

dependencies {
    api(project(":srcCore"))
    api(project(":srcParchment"))

    compileOnly("org.ow2.asm:asm:9.6")
    compileOnly("org.ow2.asm:asm-commons:9.6")
    compileOnly("org.ow2.asm:asm-tree:9.6")

    compileOnly("net.minecraftforge:forgespi:3.2.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.test {
    useJUnitPlatform()
}
