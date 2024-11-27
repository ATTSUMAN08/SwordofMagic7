package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
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

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Function.*;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.particleActivate;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Mage extends BaseSkillClass {

    public Mage(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Teleportation(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = skillData.ParameterValue(0);
            playerData.EffectManager.addEffect(EffectType.Invincible, skillData.ParameterValueInt(1) * 20);
            playerData.EffectManager.addEffect(EffectType.Teleportation, skillData.ParameterValueInt(2) * 20);
            RuneParameter rune = playerData.Equipment.equippedRune("無尽蔵のルーン");
            if (rune != null) {
                int time = rune.AdditionParameterValueInt(0)*20;
                double value = rune.AdditionParameterValue(1)/100;
                playerData.EffectManager.addEffect(EffectType.Inexhaustible, time, value);
            } else {
                Location origin;
                Ray ray = RayTrace.rayLocationBlock(player.getEyeLocation(), length, false);
                origin = ray.HitPosition;
                origin.add(player.getEyeLocation().getDirection().multiply(-1));
                origin.add(0, 0.2, 0);
                final ParticleData particleData = new ParticleData(Particle.FIREWORK);


                for (int i = 0; i < skillData.CastTime; i++) {
                    ParticleManager.CircleParticle(particleData, player.getLocation(), 1, 10);
                    ParticleManager.CircleParticle(particleData, origin, 1, 10);
                    MultiThread.sleepMillis(millis);
                }

                ParticleManager.CircleParticle(particleData, player.getLocation(), 1, 10);
                ParticleManager.CircleParticle(particleData, origin, 1, 10);
                origin.setDirection(player.getLocation().getDirection());
                MultiThread.TaskRunSynchronized(() -> player.teleportAsync(origin.add(0, 0.2, 0)));
            }
            playSound(player, SoundList.Warp);
            skillProcess.SkillRigid(skillData);
        }, "Teleportation");
    }

    public void MagicMissile(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.FIREWORK, 0.05f);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, 5);
            }

            skillProcess.SkillRigid(skillData);

            for (int i = -2; i < 2; i++) {
                Location lineLocation = player.getEyeLocation().clone().add(VectorUp).add(getRightDirection(player.getEyeLocation().clone()).multiply(i));
                ParticleManager.LineParticle(particleData, lineLocation, ray.HitPosition, 0, 10);
                playSound(player, SoundList.RodAttack);
                MultiThread.sleepTick(2);
            }
        }, skillData.Id);
    }

    public void Infall(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            double radius = skillData.ParameterValue(1);
            skill.setCastReady(false);
            final Location origin = player.getLocation().clone();
            ParticleData particleData = new ParticleData(Particle.CRIT);
            ParticleData particleData1 = new ParticleData(Particle.FIREWORK, 0.5f, VectorDown);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
            Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
            skillProcess.SkillRigid(skillData);
            for (LivingEntity victim : victims) {
                Location top = victim.getLocation().clone().add(0, 8, 0);
                ParticleManager.LineParticle(particleData, victim.getLocation(), top, 0.1, 5);
                ParticleManager.LineParticle(particleData1, victim.getLocation(), top, 0.1, 5);
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1);
                MultiThread.sleepTick(2);
            }
        }, "Infall");
    }
}
