package swordofmagic7;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import swordofmagic7.Command.Builder.FlySpeed;
import swordofmagic7.Command.Builder.GameModeChange;
import swordofmagic7.Command.Builder.PlayMode;
import swordofmagic7.Command.Developer.*;
import swordofmagic7.Command.Player.*;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.DataLoader;
import swordofmagic7.Data.Editor;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Dungeon.DefenseBattle;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.Map.WarpGateParameter;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextViewManager;
import swordofmagic7.Trade.TradeManager;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Party.PartyManager.partyCommand;
import static swordofmagic7.Sound.CustomSound.playSound;

public final class SomCore extends JavaPlugin implements PluginMessageListener {

    public static Plugin plugin;
    public static JavaPlugin javaPlugin;
    public static final Random random = new Random();
    public static final Set<Hologram> HologramSet = new HashSet<>();
    public static final HashMap<Player, Location> PlayerLastLocation = new HashMap<>();
    public static final int AFKTimePeriod = 1;
    public static final int AFKTime = 600;//1800;

    public static Hologram createHologram(Location location) {
        Hologram hologram = HologramsAPI.createHologram(plugin, location);
        HologramSet.add(hologram);
        return hologram;
    }

    public static Hologram createTouchHologram(String Display, Location location, TouchHandler touchHandler) {
        Hologram hologram = createHologram(location);
        hologram.appendTextLine(Display).setTouchHandler(touchHandler);
        return hologram;
    }

    public static boolean isEventServer() {
        return ServerId.equalsIgnoreCase("Event");
    }

    public static boolean isDevServer() {
        return ServerId.equalsIgnoreCase("Dev");
    }

    public static boolean isDevEventServer() {
        return isEventServer() || isDevServer();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        plugin = this;
        javaPlugin = this;
        ServerId = getConfig().getString("ServerId");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        Client.Host = getConfig().getString("Host", "localhost");
        Client.connect();
        //FileClient.connect();

        Tutorial.onLoad();

        new Events(this);
        DataLoad();
        Dungeon.Initialize();

        PlayerList.load();

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        if (protocolManager != null) {
            protocolManager.addPacketListener(new PacketListener(plugin, PacketType.Play.Server.BLOCK_CHANGE));
            protocolManager.addPacketListener(new PacketListener(plugin, PacketType.Play.Server.WORLD_PARTICLES));
            protocolManager.addPacketListener(new PacketListener(plugin, PacketType.Play.Server.STOP_SOUND));
            protocolManager.addPacketListener(new PacketListener(plugin, PacketType.Play.Server.CUSTOM_SOUND_EFFECT));
            protocolManager.addPacketListener(new PacketListener(plugin, PacketType.Play.Server.ENTITY_SOUND));
            protocolManager.addPacketListener(new PacketListener(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT));
        }

        for (WarpGateParameter warp : WarpGateList.values()) {
            warp.start();
        }

        World world = Bukkit.getWorld("world");
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000);

