plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://maven.firstdark.dev/releases") }
}

dependencies {
    implementation("com.hypherionmc.modutils:modpublisher:2.2.1")

    // Source: https://mvnrepository.com/artifact/com.gradleup.shadow/shadow-gradle-plugin
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.2.2")

    // https://github.com/unimined/JvmDowngrader/issues/40#issuecomment-4545707904
    implementation("commons-io:commons-io:2.22.0")
}
