
plugins {
    id("com.hypherionmc.modutils.modpublisher")
}

publisher {
    if (propertyBool("publish_to_modrinth")) {
        apiKeys {
            System.getProperty("MODRINTH_TOKEN")?.let(this::modrinth)
        }
        modrinthID = propertyString("modrinth_project_id")
    }
    if (propertyBool("publish_to_curseforge")) {
        apiKeys {
            System.getProperty("CURSEFORGE_TOKEN")?.let(this::curseforge)
        }
        curseID = propertyString("curseforge_project_id")
    }

    debug = propertyBool("debug_publish")

    versionType = propertyString("release_type")
    changelog = rootProject.file("CHANGELOG.md")
    projectVersion = propertyString("mod_version")
    displayName = "${propertyString("mc_version")}-${propertyString("mod_version")}"
    gameVersions = listOf(propertyString("mc_version"))
    loaders = listOf("forge")
    curseEnvironment = "client"
    artifact.set(tasks.named("remapJar"))
    javaVersions = listOf(JavaVersion.toVersion(propertyString("java_target")))
}
