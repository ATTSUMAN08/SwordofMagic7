package swordofmagic7.Dungeon;

import org.bukkit.Location;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;

import java.util.*;

import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.System.plugin;
import static swordofmagic7.System.random;

public class DefenseBattle {
    private static final Location location = new Location(world, 2234.5,139,2345.5);
    private static final Location targetLocation = location.clone().add(0, -58, 0);
    private static final Location[] spawnLocation = new Location[9];
    private static final List<MobData> MobList = new ArrayList<>();
    public static int wave = 1;
    public static double Health = 10000;
    public static int startTime = 3600;
    public static int time = startTime;

    public static void onLoad() {
        spawnLocation[0] = location.clone().add(0, -74, 4);
        spawnLocation[1] = location.clone().add(67, -75, 68);
        spawnLocation[2] = location.clone().add(80, -75, 31);
        spawnLocation[3] = location.clone().add(83, -75, -21);
        spawnLocation[4] = location.clone().add(55, -73, -65);
        spawnLocation[5] = location.clone().add(-14, -75, -38);
        spawnLocation[6] = location.clone().add(-83, -75, 9);
        spawnLocation[7] = location.clone().add(-53, -75, 77);
        spawnLocation[8] = location.clone().add(-11, -74, 100);

        MobList.add(DataBase.getMobData("ゴブリン"));
    }


    public static void startWave(int i) {
        MultiThread.TaskRun(() -> {
            int enemyCount = (int) (Math.pow(wave, 2) + 30);
            List<EnemyData> list = new ArrayList<>();
            while (plugin.isEnabled()) {
                if (enemyCount > 0) {
                    EnemyData enemyData = MobManager.mobSpawn(MobList.get(random.nextInt(MobList.size()-1)), wave*5, spawnLocation[random.nextInt(spawnLocation.length-1)]);
                    list.add(enemyData);
                }
                if (enemyCount == 0 && list.size() == 0) break;
                MultiThread.sleepTick(20);
            }
            wave++;
            startWave(wave);
        }, "DefenseBattle");
    }
}
