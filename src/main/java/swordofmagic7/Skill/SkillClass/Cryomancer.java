package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.RectangleCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Cryomancer extends BaseSkillClass {

    public Cryomancer(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void FrostPillar(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            final double radius = skillData.ParameterValue(4);
            final double distance = 16;
            final Location loc = RayTrace.rayLocationBlock(player.getEyeLocation(), distance, false).HitPosition;
            loc.setPitch(90);
            final Location origin = RayTrace.rayLocationBlock(loc, distance, false).HitPosition;
            final ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.4f, Function.VectorUp);
            particleData.randomOffset = true;
            particleData.randomOffsetMultiply = (float) (radius/2);
            particleData.speedRandom = 0.8f;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            skillProcess.SkillRigid(skillData);
            int hitRate = (int) Math.round(skillData.ParameterValue(2)*20);
            int time = skillData.ParameterValueInt(1)*20;
            int time2 = skillData.ParameterValueInt(5)*20;
            double freezePercent = skillData.ParameterValue(3)/100;
            MultiThread.TaskRun(() -> {
                for (int i = 0; i < time/hitRate; i++) {
                    ParticleManager.CircleParticle(particleData, origin, radius / 2, 10);
                    ParticleManager.CylinderParticle(particleData, origin, 2, 5, 30, 5);
                    Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
                    MultiThread.TaskRun(() -> {
                        for (LivingEntity victim : victims) {
                            ParticleManager.LineParticle(particleData, victim.getLocation(), victim.getEyeLocation(), 1, 10);
                            Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, 1);
                            if (random.nextDouble() < freezePercent) EffectManager.addEffect(victim, EffectType.Freeze, time2, player);
                            MultiThread.sleepTick(1);
                        }
                    }, skillData.Id);
                    MultiThread.sleepTick(hitRate);
                }
            }, skillData.Id);
        }, skillData.Id);

    }

    public void IceBlast(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            int count = 1 + skillData.ParameterValueInt(2);
            double radius = skillData.ParameterValue(1);
            Location origin = player.getLocation().clone().add(player.getLocation().getDirection().multiply(radius));
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.2f, Function.VectorUp);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.CircleParticle(particleData, origin, radius, 20);
            for (LivingEntity victim : Function.NearLivingEntity(origin, radius, skillProcess.Predicate())) {
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, value, EffectManager.hasEffect(victim, EffectType.Freeze) ? count : 1);
                ParticleManager.LineParticle(particleData, player.getEyeLocation(), victim.getEyeLocation(), 1, 5);
            }
            playSound(player, SoundList.DeBuff);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void IcePike(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.Parameter.get(0).Value / 100;
            double freezePercent = skillData.ParameterValue(1)/100;
            int time = skillData.ParameterValueInt(2)*20;
            double length = 10;
            double width = 3;
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.RectangleParticle(particleData, player.getLocation(), length, width, 3);
            for (LivingEntity victim : RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false)) {
                if (random.nextDouble() < freezePercent) EffectManager.addEffect(victim, EffectType.Freeze, time, player);
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, value, 1);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void SubzeroShield(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            double freezePercent  = skillData.ParameterValue(1)/100;
            int time2 = skillData.ParameterValueInt(2)*20;
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.SubzeroShield, time, new Object[]{freezePercent,time2});
            ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void SnowRolling(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            int time = skillData.ParameterValueInt(1)*20;
            int hitRate = skillData.ParameterValueInt(2)*20;
            double radius  = skillData.ParameterValue(3);
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK);

            MultiThread.sleepTick(skillData.CastTime);

            Set<LivingEntity> victims = new HashSet<>();
            for (int i = 0; i < time; i += hitRate) {
                victims.addAll(Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate()));
                Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, value, 1, 1);
                for (LivingEntity victim : victims) {
                    EffectManager.addEffect(victim, EffectType.Silence, hitRate+1,null);
                }
                ParticleManager.CylinderParticle(particleData, player.getLocation(), radius, 3, 3, 3);
                for (int i2 = 0; i2 < hitRate; i2++) {
                    for (LivingEntity victim : victims) {
                        victim.setVelocity(player.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize());
                    }
                    MultiThread.sleepTick(1);
                }
            }

            MultiThread.sleepTick(skillData.CastTime);

            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
