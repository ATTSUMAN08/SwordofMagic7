package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Peltast extends BaseSkillClass {

    public Peltast(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void ShieldBash(SkillData skillData, double length, double width) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
            Set<LivingEntity> victims = RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
            Set<LivingEntity> push = new HashSet<>(victims);
            push.add(player);
            Vector vector = player.getEyeLocation().getDirection().clone().setY(0).normalize().multiply(2).setY(0.3);
            for (LivingEntity entity : push) {
                Function.Push(entity, vector);
            }
            skillProcess.SkillRigid(skillData);
        }, "ShieldBash");
    }

    public void HighGuard(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.HighGuard, (int) skillData.Parameter.get(0).Value * 20);
            playerData.EffectManager.addEffect(EffectType.Stun, (int) skillData.Parameter.get(0).Value * 20);
            ParticleManager.CylinderParticle(new ParticleData(Particle.SMOKE_NORMAL), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, "HighGuard");
    }

    public void SwashBaring(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.SwashBaring, (int) skillData.Parameter.get(0).Value*20);
            playerData.EffectManager.addEffect(EffectType.HatePriority, 200);
            ParticleManager.CylinderParticle(new ParticleData(Particle.REDSTONE), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, "SwashBaring");
    }
}
