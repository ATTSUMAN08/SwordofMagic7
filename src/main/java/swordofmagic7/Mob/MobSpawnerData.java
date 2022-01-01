package swordofmagic7.Mob;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.System;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static swordofmagic7.Function.VectorDown;

public class MobSpawnerData {
    public MobData mobData;
    public Location location;
    public int Level = 1;
    public int MaxMob = 5;
    public int Radius = 5;
    public int RadiusY = 5;
    public int PerSpawn = 1;

    private final List<EnemyData> SpawnedList = new ArrayList<>();

    public void start() {
        new BukkitRunnable() {

            @Override
            public void run() {
                int perSpawn = PerSpawn;
                List<EnemyData> dataList = new ArrayList<>(SpawnedList);
                for (EnemyData data : dataList) {
                    if (data.entity.isDead() || data.isDead) {
                        data.delete();
                        SpawnedList.remove(data);
                    } else if (data.entity.getLocation().distance(location) > Radius + 24) {
                        data.entity.teleportAsync(location);
                    }
                }
                if (SpawnedList.size() + perSpawn > MaxMob) {
                    perSpawn = MaxMob - SpawnedList.size();
                }
                if (perSpawn > 0 && location.getNearbyPlayers(Radius + 48, RadiusY + 8).size() > 0) {

                    for (int i = 0; i < perSpawn; i++) spawn();
                }
            }
        }.runTaskTimer(System.plugin, 0, 20);
    }

    private final Random random = new Random();

    public void spawn() {
        for (int i = 0; i < 4; i++) {
            double x = location.getX() + (2 * random.nextDouble() * Radius) - Radius;
            double z = location.getZ() + (2 * random.nextDouble() * Radius) - Radius;
            double y = location.getY() + RadiusY;
            Location origin = new Location(location.getWorld(), x, y, z, 0, 90);
            Location loc = origin.clone();
            for (int i2 = 0; i2 < RadiusY * 2; i2++) {
                boolean spawnAble = !loc.getBlock().getType().isSolid() && loc.clone().add(VectorDown).getBlock().getType().isSolid();
                if (spawnAble) {
                    EnemyData enemyData = MobManager.mobSpawn(mobData, Level, loc);
                    SpawnedList.add(enemyData);
                    return;
                }
                loc.add(VectorDown);
            }
        }
    }
}
