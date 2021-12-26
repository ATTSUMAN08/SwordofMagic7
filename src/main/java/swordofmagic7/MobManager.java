package swordofmagic7;

import com.destroystokyo.paper.entity.Pathfinder;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.getItemParameter;
import static swordofmagic7.DataBase.playerData;
import static swordofmagic7.Function.Log;
import static swordofmagic7.System.plugin;

class MobData {
    String Display;
    EntityType entityType;
    MobDisguise disguise;
    double Health;
    double ATK;
    double DEF;
    double ACC;
    double EVA;
    double CriticalRate;
    double CriticalResist;
    double Exp;
    double Mov;
    double Reach;
    boolean Hostile = false;
    List<MobSkillData> SkillList = new ArrayList<>();
    List<DropItemData> DropItemTable = new ArrayList<>();
    List<DropRuneData> DropRuneTable = new ArrayList<>();
}

class MobSkillData {
    String Skill;
    double Percent;
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

class DropRuneData {
    RuneParameter runeParameter;
    double Percent = 0;
    int MaxLevel = 0;
    int MinLevel = 0;

    DropRuneData(RuneParameter runeParameter) {
        this.runeParameter = runeParameter;
    }
}

class EnemyData {
    private final Plugin plugin = System.plugin;
    private final Random random = new Random();

    UUID uuid;
    LivingEntity entity;
    MobData mobData;
    int Level;
    double MaxHealth;
    double Health;
    double ATK;
    double DEF;
    double ACC;
    double EVA;
    double CriticalRate;
    double CriticalResist;
    double Exp;

    void updateEntity() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            entity = (LivingEntity) Bukkit.getEntity(uuid);
        });
    }

    private final List<Player> Involved = new ArrayList<>();
    private final HashMap<LivingEntity, Double> Priority = new HashMap<>();
    Location SpawnLocation;
    private final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1));
    private final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 1));
    private LivingEntity target;
    private boolean SkillReady = true;
    boolean isDead = false;

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
    private BukkitTask runPathfinderTask;

    void stopAI() {
        if (runAITask != null) runAITask.cancel();
        if (runPathfinderTask != null) runPathfinderTask.cancel();
    }

    void runAI() {
        stopAI();
        SpawnLocation = entity.getLocation();
        if (entity instanceof Mob mob) {
            runPathfinderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                double topPriority = 0;
                for (Map.Entry<LivingEntity, Double> priority : Priority.entrySet()) {
                    if (topPriority < priority.getValue()) {
                        target = priority.getKey();
                        topPriority = priority.getValue();
                    }
                }

                if (target != null) {
                    Vector vector = target.getLocation().toVector().subtract(entity.getLocation().toVector());
                    entity.getLocation().setDirection(vector);
                    Pathfinder pathfinder = mob.getPathfinder();
                    pathfinder.moveTo(target, mobData.Mov);
                }
            }, 0, 5);
            runAITask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                if (target == null && mobData.Hostile) {
                    for (Player player : PlayerList.getNear(entity.getLocation(), 16)) {
                        target = player;
                        break;
                    }
                }

                if (target != null) {
                    if (target instanceof Player player && player.getGameMode() != GameMode.SURVIVAL) {
                        Priority.remove(target);
                        target = null;
                    } else {
                        if (SkillReady) {
                            for (MobSkillData skill : mobData.SkillList) {
                                if (SkillReady) mobSkillCast(skill);
                                else break;
                            }
                        }

                        final Location TargetLocation = target.getLocation();
                        final Location EntityLocation = entity.getLocation();
                        if (Math.abs(TargetLocation.getX() - EntityLocation.getX()) <= mobData.Reach
                        && Math.abs(TargetLocation.getZ() - EntityLocation.getZ()) <= mobData.Reach) {
                            Damage.makeDamage(entity, target, DamageCause.ATK, "attack", 1, 1);
                        }
                    }
                }

                if (SpawnLocation.distance(entity.getLocation()) > 32) delete();
            }, 0, 20);
        }
    }

    void mobSkillCast(MobSkillData mobSkillData) {
        if (random.nextDouble() < mobSkillData.Percent) {
            switch (mobSkillData.Skill) {
                case "PullUpper" -> PullUpper();
            }
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
                        ParticleManager.FanShapedParticle(particleCasting, origin, radius, angle, 3);
                    } else {
                        this.cancel();
                        ParticleManager.FanShapedParticle(particleActivate, origin, radius, angle, 3);
                        List<LivingEntity> Targets = PlayerList.getNearLivingEntity(entity.getLocation(), radius);
                        List<LivingEntity> victims = ParticleManager.FanShapedCollider(origin, Targets, angle);
                        Damage.makeDamage(entity, victims, DamageCause.ATK, "PullUpper", 2, 1, 2);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 10);
                    }
                    i += period;
                }
            }.runTaskTimer(plugin, 0, period);
        }
    }

    void addPriority(LivingEntity entity, double addPriority) {
        Priority.putIfAbsent(entity, addPriority);
        Priority.put(entity, Priority.get(entity) + addPriority);
    }

    void delete() {
        isDead = true;
        stopAI();
        MobManager.getEnemyTable().remove(entity.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            entity.setHealth(0);
            entity.remove();
        });
    }

    void dead() {
        if (isDead) return;
        isDead = true;
        MobManager.getEnemyTable().remove(entity.getUniqueId());
        ParticleManager.RandomVectorParticle(new ParticleData(Particle.FIREWORKS_SPARK, 0.22f), entity.getLocation(), 100);
        playSound(entity.getLocation(), SoundList.Death);
        stopAI();

        Bukkit.getScheduler().runTask(plugin, () -> {
            entity.setHealth(0);
            entity.remove();
        });

        int exp = (int) Math.floor(Exp);
        for (Player player : PlayerList.getNear(entity.getLocation(), 32)) {
            Involved(player);
        }
        for (Player player : Involved) {
            PlayerData playerData = playerData(player);
            Classes classes = playerData.Classes;
            classes.addExp(classes.classTier[0], exp);
            for (PetParameter pet : playerData.PetSummon) {
                pet.addExp(exp);
            }
            List<String> Holo = new ArrayList<>();
            Holo.add("§e§lEXP §a§l+" + exp);
            List<DropItemData> DropItemTable = new ArrayList<>(mobData.DropItemTable);
            DropItemData LifeTear = new DropItemData(getItemParameter("生命の雫"));
            LifeTear.Percent = 0.0001;
            LifeTear.MinAmount = 1;
            LifeTear.MaxAmount = 1;
            DropItemTable.add(LifeTear);
            for (DropItemData dropData : DropItemTable) {
                if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                    if (random.nextDouble() <= dropData.Percent) {
                        int amount;
                        if (dropData.MaxAmount != dropData.MinAmount) {
                            amount = random.nextInt(dropData.MaxAmount - dropData.MinAmount) + dropData.MinAmount;
                        } else {
                            amount = dropData.MinAmount;
                        }
                        playerData.ItemInventory.addItemParameter(dropData.itemParameter.clone(), amount);
                        Holo.add("§b§l[+]§e§l" + dropData.itemParameter.Display + "§a§lx" + amount);
                        if (playerData.DropLog.isItem()) {
                            player.sendMessage("§b[+]§e" + dropData.itemParameter.Display + "§ax" + amount);
                        }
                    }
                }
            }
            for (DropRuneData dropData : mobData.DropRuneTable) {
                if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                    if (random.nextDouble() <= dropData.Percent) {
                        RuneParameter runeParameter = dropData.runeParameter.clone();
                        runeParameter.Quality = random.nextDouble();
                        runeParameter.Level = Level;
                        playerData.RuneInventory.addRuneParameter(runeParameter);
                        Holo.add("§b§l[+]§e§l" + runeParameter.Display);
                        if (playerData.DropLog.isRune()) {
                            player.sendMessage("§b[+]§e" + runeParameter.Display);
                        }
                    }
                }
            }
            Location loc = entity.getLocation().clone().add(0, 1+Holo.size()*0.25, 0);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Hologram hologram = HologramsAPI.createHologram(plugin, loc);
                VisibilityManager visibilityManager = hologram.getVisibilityManager();
                visibilityManager.setVisibleByDefault(false);
                visibilityManager.showTo(player);
                for (String holo : Holo) {
                    hologram.appendTextLine(holo);
                }
                Bukkit.getScheduler().runTaskLater(plugin, hologram::delete, 50);
                playerData.viewUpdate();
            });
        }
    }
}

