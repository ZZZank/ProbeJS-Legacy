import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("repo-conventions")
    id("dev.architectury.loom") version "1.17-SNAPSHOT"
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
    tasks.withType<JavaCompile>().configureEach {
        options.release = javaTarget.majorVersion.toInt()
    }
}

java {
    toolchain {
        // other version of Java, e.g. Java 25, will cause 'runClient' to be completely unusable
        languageVersion = JavaLanguageVersion.of(21)
    }

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

        forge {
            convertAccessWideners = true
        }
    }

    if (propertyBool("use_mixin")) {
        forge {
            mixinConfigs.set(propertyStringList("mixin_configs").map { "${it}.json" })
        }

        mixin {
            useLegacyMixinAp = true
        }
    }

    interfaceInjection {
        getIsEnabled().set(false)
    }
}

dependencies {
    shade(project(":srcCommon"))
    testImplementation(testFixtures(project(":srcCore")))

    minecraft("com.mojang:minecraft:${propertyString("mc_version")}")

    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.16.5:2022.03.06")
    })

    forge("net.minecraftforge:forge:${propertyString("mc_version")}-${propertyString("forge_version")}")

    // 3.6.5
    modApi("curse.maven:rhizo-1003287:6354880")
    modApi("me.shedaniel:architectury-forge:1.32.68")
    modApi("curse.maven:kubejs-238086:3647098") {
        exclude(mapOf("group" to "dev.latvian.mods", "module" to "rhino-forge"))
    }

    if (file("./local_libs").exists()) {
        modImplementation(fileTree("./local_libs") { include("*.jar") })
    }
}

tasks.withType(net.fabricmc.loom.task.RunGameTask::class).configureEach {
    jvmArgs("-Xmx1g")
}

tasks.processResources {
    if (!propertyBool("use_access_widener")) {
        exclude("${propertyString("access_widener_name")}.accesswidener")
    }

    if (!propertyBool("use_mixin")) {
        val configs = propertyStringList("mixin_configs").map { "${it}.json" }
        exclude(*configs.toTypedArray())
    }

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
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

