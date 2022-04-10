package swordofmagic7;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.DataLoader;
import swordofmagic7.Data.Editor;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Dungeon.DefenseBattle;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Life.LifeStatus;
import swordofmagic7.Map.WarpGateParameter;
import swordofmagic7.Market.Market;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextViewManager;
import swordofmagic7.Trade.TradeManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Data.PlayerData.playerDataList;
import static swordofmagic7.Function.*;
import static swordofmagic7.Party.PartyManager.partyCommand;
import static swordofmagic7.Sound.CustomSound.playSound;

public final class SomCore extends JavaPlugin implements PluginMessageListener {

    public static Plugin plugin;
    public static JavaPlugin javaPlugin;
    public static final Random random = new Random();
    public static final Set<Hologram> HologramSet = new HashSet<>();
    public static final HashMap<Player, Location> PlayerLastLocation = new HashMap<>();
    public static final int AFKTimePeriod = 2;
    public static final int AFKTime = 72000;

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
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerData(player).load();
            }
        }, 5);

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

        BTTSet(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            BroadCast("§e[オートセーブ]§aを§b開始§aします");
            for (PlayerData playerData : new HashSet<>(PlayerData.playerDataList().values())) {
                Player player = playerData.player;
                if (player.isOnline()) {
                    playerData.saveCloseInventory();
                } else {
                    playerData.remove();
                }
            }
            BroadCast("§e[オートセーブ]§aが§b完了§aしました");
            HologramSet.removeIf(Hologram::isDeleted);
        }, 200, 6000), "AutoSave");

        /*
        MultiThread.TaskRunTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = PlayerData.playerData(player);
                if (!playerData.Map.Safe && !player.isOp()) {
                    if (PlayerLastLocation.containsKey(player)) {
                        Location location = PlayerLastLocation.get(player);
                        if (location.distance(player.getLocation()) < 1) {
                            playerData.AFKTime += AFKTimePeriod;
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
         */

        ParticleManager.onLoad();

        createTouchHologram("§e§l鍛冶場", new Location(world, 1149.5, 97.75, 17.5), (Player player) -> playerData(player).Menu.Smith.SmithMenuView());
        createTouchHologram("§e§l料理場", new Location(world, 1159.5, 94.5, 66.5), (Player player) -> playerData(player).Menu.Cook.CookMenuView());
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
            HashMap<UUID, PlayerData> list = playerDataList();
            if (list.containsKey(player.getUniqueId())) {
                PlayerData playerData = list.get(player.getUniqueId());
                playerData.save();
                for (PetParameter pet : playerData.PetSummon) {
                    pet.entity.remove();
                }
            }
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

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("SomReload")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                CloseInventory(player);
            }
            for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
                if (!hologram.isDeleted()) hologram.delete();
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getServer().dispatchCommand(sender, "plugman reload swordofmagic7"), 5);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("sendData")) {
            if (args.length == 1) {
                Client.send(args[0]);
            } else {
                Log("/sendData <text>");
            }
            return true;
        }
        if (sender instanceof Player player) {
            PlayerData playerData = playerData(player);
            if (player.hasPermission("som7.developer")) {
                if (cmd.getName().equalsIgnoreCase("test")) {
                    MultiThread.TaskRun(() -> {

                    }, "RayTest");
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("get")) {
                    if (args.length >= 1) {
                        if (getItemList().containsKey(args[0])) {
                            int amount = 1;
                            if (args.length == 2) amount = Integer.parseInt(args[1]);
                            playerData.ItemInventory.addItemParameter(getItemParameter(args[0]), amount);
                            playerData.ItemInventory.viewInventory();
                            return true;
                        }
                    }
                    for (Map.Entry<String, ItemParameter> str : getItemList().entrySet()) {
                        player.sendMessage(str.getKey());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("getRune")) {
                    if (args.length >= 1) {
                        if (getRuneList().containsKey(args[0])) {
                            RuneParameter rune = getRuneParameter(args[0]);
                            rune.Level = 1;
                            if (args.length >= 2) rune.Level = Integer.parseInt(args[1]);
                            if (args.length >= 3) rune.Quality = Double.parseDouble(args[2]);
                            playerData.RuneInventory.addRuneParameter(rune);
                            playerData.RuneInventory.viewRune();
                            return true;
                        }
                    }
                    for (Map.Entry<String, RuneParameter> str : DataBase.getRuneList().entrySet()) {
                        player.sendMessage(str.getKey());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("mobSpawn")) {
                    if (args.length >= 1) {
                        if (getMobList().containsKey(args[0])) {
                            int level = 1;
                            int perSpawn = 1;
                            if (args.length == 2) level = Integer.parseInt(args[1]);
                            if (args.length == 3) perSpawn = Integer.parseInt(args[2]);
                            for (int i = 0; i < perSpawn; i++){
                                MobManager.mobSpawn(getMobData(args[0]), level, player.getLocation());
                            }
                            return true;
                        }
                    }
                    for (Map.Entry<String, MobData> str : getMobList().entrySet()) {
                        player.sendMessage(str.getKey());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("save")) {
                    Player target = player;
                    if (args.length == 1) {
                        target = Bukkit.getPlayer(args[0]);
                    }
                    if (target.isOnline()) {
                        playerData(target).save();
                    } else {
                        player.sendMessage("§c無効なプレイヤーです");
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("load")) {
                    Player target = player;
                    if (args.length == 1) {
                        target = Bukkit.getPlayer(args[0]);
                    }
                    if (target.isOnline()) {
                        playerData(target).load();
                    } else {
                        player.sendMessage("§c無効なプレイヤーです");
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("loadFromFileServer")) {
                    Player target = player;
                    if (args.length == 1) {
                        target = Bukkit.getPlayer(args[0]);
                    }
                    if (target.isOnline()) {
                        FileClient.requestPlayerData(playerData);
                    } else {
                        player.sendMessage("§c無効なプレイヤーです");
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("bukkitTasks")) {
                    HashMap<String, Integer> sync = new HashMap<>();
                    HashMap<String, Integer> async = new HashMap<>();
                    if (BukkitTaskTag != null) {
                        BukkitTaskTag.keySet().removeIf(BukkitTask::isCancelled);
                        for (Map.Entry<BukkitTask, String> task : BukkitTaskTag.entrySet()) {
                            String[] split = task.getValue().split(":");
                            if (task.getKey().isSync()) sync.merge(task.getValue(), 1, Integer::sum);
                            else async.merge(task.getValue(), 1, Integer::sum);
                        }
                    }
                    player.sendMessage("PendingTask: " + Bukkit.getScheduler().getPendingTasks().size());
                    player.sendMessage("TaggedTask: " + BukkitTaskTag.size());
                    player.sendMessage("AsyncTask: " + async.size());
                    for (Map.Entry<String, Integer> tagCount : async.entrySet()) {
                        player.sendMessage("・" + tagCount.getKey() + ": " + tagCount.getValue());
                    }
                    player.sendMessage("SyncTask: " + sync.size());
                    for (Map.Entry<String, Integer> tagCount : sync.entrySet()) {
                        player.sendMessage("・" + tagCount.getKey() + ": " + tagCount.getValue());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("loadedPlayer")) {
                    player.sendMessage("Loaded PlayerData: ");
                    HashMap<UUID, PlayerData> list = playerDataList();
                    for (Map.Entry<UUID, PlayerData> loopData : list.entrySet()) {
                        player.sendMessage(Bukkit.getOfflinePlayer(loopData.getKey()).getName() + ": " + loopData.getKey());
                    }
                    return true;
                }  else if (cmd.getName().equalsIgnoreCase("getExp")) {
                    if (args.length >= 1) {
                        try {
                            playerData.addPlayerExp(Integer.parseInt(args[0]));
                        } catch (Exception e) {
                            player.sendMessage("§c" + "/getExp <exp>");
                        }
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("getLevel")) {
                    if (args.length >= 1) {
                        try {
                            playerData.addPlayerLevel(Integer.parseInt(args[0]));
                        } catch (Exception e) {
                            player.sendMessage("§c" + "/getLevel <exp>");
                        }
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("getClassExp")) {
                    if (args.length == 2 && getClassList().containsKey(args[1])) {
                        try {
                            playerData.Classes.addClassExp(getClassData(args[1]), Integer.parseInt(args[0]));
                        } catch (Exception e) {
                            player.sendMessage("§c" + "/getClassExp <exp> <class>");
                        }
                    } else {
                        player.sendMessage("§c" + "/getClassExp <exp> <class>");
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("getLevel")) {
                    if (args.length == 2 && getClassList().containsKey(args[1])) {
                        try {
                            playerData.Classes.addClassLevel(getClassData(args[1]), Integer.parseInt(args[0]));
                        } catch (Exception e) {
                            player.sendMessage("§c" + "/getLevel <level> <class>");
                        }
                    } else {
                        player.sendMessage("§c" + "/getLevel <level> <class>");
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("getEffect")) {
                    if (args.length >= 1) {
                        try {
                            int time = 200;
                            if (args.length >= 2) {
                                time = Integer.parseInt(args[1]);
                            }
                            playerData.EffectManager.addEffect(EffectType.valueOf(args[0]), time);
                        } catch (Exception e) {
                            player.sendMessage("§c" + "/getEffect <effect> [<time=200>]");
                        }
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("setItemEquipmentStatusMultiply")) {
                    if (args.length == 2) {
                        try {
                            for (EquipmentCategory category : EquipmentCategory.values()) {
                                String itemId = args[0] + category.Display2;
                                if (DataBase.ItemList.containsKey(itemId)) {
                                    File file = getItemParameter(itemId).File;
                                    FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                                    data.set("StatusMultiply", Double.parseDouble(args[1]));
                                    data.save(file);
                                    player.sendMessage(itemId + " Change to Saved");
                                }
                            }
                        } catch (Exception e) {
                            player.sendMessage("/setItemEquipmentStatusMultiply <Series> <StatusMultiply>");
                        }
                    }
                } else if (cmd.getName().equalsIgnoreCase("itemDataEdit")) {
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
                } else if (cmd.getName().equalsIgnoreCase("classSelect")) {
                    try {
                        playerData.Classes.classSlot[Integer.parseInt(args[0])] = getClassData(args[1]);
                    } catch (Exception e) {
                        player.sendMessage("/classSelect <slot> <class>");
                    }
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
                }
            }

            if (player.hasPermission("som7.builder")) {
                if (cmd.getName().equalsIgnoreCase("gm")) {
                    if (args.length == 0) {
                        if (player.getGameMode().equals(GameMode.CREATIVE)) {
                            player.setGameMode(GameMode.SURVIVAL);
                        } else {
                            player.setGameMode(GameMode.CREATIVE);
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("0")) {
                            player.setGameMode(GameMode.SURVIVAL);
                        } else if (args[0].equalsIgnoreCase("1")) {
                            player.setGameMode(GameMode.CREATIVE);
                        } else if (args[0].equalsIgnoreCase("2")) {
                            player.setGameMode(GameMode.ADVENTURE);
                        } else if (args[0].equalsIgnoreCase("3")) {
                            player.setGameMode(GameMode.SPECTATOR);
                        }
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("playMode")) {
                    playerData.PlayMode = !playerData.PlayMode;
                    if (playerData.PlayMode) {
                        player.setGameMode(GameMode.SURVIVAL);
                        player.closeInventory();
                    } else {
                        player.setGameMode(GameMode.CREATIVE);
                        player.getInventory().clear();
                    }
                    player.sendMessage("§ePlayMode: " + playerData.PlayMode);
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("flySpeed")) {
                    if (args.length == 1) {
                        player.setFlySpeed(Float.parseFloat(args[0]));
                    } else {
                        player.setFlySpeed(0.2f);
                    }
                    player.sendMessage("FlySpeed: " + player.getFlySpeed());
                    return true;
                }
            }

            if (player.hasPermission("som7.title.editor")) {
                if (cmd.getName().equalsIgnoreCase("titleReload")) {
                    DataLoader.TitleDataLoad();
                    Log("§aDataLoader -> TitleLoad");
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
            } else if (cmd.getName().equalsIgnoreCase("info")) {
                Player target = player;
                if (args.length == 1 && Bukkit.getPlayer(args[0]) != null) {
                    target = Bukkit.getPlayer(args[0]);
                }
                playerData.Menu.StatusInfo.StatusInfoView(target);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("tickTime")) {
                for (World world : Bukkit.getWorlds()) {
                    player.sendMessage("§e" + world.getName() + "§7: §a" + world.getFullTime());
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("reqExp")) {
                if (args.length == 1) {
                    try {
                        int level = Integer.parseInt(args[0]);
                        int reqExp = Classes.ReqExp(level);
                        player.sendMessage("§eLv" + level + "§7: §a" + reqExp);
                    } catch (Exception ignored) {
                        player.sendMessage("§e/reqExp <Level>");
                    }
                } else {
                    player.sendMessage("§e/reqExp <Level>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("reqExpAll")) {
                if (args.length == 1) {
                    try {
                        int level = Integer.parseInt(args[0]);
                        if (level > PlayerData.MaxLevel) level = PlayerData.MaxLevel;
                        int reqExp = 0;
                        for (int i = 1; i < level; i++) {
                            reqExp += Classes.ReqExp(level);
                        }
                        player.sendMessage("§eLv" + level + "§7: §a" + reqExp);
                    } catch (Exception ignored) {
                        player.sendMessage("§e/reqExpAll <Level>");
                    }
                } else {
                    player.sendMessage("§e/reqExpAll <Level>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("reqLifeExp")) {
                if (args.length == 1) {
                    try {
                        int level = Integer.parseInt(args[0]);
                        int reqExp = LifeStatus.LifeReqExp(level);
                        player.sendMessage("§eLv" + level + "§7: §a" + reqExp);
                    } catch (Exception ignored) {
                        player.sendMessage("§e/reqLifeExp <Level>");
                    }
                } else {
                    player.sendMessage("§e/reqLifeExp <Level>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("reqLifeExpAll")) {
                if (args.length == 1) {
                    try {
                        int level = Integer.parseInt(args[0]);
                        if (level > LifeStatus.MaxLifeLevel) level = LifeStatus.MaxLifeLevel;
                        int reqExp = 0;
                        for (int i = 1; i < level; i++) {
                            reqExp += LifeStatus.LifeReqExp(level);
                        }
                        player.sendMessage("§eLv" + level + "§7: §a" + reqExp);
                    } catch (Exception ignored) {
                        player.sendMessage("§e/reqLifeExpAll <Level>");
                    }
                } else {
                    player.sendMessage("§e/reqLifeExpAll <Level>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("tagGame")) {
                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("join")) {
                        TagGame.join(player);
                    } else if (args[0].equalsIgnoreCase("leave")) {
                        TagGame.leave(player);
                    }
                } else {
                    for (String str : TagGame.info()) {
                        player.sendMessage(str);
                    }
                    player.sendMessage("§e/tagGame [join/leave]");
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
            } else if (cmd.getName().equalsIgnoreCase("effectInfo")) {
                if (args.length == 1) {
                    for (EffectType effectType : EffectType.values()) {
                        if (effectType.Display.equals(args[0])) {
                            player.sendMessage(decoText(effectType.Display));
                            for (String str : effectType.Lore) {
                                player.sendMessage("§a" + str);
                            }
                            return true;
                        }
                    }
                }
                sendMessage(player, "§e/effectInfo <効果名>");
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
            } else if (cmd.getName().equalsIgnoreCase("auction")) {
                Auction.auctionCommand(playerData, args);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("market")) {
                Market.marketCommand(playerData, args);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("mobInfo")) {
                if (args.length >= 1) {
                    if (MobList.containsKey(args[0])) {
                        MobData mobData = getMobData(args[0]);
                        List<String> message = new ArrayList<>();
                        message.add(decoText(mobData.Display));
                        message.addAll(playerData.Menu.mobInfo.toStringList(mobData));
                        if (args.length == 2) {
                            message.addAll(EnemyData.enemyLore(mobData, Integer.parseInt(args[1])));
                        }
                        sendMessage(player, message, SoundList.Nope);
                    } else {
                        sendMessage(player, "§a存在しない§cエネミー§aです", SoundList.Nope);
                    }
                } else {
                    playerData.Menu.mobInfo.MobInfoView();
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("itemInfo")) {
                if (args.length == 1) {
                    if (ItemList.containsKey(args[0])) {
                        ItemParameter item = getItemParameter(args[0]);
                        List<String> list = new ArrayList<>();
                        list.add(decoText(item.Display));
                        list.addAll(ItemInfoData.get(item.Id));
                        sendMessage(player, list);
                    } else player.sendMessage("§a存在しない§eアイテム§aです");
                } else {
                    player.sendMessage("§e/itemInfo <ItemID>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("runeInfo")) {
                if (args.length >= 1) {
                    if (RuneList.containsKey(args[0])) {
                        RuneParameter rune = getRuneParameter(args[0]);
                        try {
                            if (args[1] != null) rune.Level = Math.min(Math.max(Integer.parseInt(args[1]), 1), PlayerData.MaxLevel);
                            if (args[2] != null) rune.Quality = Math.min(Math.max(Double.parseDouble(args[2])/100f, 0), 200);
                        } catch (Exception ignore) {}
                        ItemStack itemStack = rune.viewRune(playerData.ViewFormat());
                        List<String> list = new ArrayList<>();
                        list.add(itemStack.getItemMeta().getDisplayName());
                        list.addAll(itemStack.getLore());
                        sendMessage(player, list);
                    } else player.sendMessage("§a存在しない§eルーン§aです");
                } else {
                    player.sendMessage("§e/runeInfo <RuneID> [<Level>] [<0~200>]");
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
                    if (args[0].equalsIgnoreCase("Quality")) {
                        double value = Double.parseDouble(args[1])/100;
                        if (0 <= value && value <= 100) {
                            playerData.RuneQualityFilter = value;
                            sendMessage(player, "§eルーンフィルター[品質] §b-> §a" + value*100 + "%");
                            return true;
                        }
                    }
                } catch (Exception ignored) {}
                sendMessage(player, "§e/runeFilter Quality <0~100>");
                return true;
            } else if (cmd.getName().equalsIgnoreCase("entities")) {
                sendMessage(player, "EntityCount: " + player.getWorld().getEntityCount());
                return true;
            } else if (cmd.getName().equalsIgnoreCase("loadOnLiveServer")) {
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
            }
        }
        return false;
    }

    public static void spawnPlayer(Player player) {
        MultiThread.TaskRunSynchronized(() -> {
            MapList.get("Alden").enter(player);
            player.setFlying(false);
            player.setGravity(true);
            player.teleportAsync(SpawnLocation);
        }, "spawnPlayer");
    }

    public static HashMap<BukkitTask, String> BukkitTaskTag = new HashMap<>();
    public static void BTTSet(BukkitTask task, String tag) {
        BukkitTaskTag.put(task, tag);
    }

    @Override
    public void onPluginMessageReceived(@NonNull String channel, @NonNull Player player, byte[] message) {

    }
}