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
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.RectangleCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.SomCore.plugin;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;

public class Assassin {

    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public Assassin(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void InstantAccel(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = 10;

            MultiThread.sleepTick(skillData.CastTime);

            ParticleData particleData = new ParticleData(Particle.SPELL_WITCH, 0.2f, player.getLocation().getDirection());
            Location start = player.getEyeLocation().clone();
            start.setPitch(0);
            Ray ray = RayTrace.rayLocationBlock(start, length, false);
            Location origin = ray.HitPosition;
            origin.add(player.getEyeLocation().getDirection().multiply(-1));
            origin.setDirection(start.getDirection());
            ParticleManager.LineParticle(particleData, origin, player.getLocation(), 2, 10);
            Set<LivingEntity> victims = RectangleCollider(player.getLocation(), length, 2, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, 1, 1);
            if (victims.size() == 1) {
                playerData.Skill.resetSkillCoolTime("Cloaking");
            }
            playSound(player, SoundList.Shun);
            MultiThread.TaskRunSynchronized(() -> {
                player.teleportAsync(origin);
                MultiThread.TaskRunLater( () -> player.setVelocity(origin.getDirection()), 1, "InstantAccel");
            });
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

            while (skill.SkillCastProgress < 1) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            Location origin = player.getLocation().clone();
            int hitRate = 20;
            int time = skillData.ParameterValueInt(0)*20;
            MultiThread.TaskRun(() -> {
                int i = 0;
                while (i < time) {
                    ParticleManager.CircleParticle(particleData, origin, radius/2, 10);
                    for (LivingEntity entity : Function.NearLivingEntity(origin, radius, skillProcess.Predicate())) {
                        EffectManager.addEffect(entity, EffectType.HallucinationSmoke, effectTime, EffectManager.hasEffect(entity, EffectType.HallucinationSmoke) ? null : player);
                    }
                    i += hitRate;
                    MultiThread.sleepTick(hitRate);
                }
            }, "HallucinationSmoke");
            skillProcess.SkillRigid(skillData);
        }, "HallucinationSmoke");
    }

    public void PiercingHeart(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double perforate = skillData.ParameterValue(1)/100;
            int time = skillData.ParameterValueInt(2)*20;

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), ray.HitPosition, 0, 10);
            ParticleManager.LineParticle(new ParticleData(Particle.SPELL), playerHandLocation(player), ray.HitPosition, 0, 10);
            ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), playerHandLocation(player), ray.HitPosition, 0, 10);
            if (ray.isHitEntity()) {
                Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, 1, perforate, true);
                EffectManager.addEffect(ray.HitEntity, EffectType.RecoveryInhibition, time, player);
            }
            playSound(player, GunAttack);
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
            MultiThread.TaskRunSynchronized(() -> {
                for (Player player : PlayerList.get()) {
                    player.hidePlayer(plugin, playerData.player);
                }
            });
            MultiThread.TaskRun(() -> {
                MultiThread.sleepTick(time);
                MultiThread.TaskRunSynchronized(() -> {
                    for (Player player : PlayerList.get()) {
                        player.showPlayer(plugin, playerData.player);
                    }
                });
            }, "Cloaking");
            playSound(player, SoundList.Shoot);
            skillProcess.SkillRigid(skillData);
        }, "PeaceMaker");
    }

    public void Annihilation(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(4);
            int time = skillData.ParameterValueInt(1)*20;
            int count = skillData.ParameterValueInt(2);
            int hitRate = (int) Math.round(skillData.ParameterValue(3)*20);
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Invincible, time);
            int i = 0;
            Location lastLocation = player.getEyeLocation();
            while ((i += hitRate) < time) {
                int wait = hitRate;
                Set<LivingEntity> victims = Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate());
                for (LivingEntity victim : victims) {
                    ParticleManager.LineParticle(particleData, lastLocation, victim.getEyeLocation(), 1, 2);
                    Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, count);
                    lastLocation = victim.getEyeLocation();
                    wait--;
                    MultiThread.sleepTick(1);
                }
                ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 10);
                playSound(player, SoundList.Shun);
                if (wait > 0) MultiThread.sleepTick(hitRate);
            }
            skillProcess.SkillRigid(skillData);
        }, "PeaceMaker");
    }

}
