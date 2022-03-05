package swordofmagic7.Dungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.A;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.*;

import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Data.DataBase.getWarpGate;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.BTTSet;
import static swordofmagic7.System.plugin;

public class AusMine extends Dungeon{

    private static boolean AusMineB1 = false;
    private static boolean AusMineB1Start = false;
    private static int AusMineB1Time;
    private static int AusMineB1Count;
    private static final Location AusMineB1EventLocation = new Location(world,1145, 141, 1293);
    private static final Set<EnemyData> AusMineB1EnemyList = new HashSet<>();
    private static final Set<Player> AusMineB1Player = new HashSet<>();
    public static boolean AusMineB1() {
        if (!AusMineB1Start) {
            AusMineB1Start = true;
            AusMineB1Time = 180;
            AusMineB1Count = 15;
            for (Player player : PlayerList.getNear(AusMineB1EventLocation, Radius)) {
                player.sendMessage("§e[エレベーター]§aを動かそうとしたら§c[ゴブリン]§aが襲ってきました");
                player.sendMessage("§aこのままでは§e[エレベーター]§aを動かせません");
                player.sendMessage("§c[ゴブリン]§aを退治してください");
                triggerTitle(player, DungeonQuestTrigger, "§cゴブリン§aを§c" + AusMineB1Count + "体§a討伐せよ", SoundList.DungeonTrigger);
            }

            BTTSet(new BukkitRunnable() {
                @Override
                public void run() {
                    Set<Player> list = PlayerList.getNear(AusMineB1EventLocation, Radius);
                    AusMineB1Player.addAll(list);
                    if (list.size() == 0 || AusMineB1Time == 0) {
                        this.cancel();
                        AusMineB1End();
                        AusMineB1Start = false;
                        for (Player player : AusMineB1Player) {
                            triggerTitle(player, DungeonQuestFailed, "", SoundList.DungeonTrigger);
                        }
                        return;
                    }
                    for (EnemyData enemyData : new ArrayList<>(AusMineB1EnemyList)) {
                        if (enemyData.isDead) {
                            AusMineB1EnemyList.remove(enemyData);
                            AusMineB1Count--;
                        }
                    }
                    if (AusMineB1Count > 0) {
                        AusMineB1Time--;
                        if (AusMineB1EnemyList.size() < 3) {
                            Location loc = AusMineB1EventLocation.clone().add(random.nextDouble() * 20, 0, random.nextDouble() * 20);
                            AusMineB1EnemyList.add(MobManager.mobSpawn(getMobData("ゴブリン"), 15, loc));
                        }
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("残存敵数") + AusMineB1Count);
                        textData.add(decoLore("残り時間") + AusMineB1Time + "秒");
                        for (Player player : AusMineB1Player) {
                            playerData(player).ViewBar.setSideBar("AusMineB1", textData);
                        }
                    } else {
                        this.cancel();
                        AusMineB1End();
                        getWarpGate("AusMineB1_to_AusMineB2").ActiveAtTime(600);
                        elevatorActive(AusMineB1EventLocation, "ゴブリン");
                        AusMineB1 = true;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            AusMineB1 = false;
                            AusMineB1Start = false;
                        }, 600);
                    }
                }
            }.runTaskTimer(plugin, 0, 20), "AusMineB1DungeonQuest");
        }
        return !AusMineB1;
    }

    public static void AusMineB1End() {
        for (EnemyData enemyData : AusMineB1EnemyList) {
            enemyData.delete();
        }
        for (Player player : AusMineB1Player) {
            if (player.isOnline()) {
                playerData(player).ViewBar.resetSideBar("AusMineB1");
            }
        }
        AusMineB1EnemyList.clear();
        AusMineB1Player.clear();
    }


    private static final Location AusMineB2EventLocation = new Location(world,907, 81, 1457);
    private static boolean AusMineB2 = false;
    private static boolean AusMineB2Start = false;
    private static int AusMineB2Time;
    private static EnemyData AusMineB2Enemy;
    private static final Set<Player> AusMineB2Player = new HashSet<>();
    public static boolean AusMineB2() {
        if (!AusMineB2Start) {
            AusMineB2Start = true;
            AusMineB2Time= 300;
            for (Player player : AusMineB2EventLocation.getNearbyPlayers(Radius)) {
                player.sendMessage("§e[エレベーター]§aを動かすための動力結晶が動いていません");
                player.sendMessage("§a動力結晶付近にいる§c[サイモア]§aが原因だと思われます");
                player.sendMessage("§c[サイモア]§aを退治してください");
                triggerTitle(player, DungeonQuestTrigger, "§cサイモア§aを討伐せよ", SoundList.DungeonTrigger);
            }
            AusMineB2Enemy = MobManager.mobSpawn(getMobData("サイモア"), 15, AusMineB2EventLocation);
            BTTSet(new BukkitRunnable() {
                @Override
                public void run() {
                    Set<Player> list = PlayerList.getNear(AusMineB2EventLocation, Radius);
                    AusMineB2Player.addAll(list);
                    if (list.size() == 0) {
                        this.cancel();
                        AusMineB2End();
                        AusMineB2Start = false;
                    } else if (AusMineB2Enemy.isDead) {
                        this.cancel();
                        AusMineB2End();
                        getWarpGate("AusMineB2_to_AusMineB3").ActiveAtTime(ElevatorActiveTime);
                        elevatorActive(AusMineB2EventLocation, "サイモア");
                        AusMineB2 = true;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            AusMineB2 = false;
                            AusMineB2Start = false;
                        }, ElevatorActiveTime);
                    } else {
                        AusMineB2Time--;
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("ボス体力") + Math.ceil(AusMineB2Enemy.Health));
                        textData.add(decoLore("残り時間") + AusMineB2Time + "秒");
                        for (Player player : AusMineB2Player) {
                            if (player.isOnline()) {
                                playerData(player).ViewBar.setSideBar("AusMineB2", textData);
                            }
                        }
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0, 20), "AusMineB2DungeonQuest");
        }
        return !AusMineB2;
    }

    public static void AusMineB2End() {
        AusMineB2Enemy.delete();
        for (Player player : AusMineB2Player) {
            if (player.isOnline()) {
                playerData(player).ViewBar.resetSideBar("AusMineB2");
            }
        }
        AusMineB2Enemy = null;
        AusMineB2Player.clear();
    }

    private static final Location AusMineB3EventLocation = new Location(world,945, 121, 1709);
    private static boolean AusMineB3 = false;
    private static boolean AusMineB3Start = false;
    private static int AusMineB3Time;
    private static int AusMineB3Health;
    private static final int AusMineB3SpawnRadius = 21;
    private static final Set<EnemyData> AusMineB3EnemyList = new HashSet<>();
    private static final Set<Player> AusMineB3Player = new HashSet<>();
    public static boolean AusMineB3() {
        if (!AusMineB3Start) {
            AusMineB3Start = true;
            AusMineB3Time = 90;
            AusMineB3Health = 7000;
            for (Player player : AusMineB3EventLocation.getNearbyPlayers(Radius)) {
                player.sendMessage("§e[エレベーター]§aを動かすための動力結晶が襲われています");
                player.sendMessage("§a動力結晶付近を守ってください");
                triggerTitle(player, DungeonQuestTrigger, "§e動力結晶§aを防衛せよ", SoundList.DungeonTrigger);
            }
            Set<PlayerData> PlayersData = new HashSet<>();
            BTTSet(new BukkitRunnable() {
                int spawnWait = 0;
                @Override
                public void run() {
                    Collection<Player> Players = AusMineB3EventLocation.getNearbyPlayers(Radius);
                    for (Player player : Players) {
                        AusMineB3Player.add(player);
                        PlayersData.add(playerData(player));
                    }
                    List<EnemyData> enemyListClone = new ArrayList<>(AusMineB3EnemyList);
                    for (EnemyData enemyData : enemyListClone) {
                        if (enemyData.isDead) {
                            AusMineB3EnemyList.remove(enemyData);
                        }
                    }
                    spawnWait++;
                    if (spawnWait >= 3) {
                        if (AusMineB3EnemyList.size() < 5) {
                            spawnWait = 0;
                            Location spawnLoc = AusMineB3EventLocation.clone();
                            double randomLoc = 2 * Math.PI * random.nextDouble();
                            spawnLoc.add(Math.cos(randomLoc) * AusMineB3SpawnRadius, 0, Math.sin(randomLoc) * AusMineB3SpawnRadius);
                            String[] MobList = {"ロウスパイダー", "スケール", "レースト", "ベース"};
                            int MobSelect = random.nextInt(MobList.length);
                            EnemyData enemyData = MobManager.mobSpawn(getMobData(MobList[MobSelect]), 20, spawnLoc);
                            enemyData.overrideTargetLocation = AusMineB3EventLocation;
                            AusMineB3EnemyList.add(enemyData);
                        }
                    }
                    if (AusMineB3Time <= 0) {
                        this.cancel();
                        AusMineB3End();
                        AusMineB3 = true;
                        getWarpGate("AusMineB3_to_AusMineB4").ActiveAtTime(ElevatorActiveTime);
                        for (Player player : Players) {
                            player.sendMessage("§e[動力結晶]§aの防衛に§a成功§aしました");
                            player.sendMessage("§e[エレベーター]§aが§e[" + ElevatorActiveTime/20 + "秒間]§a稼働します");
                            player.sendMessage("§a急いで§e[エレベーター]§aを使用してください");
                            triggerTitle(player, DungeonQuestClear, "§e[エレベーター]§aに向かってください", SoundList.LevelUp);
                        }
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            AusMineB3 = false;
                            AusMineB3Start = false;
                        }, ElevatorActiveTime);
                    } else if (PlayerList.getNear(AusMineB3EventLocation, Radius).size() == 0) {
                        this.cancel();
                        AusMineB3End();
                        for (EnemyData enemyData : AusMineB3EnemyList) {
                            enemyData.delete();
                        }
                    } else {
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("動力結晶耐久") + AusMineB3Health);
                        textData.add(decoLore("残り時間") + AusMineB3Time + "秒");
                        for (PlayerData playerData : PlayersData) {
                            playerData.ViewBar.setSideBar("AusMineB3", textData);
                        }
                        for (LivingEntity entity : AusMineB3EventLocation.getNearbyLivingEntities(5)) {
                            if (MobManager.isEnemy(entity)) {
                                EnemyData enemyData = MobManager.EnemyTable(entity.getUniqueId());
                                AusMineB3Health -= enemyData.ATK/5;
                            }
                        }
                        if (AusMineB3Health < 0) {
                            this.cancel();
                            AusMineB3End();
                            AusMineB3Health = 0;
                            AusMineB3 = false;
                            AusMineB3Start = false;
                            for (Player player : AusMineB3EventLocation.getNearbyPlayers(Radius)) {
                                player.sendMessage("§e[動力結晶]§aの防衛に§c失敗§aしました");
                                triggerTitle(player, DungeonQuestFailed, "§e[動力結晶]§aの防衛に§c失敗§aしました", SoundList.Failed);
                                player.teleportAsync(getWarpGate("AusMineB3_to_AusMineB4").Location);
                            }
                        }
                    }
                    AusMineB3Time--;
                }
            }.runTaskTimer(plugin, 0, 20), "AusMineB3DungeonQuest");
        }
        return !AusMineB3;
    }

    public static void AusMineB3End() {
        for (EnemyData enemyData : AusMineB3EnemyList) {
            enemyData.delete();
        }
        for (Player player : AusMineB3Player) {
            if (player.isOnline()) {
                playerData(player).ViewBar.resetSideBar("AusMineB3");
            }
        }
        AusMineB3EnemyList.clear();
        AusMineB3Player.clear();
    }

    private static final Location AusMineB4EventLocation = new Location(world,704, 119, 1979);
    private static boolean AusMineB4 = false;
    private static boolean AusMineB4Start = false;
    private static final Set<Player> AusMineB4Player = new HashSet<>();
    public static double AusMineB4SkillTime = 0;
    public static BukkitTask AusMineB4Task;
    public static boolean AusMineB4() {
        if (!AusMineB4Start) {
            AusMineB4Start = true;
            for (Player player : PlayerList.getNear(AusMineB1EventLocation, Radius*2)) {
                triggerTitle(player, DungeonQuestTrigger, "§cグリフィア§aを討伐せよ", SoundList.DungeonTrigger);
            }
            EnemyData enemyData = MobManager.mobSpawn(getMobData("グリフィア"), 25, AusMineB4EventLocation);
            final Set<PlayerData> PlayersData = new HashSet<>();
            BTTSet(new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : PlayerList.getNear(AusMineB4EventLocation, Radius*2)) {
                        AusMineB4Player.add(player);
                        PlayersData.add(playerData(player));
                    }
                    if (PlayerList.getNear(AusMineB4EventLocation, Radius*2).size() == 0) {
                        this.cancel();
                        enemyData.delete();
                        AusMineB4Start = false;
                        AusMineB4 = false;
                        AusMineB4End();
                        for (Player player : AusMineB4Player) {
                            player.sendTitle("§cグリフィアの討伐失敗", "", 20, 60, 20);
                            playSound(player, SoundList.DungeonTrigger);
                        }
                    } else if (enemyData.isDead) {
                        this.cancel();
                        for (Player player : PlayerList.getNear(AusMineB4EventLocation, Radius*2)) {
                            triggerTitle(player, DungeonQuestClear, "§eスニーク§aを続けると§e[ダンジョンの入り口]§aへ§b転移§aされます", SoundList.LevelUp);
                            player.sendMessage("§cグリフィア§aを討伐しました！");
                            player.sendMessage("§eスニーク§aを続けると§e退場§aします");
                        }
                        AusMineB4 = true;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            AusMineB4 = false;
                            AusMineB4Start = false;
                            for (Player player : PlayerList.getNear(AusMineB4EventLocation, Radius*2)) {
                                if (!player.isSneaking()) {
                                    player.teleportAsync(getWarpGate("AusMineB4_to_AusMineB4Boss").Location);
                                } else {
                                    player.teleportAsync(getWarpGate("AusForest_to_AusMineB1").Location);
                                }
                            }
                        }, 100);
                        AusMineB4End();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0, 20), "AusMineB4DungeonQuest");
            AusMineB4Task = new BukkitRunnable() {
                @Override
                public void run() {
                    List<String> textData = new ArrayList<>();
                    textData.add(decoText("§c§lダンジョンクエスト"));
                    textData.add(decoLore("BOSS体力") + String.format("%.0f", enemyData.Health) + " (" + String.format("%.1f", enemyData.Health/enemyData.MaxHealth*100f) + "%)");
                    if (AusMineB4SkillTime > -1) {
                        textData.add(decoLore("ボススキル詠唱時間") + String.format("%.0f", AusMineB4SkillTime*100) + "%");
                    }
                    for (PlayerData playerData : PlayersData) {
                        playerData.ViewBar.setSideBar("AusMineB4", textData);
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0, 10);
        }
        return AusMineB4;
    }

    public static void AusMineB4End() {
        if (AusMineB4Task != null) {
            AusMineB4Task.cancel();
            AusMineB4Task = null;
        }
        for (Player player : AusMineB4Player) {
            if (player.isOnline()) {
                playerData(player).ViewBar.resetSideBar("AusMineB4");
            }
        }
        AusMineB4Player.clear();
    }
}
