package swordofmagic7.Mob;

import org.bukkit.Location;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.ServerId;
import static swordofmagic7.Function.VectorDown;
import static swordofmagic7.SomCore.random;

public class MobSpawnerData {
    public MobData mobData;
    public Location location;
    public int Level = 1;
    public int MaxMob = 5;
    public int Radius = 5;
    public int RadiusY = 5;
    public int PerSpawn = 1;
    public File file;

    private boolean Started = false;
    private final List<EnemyData> SpawnedList = new ArrayList<>();

    public void start() {
        if (!Started && !ServerId.equalsIgnoreCase("Event")) {
            Started = true;
            MultiThread.TaskRunTimer(() -> {
                if (PlayerList.getNear(location, Radius + 16 + mobData.Search).size() > 0) {
                    for (EnemyData data : SpawnedList) {
                        if (data.entity == null || data.entity.isDead() || data.isDead()) {
                            data.delete();
                        } else if (data.entity.getLocation().distance(location) > Radius + mobData.Search) {
                            data.entity.teleportAsync(location);
                            data.resetPriority();
                        }
                    }
                    SpawnedList.removeIf(data -> data.entity == null || data.entity.isDead() || data.isDead());
                    if (SpawnedList.size() < MaxMob) {
                        MultiThread.TaskRunSynchronized(() -> {
                            for (int i = 0; i < PerSpawn; i++) {
                                spawn();
                            }
                        }, "EnemySpawnerSpawn");
                    }
                }
            }, 30);
        }
    }

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
                    SpawnedList.add(MobManager.mobSpawn(mobData, Level, loc));
                    return;
                }
                loc.add(VectorDown);
            }
        }
    }
}
