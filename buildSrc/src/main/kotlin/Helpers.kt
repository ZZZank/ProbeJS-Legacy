import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

fun Project.propertyString(key: String): String {
    val value = findProperty(key)?.toString() ?: error("Property $key is not defined!")
    return interpolate(value)
}

fun Project.propertyBool(key: String): Boolean =
    propertyString(key).toBoolean()

fun Project.propertyStringList(key: String, delimit: String = " "): List<String> =
    propertyString(key)
        .split(delimit)
        .map { it.trim() }
        .filter { it.isNotEmpty() }

fun Project.interpolate(value: String): String {
    if (value.contains($$"${")) {
        val regex = Regex("\\$\\{([^}]+)}")
        return regex.replace(value) { match ->
            findProperty(match.groupValues[1])?.toString() ?: match.value
        }
    }
    return value
}

fun Project.nextMCVersion(version: String): String {
    val parts = version.split(".").take(2)
    return "${parts[0]}.${parts[1].toInt() + 1}"
}

fun Project.setDefaultProperty(propertyName: String, warn: Boolean, defaultValue: Any? = null) {
    val property = findProperty(propertyName)
    var exists = true
    if (property == null) {
        exists = false
        if (warn) {
            project.logger.log(LogLevel.WARN, "Property $propertyName is not defined!")
        }
    } else if (property is String && property.isEmpty()) {
        exists = false
        if (warn) {
            project.logger.log(LogLevel.WARN, "Property $propertyName is empty!")
        }
    }
    if (!exists) {
        project.setProperty(propertyName, defaultValue.toString())
    }
}

fun Project.assertEnvironmentVariable(propertyName: String) {
    val property = System.getenv(propertyName)
    if (property == null) {
        throw GradleException("System Environment Variable $propertyName is not defined!")
    }
    if (property.isEmpty()) {
        throw GradleException("Property $propertyName is empty!")
    }
}

