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

    minecraft("com.mojang:minecraft:${propertyString("mc_version")}")

    mappings(loom.officialMojangMappings())

    forge("net.minecraftforge:forge:${propertyString("mc_version")}-${propertyString("forge_version")}")

    modApi("curse.maven:rhino-416294:6186971")
    modApi("curse.maven:architectury-api-419699:5137938")
    modApi("curse.maven:kubejs-238086:8020595")

    // for dev test only
    modRuntimeOnly("curse.maven:modernfix-790626:8664181")
    runtimeOnly("io.github.llamalad7:mixinextras-forge:0.5.4")

    if (file("./local_libs").exists()) {
        modImplementation(fileTree("./local_libs") { include("*.jar") })
    }
}

// Only for `runClient` task
// Forge's ModuleClassLoader isolates each mod to its own module.
// Since srcCore and srcCommon are `shade` dependencies (bundled into the final jar via ShadowJar),
// their classes are NOT part of the mod's module in dev mode, causing ClassNotFoundException.
// This task copies their compiled classes into src1201's classes directory so they're accessible.
val copyShadedClasses by tasks.registering(Copy::class) {
    dependsOn(":srcCore:classes", ":srcCommon:classes")
    from(project(":srcCore").sourceSets.main.get().output.classesDirs)
    from(project(":srcCommon").sourceSets.main.get().output.classesDirs)
    into(project.layout.buildDirectory.dir("classes/java/main"))
}

tasks.named("classes") {
    dependsOn(copyShadedClasses)
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

tasks.withType(net.fabricmc.loom.task.RunGameTask::class).configureEach {
    jvmArgs("-Xmx1g")
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
