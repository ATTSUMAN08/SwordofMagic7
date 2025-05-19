package swordofmagic7.Mob;

import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.somrpg.swordofmagic7.SomCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import swordofmagic7.Dungeon.Novaha.Novaha3;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.VectorDown;
import static net.somrpg.swordofmagic7.SomCore.random;

public class MobSpawnerData {
    public String Id;
    public MobData mobData;
    public Location location;
    public int Level = 1;
    public int MaxMob = 5;
    public int Radius = 5;
    public int RadiusY = 5;
    public int PerSpawn = 1;
    public String DeathTrigger;
    public File file;

    private boolean Started = false;
    private final List<EnemyData> SpawnedList = new ArrayList<>();

    public void start() {
        if (!Started && (!SomCore.Companion.isEventServer() || DeathTrigger != null)) {
            Started = true;
            MultiThread.TaskRunTimer(() -> {
                if (DeathTrigger != null) for (EnemyData enemyData : SpawnedList) {
                    if (enemyData.isDead()) {
                        if ("Novaha3".equals(DeathTrigger)) {
                            Novaha3.Count--;
                        }
                    }
                }
                SpawnedList.removeIf(data -> data.entity == null || data.entity.isDead() || data.isDead());
                if (PlayerList.getNear(location, Radius + 16 + mobData.Search).size() > 0) {
                    for (EnemyData data : SpawnedList) {
                        if (data.entity == null || data.entity.isDead() || data.isDead()) {
                            data.delete();
                        } else if (data.entity.getLocation().distance(location) > Radius + mobData.Search) {
                            data.entity.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS);
                            data.resetPriority();
                        }
                    }
                    if (SpawnedList.size() < MaxMob && !PlayerList.getNearNonDead(location, Radius + mobData.Search).isEmpty()) {
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
