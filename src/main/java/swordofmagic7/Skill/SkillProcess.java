package swordofmagic7.Skill;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Function;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Sound.SoundList;

import java.util.*;
import java.util.function.Predicate;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;

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

    public Predicate<LivingEntity> PredicateA() {
        return entity -> entity != player && entity instanceof Player target && isAllies(target);
    }

    public Predicate<LivingEntity> PredicateA2() {
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
        } else if (enemy instanceof Player target) {
            return isAlive(target) && !isPlayerAllies(target);
        } else return MobManager.isEnemy(enemy);
    }

    public boolean isAllies(Player target) {
        if (target == player) return false;
        return isAlive(target) && isPlayerAllies(target);
    }

    public boolean isRevivalAble(Player target) {
        if (target == player) return false;
        return target.isOnline() && !isAlive(target) && isPlayerAllies(target);
    }

    public boolean isPlayerAllies(Player target) {
        PlayerData targetData = playerData(target);
        if (playerData.PvPMode && targetData.PvPMode) {
            if (playerData.Party == null || targetData.Party == null) {
                return false;
            } else return playerData.Party == targetData.Party;
        } else return true;
    }

    public static Set<LivingEntity> FanShapedCollider(Location location, double radius, double angle, Predicate<LivingEntity> Predicate, boolean single) {
        Set<LivingEntity> Targets = new HashSet<>(Function.NearLivingEntity(location, radius, Predicate));
        if (Targets.size() == 0) return Targets;
        Targets = ParticleManager.FanShapedCollider(location, Targets, angle);
        if (single && Targets.size() > 0) Targets = Collections.singleton(Nearest(location, Targets).get(0));
        return Targets;
    }

    public static Set<LivingEntity> RectangleCollider(Location location, double length, double width, Predicate<LivingEntity> Predicate, boolean single) {
        Set<LivingEntity> Targets = new HashSet<>(Function.NearLivingEntity(location, length, Predicate));
        if (Targets.size() == 0) return Targets;
        Targets = ParticleManager.RectangleCollider(location, Targets, length, width);
        if (single && Targets.size() > 0) Targets = Collections.singleton(Nearest(location, Targets).get(0));
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
    public int normalAttackCoolTime = 0;
    public int SkillCastTime = 0;

    public void SkillRigid(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            playerData.EffectManager.addEffect(EffectType.Rigidity, skillData.RigidTime);
            MultiThread.sleepTick(skillData.RigidTime);
            skill.setCastReady(true);
            skill.SkillCastProgress = 0f;
        }, "SkillRigid: ");
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
                        Ray ray = rayLocationEntity(player.getEyeLocation(), 15, 0.5, Predicate());
                        if (ray.isHitEntity()) {
                            victims.add(ray.HitEntity);
                        }
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
            }
        } else {
            player.sendMessage("§e[武器]§aが§e装備§aされていません");
            playSound(player, SoundList.Nope);
        }
    }

    public void BuffApply(SkillData skillData, EffectType effectType, ParticleData particleData, int time) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(effectType, time);
            ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            SkillRigid(skillData);
        }, "BuffApply");
    }
}