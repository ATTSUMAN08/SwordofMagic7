package swordofmagic7.Mob;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.UUID;

import static swordofmagic7.Function.Log;

public final class MobManager {

    private static final HashMap<UUID, EnemyData> EnemyTable = new HashMap<>();

    public static EnemyData EnemyTable(UUID uuid) {
        if (EnemyTable.containsKey(uuid)) {
            return EnemyTable.get(uuid);
        }
        Log("§cNon-EnemyData: " + uuid, true);
        return null;
    }

    public static boolean isEnemy(Entity uuid) {
        return getEnemyTable().containsKey(uuid.getUniqueId());
    }

    public static HashMap<UUID, EnemyData> getEnemyTable() {
        return EnemyTable;
    }

    public static EnemyData mobSpawn(MobData baseData, int level, Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, baseData.entityType, false);
        EnemyData enemyData = new EnemyData(entity, baseData, level);
        EnemyTable.put(entity.getUniqueId(), enemyData);
        enemyData.runAI();
        return enemyData;
    }
}

