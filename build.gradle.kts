import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import xyz.jpenilla.resourcefactory.bukkit.Permission

plugins {
    id("java")
    alias(libs.plugins.resourceFactory)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.hidetakeSSH)
    alias(libs.plugins.grgit)
    alias(libs.plugins.paperweight)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlinter)
}

group = "swordofmagic7"
version = "0.1.0+${versionMetadata()}"

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public")
    maven(url = "https://oss.sonatype.org/content/groups/public")
    maven(url = "https://mvn.lib.co.nz/public") // LibsDisguises
    maven(url = "https://jitpack.io") // DecentHolograms, ForestRedisAPI
    maven(url = "https://repo.codemc.io/repository/maven-public") // PacketEvents
    maven(url = "https://repo.bluecolored.de/releases/") // BlueMap
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven(url = "https://repo.opencollab.dev/main/") // Floodgate
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paperApi.get())
    compileOnly(libs.packetEvents)
    compileOnly(libs.libsDisguises)
    compileOnly(libs.decentHolograms)
    compileOnly(libs.blueMap)
    compileOnly(libs.floodgate)
    compileOnly(libs.forestRedisApi)

    implementation(libs.bundles.libraries) {
        exclude(group = "net.kyori")
    }
}

remotes {
    withGroovyBuilder {
        "create"("dev") {
            setProperty("host", properties["SFTP_HOST_SOM7"] ?: "localhost")
            setProperty("port", properties["SFTP_PORT"].toString().toIntOrNull() ?: 22)
            setProperty("user", properties["SFTP_USER_SOM7_DEV"] ?: "som7")
            setProperty("password", properties["SFTP_PASSWORD"] ?: "som7")
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude {
        it.name.startsWith("acf-") && !it.name.endsWith("_ja.properties")
    }

    fun relocateLibs(
        packageName: String,
        targetPackageName: String,
    ) {
        relocate(packageName, "net.somrpg.swordofmagic7.libs.$targetPackageName")
    }

    relocateLibs("com.ezylang.evalex", "evalex")

    relocateLibs("co.aikar.commands", "acf")
    relocateLibs("co.aikar.locales", "locales")

    relocateLibs("dev.triumphteam.gui", "gui")
}

tasks.register("deploy") {
    description = "Deploy the plugin to the server"
    group = JavaBasePlugin.BUILD_TASK_NAME
    dependsOn("shadowJar")
    doLast {
        ssh.run(
            delegateClosureOf<RunHandler> {
                session(
                    remotes["dev"],
                    delegateClosureOf<SessionHandler> {
                        put(
                            hashMapOf(
                                "from" to "${getLayout().buildDirectory.get()}/libs/${project.name}-${project.version}.jar",
                                "into" to "plugins/DevTools/pluginReloader/${project.name}.jar",
                            ),
                        )
                    },
                )
            },
        )
    }
}

fun versionMetadata(): String {
    if (!grgit.status().isClean) {
        return "${grgit.head().abbreviatedId}-Dev"
    }

    val tag = grgit.tag.list().find { it.commit.id == grgit.head().id }
    if (tag != null) {
        return ""
    }
    return grgit.head().abbreviatedId
}

bukkitPluginYaml {
    name = project.name
    description = "Sword of Magic 7のプラグインのFork"
    authors = listOf("MomiNeko", "SomNetworkMembers", "ATTSUMAN08")
    apiVersion = "1.13"

    main = "net.somrpg.swordofmagic7.SomCore"
    depend = listOf("ForestRedisAPI", "DecentHolograms", "LibsDisguises", "packetevents")
    softDepend = listOf("BlueMap", "Floodgate")

    permissions {
        register("som7.developer") {
            default = Permission.Default.OP
            description = "SOM7の開発者権限"
        }

        register("som7.builder") {
            default = Permission.Default.OP
            description = "SOM7の建築者権限"
        }

        register("som7.data.reload") {
            default = Permission.Default.OP
            description = "SOM7のデータリロード権限"
        }

        register("som7.title.editor") {
            default = Permission.Default.OP
            description = "SOM7のタイトルエディタ権限"
        }

        register("som7.user") {
            default = Permission.Default.TRUE
            description = "SOM7のユーザー権限"
        }

        register("som7.chat") {
            default = Permission.Default.TRUE
            description = "チャットする権限"
        }
    }

    commands {
        register("test") {
            permission = "som7.developer"
        }

        register("itemDataEdit") {
            permission = "som7.developer"
        }

        register("mobSpawnerDataEdit") {
            permission = "som7.developer"
        }

        register("mobSpawnerDataCreate") {
            permission = "som7.developer"
        }

        register("mobDropItemCreate") {
            permission = "som7.developer"
        }

        register("defenseBattleStartWave") {
            permission = "som7.developer"
        }

        register("defenseBattleEndWave") {
            permission = "som7.developer"
        }

        register("killMob") {
            permission = "som7.developer"
        }

        register("reqExpAll") {
            permission = "som7.user"
        }

        register("itemInventorySort") {
            aliases = listOf("iis")
        }

        register("runeInventorySort") {
            aliases = listOf("ris")
        }

        register("petInventorySort") {
            aliases = listOf("pis")
        }

        register("itemInventorySortReverse") {
            aliases = listOf("iisr")
        }

        register("runeInventorySortReverse") {
            aliases = listOf("risr")
        }

        register("petInventorySortReverse") {
            aliases = listOf("pisr")
        }

        register("tutorial") {
            permission = "som7.user"
        }

        register("trade") {
            permission = "som7.user"
        }

        register("textView") {
            permission = "som7.user"
        }

        register("setTitle") {
            permission = "som7.user"
        }

        register("reqlifeExpAll") {
            permission = "som7.user"
        }

        register("setFishingCombo") {
            permission = "som7.user"
        }

        register("nickReset") {
            permission = "som7.user"
        }

        register("skillSlot") {
            aliases = listOf("ss")
        }

        register("entities") {
            permission = "som7.user"
        }

        register("setFastUpgrade") {
            permission = "som7.user"
        }

        register("cast") {
            permission = "som7.user"
        }

        register("itemSearch") {
            aliases = listOf("is")
        }

        register("runeSearch") {
            aliases = listOf("rs")
        }

        register("petSearch") {
            aliases = listOf("ps")
        }

        register("damageSimulator") {
            aliases = listOf("ds")
        }
    }
}

tasks {
    kotlin {
        jvmToolchain(25)
    }
}
