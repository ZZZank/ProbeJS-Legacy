pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        maven("https://maven.firstdark.dev/releases")
    }
}

include("srcCore", "srcCommon", "src1165", "src1201", "srcParchment")
