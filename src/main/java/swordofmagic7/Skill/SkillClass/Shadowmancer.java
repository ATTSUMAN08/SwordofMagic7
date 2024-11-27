package swordofmagic7.Skill.SkillClass;

import org.bukkit.Color;
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
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.RectangleCollider;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Shadowmancer extends BaseSkillClass {
    public Shadowmancer(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public static ParticleData particleData = new ParticleData(Particle.DUST, new Particle.DustOptions(Color.BLACK, 1));

    public void ShadowPool(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.ShadowPool, time);
            playerData.showHide(time);
            double offset = 0;
            for (int i = 0; i < time; i+=2) {
                if (offset >= 1) offset = 0;
                else offset += 0.1;
                ParticleManager.CircleParticle(particleData, player.getLocation().clone().add(0, offset, 0), 1, 12);
                MultiThread.sleepTick(2);
            }
            playSound(player, SoundList.Shoot);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Hallucination(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            double value = skillData.ParameterValue(1)/100;
            ParticleData particleData = new ParticleData(Particle.DUST, new Particle.DustOptions(Color.YELLOW, 1));

            MultiThread.sleepTick(skillData.CastTime);

            playerData.changeShield(playerData.Status.Health*value, time);
            ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void ShadowThorn(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double length = skillData.ParameterValue(2);
            double width = length/2;
            double value = skillData.ParameterValue(0)/100;
            double perforate = skillData.ParameterValue(1)/100;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.RectangleParticle(particleData, player.getLocation(), length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false)) {
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, value, 1, perforate, true);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void ShadowCondensation(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            int max = skillData.ParameterValueInt(1);
            double radius = skillData.ParameterValue(2);
            double radius2 = skillData.ParameterValue(3);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 24);
                MultiThread.sleepMillis(millis);
            }

            int i = 0;
            RuneParameter rune = playerData.Equipment.equippedRune("影の世界のルーン");
            boolean bool = rune != null;
            int time = bool ? rune.AdditionParameterValueInt(0)*20 : 0;
            for (LivingEntity victim : Function.NearLivingEntity(player.getLocation(), radius, player -> player != this.player)) {
                ParticleManager.CircleParticle(particleData, victim.getLocation(), radius2, 12);
                for (LivingEntity victim2 : Function.NearLivingEntity(victim.getLocation(), radius2, skillProcess.Predicate())) {
                    if (bool) EffectManager.addEffect(victim2, EffectType.ShadowFatter, time, player, victim2.getLocation());
                    else Damage.makeDamage(player, victim2, DamageCause.MAT, skillData.Id, value, 1);
                    MultiThread.sleepMillis(25);
                }
                i++;
                if (i >= max) break;
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void ShadowFatter(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                ParticleManager.LineParticle(particleData, playerHandLocation(player), ray.HitEntity.getEyeLocation(), 0, 5);
                EffectManager.addEffect(ray.HitEntity, EffectType.ShadowFatter, time, player, new Object[]{ray.HitEntity.getLocation()});
                playSound(player, SoundList.DeBuff);
            } else {
                sendMessage(player, "§c対象§aがいません", SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
