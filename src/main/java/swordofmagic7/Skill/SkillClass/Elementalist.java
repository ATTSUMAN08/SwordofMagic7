package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.RectangleCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.RodAttack;

public class Elementalist {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public Elementalist(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void ElementalBurst(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.ElementalBurst, (int) skillData.Parameter.get(0).Value*20);
            ParticleManager.CylinderParticle(new ParticleData(Particle.CRIT_MAGIC), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, "ElementalBurst");
    }

    public void Heil(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            final double radius = skillData.ParameterValue(4);
            final double distance = 16;
            final Location loc = RayTrace.rayLocationBlock(player.getEyeLocation(), distance, false).HitPosition;
            loc.setPitch(90);
            final Location origin = RayTrace.rayLocationBlock(loc, distance, false).HitPosition;
            final ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.4f, Function.VectorDown);
            particleData.randomOffset = true;
            particleData.randomOffsetMultiply = (float) (radius/2);
            particleData.speedRandom = 0.8f;

            while (skill.SkillCastProgress < 1) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            skillProcess.SkillRigid(skillData);
            int hitRate = (int) Math.round(skillData.ParameterValue(2)*20);
            int time = (int) Math.round(skillData.ParameterValue(1)*20);
            double freezePercent = skillData.ParameterValue(3)/100;
            MultiThread.TaskRun(() -> {
                int i = 0;
                while (i < time) {
                    ParticleManager.CircleParticle(particleData, origin.clone().add(0, 6, 0), radius / 2, 10);
                    Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
                    MultiThread.TaskRun(() -> {
                        for (LivingEntity victim : victims) {
                            ParticleManager.LineParticle(particleData, victim.getLocation(), victim.getEyeLocation().clone().add(0, 3, 0), 1, 10);
                            Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, 1);
                            if (random.nextDouble() < freezePercent) {
                                EffectManager.addEffect(victim, EffectType.Freeze, 20, player);
                            }
                            MultiThread.sleepTick(2);
                        }
                    }, "MagicCircleDataHeilTick");
                    i += hitRate;
                    MultiThread.sleepTick(hitRate);
                }
            }, "MagicCircleDataHeil");
        }, "Heil");
    }

    public void FireClaw(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            final double length = 8;
            final double width = 3;
            final double distance = 8;
            final Location loc = RayTrace.rayLocationBlock(player.getEyeLocation(), distance, false).HitPosition;
            loc.setPitch(90);
            final Location origin = RayTrace.rayLocationBlock(loc, distance, false).HitPosition;
            origin.setPitch(0);

            while (skill.SkillCastProgress < 1) {
                for (int i = 0; i < 3; i++) {
                    origin.setYaw(origin.getYaw() + 60);
                    Location pivot = origin.clone().add(origin.getDirection().multiply(-length));
                    ParticleManager.RectangleParticle(particleCasting, pivot, length * 2, width, 3);
                }
                MultiThread.sleepMillis(millis);
            }
            HashMap<LivingEntity, Integer> HitCount = new HashMap<>();
            for (int i = 0; i < 6; i++) {
                origin.setYaw(origin.getYaw() + 60);
                Location pivot = origin.clone().add(origin.getDirection().multiply(-1));
                ParticleManager.RectangleParticle(new ParticleData(Particle.FLAME), pivot, length, width, 3);
                Set<LivingEntity> victims = RectangleCollider(pivot, length, width, skillProcess.Predicate(), false);
                for (LivingEntity victim : victims) {
                    HitCount.put(victim, HitCount.getOrDefault(victim, 0) + 1);
                }
            }
            for (Map.Entry<LivingEntity, Integer> data : HitCount.entrySet()) {
                Damage.makeDamage(player, data.getKey(), DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, data.getValue());
                MultiThread.sleepTick(1);
            }
            playSound(player, SoundList.Fire, 6, 1);
            skillProcess.SkillRigid(skillData);
        }, "FireClaw");
    }

    public void Electrocute(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            double length = 20;
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                double radius = (int) skillData.ParameterValue(1);
                int count = (int) skillData.ParameterValue(2);
                LivingEntity target = ray.HitEntity;
                LivingEntity lastTarget = target;
                Set<LivingEntity> Hit = new HashSet<>();
                Damage.makeDamage(player, target, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1);
                ParticleManager.LineParticle(new ParticleData(Particle.FIREWORKS_SPARK), player.getEyeLocation(), target.getEyeLocation(), 0.5, 10);
                for (int i = 0; i < count; i++) {
                    Set<LivingEntity> nextTargets = new HashSet<>(Function.NearLivingEntity(target.getLocation(), radius, skillProcess.Predicate()));
                    nextTargets.removeAll(Hit);
                    if (nextTargets.size() > 0) {
                        target = SkillProcess.Nearest(target.getLocation(), nextTargets).get(0);
                        Damage.makeDamage(player, target, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1);
                        ParticleManager.LineParticle(new ParticleData(Particle.FIREWORKS_SPARK), lastTarget.getEyeLocation(), target.getEyeLocation(), 0.5, 10);
                        Hit.add(target);
                        lastTarget = target;
                        MultiThread.sleepTick(2);
                    } else break;
                }
                playSound(player, RodAttack);
            }
            skillProcess.SkillRigid(skillData);
        }, "Electrocute");
    }

    public void StormDust(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            final double radius = skillData.ParameterValue(3);
            final double distance = 16;
            final Location loc = RayTrace.rayLocationBlock(player.getEyeLocation(), distance, false).HitPosition;
            loc.setPitch(90);
            final Location origin = RayTrace.rayLocationBlock(loc, distance, false).HitPosition;
            final ParticleData particleData = new ParticleData(Particle.CLOUD, 0.2f, Function.VectorUp);
            particleData.randomOffset = true;
            particleData.randomOffsetMultiply = (float) (radius/2);
            particleData.speedRandom = 0.5f;

            while (skill.SkillCastProgress < 1) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            int hitRate = (int) Math.round(skillData.ParameterValue(2)*20);
            final int time = (int) Math.round(skillData.ParameterValue(1)*20);
            MultiThread.TaskRun(() -> {
                int i = 0;
                while (i < time) {
                    ParticleManager.CircleParticle(particleData, origin, radius/2, 10);
                    Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    i += hitRate;
                    MultiThread.sleepTick(hitRate);
                }
            }, "MagicCircleStormDust");
            skillProcess.SkillRigid(skillData);
        }, "StormDust");
    }
}
