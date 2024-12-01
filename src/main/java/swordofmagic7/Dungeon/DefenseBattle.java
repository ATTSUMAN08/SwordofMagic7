package swordofmagic7.Dungeon;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import swordofmagic7.Client;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.SomCore;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;
import swordofmagic7.ViewBar.ViewBar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static swordofmagic7.Data.DataBase.MapList;
import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Dungeon.Dungeon.Message;
import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.SomCore.plugin;
import static swordofmagic7.SomCore.random;

public class DefenseBattle {
    private static final Location location = new Location(world, 2234.5,139,2345.5);
    private static final Location targetLocation = new Location(world, 2234.5,81,2345.5);
    private static final Location teleportLocation = new Location(world,2234.5, 97, 2357.5);
    private static final Location[] spawnLocation = new Location[9];
    public static final List<MobData> MobList = new ArrayList<>();
    public static final List<EnemyData> EnemyList = new ArrayList<>();
    public static int EnemyCount = 0;
    public static int wave = 1;
    public static double Health = 10000;
    public static int startTime = 1200;
    public static int time = startTime;
    public static boolean isStarted = false;
    private static final int Radius = 96;
    private static final String sidebarId = "DefenseBattle";
    public static boolean last = false;
    static boolean isStart = false;
    static boolean isAlarm = false;

    public static void onLoad() {
        spawnLocation[0] = new Location(world, 2279.5,66,2274.5);
        spawnLocation[1] = new Location(world, 2220.5,64,2274.5);
        spawnLocation[2] = new Location(world, 2168.5,65,2301.5);
        spawnLocation[3] = new Location(world, 2151.5,64,2348.5);
        spawnLocation[4] = new Location(world, 2181.5,64,2416.5);
        spawnLocation[5] = new Location(world, 2224.5,65,2439.5);
        spawnLocation[6] = new Location(world, 2279.5,66,2274.5);
        spawnLocation[7] = new Location(world, 2300.5,64,2407.5);
        spawnLocation[8] = new Location(world, 2314.5,64,2370.5);

        if (SomCore.isEventServer()) MultiThread.TaskRunTimer(() -> {
            LocalDateTime time = LocalDateTime.now();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
            String display = format.format(time);
            switch (display) {
                case "13:55", "18:55", "21:55", "23:55" -> {
                    if (!isAlarm) {
                        Client.sendBroadCast(new TextView("§aまもなく§c防衛戦§aが開始されます"));
                        isAlarm = true;
                    }
                }
                case "14:00", "19:00", "22:00", "00:00" -> {
                    if (!isStart) {
                        startWave(1);
                        isStart = true;
                    }
                }
                default -> {
                    isAlarm = true;
                    isStart = false;
                }
            }
        }, 100);
    }

    public static void teleport(Player player) {
        player.teleport(teleportLocation);
        MapList.get("DefenseBattle").enter(player);
    }

