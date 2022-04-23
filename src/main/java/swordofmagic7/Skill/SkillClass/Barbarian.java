package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
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
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Barbarian extends BaseSkillClass {

    public Barbarian(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Embowel(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = SkillProcess.BladeLength;
            double value = skillData.ParameterValue(0)/100;
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(particleData, playerHandLocation(player), length, 0, 3);
            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                LivingEntity victim = ray.HitEntity;
                Damage.makeDamage(player, victim, DamageCause.ATK, skillData.Id, value, 1);
                EffectManager.addEffect(victim, EffectType.Stun, 10, player);
                EffectManager.addEffect(player, EffectType.Stun, 10, null);
                MultiThread.TaskRunLater(() -> {
                    Function.setVelocity(victim, victim.getLocation().getDirection().setY(0).normalize().multiply(-1).setY(0.5));
                    Function.setVelocity(player, player.getLocation().getDirection().setY(0).normalize().multiply(-1).setY(0.5));
                }, 15, skillData.Id);
            }
            playSound(player, SoundList.AttackSweep);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void StompingKick(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100 * skillData.ParameterValue(2);
            double radius = skillData.ParameterValue(1);
            int time = skillData.ParameterValueInt(3)*20;
            ParticleData particleData = new ParticleData(Particle.CRIT);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : Function.NearLivingEntity(player.getLocation().clone().add(0, radius/2, 0), radius, skillProcess.Predicate())) {
                if (!victim.isOnGround()) {
                    Damage.makeDamage(player, victim, DamageCause.ATK, skillData.Id, value, 1);
                    EffectManager.addEffect(victim, EffectType.Stun, time, player);
                    ParticleManager.LineParticle(particleData, player.getEyeLocation(), victim.getEyeLocation(), 0.5, 2);
                }
            }

            playSound(player, SoundList.AttackSweep);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Cleave(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            double value2 = value * skillData.ParameterValue(2);
            double radius = skillData.ParameterValue(1);
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate())) {
                Damage.makeDamage(player, victim, DamageCause.ATK, skillData.Id, EffectManager.hasEffect(victim, EffectType.Stun) ? value2 : value, 1);
                ParticleManager.LineParticle(particleData, player.getEyeLocation(), victim.getEyeLocation(), 0.5, 1);
            }

            playSound(player, SoundList.AttackSweep);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Warcry(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            double radius = skillData.ParameterValue(3);
            ParticleData particleData = new ParticleData(Particle.VILLAGER_ANGRY);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            int stack = 0;
            RuneParameter rune = playerData.Equipment.equippedRune("タイマン上等のルーン");
            if (rune != null) {
                stack = rune.AdditionParameterValueInt(0);
            } else for (LivingEntity victim : Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate())) {
                particleData.spawn(victim.getEyeLocation());
                stack++;
            }
            playerData.EffectManager.addEffect(EffectType.Warcry, time, null, stack);

            playSound(player, SoundList.AttackSweep);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
