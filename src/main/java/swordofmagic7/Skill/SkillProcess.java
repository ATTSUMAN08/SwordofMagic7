package swordofmagic7.Skill;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;
import static swordofmagic7.System.plugin;

public class SkillProcess {
    public final Player player;
    public final PlayerData playerData;
    public final Skill skill;

    public SkillProcess(Skill skill) {
        this.player = skill.player;
        this.playerData = skill.playerData;
        this.skill = skill;
    }

    public Predicate<LivingEntity> Predicate() {
        return entity -> entity != player && isEnemy(entity);
    }

    public Predicate<Entity> PredicateE() {
        return entity -> entity != player && isEnemy(entity);
    }

    public Predicate<Entity> PredicateA() {
        return entity -> entity != player && entity instanceof Player target && isAllies(target);
    }

    public Predicate<Entity> PredicateA2() {
        return entity -> entity != player && entity instanceof Player target && isRevivalAble(target);
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
                if (playerData.Party == null) {
                    return playerData.PvPMode && targetData.PvPMode;
                } else if (playerData.Party == targetData.Party) {
                    return false;
                }
            } else return false;
        } else if (MobManager.isEnemy(enemy)) {
            MobManager.EnemyTable(enemy.getUniqueId()).updateEntity();
            return true;
        } else return false;
        return false;
    }

    public boolean isAllies(Player target) {
        if (target == player) {
            return false;
        }
        if (target.isOnline() && isAlive(target)) {
            PlayerData targetData = playerData(target);
            if (playerData.Party == targetData.Party) return true;
            else return !(playerData.PvPMode && targetData.PvPMode);
        } else return false;
    }

    public boolean isRevivalAble(Player target) {
        if (target == player) {
            return false;
        }
        if (target.isOnline() && !isAlive(target)) {
            PlayerData targetData = playerData(target);
            if (playerData.Party == targetData.Party) return true;
            else return !(playerData.PvPMode && targetData.PvPMode);
        } else return false;
    }

    public static Set<LivingEntity> FanShapedCollider(Location location, double radius, double angle, Predicate<LivingEntity> Predicate, boolean single) {
        Set<LivingEntity> Targets = new HashSet<>(location.getNearbyLivingEntities(radius, Predicate));
        if (Targets.size() == 0) return Targets;
        if (single) {
            Targets.add(Nearest(location, Targets).get(0));
        } else {
            Targets = ParticleManager.FanShapedCollider(location, Targets, angle);
        }
        return Targets;
    }

    public static Set<LivingEntity> RectangleCollider(Location location, double length, double width, Predicate<LivingEntity> Predicate, boolean single) {
        Set<LivingEntity> Targets = new HashSet<>(location.getNearbyLivingEntities(length, Predicate));
        if (Targets.size() == 0) return Targets;
        if (single) {
            Targets.add(Nearest(location, Targets).get(0));
        } else {
            Targets = ParticleManager.RectangleCollider(location, Targets, length, width);
        }
        return Targets;
    }

    public static List<LivingEntity> Nearest(Location location, Set<LivingEntity> Entities) {
        return Nearest(location, Entities, 64);
    }

    public static List<LivingEntity> Nearest(Location location, Set<LivingEntity> Entities, double distance) {
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

    public static final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.YELLOW, 1));
    public static final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.ORANGE, 1));
    public static final int period = 1;
    private int normalAttackCoolTime = 0;
    public int SkillCastTime = 0;

    public void SkillRigid(SkillData skillData) {
        playerData.EffectManager.addEffect(EffectType.Rigidity, skillData.RigidTime);
        Bukkit.getScheduler().runTaskLater(plugin, () -> skill.setCastReady(true), skillData.RigidTime);
    }

    public void normalAttackTargetSelect() {
        if (playerData.Equipment.isWeaponEquip()) {
            if (0 >= normalAttackCoolTime) {
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.EquipmentCategory;
                Set<LivingEntity> victims = new HashSet<>();
                switch (category) {
                    case Blade -> victims = RectangleCollider(player.getLocation(), 4, 0.75, Predicate(), true);
                    case Mace -> victims = RectangleCollider(player.getLocation(), 6, 1.25, Predicate(), true);
                    case Rod, ActGun -> {
                        Ray ray = rayLocationEntity(player.getEyeLocation(), 15, 0.5, PredicateE());
                        if (ray.isHitEntity()) victims.add(ray.HitEntity);
                    }
                }
                normalAttack(victims);
            }
        }
    }

    public void normalAttack(Set<LivingEntity> victims) {
        final String damageSource = "attack";
        if (playerData.Equipment.isWeaponEquip()) {
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
                        ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), playerHandLocation(player), playerEyeLocation(player, 15), 0, 10);
                        Damage.makeDamage(player, victims, DamageCause.MAT, damageSource, 1, 1, 2);
                        playSound(player, SoundList.RodAttack);
                        normalAttackCoolTime = 12;
                    }
                    case ActGun -> {
                        ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), playerEyeLocation(player, 15), 0, 10);
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

    public void BuffApply(SkillData skillData, EffectType effectType, ParticleData particleData, int time) {
        skill.setCastReady(false);
        playerData.EffectManager.addEffect(effectType, time);
        ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
        playSound(player, SoundList.Heal);
        SkillRigid(skillData);
    }
}