package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Matador extends BaseSkillClass {

    public Matador(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Capote(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = 20;
            int time = skillData.ParameterValueInt(0)*20;
            ParticleData particleData = new ParticleData(Particle.REDSTONE);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(particleData, playerHandLocation(player), length, 1, 5);
            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 2, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                EffectManager.addEffect(ray.HitEntity, EffectType.Capote, time, player, new Object[]{player});
            } else {
                sendMessage(player, "§c対象§aがいません", SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            playSound(player, SoundList.Howl);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Ole(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = 20;
            int time = skillData.ParameterValueInt(0)*20;
            double movement = skillData.ParameterValue(2)/100;
            ParticleData particleData = new ParticleData(Particle.REDSTONE);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(particleData, playerHandLocation(player), length, 1, 5);
            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 2, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                LivingEntity victim = ray.HitEntity;
                if (MobManager.isEnemy(victim)) {
                    if (MobManager.EnemyTable(victim.getUniqueId()).target != player) {
                        sendMessage(player, "§cトップヘイト§aではありません", SoundList.Nope);
                        skill.resetSkillCoolTimeWaited(skillData);
                        skillProcess.SkillRigid(skillData);
                        return;
                    }
                }
                playerData.EffectManager.addEffect(EffectType.Ole, time, movement);
            } else {
                sendMessage(player, "§c対象§aがいません", SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            playSound(player, SoundList.Howl);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Faena(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = SkillProcess.BladeLength;
            double value = skillData.ParameterValue(0)/100;
            int count = skillData.ParameterValueInt(1);
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(particleData, playerHandLocation(player), length, 1, 3);
            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 2, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                Damage.makeDamage(player, ray.HitEntity, DamageCause.ATK, skillData.Id, value, count);
            }
            playSound(player, SoundList.AttackSweep, count/2, 1);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Muleta(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            int time = skillData.ParameterValueInt(1)*20;
            ParticleData particleData = new ParticleData(Particle.LAVA);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
            playerData.EffectManager.addEffect(EffectType.Muleta, time, value);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void CorridaFinale(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            double length = 14;
            double width = 6;
            Set<LivingEntity> victims;

            Location origin = player.getLocation().clone();
            origin.setDirection(origin.getDirection().clone().setY(0));
            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.RectangleParticle(particleCasting, origin, length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            victims = RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, value, 1, 1);
            playSound(player, SoundList.Howl);

            MultiThread.sleepTick(20);
            origin = origin.add(origin.getDirection().clone().setY(0).normalize().multiply(length));
            origin.setDirection(origin.getDirection().clone().multiply(-1));
            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.RectangleParticle(particleCasting, origin, length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            victims = RectangleCollider(origin, length, width, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, value, 1, 1);
            playSound(player, SoundList.Howl);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
