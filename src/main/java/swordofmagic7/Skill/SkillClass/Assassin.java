package swordofmagic7.Skill.SkillClass;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.RectangleCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GUN_ATTACK;

public class Assassin extends BaseSkillClass {

    public Assassin(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void InstantAccel(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = 10;

            MultiThread.sleepTick(skillData.CastTime);

            ParticleData particleData = new ParticleData(Particle.WITCH, 0.2f, player.getLocation().getDirection());
            Location start = player.getEyeLocation().clone();
            start.setPitch(0);
            Ray ray = RayTrace.rayLocationBlock(start, length, false);
            Location origin = ray.HitPosition;
            origin.add(origin.toVector().subtract(player.getLocation().toVector()).normalize().multiply(-1));
            origin.setDirection(start.getDirection());
            ParticleManager.LineParticle(particleData, origin, player.getLocation(), 2, 10);
            Set<LivingEntity> victims = RectangleCollider(player.getLocation(), length, 2, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, 1, 1);
            if (victims.size() == 1) {
                playerData.Skill.resetSkillCoolTime("Cloaking");
            }
            playSound(player, SoundList.SHUN);
            if (!playerData.Equipment.isEquipRune("手練のルーン")) {
                MultiThread.TaskRunSynchronized(() -> {
                    player.teleportAsync(origin, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS);
                    MultiThread.TaskRunLater( () -> player.setVelocity(origin.getDirection()), 1, "InstantAccel");
                });
            }

            skillProcess.SkillRigid(skillData);
        }, "InstantAccel");
    }

    public void HallucinationSmoke(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(1);
            int effectTime = skillData.ParameterValueInt(2) * 20;
            final ParticleData particleData = new ParticleData(Particle.CLOUD);
            particleData.randomOffset = true;
            particleData.randomOffsetMultiply = (float) (radius/2);
            particleData.speedRandom = 0.5f;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            Location origin = player.getLocation().clone();
            int hitRate = 20;
            int time = skillData.ParameterValueInt(0)*20;
            MultiThread.TaskRun(() -> {
                for (int i = 0; i < time; i+=hitRate) {
                    ParticleManager.CircleParticle(particleData, origin, radius/2, 10);
                    for (LivingEntity entity : Function.NearLivingEntity(origin, radius, skillProcess.Predicate())) {
                        EffectManager.addEffect(entity, EffectType.HallucinationSmoke, effectTime, EffectManager.hasEffect(entity, EffectType.HallucinationSmoke) ? null : player);
                    }
                    MultiThread.sleepTick(hitRate);
                }
            }, "HallucinationSmoke");
            skillProcess.SkillRigid(skillData);
        }, "HallucinationSmoke");
    }

    public void PiercingHeart(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            double perforate = skillData.ParameterValue(1)/100;
            int time = skillData.ParameterValueInt(2)*20;

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), ray.HitPosition, 0, 10);
            ParticleManager.LineParticle(new ParticleData(Particle.WITCH), playerHandLocation(player), ray.HitPosition, 0, 10);
            ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), ray.HitPosition, 0, 10);
            if (ray.isHitEntity()) {
                LivingEntity victim = ray.HitEntity;
                int count = 1;
                if (EffectManager.hasEffect(victim, EffectType.HallucinationSmoke) && playerData.Equipment.isEquipRune("暗殺のルーン")) count++;
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, value, count, perforate, true);
                EffectManager.addEffect(victim, EffectType.RecoveryInhibition, time, player);
            }
            playSound(player, GUN_ATTACK);
            skillProcess.SkillRigid(skillData);
        }, "PeaceMaker");
    }

    public void Cloaking(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Covert, time);
            playerData.EffectManager.addEffect(EffectType.Cloaking, time);
            playerData.showHide(time);
            playSound(player, SoundList.SHOOT);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Annihilation(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(4);
            int time = skillData.ParameterValueInt(1)*20;
            int count = skillData.ParameterValueInt(2);
            int hitRate = Math.toIntExact(Math.round(skillData.ParameterValue(3) * 20));
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Invincible, time);
            Location lastLocation = player.getEyeLocation();
            for (int i = 0; i < time; i+=hitRate) {
                Set<LivingEntity> victims = Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate());
                for (LivingEntity victim : victims) {
                    ParticleManager.LineParticle(particleData, lastLocation, victim.getEyeLocation(), 1, 2);
                    lastLocation = victim.getEyeLocation();
                }
                Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, count, 1);
                ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 10);
                playSound(player, SoundList.SHUN);
                MultiThread.sleepTick(hitRate);
            }

            RuneParameter rune = playerData.Equipment.equippedRune("現状離脱のルーン");
            if (rune != null) {
                int time2 = rune.AdditionParameterValueInt(0)*20;
                playerData.EffectManager.addEffect(EffectType.Cloaking, time2);
            }
            skillProcess.SkillRigid(skillData);
        }, "PeaceMaker");
    }

}
