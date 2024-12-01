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
import swordofmagic7.Effect.EffectManager;
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

    public static final double BladeLength = 6.5;

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

    public Predicate<LivingEntity> PredicateA_ME() {
        return entity -> entity == player || entity instanceof Player target && isAllies(target);
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
        if (Targets.isEmpty()) return Targets;
        Targets = ParticleManager.FanShapedCollider(location, Targets, angle);
        if (single && !Targets.isEmpty()) Targets = Collections.singleton(Nearest(location, Targets).getFirst());
        return Targets;
    }

    public static Set<LivingEntity> RectangleCollider(Location location, double length, double width, Predicate<LivingEntity> Predicate, boolean single) {
        Set<LivingEntity> Targets = new HashSet<>(Function.NearLivingEntity(location, length, Predicate));
        if (Targets.isEmpty()) return Targets;
        Targets = ParticleManager.RectangleCollider(location, Targets, length, width);
        if (single && !Targets.isEmpty()) Targets = Collections.singleton(Nearest(location, Targets).getFirst());
        return Targets;
    }

    public static List<LivingEntity> Nearest(Location location, Set<LivingEntity> Entities) {
        return Nearest(location, Entities, 64);
    }

    public static List<LivingEntity> Nearest(Location location, Set<LivingEntity> Entities, double distance) {
        if (Entities.isEmpty()) return new ArrayList<>();
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

    public static final ParticleData particleCasting = new ParticleData(Particle.DUST, new Particle.DustOptions(Color.YELLOW, 1));
    public static final ParticleData particleActivate = new ParticleData(Particle.DUST, new Particle.DustOptions(Color.ORANGE, 1));
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
        if (playerData.Equipment.isMainHandEquip()) {
            if (0 >= normalAttackCoolTime) {
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.equipmentCategory;
                Set<LivingEntity> victims = new HashSet<>();
                switch (category) {
                    case Blade -> victims = RectangleCollider(player.getLocation(), SkillProcess.BladeLength, 1.5, Predicate(), true);
                    case Mace -> victims = RectangleCollider(player.getLocation(), 6, 1.25, Predicate(), true);
                    case Rod, ActGun -> {
                        Ray ray = rayLocationEntity(player.getEyeLocation(), 25, 0.75, Predicate());
                        if (ray.isHitEntity()) {
                            victims.add(ray.HitEntity);
                        }
                    }
                }
                normalAttack(victims);
            }
        }
    }

    public void normalAttackParticle(LivingEntity victim, Particle particle, double width, double length) {
        if (victim != null) {
            ParticleManager.LineParticle(new ParticleData(particle), playerHandLocation(player), victim.getEyeLocation(), width, 10);
        } else {
            ParticleManager.LineParticle(new ParticleData(particle), playerHandLocation(player), playerEyeLocation(player, length), width, 10);
        }
    }

    public void normalAttack(Set<LivingEntity> victims) {
        if (playerData.isAFK()) return;
        final String damageSource = "attack";
        if (playerData.Equipment.isMainHandEquip()) {
            if (0 >= normalAttackCoolTime) {
                LivingEntity victim = null;
                for (LivingEntity entity : victims) {
                    victim = entity;
                }
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.equipmentCategory;
                switch (category) {
                    case Blade -> {
                        normalAttackParticle(victim, Particle.SWEEP_ATTACK, 0, SkillProcess.BladeLength);
                        normalAttackCoolTime = 7;
                    }
                    case Mace -> {
                        normalAttackParticle(victim, Particle.CRIT, 0, 6);
                        normalAttackCoolTime = 15;
                    }
                    case Rod -> {
                        normalAttackParticle(victim, Particle.CRIT, 0, 25);
                        playSound(player, SoundList.RodAttack);
                        normalAttackCoolTime = 12;
                    }
                    case ActGun -> {
                        normalAttackParticle(victim, Particle.CRIT, 0, 25);
                        playSound(player, GunAttack);
                        normalAttackCoolTime = playerData.EffectManager.hasEffect(EffectType.DoubleGunStance) ? 7 : 10;
                    }
                    case Baton -> {
                        if (playerData.PetSummon.isEmpty()) {
                            sendMessage(player, "§e[ペット]§aが§e召喚§aされていません", SoundList.Nope);
                        }
                    }
                    default -> sendMessage(player, "§e[武器]§aが§e装備§aされていません", SoundList.Nope);
                }
                double damageMultiply = 1;
                if (playerData.EffectManager.hasEffect(EffectType.CoveringFire)) damageMultiply += playerData.EffectManager.getData(EffectType.CoveringFire).getDouble(0);
                if (victim != null) {
                    if (category == EquipmentCategory.Mace) damageMultiply /= 2;
                    switch (category) {
                        case Blade, Mace -> Damage.makeDamage(player, victim, DamageCause.ATK, damageSource, damageMultiply, 1);
                        case Rod, ActGun -> Damage.makeDamage(player, victim, DamageCause.MAT, damageSource, damageMultiply, 1);
                    }
                }
            }
        } else {
            sendMessage(player, "§e[武器]§aが§e装備§aされていません", SoundList.Nope);
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
        }, skillData.Id);
    }

    public void PartyBuffApply(SkillData skillData, EffectType effectType, ParticleData particleData, int time) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            Set<Player> players = new HashSet<>();
            players.add(player);
            if (playerData.Party != null) players.addAll(playerData.Party.Members);
            for (Player player : players) {
                EffectManager.addEffect(player, effectType, time, this.player);
                ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
                playSound(player, SoundList.Heal);
            }
            SkillRigid(skillData);
        }, skillData.Id);
    }
}