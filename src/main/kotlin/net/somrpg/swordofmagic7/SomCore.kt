package net.somrpg.swordofmagic7

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerCommon
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.google.gson.Gson
import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.somrpg.swordofmagic7.commands.CommandManager
import net.somrpg.swordofmagic7.lisiteners.PacketEventsListener
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.Builder.FlySpeed
import swordofmagic7.Command.Builder.GameModeChange
import swordofmagic7.Command.Builder.PlayMode
import swordofmagic7.Command.Developer.AddTitle
import swordofmagic7.Command.Developer.BukkitTasks
import swordofmagic7.Command.Developer.ClassSelect
import swordofmagic7.Command.Developer.GetClassExp
import swordofmagic7.Command.Developer.GetEffect
import swordofmagic7.Command.Developer.GetExp
import swordofmagic7.Command.Developer.GetItem
import swordofmagic7.Command.Developer.GetLevel
import swordofmagic7.Command.Developer.GetRune
import swordofmagic7.Command.Developer.Load
import swordofmagic7.Command.Developer.LoadedPlayer
import swordofmagic7.Command.Developer.MobSpawn
import swordofmagic7.Command.Developer.Save
import swordofmagic7.Command.Developer.SendData
import swordofmagic7.Command.Developer.SetNick
import swordofmagic7.Command.Developer.SkillCTReset
import swordofmagic7.Command.Developer.SomReload
import swordofmagic7.Command.Player.AuctionCommand
import swordofmagic7.Command.Player.BlockPlayer
import swordofmagic7.Command.Player.EffectInfo
import swordofmagic7.Command.Player.ItemInfo
import swordofmagic7.Command.Player.MarketCommand
import swordofmagic7.Command.Player.MobInfo
import swordofmagic7.Command.Player.Party
import swordofmagic7.Command.Player.ReqExp
import swordofmagic7.Command.Player.ReqLifeExp
import swordofmagic7.Command.Player.RuneFilter
import swordofmagic7.Command.Player.RuneInfo
import swordofmagic7.Command.Player.TagGameCommand
import swordofmagic7.Command.Player.playerInfo
import swordofmagic7.Command.SomCommand
import swordofmagic7.Data.DataBase.DataBasePath
import swordofmagic7.Data.DataBase.DataLoad
import swordofmagic7.Data.DataBase.MapList
import swordofmagic7.Data.DataBase.ServerId
import swordofmagic7.Data.DataBase.SpawnLocation
import swordofmagic7.Data.DataBase.TitleDataList
import swordofmagic7.Data.DataBase.WarpGateList
import swordofmagic7.Data.DataLoader
import swordofmagic7.Data.Editor
import swordofmagic7.Data.PlayerData
import swordofmagic7.Data.PlayerData.playerData
import swordofmagic7.Dungeon.DefenseBattle
import swordofmagic7.Dungeon.Dungeon
import swordofmagic7.Events
import swordofmagic7.Function.BroadCast
import swordofmagic7.Function.Log
import swordofmagic7.Function.createFolder
import swordofmagic7.Function.decoLore
import swordofmagic7.Function.ignoreEntity
import swordofmagic7.Function.sendMessage
import swordofmagic7.Function.teleportServer
import swordofmagic7.Mob.MobManager
import swordofmagic7.MultiThread.MultiThread
import swordofmagic7.Particle.ParticleManager
import swordofmagic7.PlayerList
import swordofmagic7.Som7Vote
import swordofmagic7.Sound.CustomSound.playSound
import swordofmagic7.Sound.SoundList
import swordofmagic7.TagGame
import swordofmagic7.TextView.TextViewManager
import swordofmagic7.Trade.TradeManager
import swordofmagic7.Tutorial
import swordofmagic7.redis.RedisManager
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.time.Duration
import java.util.*
import kotlin.math.pow

class SomCore : SuspendingJavaPlugin() {
    companion object {
        lateinit var world: World
        lateinit var instance: SomCore
        
        lateinit var random: Random
        val gson = Gson()
        const val AFK_TIME_PERIOD = 1
        const val AFK_TIME = 300

        fun isEventServer(): Boolean = ServerId.equals("Event", ignoreCase = true)
        fun isDevServer(): Boolean = ServerId.equals("Dev", ignoreCase = true)
        fun isDevEventServer(): Boolean = isEventServer() || isDevServer()
    }
    lateinit var packetEventsListener: PacketListenerCommon
    val hologramMap = HashMap<String, Hologram>()
    val hologramTouchActions = HashMap<String, (Player) -> Unit>()
    val playerLastLocation = HashMap<Player, Location>()
    

