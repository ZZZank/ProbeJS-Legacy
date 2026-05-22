import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("repo-conventions")
    id("dev.architectury.loom") version "1.14-SNAPSHOT"
    id("idea")
    id("xyz.wagyourtail.jvmdowngrader") version "1.3.6"
    `project-common`
    `mod-publish-common`
}

val javaTarget = JavaVersion.toVersion(propertyString("java_target"))
println("Target Java version is set to: ${javaTarget.majorVersion}")

jvmdg {
    downgradeTo = javaTarget
}

base {
    archivesName = "${propertyString("archives_base_name")}-${propertyString("mc_version")}"
}

version = propertyString("mod_version")
group = propertyString("maven_group")

if (!propertyBool("use_jvmdowngrader")) {
    tasks.withType<JavaCompile> {
        options.release = javaTarget.majorVersion.toInt()
    }
}

java {
    if (propertyBool("generate_sources_jar")) {
        withSourcesJar()
    }
    if (propertyBool("generate_javadocs_jar")) {
        withJavadocJar()
    }
}

loom {
    silentMojangMappingsLicense()

    if (propertyBool("use_access_widener")) {
        val f = file("src/main/resources/${propertyString("access_widener_name")}.accesswidener")
        if (!f.exists()) {
            logger.error("Configured AccessWidener not found")
        }
        accessWidenerPath = f
    }

    forge {
        if (propertyBool("use_mixin")) {
            mixinConfigs.set(propertyStringList("mixin_configs").map { "${it}.json" })
        }

        if (propertyBool("use_access_widener")) {
            convertAccessWideners = true
        }
    }

    mixin {
        useLegacyMixinAp = true
    }
}

dependencies {
    shade(project(":srcCore"))
    shade(project(":srcCommon"))

    minecraft("com.mojang:minecraft:${propertyString("mc_version")}")

    mappings(loom.officialMojangMappings())

    forge("net.minecraftforge:forge:${propertyString("mc_version")}-${propertyString("forge_version")}")

    modCompileOnly("dev.latvian.mods:kubejs-forge:2001.6.5-build.16")

    val localLibsDir = file("./local_libs")
    if (localLibsDir.exists()) {
        localLibsDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".jar") }
            ?.forEach { modImplementation(files(rootProject.relativePath(it))) }
    }
}

tasks.processResources {
    if (!propertyBool("use_access_widener")) {
        exclude("${propertyString("access_widener_name")}.accesswidener")
    }

    if (!propertyBool("use_mixin")) {
        val configs = propertyStringList("mixin_configs").map { "${it}.json" }
        exclude(*configs.toTypedArray())
    }

    expand(
        mapOf(
            "version" to propertyString("mod_version"),
            "forge_version" to propertyString("forge_version"),
            "loader_version_range" to propertyString("loader_version_range"),
            "license" to propertyString("license"),
            "mod_id" to propertyString("mod_id"),
            "mod_display_name" to propertyString("mod_display_name"),
            "mod_author" to propertyString("mod_author"),
            "mod_description" to propertyString("mod_description"),
            "minecraft_version" to propertyString("mc_version"),
            "required_minecraft_range" to propertyString("mc_version_range"),
            "resource_pack_format" to "16"
        )
    )
}

tasks.named("remapJar", RemapJarTask::class.java) {
    inputFile = tasks.shadeDowngradedApi.flatMap { it.archiveFile }
    dependsOn(tasks.named("shadowJar"))
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadeDowngradedApi"))
}

tasks.register<Copy>("buildAndCollect") {
    from(tasks.remapJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${propertyString("mod_version")}"))
    dependsOn("build")
}