        BTTSet(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            BroadCast("§e[オートセーブ]§aを§b開始§aします");
            PlayerList.ResetPlayer.clear();
            Collection<PlayerData> PlayerDataList = new HashSet<>(PlayerData.getPlayerData().values());
            for (PlayerData playerData : PlayerDataList) {
                Player player = playerData.player;
                if (player != null) {
                    if (player.isOnline()) {
                        playerData.save();
                    } else {
                        PlayerData.remove(player);
                    }
                }
            }
            BroadCast("§e[オートセーブ]§aが§b完了§aしました");
            HologramSet.removeIf(Hologram::isDeleted);
        }, 200, 6000), "AutoSave");

        MultiThread.TaskRunTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!PlayerData.ContainPlayer(player) && !isDevServer()) {
                    sendMessage(player, "§cプレイヤーデータが読み込まれていません");
                    teleportServer(player, "Lobby");
                } else {
                    PlayerData playerData = PlayerData.playerData(player);
                    if (PlayerLastLocation.containsKey(player)) {
                        Location location = PlayerLastLocation.get(player);
                        if (location.distance(player.getLocation()) < 2) {
                            playerData.AFKTime += AFKTimePeriod;
                            playerData.statistics.AFKTime += AFKTimePeriod;
                            if (playerData.isAFK())
                                player.sendTitle("§eAFKTime: §a" + playerData.AFKTime + "秒", "", 0, AFKTimePeriod * 20 + 5, 0);
                        } else {
                            PlayerLastLocation.put(player, player.getLocation().clone());
                            playerData.AFKTime = 0;
                        }
                    } else {
                        PlayerLastLocation.put(player, player.getLocation().clone());
                        playerData.AFKTime = 0;
                    }
                }
            }
            PlayerLastLocation.keySet().removeIf(player -> !player.isOnline());
        }, AFKTimePeriod*20);

        ParticleManager.onLoad();

        createTouchHologram("§e§l鍛冶場", new Location(world, 1149.5, 97.75, 17.5), (Player player) -> playerData(player).Menu.Smith.SmithMenuView());
        createTouchHologram("§e§l料理場", new Location(world, 1159.5, 94.5, 66.5), (Player player) -> playerData(player).Menu.Cook.CookMenuView());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerData(player).load();
            }
        }, 20);

        commandRegister();
    }

    @Override
    public void onDisable() {

        MultiThread.closeMultiThreads();

        for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
            if (!hologram.isDeleted()) hologram.delete();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            player.sendMessage("§cシステムをリロードします");
        }

        int count = 0;
        for (EnemyData enemyData : MobManager.getEnemyList()) {
            if (enemyData.entity != null) enemyData.entity.remove();
            count++;
        }
        for (Entity entity : Bukkit.getWorld("world").getEntities()) {
            if (!(entity instanceof Player) && !ignoreEntity(entity)) {
                entity.remove();
                count++;
            }
        }
        Log("CleanEnemy: " + count);
    }

    public void commandRegister() {
        //Developer
        SomCommand.register("SomReload", new SomReload());
        SomCommand.register("SendData", new SendData());
        SomCommand.register("getItem", new GetItem());
        SomCommand.register("getRune", new GetRune());
        SomCommand.register("mobSpawn", new MobSpawn());
        SomCommand.register("setNick", new SetNick());
        SomCommand.register("save", new Save());
        SomCommand.register("load", new Load());
        SomCommand.register("loadedPlayer", new LoadedPlayer());
        SomCommand.register("getExp", new GetExp());
        SomCommand.register("getLevel", new GetLevel());
        SomCommand.register("getClassExp", new GetClassExp());
        SomCommand.register("getEffect", new GetEffect());
        SomCommand.register("bukkitTasks", new BukkitTasks());
        SomCommand.register("classSelect", new ClassSelect());
        SomCommand.register("skillCTReset", new SkillCTReset());
        //Builder
        SomCommand.register("gm", new GameModeChange());
        SomCommand.register("playMode", new PlayMode());
        SomCommand.register("flySpeed", new FlySpeed());
        //Player
        SomCommand.register("reqExp", new ReqExp());
        SomCommand.register("reqLifeExp", new ReqLifeExp());
        SomCommand.register("tagGame", new TagGameCommand());
        SomCommand.register("playerInfo", new playerInfo());
        SomCommand.register("party", new Party());
        SomCommand.register("effectInfo", new EffectInfo());
        SomCommand.register("itemInfo", new ItemInfo());
        SomCommand.register("runeInfo", new RuneInfo());
        SomCommand.register("mobInfo", new MobInfo());
        SomCommand.register("market", new MarketCommand());
        SomCommand.register("auction", new AuctionCommand());
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            PlayerData playerData = playerData(player);
            if (player.hasPermission("som7.developer")) {
                if (cmd.getName().equalsIgnoreCase("itemDataEdit")) {
                    Editor.itemDataEditCommand(player, args);
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("mobSpawnerDataEdit")) {
                    Editor.mobSpawnerDataEditCommand(player, args);
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("mobSpawnerDataCreate")) {
                    Editor.mobSpawnerDataCreateCommand(player, args);
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("defenseBattleStartWave")) {
                    int wave = 1;
                    if (args.length == 1) wave = Integer.parseInt(args[0]);
                    DefenseBattle.startWave(wave);
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("defenseBattleEndWave")) {
                    DefenseBattle.endWave();
                    return true;
                }
            }

            if (player.hasPermission("som7.data.reload")) {
                if (cmd.getName().equalsIgnoreCase("dataReload")) {
                    for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                        playerData(loopPlayer).save();
                    }
                    DataLoader.AllLoad();
                    for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                        playerData(loopPlayer).load();
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("itemReload")) {
                    DataLoader.ItemDataLoad();
                    DataLoader.ItemInfoDataLoad();
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("runeReload")) {
                    DataLoader.RuneDataLoad();
                    DataLoader.RuneInfoDataLoad();
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("skillReload")) {
                    DataLoader.SkillDataLoad();
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("shopReload")) {
                    DataLoader.ShopDataLoad();
                    return true;
                }
            }

            if (player.hasPermission("som7.title.editor")) {
                if (cmd.getName().equalsIgnoreCase("titleReload")) {
                    DataLoader.TitleDataLoad();
                    return true;
                }
            }

            if (cmd.getName().equalsIgnoreCase("menu") || cmd.getName().equalsIgnoreCase("m")) {
                playerData.Menu.UserMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("skill")) {
                playerData.Skill.SkillMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("attribute")) {
                playerData.Attribute.AttributeMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("damageLog")) {
                playerData.DamageLog();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("expLog")) {
                playerData.ExpLog();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("dropLog")) {
                playerData.DropLog();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("pvpMode")) {
                playerData.PvPMode();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("effectLog")) {
                playerData.EffectLog();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("strafeMode")) {
                playerData.StrafeMode();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("fishingDisplayNum")) {
                playerData.FishingDisplayNum();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("castMode")) {
                playerData.CastMode();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("viewFormat")) {
                playerData.changeViewFormat();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("holoSelfView")) {
                playerData.HoloSelfView();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("spawn")) {
                if (TagGame.isTagPlayerNonMessage(player)) return true;
                if (playerData.isPvPModeNonMessage()) return true;
                spawnPlayer(player);
                return true;
            } if (cmd.getName().equalsIgnoreCase("tickTime")) {
                for (World world : Bukkit.getWorlds()) {
                    player.sendMessage("§e" + world.getName() + "§7: §a" + world.getFullTime());
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("party")) {
                partyCommand(player, playerData, args);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("itemInventorySort")) {
                playerData.ItemInventory.ItemInventorySort();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("runeInventorySort")) {
                playerData.RuneInventory.RuneInventorySort();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("petInventorySort")) {
                playerData.PetInventory.PetInventorySort();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("itemInventorySortReverse")) {
                playerData.ItemInventory.ItemInventorySortReverse();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("runeInventorySortReverse")) {
                playerData.RuneInventory.RuneInventorySortReverse();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("petInventorySortReverse")) {
                playerData.PetInventory.PetInventorySortReverse();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("tutorial")) {
                if (TagGame.isTagPlayerNonMessage(player)) return true;
                Tutorial.tutorialHub(player);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("trade")) {
                TradeManager.tradeCommand(player, playerData, args);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("textView")) {
                TextViewManager.TextView(player, args);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("checkTitle")) {
                playerData.statistics.checkTitle();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("uuid")) {
                Player target;
                if (args.length == 1 && Bukkit.getPlayer(args[0]) != null) {
                    target = Bukkit.getPlayer(args[0]);
                } else {
                    target = player;
                }
                player.sendMessage(target.getName() + ": " + target.getUniqueId());
                return true;
            } else if (cmd.getName().equalsIgnoreCase("sideBarToDo")) {
                playerData.SideBarToDo.SideBarToDoCommand(args);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("setTitle")) {
                if (args.length == 1) {
                    if (TitleDataList.containsKey(args[0])) {
                        playerData.titleManager.setTitle(TitleDataList.get(args[0]));
                    } else {
                        player.sendMessage("§a存在しない称号です");
                        playSound(player, SoundList.Nope);
                    }
                } else {
                    playerData.titleManager.Title = TitleDataList.get("称号無し");
                    player.sendMessage("§a称号を外しました");
                    playSound(player, SoundList.Tick);
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("serverInfo")) {
                Runtime runtime = Runtime.getRuntime();
                int ex = 1048576;
                player.sendMessage(decoLore("UseRAM") + (runtime.totalMemory()-runtime.freeMemory())/ex);
                player.sendMessage(decoLore("FreeRAM") + runtime.freeMemory()/ex);
                player.sendMessage(decoLore("TotalRAM") + runtime.totalMemory()/ex);
                player.sendMessage(decoLore("MaxRAM") + runtime.maxMemory()/ex);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("setFishingCombo")) {
                if (!playerData.Gathering.FishingUseCombo) {
                    try {
                        int combo = Integer.parseInt(args[0]);
                        if (playerData.Gathering.FishingComboBoost > combo && combo > 0) {
                            playerData.Gathering.FishingSetCombo = combo;
                            player.sendMessage("§eComboを" + combo + "に設定しました");
                        } else {
                            player.sendMessage("§eCombo: 1 ~ " + (playerData.Gathering.FishingComboBoost - 1));
                        }
                    } catch (Exception ignored) {
                        player.sendMessage("§e/setFishingCombo <combo>");
                    }
                } else {
                    player.sendMessage("§a現在の§e[釣獲モード]§aでは利用できません");
                }
                playSound(player, SoundList.Tick);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("ch")) {
                if (playerData.isPlayDungeonQuest) {
                    sendMessage(player, "§cダンジョンクエスト§a中は§eチャンネル§aを変更できません", SoundList.Nope);
                    return true;
                }
                if (args.length == 1) {
                    String teleportServer;
                    switch (args[0]) {
                        case "1" -> teleportServer = "CH1";
                        case "2" -> teleportServer = "CH2";
                        case "3" -> teleportServer = "CH3";
                        case "4" -> teleportServer = "CH4";
                        case "5" -> teleportServer = "CH5";
                        case "Ev", "ev", "Event", "event" -> teleportServer = "Event";
                        case "dev", "Dev" -> teleportServer = "Dev";
                        default -> {
                            player.sendMessage("存在しないチャンネルです");
                            return true;
                        }
                    }
                    playerData.saveTeleportServer = "Som7" + teleportServer;
                    playerData.save();
                } else {
                    player.sendMessage("§e/channel <channel>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("nickReset")) {
                playerData.Nick = player.getName();
                sendMessage(player, "§eプレイヤ名§aを§e[" + playerData.getNick() + "]§aに§cリセット§aしました", SoundList.Tick);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("skillSlot")) {
                playerData.HotBar.SkillSlotCommand(args);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("runeFilter")) {
                try {
                    if (args[0].equalsIgnoreCase("Quality") || args[0].equalsIgnoreCase("Q")) {
                        double value = Double.parseDouble(args[1])/100;
                        if (0 <= value && value <= 100) {
                            playerData.RuneQualityFilter = value;
                            sendMessage(player, "§eルーンフィルター[品質] §b-> §a" + value*100 + "%");
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("Id")) {
                        String runeId = args[1];
                        if (DataBase.getRuneList().containsKey(runeId)) {
                            if (playerData.RuneIdFilter.contains(runeId)) {
                                playerData.RuneIdFilter.remove(runeId);
                            } else {
                                playerData.RuneIdFilter.add(runeId);
                            }
                            sendMessage(player, "§eルーンフィルター[ID:" + runeId + "] §b-> §a" + (playerData.RuneIdFilter.contains(runeId) ? "§b有効" : "§c無効"));
                            return true;
                        } else {
                            sendMessage(player, "§a存在しない§eルーン§aです");
                        }
                    }
                } catch (Exception ignored) {}
                sendMessage(player, "§e/runeFilter Quality <0~100>");
                sendMessage(player, "§e/runeFilter Id <RuneId>");
                return true;
            } else if (cmd.getName().equalsIgnoreCase("entities")) {
                sendMessage(player, "EntityCount: " + player.getWorld().getEntityCount());
                return true;
            } else if (cmd.getName().equalsIgnoreCase("loadOnLiveServer")) {
                if (TagGame.isTagPlayerNonMessage(player)) return true;
                MultiThread.TaskRun(() -> {
                    if (ServerId.equalsIgnoreCase("Dev")) {
                        try {
                            URL url = new URL("http://192.168.0.18:81/PlayerData/" + player.getUniqueId() + ".yml");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setAllowUserInteraction(false);
                            conn.setInstanceFollowRedirects(true);
                            conn.setRequestMethod("GET");
                            conn.connect();
                            int httpStatusCode = conn.getResponseCode();
                            if (httpStatusCode != HttpURLConnection.HTTP_OK) {
                                throw new Exception("HTTP Status " + httpStatusCode);
                            }
                            String contentType = conn.getContentType();
                            System.out.println("Content-Type: " + contentType);
                            DataInputStream dataInStream = new DataInputStream(conn.getInputStream());
                            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(DataBasePath + "PlayerData\\" + player.getUniqueId() + ".yml")));
                            byte[] b = new byte[4096];
                            int readByte;
                            while (-1 != (readByte = dataInStream.read(b))) {
                                dataOutStream.write(b, 0, readByte);
                            }
                            dataInStream.close();
                            dataOutStream.close();
                            MultiThread.TaskRunSynchronizedLater(playerData::load, 5);
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(player, "§cデータのダウンロードに失敗しました");
                        }
                    } else {
                        sendMessage(player, "§b開発鯖§a以外では利用できません");
                    }
                }, "loadOnLiveServer");
                return true;
            } else if (cmd.getName().equalsIgnoreCase("setFastUpgrade")) {
                try {
                    playerData.Upgrade.fastUpgrade = Math.min(Math.max(Integer.parseInt(args[0]), 1), 25);
                    sendMessage(player, "§aFastUpgrade: " + playerData.Upgrade.fastUpgrade);
                } catch (Exception e) {
                    sendMessage(player, "§e/setFastUpgrade <1~25>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("cast")) {
                try {
                    int slot = Integer.parseInt(args[0])-1;
                    if (0 <= slot && slot <= 31) {
                        playerData.HotBar.use(slot);
                    } else {
                        sendMessage(player, "§e/cast <1~32>");
                    }
                } catch (Exception e) {
                    sendMessage(player, "§e/cast <1~32>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("itemSearch")) {
                if (args.length == 1) {
                    sendMessage(player, "§e[インベントリサーチ] §b-> §e[アイテム] §b-> §e[" + args[0] + "]", SoundList.Tick);
                    playerData.ItemInventory.wordSearch = args[0];
                } else {
                    sendMessage(player, "§e[インベントリサーチ] §b-> §e[アイテム] §b-> §e[すべて]", SoundList.Tick);
                    playerData.ItemInventory.wordSearch = null;
                }
                playerData.viewUpdate();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("runeSearch")) {
                if (args.length == 1) {
                    sendMessage(player, "§e[インベントリサーチ] §b-> §e[ルーン] §b-> §e[" + args[0] + "]", SoundList.Tick);
                    playerData.RuneInventory.wordSearch = args[0];
                } else {
                    sendMessage(player, "§e[インベントリサーチ] §b-> §e[ルーン] §b-> §e[すべて]", SoundList.Tick);
                    playerData.RuneInventory.wordSearch = null;
                }
                playerData.viewUpdate();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("petSearch")) {
                if (args.length == 1) {
                    sendMessage(player, "§e[インベントリサーチ] §b-> §e[ペット] §b-> §e[" + args[0] + "]", SoundList.Tick);
                    playerData.PetInventory.wordSearch = args[0];
                } else {
                    sendMessage(player, "§e[インベントリサーチ] §b-> §e[ペット] §b-> §e[すべて]", SoundList.Tick);
                    playerData.PetInventory.wordSearch = null;
                }
                playerData.viewUpdate();
                return true;
            }
        }
        return false;
    }

    private static final Set<Player> nextSpawnPlayer = new HashSet<>();
    public static void spawnPlayer(Player player) {
        if (!nextSpawnPlayer.contains(player)) {
            nextSpawnPlayer.add(player);
            MultiThread.TaskRunSynchronizedLater(() -> {
                MapList.get("Alden").enter(player);
                player.setFlying(false);
                player.setGravity(true);
                player.teleportAsync(SpawnLocation);
                nextSpawnPlayer.remove(player);
            }, 1, "spawnPlayer");
        }
    }

    public static HashMap<BukkitTask, String> BukkitTaskTag = new HashMap<>();
    public static void BTTSet(BukkitTask task, String tag) {
        BukkitTaskTag.put(task, tag);
    }

    @Override
    public void onPluginMessageReceived(@NonNull String channel, @NonNull Player player, byte[] message) {

    }
}