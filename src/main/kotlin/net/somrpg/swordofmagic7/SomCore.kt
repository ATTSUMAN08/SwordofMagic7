package net.somrpg.swordofmagic7

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerCommon
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import com.google.gson.Gson
import de.bluecolored.bluemap.api.BlueMapAPI
import de.bluecolored.bluemap.api.markers.MarkerSet
import de.bluecolored.bluemap.api.markers.ShapeMarker
import de.bluecolored.bluemap.api.math.Color
import de.bluecolored.bluemap.api.math.Shape
import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import io.papermc.paper.entity.TeleportFlag
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.somrpg.swordofmagic7.commands.CommandManager
import net.somrpg.swordofmagic7.extensions.asyncDispatcher
import net.somrpg.swordofmagic7.lisiteners.PacketEventsListener
import net.somrpg.swordofmagic7.npc.NPCManager
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.persistence.PersistentDataType
import swordofmagic7.*
import swordofmagic7.Command.Developer.*
import swordofmagic7.Command.Player.*
import swordofmagic7.Command.SomCommand
import swordofmagic7.Data.DataBase.*
import swordofmagic7.Data.Editor
import swordofmagic7.Data.PlayerData
import swordofmagic7.Data.PlayerData.playerData
import swordofmagic7.Dungeon.DefenseBattle
import swordofmagic7.Dungeon.Dungeon
import swordofmagic7.Effect.EffectManager
import swordofmagic7.Function.*
import swordofmagic7.Map.TeleportGateParameter
import swordofmagic7.Map.WarpGateParameter
import swordofmagic7.Mob.MobManager
import swordofmagic7.MultiThread.MultiThread
import swordofmagic7.Particle.ParticleManager
import swordofmagic7.Pet.PetManager
import swordofmagic7.Sound.CustomSound.playSound
import swordofmagic7.Sound.SoundList
import swordofmagic7.TextView.TextViewManager
import swordofmagic7.Trade.TradeManager
import swordofmagic7.viewBar.ViewBar
import java.io.*
import java.security.SecureRandom
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class SomCore : SuspendingJavaPlugin() {
    companion object {
        lateinit var world: World
        lateinit var instance: SomCore
        lateinit var random: Random
        val restartNotifyTimes: Set<Int> = setOf(1800, 1200, 600, 300, 240, 180, 120, 60, 30, 20, 10, 5, 4, 3, 2, 1)
        val gson = Gson()
        var blueMapEnabled = false

        private const val BLUEMAP_SPAWNERS_MARKERS_ID = "som7_spawners"
        const val AFK_TIME_PERIOD = 1
        const val AFK_TIME = 300
        const val PLAYER_MAX_LEVEL = 65
        const val CLASS_MAX_LEVEL = 25

        fun isEventServer(): Boolean = ServerId.equals("Event", ignoreCase = true)
        fun isDevServer(): Boolean = ServerId.equals("Dev", ignoreCase = true)
    }
    lateinit var packetEventsListener: PacketListenerCommon
    private val hologramMap = HashMap<String, Hologram>()
    val hologramTouchActions = HashMap<String, (Player) -> Unit>()
    val playerLastLocation = HashMap<Player, Location>()
    val repeatingTaskScheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(10)
    

    fun createHologram(location: Location): Hologram {
        val hologram = DHAPI.createHologram("SOM7_${UUID.randomUUID()}", location)
        hologramMap[hologram.id] = hologram
        return hologram
    }

    private fun createTouchHologram(display: String, location: Location, action: (Player) -> Unit) {
        val hologram = createHologram(location)
        DHAPI.addHologramLine(hologram, display)
        hologramTouchActions[hologram.id] = action
    }

    override suspend fun onEnableAsync() {
        val time = System.currentTimeMillis()
        saveDefaultConfig()
        reloadConfig()
        instance = this
        random = SecureRandom()
        world = Bukkit.getWorld("world") ?: throw IllegalStateException("World not found")
        ServerId = config.getString("serverId") ?: "Default"
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")

        // Initialize folders
        if (!dataFolder.exists()) {
            createFolder(dataFolder)
        }
        val marketFolder = File(DataBasePath, "Market")
        if (!marketFolder.exists()) {
            createFolder(marketFolder)
        }

        DataLoad()

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
            setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            setGameRule(GameRule.SPAWN_RADIUS, 0)
            this.time = 6000L
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            val start = System.currentTimeMillis()
            broadcastNoConsole("§e[オートセーブ]§aを§b開始§aします")
            PlayerList.ResetPlayer.clear()
            val playerDataList = PlayerData.getPlayerData().values.toSet()
            playerDataList.forEach { data ->
                val player = data.player
                if (player != null) {
                    if (player.isOnline) data.save() else PlayerData.remove(player)
                }
            }
            broadcastNoConsole("§e[オートセーブ]§aが§b完了§aしました §7(${System.currentTimeMillis() - start}ms)")
        }, 200, 6000)

        MultiThread.TaskRunTimer({
            Bukkit.getOnlinePlayers().forEach { player ->
                if (!PlayerData.ContainPlayer(player) && !isDevServer()) {
                    sendMessage(player, "§cPlayer data not loaded")
                    teleportServer(player, "Lobby")
                } else {
                    val playerData = playerData(player)
                    playerLastLocation[player]?.let { location ->
                        if (location.world == player.location.world && location.distance(player.location) < 2) {
                            playerData.AFKTime += AFK_TIME_PERIOD
                            playerData.statistics.AFKTime += AFK_TIME_PERIOD
                            if (playerData.isAFK) {
                                player.showTitle(
                                    Title.title(
                                        Component.text("§eAFKTime: §a${playerData.AFKTime} seconds"),
                                        Component.empty(),
                                        Title.Times.times(
                                            Duration.ZERO,
                                            Duration.ofSeconds(AFK_TIME_PERIOD + 4L),
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
        createTouchHologram("", Location(world, -196.2, 24.0, 1187.5, 90F, 0F)) { player ->
            playerData(player).Menu.Smith.SmithMenuView()
        }
        createTouchHologram("", Location(world, -203.5, 20.0, 1112.0, 0F, 0F)) { player ->
            playerData(player).Menu.Cook.CookMenuView()
        }
        createTouchHologram("", Location(world, -207.5, 20.0, 1112.0, 0F, 0F)) { player ->
            playerData(player).Menu.Cook.CookMenuView()
        }

        Bukkit.getScheduler().runTaskLater(instance, Runnable {
            Bukkit.getOnlinePlayers().forEach { playerData(it).load() }
        }, 20)

        commandRegister()
        CommandManager.registerCommands()
        NPCManager.spawnAllNPCs()

        initWarpGate()
        initTeleportGate()
        initEffectManager()
        initPlayerThread()

        if (server.pluginManager.isPluginEnabled("BlueMap")) {
            blueMapEnabled = true
            initBlueMap()
        }

        logger.info("Plugin Enabled: ${System.currentTimeMillis() - time}ms")
    }

    override suspend fun onDisableAsync() {
        deleteHolograms()
        Bukkit.getOnlinePlayers().forEach {
            it.closeInventory()
            it.sendMessage("§cSystem Reloading")
        }

        delay(100)

        // Save all player data
        Bukkit.getOnlinePlayers().forEach {
            playerData(it).save()
        }

        delay(100)

        var count = 0
        MobManager.getEnemyList().forEach { enemyData ->
            enemyData.entity?.remove()
            count++
        }
        world.entities.filterNot { it is Player || ignoreEntity(it) }.forEach {
            it.remove()
            count++
        }
        world.entities.filter {
            it.persistentDataContainer.getOrDefault(NamespacedKey(instance, "som7entity"), PersistentDataType.BOOLEAN, false) == true
        }.forEach { entity ->
            entity.remove()
            count++
        }
        Log("CleanEnemy: $count")
        Bukkit.getScheduler().cancelTasks(this)
        Log("Plugin Task Cancelled")
        PacketEvents.getAPI().eventManager.unregisterListener(packetEventsListener)
        Log("PacketListener unregister")

        if (blueMapEnabled) {
            val blueMapApi = BlueMapAPI.getInstance().get()
            blueMapApi.getWorld(world).get().maps.map { map ->
                val markerSet = map.markerSets[BLUEMAP_SPAWNERS_MARKERS_ID]
                markerSet?.markers?.clear()
            }
        }

        repeatingTaskScheduler.shutdown()
    }

    private fun deleteHolograms() {
        hologramMap.values.forEach { hologram ->
            if (!hologram.isDisabled) {
                DHAPI.removeHologram(hologram.id)
            }
        }
    }

    private fun initBlueMap() {
        launch(asyncDispatcher) {
            val blueMapApi by lazy {
                BlueMapAPI.getInstance().get()
            }
            repeat(15) { // BlueMapの初期化を待つ 最大30秒
                if (BlueMapAPI.getInstance().isPresent) return@repeat
                delay(2000)
            }
            blueMapApi.getWorld(world).get().maps.map { map ->
                map.markerSets.put(BLUEMAP_SPAWNERS_MARKERS_ID, MarkerSet.builder().label("スポナー").build())
            }

            for (i in MobSpawnerList.values) {
                val loc = i.location
                val startX = loc.x - i.Radius
                val endX = loc.x + i.Radius
                val startZ = loc.z - i.Radius
                val endZ = loc.z + i.Radius
                val border = Shape.createRect(startX, startZ, endX, endZ)
                val shapeMarker = ShapeMarker.builder()
                    .label("${i.mobData.Id} (${i.Level}Lv)")
                    .shape(border, 1F)
                    .lineColor(Color(255, 0, 0, 1.0F))
                    .fillColor(Color(200, 0, 0, 0.3F))
                    .lineWidth(3)
                    .depthTestEnabled(false)
                    .build()

                blueMapApi.getWorld(world).get().maps.map { map ->
                    val markerSet = map.markerSets[BLUEMAP_SPAWNERS_MARKERS_ID] ?: throw IllegalStateException("MarkerSet not found WTF")

                    markerSet.markers.put(i.Id, shapeMarker)

                    map.markerSets.put(BLUEMAP_SPAWNERS_MARKERS_ID, markerSet)
                }
            }

            logger.info("BlueMapのマーカーセットを初期化しました")
        }
    }

    private fun createRepeatTask(delay: Long, threadName: String, block: () -> Unit) {
        repeatingTaskScheduler.execute {
            Thread.currentThread().name = "SwordofMagic7-Thread - $threadName"
            while (isEnabled) {
                block()
                Thread.sleep(delay)
            }
        }
    }

    private fun initWarpGate() {
        val counts = mutableMapOf<WarpGateParameter, Int>()
        val increment = (2 * Math.PI) / 90
        val radius = 2.0

        createRepeatTask(10, "WarpGateParticle") {
            for (warpGate in WarpGateList.values) {
                if (!warpGate.isStarted) continue
                val i = counts[warpGate] ?: 0
                val angle = i * increment
                val x = radius * cos(angle)
                val z = radius * sin(angle)
                val nLoc = Location(world, warpGate.location.x + x, warpGate.location.y, warpGate.location.z + z)
                val nLoc2 = Location(world, warpGate.location.x - x, warpGate.location.y, warpGate.location.z - z)
                ParticleManager.spawnParticle(warpGate.particleData, nLoc)
                ParticleManager.spawnParticle(warpGate.particleData, nLoc2)
                counts[warpGate] = i + 1
                if ((i+1) >= 90) {
                    counts[warpGate] = 0
                }
            }
        }
    }

    private fun initTeleportGate() {
        val counts = mutableMapOf<TeleportGateParameter, Int>()
        val increment = (2 * Math.PI) / 90
        val radius = 1.5

        createRepeatTask(25, "TeleportGateParticle") {
            for (teleportGate in TeleportGateList.values) {
                val i = counts[teleportGate] ?: 0
                val angle = i * increment
                val x = radius * cos(angle)
                val z = radius * sin(angle)
                val nLoc = Location(world, teleportGate.Location.x + x, teleportGate.Location.y, teleportGate.Location.z + z)
                val nLoc2 = Location(world, teleportGate.Location.x - x, teleportGate.Location.y, teleportGate.Location.z - z)
                ParticleManager.spawnParticle(teleportGate.particleData, nLoc)
                ParticleManager.spawnParticle(teleportGate.particleData, nLoc2)
                counts[teleportGate] = i + 1
                if ((i+1) >= 90) {
                    counts[teleportGate] = 0
                }
            }
        }
    }

    private fun initEffectManager() {
        val effectManagerPeriod = EffectManager.period * 50 // tickをミリ秒に変換

        createRepeatTask(effectManagerPeriod, "EffectManager_EnemyData") {
            for (enemyData in MobManager.EnemyTable.values) {
                if (enemyData == null) continue
                val effectManager = enemyData.effectManager
                if (effectManager.isRunnable && enemyData.isAlive) {
                    effectManager.onTaskRun()
                }
            }
        }

        createRepeatTask(effectManagerPeriod, "EffectManager_PlayerData") {
            for (data in playerData.values) {
                if (data == null) continue
                val effectManager = data.EffectManager
                if (effectManager.isRunnable && effectManager.playerData.player.isOnline) {
                    effectManager.onTaskRun()
                }
            }
        }

        createRepeatTask(effectManagerPeriod, "EffectManager_PetData") {
            for (data in PetManager.PetSummonedList.values) {
                if (data == null) continue
                val effectManager = data.effectManager
                if (effectManager.isRunnable && effectManager.petParameter.player.isOnline) {
                    effectManager.onTaskRun()
                }
            }
        }
    }

    private fun initPlayerThread() {
        createRepeatTask(500, "PlayerBossBarUpdate") {
            for (playerData in playerData.values) {
                if (!playerData.player.isOnline) continue
                if (!playerData.bossBarInitialized) continue

                playerData.updateBossbar()
            }
        }


        createRepeatTask(50, "SkillCoolTime") {
            for (playerData in playerData.values) {
                if (!playerData.player.isOnline) continue
                if (playerData.Skill == null) continue

                playerData.Skill.onTick()
            }
        }

        createRepeatTask(1000, "PlayerInstantBuff") {
            for (playerData in playerData.values) {
                if (!playerData.player.isOnline) continue
                if (playerData.instantBuff == null) continue

                playerData.instantBuff.onSecond()
            }
        }

        createRepeatTask(ViewBar.period * 50, "PlayerTickUpdate") {
            for (playerData in playerData.values) {
                if (!playerData.player.isOnline) continue
                if (!playerData.ViewBar.tickUpdate) continue

                playerData.ViewBar.onTickUpdate()
            }
        }

        createRepeatTask(1000, "PlayerPetInventory") {
            for (playerData in playerData.values) {
                if (!playerData.player.isOnline) continue
                if (playerData.PetInventory == null) continue
                if (playerData.PetInventory.List.isEmpty()) continue

                for (pet in playerData.PetInventory.List) {
                    if (!pet.Summoned) {
                        pet.changeStamina(1)
                    }
                    pet.Health += pet.HealthRegen / 5
                    pet.Mana += pet.ManaRegen / 5
                    if (pet.Health > pet.MaxHealth) pet.Health = pet.MaxHealth
                    if (pet.Mana > pet.MaxMana) pet.Mana = pet.MaxMana
                }
                if (playerData.ViewInventory.isPet) {
                    playerData.PetInventory.viewPet()
                }
            }
        }
    }

    private fun commandRegister() {
        //Developer
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

            when (cmd.name.lowercase()) {
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
                "settitle" -> {
                    if (args.size == 1) {
                        val title = TitleDataList[args[0]]
                        if (title != null) {
                            playerData.titleManager.setTitle(title)
                        } else {
                            sender.sendMessage("§a存在しない称号です")
                            playSound(sender, SoundList.NOPE)
                        }
                    } else {
                        playerData.titleManager.Title = TitleDataList["称号無し"]
                        sender.sendMessage("§a称号を外しました")
                        playSound(sender, SoundList.TICK)
                    }
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
                    playSound(sender, SoundList.TICK)
                    return true
                }
                "nickreset" -> {
                    playerData.Nick = sender.name
                    sendMessage(sender, "§eプレイヤ名§aを§e[${playerData.nick}]§aに§cリセット§aしました", SoundList.TICK)
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
                    sendMessage(sender, "§e[インベントリサーチ] §b-> §e[アイテム] §b-> §e[${playerData.ItemInventory.wordSearch ?: "すべて"}]", SoundList.TICK)
                    playerData.viewUpdate()
                    return true
                }
                "runesearch" -> {
                    playerData.RuneInventory.wordSearch = args.getOrNull(0)
                    sendMessage(sender, "§e[インベントリサーチ] §b-> §e[ルーン] §b-> §e[${playerData.RuneInventory.wordSearch ?: "すべて"}]", SoundList.TICK)
                    playerData.viewUpdate()
                    return true
                }
                "petsearch" -> {
                    playerData.PetInventory.wordSearch = args.getOrNull(0)
                    sendMessage(sender, "§e[インベントリサーチ] §b-> §e[ペット] §b-> §e[${playerData.PetInventory.wordSearch ?: "すべて"}]", SoundList.TICK)
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
                    teleportAsync(SpawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS)
                }
                nextSpawnPlayer.remove(player)
            }, 1, "spawnPlayer")
        }
    }

    fun createTextDisplay(loc: Location, defaultText: Component): TextDisplay {
        val textDisplayLocation = loc.clone().apply { pitch = 0F }
        val textDisplay = loc.world.spawnEntity(textDisplayLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplay.billboard = Display.Billboard.VERTICAL
        textDisplay.backgroundColor = org.bukkit.Color.fromARGB(0, 0, 0, 0)
        textDisplay.text(defaultText)
        textDisplay.persistentDataContainer[NamespacedKey(instance, "som7entity"), PersistentDataType.BOOLEAN] = true
        return textDisplay
    }



}