public final class MobManager {

    private static final HashMap<UUID, EnemyData> EnemyTable = new HashMap<>();

    private static double StatusMultiply(int level) {
        return Math.pow(0.74+(level/3f), 1.4);
    }

    static EnemyData EnemyTable(UUID uuid) {
        if (EnemyTable.containsKey(uuid)) {
            return EnemyTable.get(uuid);
        }
        Log("§cNon-EnemyData: " + uuid, true);
        return new EnemyData();
    }

    static boolean isEnemy(Entity uuid) {
        return getEnemyTable().containsKey(uuid.getUniqueId());
    }

    static HashMap<UUID, EnemyData> getEnemyTable() {
        return EnemyTable;
    }

    static EnemyData mobSpawn(MobData baseData, int level, Location location) {
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

class MobSpawnerData {
    MobData mobData;
    Location location;
    int Level = 1;
    int MaxMob = 5;
    int Radius = 5;
    int RadiusY = 5;
    int PerSpawn = 1;

    private final List<EnemyData> SpawnedList = new ArrayList<>();
    void start() {
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
    void spawn() {
        for (int i = 0; i < 4; i++) {
            double x = location.getX() + (2 * random.nextDouble() * Radius) - Radius;
            double z = location.getZ() + (2 * random.nextDouble() * Radius) - Radius;
            double y = location.getY() + RadiusY;
            Location origin = new Location(location.getWorld(), x, y, z, 0, 90);
            Location loc = origin.clone();
            for (int i2 = 0; i2 < RadiusY*2; i2++) {
                boolean spawnAble = !loc.getBlock().getType().isSolid() && loc.clone().add(0, -1, 0).getBlock().getType().isSolid();
                if (spawnAble) {
                    EnemyData enemyData = MobManager.mobSpawn(mobData, Level, loc);
                    SpawnedList.add(enemyData);
                    return;
                }
                loc.add(0, -1, 0);
            }
        }
    }
}