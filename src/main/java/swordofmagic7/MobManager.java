package swordofmagic7;

import com.destroystokyo.paper.entity.Pathfinder;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Predicate;

import static swordofmagic7.DataBase.playerData;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.colored;

class MobData {
    String Display;
    EntityType entityType;
    MobDisguise disguise;
    double Health;
    double ATK;
    double DEF;
    double ACC;
    double EVA;
    double Exp;
    double Movement;
    boolean Hostile = false;
    List<DropItemData> DropItemTable;
    List<DropModuleData> DropModuleTable;
}

class DropItemData {
    ItemParameter itemParameter;
    int MaxAmount = 0;
    int MinAmount = 0;
    double Percent = 0;
    int MaxLevel = 0;
    int MinLevel = 0;

    DropItemData(ItemParameter itemParameter) {
        this.itemParameter = itemParameter;
    }
}

class DropModuleData {
    ModuleParameter moduleParameter;
    double Percent = 0;
    int MaxLevel = 0;
    int MinLevel = 0;

    DropModuleData(ModuleParameter moduleParameter) {
        this.moduleParameter = moduleParameter;
    }
}

class EnemyData {
    private final Plugin plugin = System.plugin;
    private final Random random = new Random();
    private final ParticleManager particleManager = new ParticleManager();

    LivingEntity entity;
    MobData mobData;
    int Level;
    double MaxHealth;
    double Health;
    double ATK;
    double DEF;
    double ACC;
    double EVA;
    double Exp;

    private final List<Player> Involved = new ArrayList<>();
    private final HashMap<Player, Double> Priority = new HashMap<>();
    private final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1));
    private final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 1));
    private Player target;
    private boolean SkillReady = true;

    EnemyData() {}

    EnemyData(LivingEntity entity) {
        this.entity = entity;
    }

    void Involved(Player player) {
        if (!Involved.contains(player)) {
            Involved.add(player);
        }
    }

    private final Predicate<LivingEntity> Predicate = entity -> entity.getType() == EntityType.PLAYER;

    private BukkitTask runAITask;

    void stopAI() {
        if (runAITask != null) runAITask.cancel();
    }

    void runAI() {
        stopAI();
        if (entity instanceof Mob mob) {
            runAITask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (target == null && mobData.Hostile) {
                    for (Player player : PlayerList.getNear(entity.getLocation(), 16)) {
                        target = player;
                        break;
                    }
                }

                double topPriority = 0;
                for (Map.Entry<Player, Double> priority : Priority.entrySet()) {
                    if (topPriority < priority.getValue()) {
                        target = priority.getKey();
                        topPriority = priority.getValue();
                    }
                }

                if (target != null) {
                    Pathfinder pathfinder = mob.getPathfinder();
                    pathfinder.moveTo(target, mobData.Movement);

                    if (SkillReady) {
                        if (random.nextDouble() <= 0.99) PullUpper();
                    }

                    if (target.getGameMode() != GameMode.ADVENTURE) {
                        Priority.remove(target);
                        target = null;
                    }
                }
            }, 0, 10);
        }
    }

    void CastSkill(boolean bool) {
        if (bool) {
            entity.setAI(false);
            SkillReady = false;
        } else {
            entity.setAI(true);
            SkillReady = true;
        }
    }

    private final int period = 5;

    void PullUpper() {
        if (entity.getLocation().distance(target.getLocation()) <= 5) {
            double radius = 8;
            double angle = 80;
            int CastTime = 20;

            Location origin = entity.getLocation().clone();
            CastSkill(true);

            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (i < CastTime) {
                        particleManager.FanShapedParticle(particleCasting, origin, radius, angle, 3);
                    } else {
                        this.cancel();
                        particleManager.FanShapedParticle(particleActivate, origin, radius, angle, 3);
                        List<LivingEntity> Targets = PlayerList.getNearLivingEntity(entity.getLocation(), radius);
                        List<LivingEntity> victims = particleManager.FanShapedCollider(origin, Targets, angle);
                        Damage.makeDamage(entity, victims, DamageCause.ATK, 1, 1, 2);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 10);
                    }
                    i += period;
                }
            }.runTaskTimer(plugin, 0, period);
        }
    }

    void addPriority(Player player, double addPriority) {
        Priority.putIfAbsent(player, addPriority);
        Priority.put(player, Priority.get(player) + addPriority);
    }

    void dead() {
        if (!entity.isDead()) particleManager.CylinderParticle(new ParticleData(Particle.EXPLOSION_NORMAL), entity.getLocation(), 1, 2, 3, 3);
        stopAI();
        MobManager.getEnemyTable().remove(entity.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            entity.remove();
            int exp = (int) Math.floor(Exp);
            for (Player player : PlayerList.getNear(entity.getLocation(), 32)) {
                Involved(player);
            }
            for (Player player : Involved) {
                PlayerData playerData = playerData(player);
                Classes classes = playerData.Classes;
                classes.addExp(classes.classT0, exp);
                for (DropItemData dropData : mobData.DropItemTable) {
                    if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                        if (random.nextDouble() <= dropData.Percent) {
                            int amount;
                            if (dropData.MaxAmount != dropData.MinAmount) {
                                amount =random.nextInt(dropData.MaxAmount - dropData.MinAmount) + dropData.MinAmount;
                            } else {
                                amount = dropData.MinAmount;
                            }
                            playerData.ItemInventory.addItemParameter(dropData.itemParameter.clone(), amount);
                        }
                    }
                }
                for (DropModuleData dropData : mobData.DropModuleTable) {
                    if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                        if (random.nextDouble() <= dropData.Percent) {
                            ModuleParameter moduleParameter = dropData.moduleParameter.clone();
                            moduleParameter.Quality = random.nextDouble();
                            moduleParameter.Level = Level;
                            playerData.ModuleInventory.addModuleParameter(moduleParameter);
                        }
                    }
                }
                playerData.viewUpdate();
            }
        });
    }
}

public final class MobManager {

    private static final HashMap<UUID, EnemyData> EnemyTable = new HashMap<>();

    private static double StatusMultiply(int level) {
        return Math.pow(level, 1.1);
    }

    static EnemyData EnemyTable(UUID uuid) {
        if (EnemyTable.containsKey(uuid)) {
            return EnemyTable.get(uuid);
        }
        Log("&cNon-EnemyData: " + uuid, true);
        return new EnemyData();
    }

    static HashMap<UUID, EnemyData> getEnemyTable() {
        return EnemyTable;
    }

    static void mobSpawn(MobData baseData, int level, Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, baseData.entityType, false);
        EnemyData enemyData = new EnemyData(entity);
        enemyData.mobData = baseData;

        double multiply = StatusMultiply(level);
        enemyData.Level = level;
        enemyData.MaxHealth = baseData.Health * multiply;
        enemyData.Health = baseData.Health * multiply;
        enemyData.ATK = baseData.ATK * multiply;
        enemyData.DEF = baseData.DEF * multiply;
        enemyData.ACC = baseData.ACC * multiply;
        enemyData.EVA = baseData.EVA * multiply;
        enemyData.Exp = baseData.Exp * multiply;

        if (baseData.disguise != null) {
            baseData.disguise.setEntity(entity);
            baseData.disguise.startDisguise();
        }

        EnemyTable.put(entity.getUniqueId(), enemyData);
        entity.setCustomNameVisible(true);
        entity.setCustomName(colored("&c&l《" + baseData.Display) + " Lv" + level + "》");
        entity.setMaxHealth(enemyData.MaxHealth);
        entity.setHealth(entity.getMaxHealth());

        enemyData.runAI();
    }
}
