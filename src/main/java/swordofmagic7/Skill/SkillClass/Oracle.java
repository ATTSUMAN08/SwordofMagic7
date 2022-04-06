package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.FanShapedCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Oracle {

    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;


    public Oracle(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void ArcaneEnergy(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            ParticleData particleData = new ParticleData(Particle.CRIT_MAGIC);

            MultiThread.sleepTick(skillData.CastTime);

            Set<Player> players = new HashSet<>();
            players.add(player);
            if (playerData.Party != null) players.addAll(playerData.Party.Members);
            for (Player player : players) {
                ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
                EffectManager.addEffect(player, EffectType.ArcaneEnergy, time, player);
                playSound(player, SoundList.Heal);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, "ShieldBash");
    }

    public void DeathVerdict(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(0);
            double angle = radius*20;
            int time = skillData.ParameterValueInt(1)*20;

            while (skill.SkillCastProgress < 1) {
                ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                MultiThread.sleepMillis(millis);
            }

            for (LivingEntity victim : FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false)) {
                EffectManager.addEffect(victim, EffectType.DeathVerdict, time, player);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void CounterSpell(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(1);
            int time = skillData.ParameterValueInt(0);

            while (skill.SkillCastProgress < 1) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            MultiThread.TaskRun(() -> {
                for (int i = 0; i < time; i++) {
                    for (LivingEntity victim : Function.NearLivingEntity(player.getLocation(), radius, skillProcess.PredicateA())) {
                        EffectManager.addEffect(victim, EffectType.Invincible, 25, player);
                    }
                    MultiThread.sleepTick(20);
                }
            }, skillData.Id);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

}
