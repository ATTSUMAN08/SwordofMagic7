package swordofmagic7.Skill;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static swordofmagic7.Damage.Damage.makeHeal;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Particle.ParticleManager.ShapedParticle;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;
import static swordofmagic7.Sound.SoundList.Heal;

public class SkillProcess {
    private final Plugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final swordofmagic7.Skill.Skill Skill;

    public SkillProcess(Player player, PlayerData playerData, Plugin plugin, Skill Skill) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;
        this.Skill = Skill;
    }

    public Predicate<LivingEntity> Predicate() {
        return entity -> entity != player && isEnemy(entity);
    }

    public Predicate<Entity> PredicateE() {
        return entity -> entity != player && isEnemy(entity);
    }

    public Predicate<Entity> PredicateA() {
        return entity -> entity != player && entity instanceof Player && !isEnemy(entity);
    }

    public boolean isEnemy(Entity enemy) {
        if (ignoreEntity(enemy)) return false;
        if (PetManager.isPet(enemy)) {
            PetParameter pet = PetManager.PetParameter(enemy);
            enemy = pet.player;
        }
        if (enemy == player) {
            return false;
        }
        if (enemy instanceof Player target) {
            if (target.isOnline() && isAlive(target)) {
                PlayerData targetData = playerData(target);
                return playerData.PvPMode && targetData.PvPMode;
            } else return false;
        } else if (MobManager.isEnemy(enemy)) {
            MobManager.EnemyTable(enemy.getUniqueId()).updateEntity();
            return true;
        } else return false;
    }

    private List<LivingEntity> FanShapedCollider(Location location, double radius, double angle, Predicate<LivingEntity> Predicate, boolean single) {
        List<LivingEntity> Targets = (List<LivingEntity>) location.getNearbyLivingEntities(radius, Predicate);
        if (Targets.size() == 0) return Targets;
        Targets = ParticleManager.FanShapedCollider(location, Targets, angle);
        if (single) Targets = Nearest(location, Targets);
        return Targets;
    }

    private List<LivingEntity> RectangleCollider(Location location, double length, double width, Predicate<LivingEntity> Predicate, boolean single) {
        List<LivingEntity> Targets = (List<LivingEntity>) location.getNearbyLivingEntities(length, Predicate);
        if (Targets.size() == 0) return Targets;
        Targets = ParticleManager.RectangleCollider(location, Targets, length, width);
        if (single) Targets = Nearest(location, Targets);
        return Targets;
    }

    List<LivingEntity> Nearest(Location location, List<LivingEntity> Entities) {
        return Nearest(location, Entities, 64);
    }

    List<LivingEntity> Nearest(Location location, List<LivingEntity> Entities, double distance) {
        if (Entities.size() == 0) return new ArrayList<>();
        LivingEntity target = null;
        for (LivingEntity entity : Entities) {
            if (location.distance(entity.getLocation()) < distance) {
                distance = location.distance(entity.getLocation());
                target = entity;
            }
        }
        List<LivingEntity> Targets = new ArrayList<>();
        if (target != null) {
            Targets.add(target);
        }
        return Targets;
    }

    private final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.YELLOW, 1));
    private final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.ORANGE, 1));
    private final int period = 1;
    private int normalAttackCoolTime = 0;
    int SkillCastTime = 0;

    public void SkillRigid(SkillData skillData) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
    }

    public void normalAttackTargetSelect() {
        if (playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
            if (0 >= normalAttackCoolTime) {
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.EquipmentCategory;
                List<LivingEntity> victims = new ArrayList<>();
                switch (category) {
                    case Blade -> victims = RectangleCollider(player.getLocation(), 4, 0.75, Predicate(), true);
                    case Mace -> victims = RectangleCollider(player.getLocation(), 6, 1.25, Predicate(), true);
                    case Rod, ActGun -> {
                        Ray ray = rayLocationEntity(player.getEyeLocation(), 15, 0.5, PredicateE());
                        if (ray.isHitEntity()) victims = List.of(ray.HitEntity);
                    }
                }
                normalAttack(victims);
            }
        }
    }

    public void normalAttack(List<LivingEntity> victims) {
        final String damageSource = "attack";
        if (playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
            if (0 >= normalAttackCoolTime) {
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.EquipmentCategory;
                switch (category) {
                    case Blade -> {
                        Damage.makeDamage(player, victims, DamageCause.ATK, damageSource, 1, 1, 2);
                        normalAttackCoolTime = 12;
                    }
                    case Mace -> {
                        Damage.makeDamage(player, victims, DamageCause.ATK, damageSource, 1, 1, 2);
                        normalAttackCoolTime = 15;
                    }
                    case Rod -> {
                        ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), player.getEyeLocation(), 15, 0, 10, true);
                        Damage.makeDamage(player, victims, DamageCause.MAT, damageSource, 1, 1, 2);
                        playSound(player, SoundList.RodAttack);
                        normalAttackCoolTime = 12;
                    }
                    case ActGun -> {
                        ParticleManager.LineParticle(new ParticleData(Particle.CRIT), player.getEyeLocation(), 15, 0, 10, true);
                        Damage.makeDamage(player, victims, DamageCause.MAT, damageSource, 1, 1, 2);
                        playSound(player, GunAttack);
                        normalAttackCoolTime = 10;
                    }
                    case Baton -> {
                        if (playerData.PetSummon.size() == 0) {
                            player.sendMessage("§e[ペット]§aが§e召喚§aされていません");
                            playSound(player, SoundList.Nope);
                        }
                    }
                    default -> {
                        player.sendMessage("§e[武器]§aが§e装備§aされていません");
                        playSound(player, SoundList.Nope);
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        normalAttackCoolTime--;
                        if (normalAttackCoolTime <= 0) this.cancel();
                    }
                }.runTaskTimerAsynchronously(plugin, 0, 1);
            }
        } else {
            player.sendMessage("§e[武器]§aが§e装備§aされていません");
            playSound(player, SoundList.Nope);
        }
    }

    void Slash(SkillData skillData, double radius, double angle) {
        Skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                } else {
                    this.cancel();
                    ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
                    List<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    ShapedParticle(new ParticleData(Particle.SWEEP_ATTACK), player.getLocation(), radius, angle, angle/2, 1, true);
                    SkillRigid(skillData);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Vertical(SkillData skillData, double length, double width) {
        Skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                } else {
                    this.cancel();
                    ParticleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
                    List<LivingEntity> victims = RectangleCollider(player.getLocation(), length, width, Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    SkillRigid(skillData);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Rain(SkillData skillData, double radius) {
        Skill.setCastReady(false);
        final Location loc = RayTrace.rayLocation(player.getEyeLocation(),32, 0.1, false, PredicateE()).HitPosition;
        loc.setPitch(90);
        final Location origin = RayTrace.rayLocationBlock(loc, 32, false).HitPosition;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
                    List<LivingEntity> victims = (List<LivingEntity>) origin.getNearbyLivingEntities(radius, Predicate());
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    new BukkitRunnable() {
                        int i = 0;
                        final Location top = origin.clone().add(0, 8, 0);
                        @Override
                        public void run() {
                            if (i < victims.size()) {
                                ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), victims.get(i).getLocation(), top, 0.1, 10);
                            } else {
                                this.cancel();
                            }
                            i++;
                        }
                    }.runTaskTimerAsynchronously(plugin, 0, 2);
                    SkillRigid(skillData);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void DoubleTrigger(SkillData skillData) {
        Skill.setCastReady(false);
        final Location loc = RayTrace.rayLocation(player.getEyeLocation(),32, 0.1, false, PredicateE()).HitPosition;
        loc.setPitch(90);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    ParticleManager.LineParticle(new ParticleData(Particle.CRIT), player.getEyeLocation(), 20, 0, 10);
                    Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, PredicateE());
                    if (ray.isHitEntity()) Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 2);
                    playSound(player, GunAttack, 2, 2);
                    SkillRigid(skillData);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Infall(SkillData skillData, double radius) {
        Skill.setCastReady(false);
        final Location origin = player.getLocation().clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
                    List<LivingEntity> victims = (List<LivingEntity>) origin.getNearbyLivingEntities(radius, Predicate());
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    new BukkitRunnable() {
                        int i = 0;
                        @Override
                        public void run() {
                            if (i < victims.size()) {
                                Location top = victims.get(i).getLocation().clone().add(0, 8, 0);
                                ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), victims.get(i).getLocation(), top, 0.1, 5);
                                ParticleManager.LineParticle(new ParticleData(Particle.FIREWORKS_SPARK, 0.5f, VectorDown), victims.get(i).getLocation(), top, 0.1, 5);
                            } else {
                                this.cancel();
                            }
                            i++;
                        }
                    }.runTaskTimerAsynchronously(plugin, 0, 2);
                    SkillRigid(skillData);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Smite(SkillData skillData, double radius) {
        Skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                final Location origin = player.getLocation().clone().add(player.getLocation().getDirection().setY(0).normalize().multiply(radius));
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
                    List<LivingEntity> victims = (List<LivingEntity>) origin.getNearbyLivingEntities(radius, Predicate());
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    SkillRigid(skillData);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Heal(SkillData skillData, double length) {
        Skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    Ray ray = rayLocationEntity(player.getEyeLocation(), length, 1, PredicateA());
                    Player target;
                    if (ray.isHitEntity()) {
                        target = (Player) ray.HitEntity;
                        ParticleManager.LineParticle(new ParticleData(Particle.VILLAGER_HAPPY), player.getEyeLocation(), target.getEyeLocation(), 0, 10);
                    } else {
                        target = player;
                    }
                    ParticleManager.CylinderParticle(new ParticleData(Particle.VILLAGER_HAPPY), target.getLocation(), 1, 2, 3, 3);
                    makeHeal(player, target, skillData.Parameter.get(0).Value/100);
                    playSound(player, Heal);
                    SkillRigid(skillData);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}