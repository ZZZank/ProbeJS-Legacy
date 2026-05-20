
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java")
    id("com.gradleup.shadow")
}

configurations {
    val shade = create("shade")
    implementation.get().extendsFrom(shade)
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<ShadowJar>("shadowJar") {
    configurations.set(project.configurations.named("shade").map { listOf(it) })
    archiveClassifier.set("dev-shadow")
}

dependencies {
    val vers = propertyString("lombok_version")
    compileOnly("org.projectlombok:lombok:${vers}")
    annotationProcessor("org.projectlombok:lombok:${vers}")
    testCompileOnly("org.projectlombok:lombok:${vers}")
    testAnnotationProcessor("org.projectlombok:lombok:${vers}")

    if (propertyBool("enable_junit")) {
        testImplementation("org.junit.jupiter:junit-jupiter:${propertyString("junit_version")}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.ow2.asm") {
            useVersion("9.6")
            because("Force ASM to a modern version that supports Java 21")
        }
        if (requested.group == "org.lwjgl") {
            useVersion("3.3.3")
            because("Force LWJGL to a modern version that supports Java 21")
        }
    }
}

if (propertyBool("enable_junit")) {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to propertyString("mod_id"),
                "Specification-Vendor" to propertyString("mod_author"),
                "Specification-Version" to "1",
                "Implementation-Title" to propertyString("mod_display_name"),
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to propertyString("mod_author"),
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
            )
        )
    }
}

tasks.register("printArchiveClassifier") {
    description = "print name and archive classifier of all AbstractArchiveTask"
    doLast {
        for (task in tasks) {
            if (task is AbstractArchiveTask) {
                println("task=${task.name}, archiveClassifier=${task.archiveClassifier.get()}")
            }
        }
    }
}