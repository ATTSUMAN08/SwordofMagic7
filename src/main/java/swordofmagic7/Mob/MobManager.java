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

    public static double StatusMultiply(int level) {
        return Math.pow(0.74+(level/3f), 1.4);
    }

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
        EnemyData enemyData = new EnemyData(entity);
        enemyData.mobData = baseData;
        String DisplayName = "§c§l《" + baseData.Display + " Lv" + level + "》";

        double multiply = StatusMultiply(level);
        enemyData.entity = entity;
        enemyData.uuid = entity.getUniqueId();
        enemyData.Level = level;
        enemyData.MaxHealth = baseData.Health * multiply;
        enemyData.Health = baseData.Health * multiply;
        enemyData.ATK = baseData.ATK * multiply;
        enemyData.DEF = baseData.DEF * multiply;
        enemyData.ACC = baseData.ACC * multiply;
        enemyData.EVA = baseData.EVA * multiply;
        enemyData.CriticalRate = baseData.CriticalRate * multiply;
        enemyData.CriticalResist = baseData.CriticalResist * multiply;
        enemyData.Exp = baseData.Exp * multiply;

        if (baseData.disguise != null) {
            Disguise disguise = baseData.disguise.clone();
            disguise.setEntity(entity);
            disguise.setDisguiseName(DisplayName);
            disguise.setDynamicName(true);
            disguise.setCustomDisguiseName(true);
            disguise.startDisguise();
        }

        EnemyTable.put(entity.getUniqueId(), enemyData);
        entity.setCustomNameVisible(true);
        entity.setCustomName(DisplayName);
        entity.setMaxHealth(enemyData.MaxHealth);
        entity.setHealth(entity.getMaxHealth());

        enemyData.runAI();
        return enemyData;
    }
}

