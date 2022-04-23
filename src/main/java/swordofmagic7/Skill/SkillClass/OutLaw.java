package swordofmagic7.Skill.SkillClass;

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
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.FanShapedCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

public class OutLaw extends BaseSkillClass {

    public OutLaw(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void SprinkleSand(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(0);
            double angle = radius*10;
            int time = skillData.ParameterValueInt(1)*20;
            int time2 = skillData.ParameterValueInt(2)*20;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false)) {
                EffectManager.addEffect(victim, EffectType.Silence, time, player);
                EffectManager.addEffect(victim, EffectType.SprinkleSand, time2, player);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void BreakBrick(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            int time = skillData.ParameterValueInt(1)*20;
            int time2 = skillData.ParameterValueInt(2)*20;
            int multiply = skillData.ParameterValueInt(4);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), 20, 1, 10);
            Ray ray = rayLocationEntity(player.getEyeLocation(), 5, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                LivingEntity entity = ray.HitEntity;
                int count = 1;
                double percent = 0.5;
                if ((Math.abs(entity.getLocation().getYaw() % 360 - player.getLocation().getYaw() % 360) < 45)) {
                    time *= multiply;
                    time2 *= multiply;
                    count *= multiply;
                    percent *= 2;
                }
                Damage.makeDamage(player, entity, DamageCause.MAT, skillData.Id, value, count);
                if (random.nextDouble() < percent) EffectManager.addEffect(entity, EffectType.Concussion, time, player);
                EffectManager.addEffect(entity, EffectType.BreakBrick, time2, player);
            }
            playSound(player, SoundList.AttackSweep);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Bully(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.FLAME);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Bully, time);
            ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void FireBlindly(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = 7;
            double angle = 130;
            double value = skillData.ParameterValue(0)/100;
            int time = skillData.ParameterValueInt(1)*20;
            int count = skillData.ParameterValueInt(1);
            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), length, angle, 3);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : FanShapedCollider(player.getLocation(), length, angle, skillProcess.Predicate(), false)) {
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, value, count);
                EffectManager.addEffect(victim, EffectType.Scary, time, player);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Rampage(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(1);
            double value = skillData.ParameterValue(0)/100;
            int time = skillData.ParameterValueInt(2)*20;
            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 18);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate())) {
                Function.setVelocity(victim, victim.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().setY(0.5));
                int count = 1;
                if (EffectManager.hasEffect(victim, EffectType.Scary)) {
                    EffectManager.addEffect(victim, EffectType.Stun, 100, player);
                    EffectManager.addEffect(victim, EffectType.Silence, 100, player);
                    if (playerData.Equipment.isEquipRune("大激怒のルーン")) count++;
                }
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, value, count);
                MultiThread.sleepTick(1);
            }
            EffectManager.addEffect(player, EffectType.Rampage, time, player);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

}
