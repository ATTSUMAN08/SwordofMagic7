package swordofmagic7.Dungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Map.WarpGateParameter;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.BTTSet;
import static swordofmagic7.System.plugin;

public class AusMine {

    private static boolean AusMineB1 = false;
    private static final double Radius = 64;
    private static final World world = Bukkit.getWorld("world");
    private static final Location AusMineB1EventLocation = new Location(world,1145, 141, 1293);
    private static final Random random = new Random();
    private static final String DungeonQuestTrigger = "§c§l《ダンジョンクエスト発生》";
    private static final String DungeonQuestClear = "§c§l《ダンジョンクエスト達成》";

    public static void triggerTitle(Player player, String title, String subtitle, SoundList sound) {
        player.sendTitle(title, subtitle, 30, 50, 30);
        playSound(player, sound);
    }

    public static boolean AusMineB1() {
        if (!AusMineB1) {
            AusMineB1 = true;
            for (Player player : AusMineB1EventLocation.getNearbyPlayers(Radius)) {
                player.sendMessage("§e[エレベーター]§aを動かそうとしたら§c[ゴブリン]§aが襲ってきました");
                player.sendMessage("§aこのままでは§e[エレベーター]§aを動かせません");
                player.sendMessage("§c[ゴブリン]§aを退治してください");
                triggerTitle(player, DungeonQuestTrigger, "§cゴブリン§aを§c15体§a討伐せよ", SoundList.DungeonTrigger);
            }
            final List<EnemyData> EventEnemy = new ArrayList<>();

            BTTSet(new BukkitRunnable() {
                boolean dead;
                int tick = 0;
                final int count = 5;
                @Override
                public void run() {
                    dead = true;
                    if (AusMineB1EventLocation.getNearbyPlayers(Radius).size() == 0) {
                        this.cancel();
                        for (EnemyData enemyData : EventEnemy) enemyData.delete();
                        AusMineB1 = false;
                        return;
                    }
                    for (EnemyData enemyData : EventEnemy) {
                        if (!enemyData.isDead) {
                            dead = false;
                            break;
                        }
                    }
                    if (dead) {
                        tick++;
                        if (tick < count) for (int i = 0; i < 3; i++) {
                            Location loc = AusMineB1EventLocation.clone().add(random.nextDouble()*20, 0, random.nextDouble()*20);
                            EventEnemy.add(MobManager.mobSpawn(getMobData("ゴブリン"), 15, loc));
                        }
                    }
                    if (dead && tick > count) {
                        this.cancel();
                        getWarpGate("AusMineB1_to_AusMineB2").ActiveAtTime(600);
                        for (Player player : AusMineB1EventLocation.getNearbyPlayers(Radius)) {
                            player.sendMessage("§c[ゴブリン]§aが退治されました");
                            player.sendMessage("§e[エレベーター]§aが§e[30秒間]§a稼働します");
                            player.sendMessage("§a急いで§e[エレベーター]§aを使用してください");
                            triggerTitle(player, DungeonQuestClear, "§e[エレベーター]§aに向かってください", SoundList.LevelUp);
                        }
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            AusMineB1 = false;
                        }, 600);
                    }
                }
            }.runTaskTimer(plugin, 0, 40), "AusMineB1DungeonQuest");
            return true;
        } else {
            return false;
        }

    }
}