    private static final Location bossLocation = new Location(world, 2232, 72, 2379);
    public static void startWave(int i) {
        if (i == 1) time = startTime;
        isStarted = true;
        Client.sendBroadCast(new TextView("§c防衛戦Wave" + i + "§aが開始されました"));
        MultiThread.TaskRun(() -> {
            wave = i;
            Health = 10000 + 1000*(i-1);
            EnemyCount = 50 + wave*5;
            Message(PlayerList.getNear(targetLocation, Radius), "§c§l《Wave" + wave + "》", "§c生命の樹§aを防衛せよ", null, SoundList.DungeonTrigger);
            Set<Player> Players = new HashSet<>();
            MultiThread.TaskRunSynchronized(() -> {
                EnemyData enemyData = MobManager.mobSpawn(getMobData("アイアロン"), wave * 5, spawnLocation[random.nextInt(spawnLocation.length)]);
                enemyData.nonTargetLocation = targetLocation;
                EnemyList.add(enemyData);
            });
            while (plugin.isEnabled() && Health > 0 && time > 0) {
                Players = PlayerList.getNear(targetLocation, Radius);
                MultiThread.TaskRunSynchronized(() -> {
                    for (int i2 = 0; i2 < 5; i2++) {
                        if (EnemyCount > 0 && EnemyList.size() < 50) {
                            MobData mobData = MobList.get(random.nextInt(MobList.size() - 1));
                            Location location = spawnLocation[random.nextInt(spawnLocation.length)];
                            EnemyData enemyData = MobManager.mobSpawn(mobData, wave * 5, location);
                            if (random.nextDouble() < 0.5) {
                                enemyData.overrideTargetLocation = targetLocation;
                            } else {
                                enemyData.nonTargetLocation = targetLocation;
                            }
                            enemyData.isDefenseBattle = true;
                            EnemyList.add(enemyData);
                            EnemyCount--;
                        } else break;
                    }
                });
                EnemyList.removeIf(EnemyData::isDead);
                boolean isAttack = false;
                for (EnemyData enemyData : EnemyList) {
                    if (enemyData.entity.getLocation().distance(targetLocation) < 8) {
                        Health -= (100+enemyData.Level);
                        MultiThread.TaskRunSynchronized(() -> {
                            enemyData.entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 21, 0, false, false));
                        });
                        isAttack= true;
                    }
                }
                if (isAttack) Message(Players, " ", "§e§l生命の樹§aが攻撃されています！", null, SoundList.Nope, true);
                time--;
                if (EnemyCount == 0 && EnemyList.isEmpty()) break;
                List<String> textData = new ArrayList<>();
                textData.add(decoText("防衛戦 [生命の樹]"));
                textData.add(decoLore("現在Wave") + wave);
                textData.add(decoLore("生命の樹の耐久") + String.format("%.0f", Health));
                textData.add(decoLore("残りエネミー数") + (EnemyList.size() + EnemyCount) + "体 §8§l(" + EnemyCount + ")");
                textData.add(decoLore("残り時間") + time + "秒");
                ViewBar.setSideBar(Players, sidebarId, textData);
                Set<Player> finalPlayers = Players;
                MultiThread.TaskRunSynchronized(() -> {
                    Collection<LivingEntity> inList = targetLocation.getNearbyLivingEntities(Radius, entity -> entity.getName().contains("§c") || entity.getName().contains("§6"));
                    if (inList.size()+EnemyCount < 10) {
                        if (!last) {
                            last = true;
                            Message(finalPlayers, " ", "§e§lエネミーをハイライトします", null, SoundList.Tick);
                        }
                        for (LivingEntity entity : inList) {
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 21, 0, false, false));
                        }
                    }
                    if (inList.size()+EnemyCount == 0) {
                        EnemyList.clear();
                    }
                });
                MultiThread.sleepTick(20);
            }
            ViewBar.resetSideBar(Players, sidebarId);
            for (EnemyData enemyData : EnemyList) {
                enemyData.delete();
            }
            EnemyList.clear();
            if (Health > 0 && time > 0) {
                for (Player player : Players) {
                    PlayerData playerData = playerData(player);
                    if (!playerData.isAFK()) playerData.ItemInventory.addItemParameter(DataBase.getItemParameter("防衛戦ランダム報酬箱"), (int) Math.ceil(wave/1.5));
                }
                Message(PlayerList.getNear(targetLocation, Radius), "§b§l《Wave" + wave + " クリア》", "§a10秒後Waveに進みます", null, SoundList.LevelUp);
                MultiThread.sleepTick(200);
                wave++;
                startWave(wave);
            } else {
                isStarted = false;
                Message(PlayerList.getNear(targetLocation, Radius), "§c§l《防衛戦終了》", "", null, SoundList.DungeonTrigger);
                Client.sendBroadCast(new TextView("§c防衛戦§aが終了しました"));
            }
        }, "DefenseBattle");
    }

    public static void endWave() {
        Health = 0;
    }
}
