import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler

plugins {
    alias(libs.plugins.pluginYml)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.hidetakeSSH)
    alias(libs.plugins.grgit)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.paperweight)
}

group = "swordofmagic7"
version = "0.1.0+${versionMetadata()}"

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public")
    maven(url = "https://oss.sonatype.org/content/groups/public")
    maven(url = "https://repo.md-5.net/content/groups/public") // LibsDisguises
    maven(url = "https://jitpack.io") // DecentHolograms
    maven(url = "https://repo.codemc.io/repository/maven-public") // ItemNBTAPI, PacketEvents
    maven(url = "https://repo.lavafuse.net/releases") // AbyssLib
    maven(url = "https://repo.bluecolored.de/releases/") // BlueMap
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven(url = "https://repo.opencollab.dev/main/") // Floodgate
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paperApi.get())
    compileOnly(libs.packetEvents)
    compileOnly(libs.libsDisguises)
    compileOnly(libs.decentHolograms)
    compileOnly(libs.abyssLib)
    compileOnly(libs.blueMap)
    compileOnly(libs.floodgate)
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

tasks.register("deploy") {
    description = "Deploy the plugin to the server"
    group = JavaBasePlugin.BUILD_TASK_NAME
    dependsOn("build")
    doLast {
        ssh.run(delegateClosureOf<RunHandler> {
            session(remotes["dev"], delegateClosureOf<SessionHandler> {
                put(hashMapOf(
                    "from" to "${getLayout().buildDirectory.get()}/libs/${project.name}-${project.version}.jar",
                    "into" to "plugins/${project.name}.jar"
                ))
            })
        })
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

sonar {
    properties {
        property("sonar.projectKey", "ATTSUMAN08_SwordofMagic7")
        property("sonar.organization", "attsuman08")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

bukkit {
    name = project.name
    version = "${project.version}"
    description = "Sword of Magic 7のプラグインのFork"
    authors = listOf("MomiNeko", "SomNetworkMembers", "ATTSUMAN08")
    apiVersion = "1.13"

    main = "net.somrpg.swordofmagic7.SomCore"
    softDepend = listOf("AbyssLib", "DecentHolograms", "Citizens", "LibsDisguises", "PacketEvents", "BlueMap")

    permissions {
        register("som7.developer") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "SOM7の開発者権限"
        }

        register("som7.builder") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "SOM7の建築者権限"
        }

        register("som7.data.reload") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "SOM7のデータリロード権限"
        }

        register("som7.title.editor") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "SOM7のタイトルエディタ権限"
        }

        register("som7.user") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "SOM7のユーザー権限"
        }

        register("som7.chat") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "チャットする権限"
        }
    }

    commands {
        register("getItem") {
            permission = "som7.developer"
        }

        register("getRune") {
            permission = "som7.developer"
        }

        register("getExp") {
            permission = "som7.developer"
        }

        register("getLevel") {
            permission = "som7.developer"
        }

        register("getClassExp") {
            permission = "som7.developer"
        }

        register("getEffect") {
            permission = "som7.developer"
        }

        register("mobSpawn") {
            permission = "som7.developer"
            aliases = listOf("ms")
        }

        register("save") {
            permission = "som7.developer"
        }

        register("load") {
            permission = "som7.developer"
        }

        register("bukkitTasks") {
            permission = "som7.developer"
        }

        register("loadedPlayer") {
            permission = "som7.developer"
        }

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

        register("sendData") {
            permission = "som7.developer"
        }

        register("classSelect") {
            permission = "som7.developer"
        }

        register("skillCTReset") {
            permission = "som7.developer"
        }

        register("setNick") {
            permission = "som7.developer"
        }

        register("addTitle") {
            permission = "som7.developer"
        }

        register("killMob") {
            permission = "som7.developer"
        }

        register("playerInfo") {
            aliases = listOf("info", "i")
        }

        register("reqExp") {
            permission = "som7.user"
        }

        register("reqExpAll") {
            permission = "som7.user"
        }

        register("taggame") {
            permission = "som7.user"
        }

        register("party") {
            aliases = listOf("pt")
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

        register("effectInfo") {
            aliases = listOf("ei")
        }

        register("setTitle") {
            permission = "som7.user"
        }

        register("reqlifeExp") {
            permission = "som7.user"
        }

        register("reqlifeExpAll") {
            permission = "som7.user"
        }

        register("auction") {
            aliases = listOf("auc")
        }

        register("market") {
            permission = "som7.user"
        }

        register("setFishingCombo") {
            permission = "som7.user"
        }

        register("mobInfo") {
            aliases = listOf("mi")
        }

        register("itemInfo") {
            aliases = listOf("ii")
        }

        register("runeInfo") {
            aliases = listOf("ri")
        }

        register("nickReset") {
            permission = "som7.user"
        }

        register("skillSlot") {
            aliases = listOf("ss")
        }

        register("runeFilter") {
            aliases = listOf("rf")
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

        register("blockPlayer") {
            permission = "som7.user"
        }

        register("damageSimulator") {
            aliases = listOf("ds")
        }
    }
}

tasks {
    kotlin {
        jvmToolchain(21)
    }
}