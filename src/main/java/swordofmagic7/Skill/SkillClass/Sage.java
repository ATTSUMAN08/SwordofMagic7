package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Sage extends BaseSkillClass {

    public Sage(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Blink(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = skillData.ParameterValue(0);
            double radius = skillData.ParameterValue(1);
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f, Function.VectorUp);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            Set<Player> players = new HashSet<>();
            players.add(player);
            if (playerData.Party != null) for (Player player : playerData.Party.Members) {
                if (player.getLocation().distance(this.player.getLocation()) < radius) {
                    players.add(player);
                }
            }
            Ray ray = RayTrace.rayLocationBlock(player.getEyeLocation(), length, false);
            Location origin = ray.HitPosition;
            origin.add(player.getLocation().getDirection().multiply(-1));
            MultiThread.TaskRunSynchronized(() -> {
                for (Player player : players) {
                    player.teleportAsync(origin);
                    playSound(player, SoundList.LevelUp);
                }
            });
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void DimensionCompression(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            double radius = skillData.ParameterValue(1);
            ParticleData particleData = new ParticleData(Particle.PORTAL, 0.5f, Function.VectorUp).setRandomOffset(1.5f);
            ParticleData particleData2 = new ParticleData(Particle.ENCHANTMENT_TABLE);
            Location loc = RayTrace.rayLocationBlock(player.getEyeLocation(), radius, false).HitPosition;
            loc.setPitch(90);
            Location origin = RayTrace.rayLocationBlock(loc, radius, false).HitPosition;


            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            Location top = origin.clone().add(0, 2.5,0);
            MultiThread.TaskRun(() -> {
                for (int i = 0; i < time/4; i++) {
                    ParticleManager.CircleParticle(particleData, origin, radius, 36);
                    ParticleManager.RandomVectorParticle(particleData, top, 36);
                    for (LivingEntity entity : Function.NearLivingEntity(origin, radius, skillProcess.Predicate())) {
                        Vector vector = origin.toVector().subtract(entity.getLocation().toVector()).multiply(0.25);
                        if (vector.length() > 1) vector.normalize();
                        entity.setVelocity(entity.getVelocity().add(vector));
                        ParticleManager.LineParticle(particleData2, entity.getEyeLocation(), top, 0.5, 5);
                    }
                    MultiThread.sleepTick(5);
                }
            }, skillData.Id);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void MicroDimension(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(2)*20;
            double radius = skillData.ParameterValue(1);
            ParticleData particleData = new ParticleData(Particle.PORTAL, 0.1f, Function.VectorUp);
            Ray ray = RayTrace.rayLocationBlock(player.getEyeLocation(), radius, false);
            Location origin = ray.HitPosition;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            origin.add(0, 1,0);
            ParticleManager.CircleParticle(particleData, origin, radius, 10);
            for (LivingEntity entity : Function.NearLivingEntity(origin, radius, skillProcess.Predicate())) {
                EffectManager.addEffect(entity, EffectType.Confusion, time, player);
                EffectManager.addEffect(entity, EffectType.SequelaeReducedDistortion, time, player);
                Damage.makeDamage(player, entity, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, 1);
                ParticleManager.CircleParticle(particleData, entity.getLocation(), 1, 10);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void UltimateDimension(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(2)*20;
            double radius = skillData.ParameterValue(1);
            ParticleData particleData = new ParticleData(Particle.PORTAL, 0.1f, Function.VectorUp);
            Ray ray = RayTrace.rayLocationBlock(player.getEyeLocation(), radius, false);
            Location origin = ray.HitPosition;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            origin.add(0, 1,0);
            ParticleManager.CircleParticle(particleData, origin, radius, 10);
            for (LivingEntity entity : Function.NearLivingEntity(origin, radius, skillProcess.Predicate())) {
                EffectManager.addEffect(entity, EffectType.Confusion, time, player);
                int count =  EffectManager.hasEffect(entity, EffectType.SequelaeReducedDistortion) ? 2 : 1;
                Damage.makeDamage(player, entity, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, count);
                ParticleManager.CircleParticle(particleData, entity.getLocation(), 1, 10);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void MissileHole(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            double radius = skillData.ParameterValue(1);
            ParticleData particleData = new ParticleData(Particle.PORTAL, 0.1f, Function.VectorUp);

            MultiThread.sleepTick(skillData.CastTime);

            for (Player player : PlayerList.getNearNonDead(player.getLocation(), radius)) {
                if (skillProcess.isAllies(player) || player == this.player) {
                    ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 10, 3);
                    EffectManager.addEffect(player, EffectType.MissileHole, time, this.player);
                    playSound(player, SoundList.Heal);
                    MultiThread.sleepTick(1);
                }
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
