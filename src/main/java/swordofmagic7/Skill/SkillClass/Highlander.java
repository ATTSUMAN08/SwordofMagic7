package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
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

import java.util.Set;

import static swordofmagic7.Particle.ParticleManager.ShapedParticle;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Highlander extends BaseSkillClass {

    public Highlander(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void CartarStroke(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            double radius = skillData.ParameterValue(1);
            int time = skillData.ParameterValueInt(2)*20;
            ParticleData particleData = new ParticleData(Particle.CRIT).setRandomOffset(1.5f);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate())) {
                EffectManager.addEffect(victim, EffectType.Stun, time, player);
                ParticleManager.RandomVectorParticle(particleData, victim.getEyeLocation(), 30);
                Damage.makeDamage(player, victim, DamageCause.ATK, skillData.Id, value, 1);
            }
            playSound(player, SoundList.EXPLOSION);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void CrossCut(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = 5;
            double angle = 100;
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
            Set<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.ParameterValue(0) / 100, 2, 1);
            ShapedParticle(particleData, player.getLocation(), radius, angle, angle/2, 1, true);
            playSound(player, SoundList.ATTACK_SWEEP);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Crown(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(2)*20;
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK, 0.05f);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 5, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                ParticleManager.RandomVectorParticle(particleData, ray.HitEntity.getEyeLocation(), 30);
                Damage.makeDamage(player, ray.HitEntity, DamageCause.ATK, skillData.Id, skillData.ParameterValue(0) / 100, 1);
                EffectManager.addEffect(ray.HitEntity, EffectType.Concussion, time, player);
            }
            playSound(player, SoundList.ATTACK_SWEEP);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void WagonWheel(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = 6;
            double width = 3;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
            Vector vector = player.getLocation().getDirection().clone().normalize().setY(1.2);
            for (LivingEntity victim : RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false)) {
                Damage.makeDamage(player, victim, DamageCause.ATK, skillData.Id, skillData.ParameterValue(0) / 100, 1);
                Function.setVelocity(victim, vector);
            }
            playSound(player, SoundList.ATTACK_SWEEP);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void CrossGuard(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            RuneParameter rune = playerData.Equipment.equippedRune("手慣れたカウンターのルーン");
            int time = skillData.ParameterValueInt(0)*20;
            int time2 = (rune != null ? rune.AdditionParameterValueInt(0) : skillData.ParameterValueInt(1))*20;

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.CylinderParticle(particleActivate, player.getLocation(), 1, 2, 3, 3);
            playerData.EffectManager.addEffect(EffectType.CrossGuard, time, time2);

            playSound(player, SoundList.ROCK);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