    fun createHologram(location: Location): Hologram {
        val hologram = DHAPI.createHologram("SOM7_${UUID.randomUUID()}", location)
        hologramMap[hologram.id] = hologram
        return hologram
    }

    fun createTouchHologram(display: String, location: Location, action: (Player) -> Unit) {
        val hologram = createHologram(location)
        DHAPI.addHologramLine(hologram, display)
        hologramTouchActions[hologram.id] = action
    }

    override suspend fun onEnableAsync() {
        val time = System.currentTimeMillis()
        saveDefaultConfig()
        reloadConfig()
        instance = this
        random = Random()
        world = Bukkit.getWorld("world") ?: throw IllegalStateException("World not found")
        ServerId = config.getString("serverId") ?: "Default"
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
        server.pluginManager.registerEvents(Som7Vote(), this)

        // Initialize folders
        if (!dataFolder.exists()) createFolder(dataFolder)
        val marketFolder = File(dataFolder, "Market")
        if (!marketFolder.exists()) createFolder(marketFolder)

        DataLoad()

        RedisManager.connect(
            config.getString("redis.host") ?: "localhost",
            config.getInt("redis.port", 6379),
            config.getString("redis.username") ?: "null",
            config.getString("redis.password") ?: "null",
            config.getBoolean("redis.ssl", false)
        )

        Tutorial.onLoad()
        Events(this)
        Dungeon.Initialize()
        PlayerList.load()

        packetEventsListener = PacketEvents.getAPI().eventManager.registerListener(PacketEventsListener(), PacketListenerPriority.NORMAL)

        WarpGateList.values.forEach { it.start() }

        world.apply {
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.DO_FIRE_TICK, false)
            setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false)
            setGameRule(GameRule.DO_PATROL_SPAWNING, false)
            setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            setGameRule(GameRule.NATURAL_REGENERATION, false)
            setGameRule(GameRule.MOB_GRIEFING, false)
            setGameRule(GameRule.DO_MOB_LOOT, false)
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            this.time = 6000L
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            BroadCast("§e[オートセーブ]§aを§b開始§aします")
            PlayerList.ResetPlayer.clear()
            val playerDataList = PlayerData.getPlayerData().values.toSet()
            playerDataList.forEach { data ->
                val player = data.player
                if (player != null) {
                    if (player.isOnline) data.save() else PlayerData.remove(player)
                }
            }
            BroadCast("§e[オートセーブ]§aが§b完了§aしました")
        }, 200, 6000)

        MultiThread.TaskRunTimer({
            Bukkit.getOnlinePlayers().forEach { player ->
                if (!PlayerData.ContainPlayer(player) && !isDevServer()) {
                    sendMessage(player, "§cPlayer data not loaded")
                    teleportServer(player, "Lobby")
                } else {
                    val playerData = playerData(player)
                    playerLastLocation[player]?.let { location ->
                        if (location.distance(player.location) < 2) {
                            playerData.AFKTime += AFK_TIME_PERIOD
                            playerData.statistics.AFKTime += AFK_TIME_PERIOD
                            if (playerData.isAFK) {
                                player.showTitle(
                                    Title.title(
                                        Component.text("§eAFKTime: §a${playerData.AFKTime} seconds"),
                                        Component.empty(),
                                        Title.Times.times(
                                            Duration.ZERO,
                                            Duration.ofSeconds(AFK_TIME_PERIOD + 5L),
                                            Duration.ZERO
                                        )
                                    )
                                )
                                if (DefenseBattle.isStarted) teleportServer(player, "Lobby")
                            }
                        } else {
                            playerLastLocation[player] = player.location.clone()
                            playerData.AFKTime = 0
                        }
                    } ?: run {
                        playerLastLocation[player] = player.location.clone()
                        playerData.AFKTime = 0
                    }
                }
            }
            playerLastLocation.keys.removeIf { !it.isOnline }
        }, AFK_TIME_PERIOD * 20)

        ParticleManager.onLoad()

        // Initialize holograms
        createTouchHologram("§e§lForge", Location(world, 1149.5, 97.75, 17.5)) { player ->
            playerData(player).Menu.Smith.SmithMenuView()
        }
        createTouchHologram("§e§lKitchen", Location(world, 1159.5, 94.5, 66.5)) { player ->
            playerData(player).Menu.Cook.CookMenuView()
        }

        Bukkit.getScheduler().runTaskLater(instance, Runnable {
            Bukkit.getOnlinePlayers().forEach { playerData(it).load() }
        }, 20)

        commandRegister()
        CommandManager().registerCommands()
        logger.info("Plugin Enabled: ${System.currentTimeMillis() - time}ms")
    }

    override suspend fun onDisableAsync() {
        deleteHolograms()
        Bukkit.getOnlinePlayers().forEach {
            it.closeInventory()
            it.sendMessage("§cSystem Reloading")
        }

        var count = 0
        MobManager.getEnemyList().forEach { enemyData ->
            enemyData.entity?.remove()
            count++
        }
        world.entities.filterNot { it is Player || ignoreEntity(it) }.forEach {
            it.remove()
            count++
        }
        Log("CleanEnemy: $count")
        Bukkit.getScheduler().cancelTasks(this)
        Log("Plugin Task Cancelled")
        PacketEvents.getAPI().eventManager.unregisterListener(packetEventsListener)
        Log("PacketListener unregister")
    }

    private fun deleteHolograms() {
        hologramMap.values.forEach { hologram ->
            if (!hologram.isDisabled) {
                DHAPI.removeHologram(hologram.id)
            }
        }
    }

    private fun commandRegister() {
        //Developer
        SomCommand.register("SomReload", SomReload())
        SomCommand.register("SendData", SendData())
        SomCommand.register("getItem", GetItem())
        SomCommand.register("getRune", GetRune())
        SomCommand.register("mobSpawn", MobSpawn())
        SomCommand.register("setNick", SetNick())
        SomCommand.register("save", Save())
        SomCommand.register("load", Load())
        SomCommand.register("loadedPlayer", LoadedPlayer())
        SomCommand.register("getExp", GetExp())
        SomCommand.register("getLevel", GetLevel())
        SomCommand.register("getClassExp", GetClassExp())
        SomCommand.register("getEffect", GetEffect())
        SomCommand.register("bukkitTasks", BukkitTasks())
        SomCommand.register("classSelect", ClassSelect())
        SomCommand.register("skillCTReset", SkillCTReset())
        SomCommand.register("addTitle", AddTitle())
        //Builder
        SomCommand.register("gm", GameModeChange())
        SomCommand.register("playMode", PlayMode())
        SomCommand.register("flySpeed", FlySpeed())
        //Player
        SomCommand.register("reqExp", ReqExp())
        SomCommand.register("reqLifeExp", ReqLifeExp())
        SomCommand.register("tagGame", TagGameCommand())
        SomCommand.register("playerInfo", playerInfo())
        SomCommand.register("party", Party())
        SomCommand.register("effectInfo", EffectInfo())
        SomCommand.register("itemInfo", ItemInfo())
        SomCommand.register("runeInfo", RuneInfo())
        SomCommand.register("mobInfo", MobInfo())
        SomCommand.register("market", MarketCommand())
        SomCommand.register("auction", AuctionCommand())
        SomCommand.register("blockPlayer", BlockPlayer())
        SomCommand.register("runeFilter", RuneFilter())
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val playerData = playerData(sender)
            if (sender.hasPermission("som7.developer")) {
                when (cmd.name.lowercase()) {
                    "itemdataedit" -> {
                        Editor.itemDataEditCommand(sender, args)
                        return true
                    }
                    "mobspawnerdataedit" -> {
                        Editor.mobSpawnerDataEditCommand(sender, args)
                        return true
                    }
                    "mobspawnerdatacreate" -> {
                        Editor.mobSpawnerDataCreateCommand(sender, args)
                        return true
                    }
                    "mobdropitemcreate" -> {
                        Editor.mobDropItemCreateCommand(sender, args)
                        return true
                    }
                    "defensebattlestartwave" -> {
                        val wave = args.getOrNull(0)?.toIntOrNull() ?: 1
                        DefenseBattle.startWave(wave)
                        return true
                    }
                    "defensebattleendwave" -> {
                        DefenseBattle.endWave()
                        return true
                    }
                    "killmob" -> {
                        if (args.size == 1) {
                            try {
                                val radius = args[0].toDouble()
                                val count = MobManager.getEnemyList().count { enemy ->
                                    (enemy.entity?.location?.distance(sender.location) ?: Double.MAX_VALUE) < radius
                                }
                                MobManager.getEnemyList().forEach { enemy ->
                                    if ((enemy.entity?.location?.distance(sender.location)
                                            ?: Double.MAX_VALUE) < radius
                                    ) {
                                        enemy.dead()
                                    }
                                }
                                sender.sendMessage("KillMob: $count")
                            } catch (_: Exception) {
                                sender.sendMessage("§e/killMob <radius>")
                            }
                        }
                        return true
                    }
                }
            }

            if (sender.hasPermission("som7.data.reload")) {
                when (cmd.name.lowercase()) {
                    "datareload" -> {
                        Bukkit.getOnlinePlayers().forEach { playerData(it).save() }
                        DataLoader.AllLoad()
                        Bukkit.getOnlinePlayers().forEach { playerData(it).load() }
                        return true
                    }
                    "itemreload" -> {
                        DataLoader.ItemDataLoad()
                        DataLoader.ItemInfoDataLoad()
                        return true
                    }
                    "runereload" -> {
                        DataLoader.RuneDataLoad()
                        DataLoader.RuneInfoDataLoad()
                        return true
                    }
                    "skillreload" -> {
                        DataLoader.SkillDataLoad()
                        return true
                    }
                    "shopreload" -> {
                        DataLoader.ShopDataLoad()
                        return true
                    }
                }
            }

            if (sender.hasPermission("som7.title.editor") && cmd.name.equals("titlereload", ignoreCase = true)) {
                DataLoader.TitleDataLoad()
                return true
            }

            when (cmd.name.lowercase()) {
                "menu", "m" -> {
                    playerData.Menu.UserMenuView()
                    playSound(sender, SoundList.MenuOpen)
                    return true
                }
                "skill" -> {
                    playerData.Skill.SkillMenuView()
                    playSound(sender, SoundList.MenuOpen)
                    return true
                }
                "attribute" -> {
                    playerData.Attribute.AttributeMenuView()
                    playSound(sender, SoundList.MenuOpen)
                    return true
                }
                "damageholo" -> {
                    playerData.DamageHolo()
                    return true
                }
                "damagelog" -> {
                    playerData.DamageLog()
                    return true
                }
                "explog" -> {
                    playerData.ExpLog()
                    return true
                }
                "droplog" -> {
                    playerData.DropLog()
                    return true
                }
                "pvpmode" -> {
                    playerData.PvPMode()
                    return true
                }
                "effectlog" -> {
                    playerData.EffectLog()
                    return true
                }
                "particledensity" -> {
                    playerData.ParticleDensity()
                    return true
                }
                "strafemode" -> {
                    playerData.StrafeMode()
                    return true
                }
                "fishingdisplaynum" -> {
                    playerData.FishingDisplayNum()
                    return true
                }
                "castmode" -> {
                    playerData.CastMode()
                    return true
                }
                "pettame" -> {
                    playerData.PetTame()
                    return true
                }
                "viewformat" -> {
                    playerData.changeViewFormat()
                    return true
                }
                "holoselfview" -> {
                    playerData.HoloSelfView()
                    return true
                }
                "spawn" -> {
                    if (TagGame.isTagPlayerNonMessage(sender)) return true
                    if (playerData.isPvPModeNonMessage()) return true
                    spawnPlayer(sender)
                    return true
                }
                "ticktime" -> {
                    Bukkit.getWorlds().forEach {
                        sender.sendMessage("§e${it.name}§7: §a${it.fullTime}")
                    }
                    return true
                }
                "iteminventorysort" -> {
                    playerData.ItemInventory.ItemInventorySort()
                    return true
                }
                "runeinventorysort" -> {
                    playerData.RuneInventory.RuneInventorySort()
                    return true
                }
                "petinventorysort" -> {
                    playerData.PetInventory.PetInventorySort()
                    return true
                }
                "iteminventorysortreverse" -> {
                    playerData.ItemInventory.ItemInventorySortReverse()
                    return true
                }
                "runeinventorysortreverse" -> {
                    playerData.RuneInventory.RuneInventorySortReverse()
                    return true
                }
                "petinventorysortreverse" -> {
                    playerData.PetInventory.PetInventorySortReverse()
                    return true
                }
                "tutorial" -> {
                    if (TagGame.isTagPlayerNonMessage(sender)) return true
                    Tutorial.tutorialHub(sender)
                    return true
                }
                "trade" -> {
                    TradeManager.tradeCommand(sender, playerData, args)
                    return true
                }
                "textview" -> {
                    TextViewManager.TextView(sender, args)
                    return true
                }
                "checktitle" -> {
                    playerData.statistics.checkTitle()
                    return true
                }
                "uuid" -> {
                    val target = if (args.size == 1) Bukkit.getPlayer(args[0]) else sender
                    if (target == null) {
                        sender.sendMessage("§c${args[0]}は存在しないプレイヤーです")
                    } else {
                        sender.sendMessage("${target.name}: ${target.uniqueId}")
                    }
                    return true
                }
                "sidebartodo" -> {
                    playerData.SideBarToDo.SideBarToDoCommand(args)
                    return true
                }
                "settitle" -> {
                    if (args.size == 1) {
                        val title = TitleDataList[args[0]]
                        if (title != null) {
                            playerData.titleManager.setTitle(title)
                        } else {
                            sender.sendMessage("§a存在しない称号です")
                            playSound(sender, SoundList.Nope)
                        }
                    } else {
                        playerData.titleManager.Title = TitleDataList["称号無し"]
                        sender.sendMessage("§a称号を外しました")
                        playSound(sender, SoundList.Tick)
                    }
                    return true
                }
                "serverinfo" -> {
                    val runtime = Runtime.getRuntime()
                    val mb = 1048576 // Convert bytes to MB
                    sender.sendMessage(decoLore("UseRAM") + (runtime.totalMemory() - runtime.freeMemory()) / mb)
                    sender.sendMessage(decoLore("FreeRAM") + runtime.freeMemory() / mb)
                    sender.sendMessage(decoLore("TotalRAM") + runtime.totalMemory() / mb)
                    sender.sendMessage(decoLore("MaxRAM") + runtime.maxMemory() / mb)
                    return true
                }
                "setfishingcombo" -> {
                    if (!playerData.Gathering.FishingUseCombo) {
                        try {
                            val combo = args[0].toInt()
                            if (combo in 1 until playerData.Gathering.FishingComboBoost) {
                                playerData.Gathering.FishingSetCombo = combo
                                sender.sendMessage("§eComboを${combo}に設定しました")
                            } else {
                                sender.sendMessage("§eCombo: 1 ~ ${playerData.Gathering.FishingComboBoost - 1}")
                            }
                        } catch (_: Exception) {
                            sender.sendMessage("§e/setFishingCombo <combo>")
                        }
                    } else {
                        sender.sendMessage("§a現在の§e[釣獲モード]§aでは利用できません")
                    }
                    playSound(sender, SoundList.Tick)
                    return true
                }
                "ch" -> {
                    if (playerData.isPlayDungeonQuest) {
                        sendMessage(sender, "§cダンジョンクエスト§a中は§eチャンネル§aを変更できません", SoundList.Nope)
                        return true
                    }
                    if (args.size == 1) {
                        val teleportServer = when (args[0].lowercase()) {
                            "1" -> "CH1"
                            "2" -> "CH2"
                            "3" -> "CH3"
                            "4" -> "CH4"
                            "5" -> "CH5"
                            "ev", "event" -> "Event"
                            "dev" -> "Dev"
                            else -> {
                                sender.sendMessage("存在しないチャンネルです")
                                return true
                            }
                        }
                        playerData.saveTeleportServer = "Som7$teleportServer"
                        playerData.save()
                    } else {
                        sender.sendMessage("§e/channel <channel>")
                    }
                    return true
                }
                "nickreset" -> {
                    playerData.Nick = sender.name
                    sendMessage(sender, "§eプレイヤ名§aを§e[${playerData.nick}]§aに§cリセット§aしました", SoundList.Tick)
                    return true
                }
                "skillslot" -> {
                    playerData.HotBar.SkillSlotCommand(args)
                    return true
                }
                "entities" -> {
                    sendMessage(sender, "EntityCount: ${sender.world.entityCount}")
                    return true
                }
                "loadonliveserver" -> {
                    if (TagGame.isTagPlayerNonMessage(sender)) return true
                    MultiThread.TaskRun({
                        if (ServerId.equals("Dev", ignoreCase = true)) {
                            try {
                                val dataInStream = getDataInputStream(sender)
                                DataOutputStream(BufferedOutputStream(FileOutputStream("$DataBasePath/PlayerData/${sender.uniqueId}.yml"))).use { dataOutStream ->
                                    val buffer = ByteArray(4096)
                                    var bytesRead: Int
                                    while (dataInStream.read(buffer).also { bytesRead = it } != -1) {
                                        dataOutStream.write(buffer, 0, bytesRead)
                                    }
                                }
                                MultiThread.TaskRunSynchronizedLater({ playerData.load() }, 5)
                            } catch (e: Exception) {
                                sendMessage(sender, "§cデータのダウンロードに失敗しました。${e.message}")
                            }
                        } else {
                            sendMessage(sender, "§b開発鯖§a以外では利用できません")
                        }
                    }, "loadOnLiveServer")
                    return true
                }
                "setfastupgrade" -> {
                    try {
                        val fastUpgrade = args[0].toInt().coerceIn(1, 25)
                        playerData.Upgrade.fastUpgrade = fastUpgrade
                        sendMessage(sender, "§aFastUpgrade: $fastUpgrade")
                    } catch (_: Exception) {
                        sendMessage(sender, "§e/setFastUpgrade <1~25>")
                    }
                    return true
                }
                "cast" -> {
                    try {
                        val slot = args[0].toInt() - 1
                        if (slot in 0..31) {
                            playerData.HotBar.use(slot)
                        } else {
                            sendMessage(sender, "§e/cast <1~32>")
                        }
                    } catch (_: Exception) {
                        sendMessage(sender, "§e/cast <1~32>")
                    }
                    return true
                }
                "itemsearch" -> {
                    playerData.ItemInventory.wordSearch = args.getOrNull(0)
                    sendMessage(sender, "§e[インベントリサーチ] §b-> §e[アイテム] §b-> §e[${playerData.ItemInventory.wordSearch ?: "すべて"}]", SoundList.Tick)
                    playerData.viewUpdate()
                    return true
                }
                "runesearch" -> {
                    playerData.RuneInventory.wordSearch = args.getOrNull(0)
                    sendMessage(sender, "§e[インベントリサーチ] §b-> §e[ルーン] §b-> §e[${playerData.RuneInventory.wordSearch ?: "すべて"}]", SoundList.Tick)
                    playerData.viewUpdate()
                    return true
                }
                "petsearch" -> {
                    playerData.PetInventory.wordSearch = args.getOrNull(0)
                    sendMessage(sender, "§e[インベントリサーチ] §b-> §e[ペット] §b-> §e[${playerData.PetInventory.wordSearch ?: "すべて"}]", SoundList.Tick)
                    playerData.viewUpdate()
                    return true
                }
                "damagesimulator" -> {
                    if (args.size >= 2) {
                        val log = getString(args)
                        sendMessage(sender, log)
                    } else {
                        sendMessage(sender, "§e/damageSimulator <atk> <def> [<multiply>] [<perforate>]")
                    }
                    return true
                }
            }
        }
        return false
    }

    @Throws(Exception::class)
    private fun getDataInputStream(player: Player): DataInputStream {
        val url = URI("http://192.168.0.18:81/PlayerData/${player.uniqueId}.yml").toURL()
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            allowUserInteraction = false
            instanceFollowRedirects = true
            requestMethod = "GET"
            connect()
        }
        if (conn.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP Status ${conn.responseCode}")
        }
        return DataInputStream(conn.inputStream)
    }

    private fun getString(args: Array<out String>): String {
        val format = "%.1f"
        val multiply = args.getOrNull(2)?.toDoubleOrNull() ?: 1.0
        val perforate = args.getOrNull(3)?.toDoubleOrNull() ?: 0.0
        val atk = args.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val def = args.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        val damage = ((atk.pow(2) / (atk + def * 4)) * (1 - perforate)) + (atk * perforate)
        return "§cDamageSimulator§7: §a${format.format(damage * multiply)} §8(${format.format(damage)}) §f[${multiply * 100}]"
    }

    private val nextSpawnPlayer = mutableSetOf<Player>()

    fun spawnPlayer(player: Player) {
        if (nextSpawnPlayer.add(player)) {
            val playerData = playerData(player)
            playerData.Skill.setCastReady(true)
            playerData.Skill.SkillProcess.normalAttackCoolTime = 0

            MultiThread.TaskRunSynchronizedLater({
                MapList["Alden"]?.enter(player)
                player.apply {
                    isFlying = false
                    setGravity(true)
                    teleportAsync(SpawnLocation)
                }
                nextSpawnPlayer.remove(player)
            }, 1, "spawnPlayer")
        }
    }



}
