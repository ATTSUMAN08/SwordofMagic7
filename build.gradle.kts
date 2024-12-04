import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler

plugins {
    alias(libs.plugins.pluginYml)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.hidetakeSSH)
    alias(libs.plugins.grgit)
}

group = "swordofmagic7"
version = "0.1.0+${versionMetadata()}"

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.md-5.net/content/groups/public/") // LibsDisguises
    maven(url = "https://jitpack.io") // NuVotifier, DecentHolograms
    maven(url = "https://maven.citizensnpcs.co/repo") // Citizens
    maven(url = "https://repo.codemc.io/repository/maven-public/") // ItemNBTAPI, PacketEvents
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.packetEvents)
    compileOnly(libs.libsDisguises)
    compileOnly(libs.nuVotifier)
    compileOnly(libs.decentHolograms)
    compileOnly(libs.citizens) {
        exclude(group = "*", module = "*")
    }

    implementation(libs.itemNbtApi)
    implementation(libs.kotlinSerializationJson)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.cloud)

    bukkitLibrary(libs.jedis)
}

remotes {
    withGroovyBuilder {
        "create"("devServer") {
            setProperty("host", properties["TEMP_SFTP_HOST"]!!)
            setProperty("port", properties["TEMP_SFTP_PORT"]!!.toString().toInt())
            setProperty("user", properties["TEMP_SFTP_USER"]!!)
            setProperty("password", properties["TEMP_SFTP_PASSWORD"]!!)
        }
    }
}

tasks.register("deploy") {
    description = "Deploy the plugin to the dev server"
    group = JavaBasePlugin.BUILD_TASK_NAME
    dependsOn("build")
    doLast {
        ssh.run(delegateClosureOf<RunHandler> {
            session(remotes["devServer"], delegateClosureOf<SessionHandler> {
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
    return "${grgit.head().abbreviatedId}"
}

bukkit {
    name = project.name
    version = "${project.version}"
    description = "Sword of Magic 7のプラグインのFork"
    authors = listOf("MomiNeko", "SomNetworkMembers", "ATTSUMAN08")
    apiVersion = "1.21"

    main = "net.somrpg.swordofmagic7.SomCore"
    softDepend = listOf("DecentHolograms", "Citizens", "LibsDisguises", "PacketEvents", "NuVotifier")

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

        register("SomReload") {
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

        register("gm") {
            permission = "som7.builder"
        }

        register("flySpeed") {
            permission = "som7.builder"
            aliases = listOf("fs")
        }

        register("playMode") {
            permission = "som7.builder"
            aliases = listOf("pm")
        }

        register("dataReload") {
            permission = "som7.data.reload"
        }

        register("itemReload") {
            permission = "som7.data.reload"
        }

        register("runeReload") {
            permission = "som7.data.reload"
        }

        register("skillReload") {
            permission = "som7.data.reload"
        }

        register("shopReload") {
            permission = "som7.data.reload"
        }

        register("titleReload") {
            permission = "som7.title.editor"
            aliases = listOf("tr")
        }

        register("menu") {
            permission = "som7.user"
        }

        register("m") {
            permission = "som7.user"
        }

        register("skill") {
            aliases = listOf("s")
        }

        register("attribute") {
            aliases = listOf("attr", "a")
        }

        register("damageHolo") {
            permission = "som7.user"
        }

        register("damageLog") {
            permission = "som7.user"
        }

        register("expLog") {
            permission = "som7.user"
        }

        register("dropLog") {
            permission = "som7.user"
        }

        register("pvpMode") {
            permission = "som7.user"
        }

        register("strafeMode") {
            permission = "som7.user"
        }

        register("castMode") {
            permission = "som7.user"
        }

        register("effectLog") {
            permission = "som7.user"
        }

        register("particleDensity") {
            permission = "som7.user"
        }

        register("viewFormat") {
            permission = "som7.user"
        }

        register("spawn") {
            permission = "som7.user"
        }

        register("playerInfo") {
            aliases = listOf("info", "i")
        }

        register("tickTime") {
            permission = "som7.user"
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

        register("uuid") {
            permission = "som7.user"
        }

        register("effectInfo") {
            aliases = listOf("ei")
        }

        register("sideBarToDo") {
            aliases = listOf("sbtd")
        }

        register("fishingDisplayNum") {
            aliases = listOf("fdn")
        }

        register("fishingUseCombo") {
            aliases = listOf("fuc")
        }

        register("setTitle") {
            permission = "som7.user"
        }

        register("HoloSelfView") {
            aliases = listOf("hsv")
        }

        register("checkTitle") {
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

        register("serverInfo") {
            aliases = listOf("si")
        }

        register("itemInfo") {
            aliases = listOf("ii")
        }

        register("runeInfo") {
            aliases = listOf("ri")
        }

        register("ch") {
            aliases = listOf("channel")
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

        register("loadOnLiveServer") {
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

        register("petTame") {
            permission = "som7.user"
        }
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("de.tr7zw.changeme.nbtapi", "net.somrpg.swordofmagic7.shade.nbtapi")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
