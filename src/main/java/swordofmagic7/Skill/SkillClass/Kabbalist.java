package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.FanShapedCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Kabbalist extends BaseSkillClass {

    public Kabbalist(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Nachash(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = playerData.Status.HLP*skillData.ParameterValue(0)/100;
            int time = skillData.ParameterValueInt(1)*20;
            double radius = skillData.ParameterValue(2);
            ParticleData particleData = new ParticleData(Particle.VILLAGER_HAPPY);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 30);
            Set<Player> Targets = PlayerList.getNearNonDead(player.getLocation(), radius);
            if (playerData.Party != null) Targets.addAll(playerData.Party.Members);
            for (Player target : Targets) {
                if (skillProcess.isAllies(target) || target == player) {
                    EffectManager.addEffectMessage(player, target, EffectType.Nachash);
                    EffectManager.getEffectManager(target).addEffect(EffectType.Nachash, time, value);
                    playSound(target, SoundList.Heal);
                }
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void TreeOfSepiroth(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            int time = skillData.ParameterValueInt(1)*20;
            double hitRate = skillData.ParameterValue(2)*20;
            double radius = skillData.ParameterValue(3);
            ParticleData particleData = new ParticleData(Particle.VILLAGER_HAPPY);
            Location origin = player.getLocation();

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            MultiThread.TaskRun(() -> {
                for (double i = 0; i < time; i+=hitRate) {
                    for (Player victim : PlayerList.getNearNonDead(origin, radius)) {
                        Damage.makeHeal(player, victim, skillData.Id, value);
                    }
                    for (int i2 = 0; i2 < hitRate; i2+=5) {
                        ParticleManager.CirclePointLineParticle(particleData, origin, radius, 3, 0, 5);
                        ParticleManager.CircleParticle(particleData, origin, radius, 24);
                        MultiThread.sleepTick(5);
                    }
                }
            }, skillData.Id);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Gevura(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(1);
            double angle = radius*10;
            int time = skillData.ParameterValueInt(0)*20;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false)) {
                double multiply = Math.abs(victim.getName().hashCode() % 44)/100f;
                EffectManager.getEffectManager(victim).addEffect(EffectType.Gevura, time, multiply);
                EffectManager.addEffectMessage(player, victim, EffectType.Gevura, "§c[" + String.format("%.0f", multiply*100) + "%]");
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
