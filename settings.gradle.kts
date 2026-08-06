pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        maven("https://maven.firstdark.dev/releases")
    }
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("commons-io:commons-io:2.20.0") // fix older version pulled in by other plugin, jvmdg requires 2.16+
    }
}

include("srcCore", "srcCommon", "src1165", "src1201", "srcParchment")
