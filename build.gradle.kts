import java.util.*

plugins {
    id("idea")
}

val sysProps = System.getProperties() ?: Properties()

if (project.hasProperty("run_number")) {
    println("On GitHub runs #${property("run_number")}")
}
println("Gradle running on Java: ${sysProps["java.version"]} | JVM: ${sysProps["java.vm.version"]} | Vendor: ${sysProps["java.vendor"]} | Architecture: ${sysProps["os.arch"]}")

version = propertyString("mod_version")
group = propertyString("maven_group")

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}