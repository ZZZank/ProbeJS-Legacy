
plugins {
    id("com.hypherionmc.modutils.modpublisher")
}

publisher {
    apiKeys {
        System.getProperty("MODRINTH_KEY")?.let(this::modrinth)
        System.getProperty("CURSEFORGE_KEY")?.let(this::curseforge)
    }

    curseID = "956446"
    modrinthID = "KVw0Q70k"

    versionType = "release"
    changelog = rootProject.file("CHANGELOG.md")
    projectVersion = propertyString("mod_version")
    displayName = "${propertyString("mc_version")}-${propertyString("mod_version")}"
    gameVersions = listOf(propertyString("mc_version"))
    loaders = listOf("forge")
    curseEnvironment = "client"
    artifact.set(tasks.named("remapJar"))
    javaVersions = listOf(JavaVersion.toVersion(propertyString("java_target")))
}
