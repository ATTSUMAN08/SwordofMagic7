package swordofmagic7.Skill.SkillClass;

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
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.Particle.ParticleManager.ShapedParticle;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.FanShapedCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;

public class Sheriff {

    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;


    public Sheriff(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void HeadShot(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.HeadShot, skillData.ParameterValueInt(1) * 20);
            ParticleManager.CylinderParticle(new ParticleData(Particle.SPELL_WITCH), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, "HeadShot");
    }

    public void Redemption(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            if (playerData.EffectManager.hasEffect(EffectType.RedemptionAble)) {
                skill.setCastReady(false);
                int time = skillData.ParameterValueInt(1) * 20;

                MultiThread.sleepTick(skillData.CastTime);

                playerData.EffectManager.addEffect(EffectType.Redemption, time);
                ParticleManager.CylinderParticle(new ParticleData(Particle.SPELL_WITCH), player.getLocation(), 1, 2, 3, 3);
                playSound(player, SoundList.Heal);
            } else {
                player.sendMessage("§e[" + EffectType.RedemptionAble.Display + "]§aが必要です");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, "Redemption");
    }

    public void Fanning(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(1)*20;
            int count = skillData.ParameterValueInt(2);
            int hitRate = Math.toIntExact(Math.round(skillData.ParameterValue(3) * 20));
            double radius = skillData.ParameterValue(4);
            double angle = 120;
            ParticleData particleData = new ParticleData(Particle.CRIT);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                MultiThread.sleepMillis(millis);
            }

            for (int i = 0; i <= time/hitRate; i++) {
                Set<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false);
                Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, count, 1);
                ShapedParticle(particleData, player.getLocation(), radius, angle, angle, 1, true);
                playSound(player, GunAttack, 5, 1);
                MultiThread.sleepTick(hitRate);
            }
            skillProcess.SkillRigid(skillData);
        }, "Fanning");
    }

    public void PeaceMaker(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double perforate = skillData.ParameterValue(1)/100;
            int time = skillData.ParameterValueInt(3)*20;

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), ray.HitPosition, 0, 10);
            ParticleManager.LineParticle(new ParticleData(Particle.FIREWORKS_SPARK), playerHandLocation(player), ray.HitPosition, 0, 10);
            ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), playerHandLocation(player), ray.HitPosition, 0, 10);
            if (ray.isHitEntity()) {
                Set<LivingEntity> targets = Function.NearLivingEntity(ray.HitPosition, 1.2, skillProcess.Predicate());
                targets.add(ray.HitEntity);
                playerData.EffectManager.addEffect(EffectType.RedemptionAble, time);
                for (LivingEntity target : targets) {
                    Damage.makeDamage(player, target, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, 1, perforate, true);
                    EffectManager.addEffect(target, EffectType.PeaceMaker, time, player);
                }
            }
            playSound(player, GunAttack);
            skillProcess.SkillRigid(skillData);
        }, "PeaceMaker");
    }
}
