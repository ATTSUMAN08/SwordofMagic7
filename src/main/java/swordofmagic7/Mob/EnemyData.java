package swordofmagic7.Mob;

import com.destroystokyo.paper.entity.Pathfinder;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.System;

import java.util.*;

import static swordofmagic7.Data.DataBase.getItemParameter;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Sound.CustomSound.playSound;

public class EnemyData {
    private final Plugin plugin = System.plugin;
    private final Random random = new Random();

    public UUID uuid;
    public LivingEntity entity;
    public MobData mobData;
    public int Level;
    public double MaxHealth;
    public double Health;
    public double ATK;
    public double DEF;
    public double ACC;
    public double EVA;
    public double CriticalRate;
    public double CriticalResist;
    public double Exp;

    public void updateEntity() {
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
    public boolean isDead = false;

    public EnemyData() {
    }

    public EnemyData(LivingEntity entity) {
        this.entity = entity;
    }

    void Involved(Player player) {
        if (!Involved.contains(player)) {
            Involved.add(player);
        }
    }

    private final java.util.function.Predicate<LivingEntity> Predicate = entity -> entity.getType() == EntityType.PLAYER;

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

    public void addPriority(LivingEntity entity, double addPriority) {
        Priority.putIfAbsent(entity, addPriority);
        Priority.put(entity, Priority.get(entity) + addPriority);
    }

    public void delete() {
        isDead = true;
        stopAI();
        MobManager.getEnemyTable().remove(entity.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            entity.setHealth(0);
            entity.remove();
        });
    }

    public void dead() {
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
            Location loc = entity.getLocation().clone().add(0, 1 + Holo.size() * 0.25, 0);
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
