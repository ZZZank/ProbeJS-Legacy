repositories {
    mavenCentral()

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
        content {
            includeGroup("org.parchmentmc.data")
        }
    }

    maven("https://cursemaven.com") {
        name = "CurseMaven"
        content {
            includeGroup("curse.maven")
        }
    }

    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://maven.architectury.dev/") {
        content {
            includeGroup("me.shedaniel")
            includeGroup("dev.architectury")
        }
    }

    maven("https://maven.latvian.dev/releases") {
        content {
            includeGroup("dev.latvian.mods")
            includeGroup("dev.latvian.apps")
        }
    }

    mavenLocal()
}
