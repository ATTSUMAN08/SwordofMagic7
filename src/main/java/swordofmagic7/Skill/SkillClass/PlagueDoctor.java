package swordofmagic7.Skill.SkillClass;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PlagueDoctor {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;


    public PlagueDoctor(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void HealingFactor(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            double heal = playerData.Status.HLP * skillData.ParameterValue(0) / 100;
            int time = skillData.ParameterValueInt(1)*20;
            int hitRate = Math.toIntExact(Math.round(skillData.ParameterValue(2) * 20));
            LivingEntity entity = RayTrace.rayLocationEntity(player.getEyeLocation(), 20, 1, skillProcess.PredicateA()).HitEntity;
            if (entity instanceof Player target) {
                ParticleManager.LineParticle(new ParticleData(Particle.HEART), player.getLocation(), target.getEyeLocation(), 1, 2);
                PlayerData targetData = playerData(target);
                EffectManager.addEffect(target, EffectType.HealingFactor, time, player);
                playSound(target, SoundList.Heal);
                MultiThread.TaskRun(() -> {
                    double maxHealth = targetData.Status.Health;
                    for (int i = 0; i < time; i += hitRate) {
                        if (targetData.EffectManager.hasEffect(EffectType.HealingFactor)) {
                            if (targetData.Status.Health < maxHealth) {
                                targetData.changeHealth(Math.min(heal, maxHealth-targetData.Status.Health));
                            }
                            MultiThread.sleepTick(hitRate);
                        } else break;
                    }
                }, skillData.Id);
            } else {
                player.sendMessage("§e対象§aがいません");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void FumiGate(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0);
            int hitRate = Math.toIntExact(Math.round(skillData.ParameterValue(1)*20));
            double radius = skillData.ParameterValue(2);
            ParticleData particleData = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1));
            Location origin = player.getLocation();

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            MultiThread.TaskRun(() -> {
                for (int i = 0; i <= time; i+=hitRate) {
                    for (Player victim : PlayerList.getNearNonDead(origin, radius)) {
                        if (skillProcess.Predicate().test(victim)) {
                            EffectManager effectManager = EffectManager.getEffectManager(victim);
                            for (EffectType effectType : effectManager.Effect.keySet()) {
                                if (effectType.Buff) effectManager.removeEffect(effectType, player);
                            }
                        }
                    }
                    for (int i2 = 0; i2 < hitRate; i2+=5) {
                        ParticleManager.CirclePointLineParticle(particleData, origin, radius/1.5, 6, 0, 5);
                        ParticleManager.CircleParticle(particleData, origin, radius, 24);
                        MultiThread.sleepTick(5);
                    }
                }
            }, skillData.Id);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Pandemic(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(0);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 10);
                MultiThread.sleepMillis(millis);
            }

            Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(player.getLocation(), radius, skillProcess.PredicateA()));
            HashMap<EffectType, EffectData> effect = new HashMap<>();
            for (LivingEntity victim : victims) {
                EffectManager effectManager = EffectManager.getEffectManager(victim);
                for (Map.Entry<EffectType, EffectData> data : effectManager.Effect.entrySet()) {
                    if (!data.getKey().Buff && data.getKey().effectRank.isNormal()) {
                        if (effect.containsKey(data.getKey())) {
                            if (data.getValue().time > effect.get(data.getKey()).time)
                                effect.put(data.getKey(), data.getValue());
                        } else effect.put(data.getKey(), data.getValue());
                    }
                }
            }
            for (LivingEntity victim : victims) {
                EffectManager effectManager = EffectManager.getEffectManager(victim);
                effectManager.Effect.putAll(effect);
                for (EffectType effectType : effect.keySet()) {
                    if (playerData.EffectLog) sendMessage(player, "§c[" + effectType.Display + "]§aを拡散させました");
                }
            }

            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
