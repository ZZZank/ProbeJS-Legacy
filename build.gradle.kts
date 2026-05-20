import java.util.Properties

plugins {
    id("idea")
}

val props = project.properties
val sysProps = System.getProperties() ?: Properties()

if (project.hasProperty("run_number")) {
    println("On GitHub runs #${props["run_number"]}")
}
println("Gradle running on Java: ${sysProps["java.version"]} | JVM: ${sysProps["java.vm.version"]} | Vendor: ${sysProps["java.vendor"]} | Architecture: ${sysProps["os.arch"]}")

version = propertyString("mod_version")
group = propertyString("maven_group")
