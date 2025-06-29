package swordofmagic7.Mob;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static swordofmagic7.Function.Log;
import static net.somrpg.swordofmagic7.SomCore.instance;

public final class MobManager {

    public static final ConcurrentMap<String, EnemyData> EnemyTable = new ConcurrentHashMap<>();

    public static EnemyData EnemyTable(UUID uuid) {
        if (EnemyTable.containsKey(uuid.toString())) {
            return EnemyTable.get(uuid.toString());
        }
        Log("§cNon-EnemyData: " + uuid, true);
        return null;
    }

    public static boolean isEnemy(Entity uuid) {
        return EnemyTable.containsKey(uuid.getUniqueId().toString());
    }

    public static Collection<EnemyData> getEnemyList() {
        return EnemyTable.values();
    }

    public static EnemyData mobSpawn(MobData baseData, int level, Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, baseData.entityType, false);
        entity.setMetadata("SomEntity", new FixedMetadataValue(instance, true));
        if (entity instanceof Slime slime) {
            slime.setSize(baseData.Size);
        }
        entity.setAI(!baseData.NoAI);
        entity.setInvisible(baseData.Invisible);
        entity.setGlowing(baseData.Glowing);
        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);
        EnemyData enemyData = new EnemyData(entity, baseData, level);
        EnemyTable.put(entity.getUniqueId().toString(), enemyData);
        enemyData.runAI();
        return enemyData;
    }
}

