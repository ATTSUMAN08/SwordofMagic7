package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
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
            double value = skillData.ParameterValue(0)/100;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            int count = 1;
            if (playerData.Equipment.isEquipRune("打撲のルーン")) count++;
            ParticleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
            Set<LivingEntity> victims = RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, value, count, 2);
            Set<LivingEntity> push = new HashSet<>(victims);
            push.add(player);
            Vector vector = player.getEyeLocation().getDirection().clone().setY(0).normalize().multiply(2).setY(0.3);
            for (LivingEntity entity : push) {
                Function.setVelocity(entity, vector);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void HighGuard(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepMillis(skillData.CastTime);

            RuneParameter rune = playerData.Equipment.equippedRune("ヒットアンドガードのルーン");
            if (rune != null) {
                playerData.EffectManager.removeEffect(EffectType.HighGuard);
                playerData.EffectManager.addEffect(EffectType.HitAndGuard, rune.AdditionParameterValueInt(0)*20);
            }
            else {
                playerData.EffectManager.removeEffect(EffectType.HitAndGuard);
                playerData.EffectManager.addEffect(EffectType.HighGuard, time);
            }
            playerData.EffectManager.addEffect(EffectType.Stun, time);
            ParticleManager.CylinderParticle(new ParticleData(Particle.SMOKE), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void SwashBaring(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int value = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepMillis(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.SwashBaring, value);
            playerData.EffectManager.addEffect(EffectType.HatePriority, 200);
            RuneParameter rune = playerData.Equipment.equippedRune("追加攻撃のルーン");
            if (rune != null) {
                double value2 = rune.AdditionParameterValue(0)/100;
                double radius = rune.AdditionParameterValue(1);
                Damage.makeDamage(player, Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate()), DamageCause.ATK, skillData.Id, value2, 1, 1);
            }
            ParticleManager.CylinderParticle(new ParticleData(Particle.DUST), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
