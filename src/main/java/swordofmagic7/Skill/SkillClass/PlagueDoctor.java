package swordofmagic7.Skill.SkillClass;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.*;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PlagueDoctor extends BaseSkillClass {

    public PlagueDoctor(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void HealingFactor(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            double value = skillData.ParameterValue(0) / 100;
            int time = skillData.ParameterValueInt(1)*20;
            int hitRate = Math.toIntExact(Math.round(skillData.ParameterValue(2) * 20));
            LivingEntity entity = RayTrace.rayLocationEntity(player.getEyeLocation(), 20, 1, skillProcess.PredicateA()).HitEntity;
            if (entity == null) entity = player;
            if (entity instanceof Player target) {
                ParticleManager.LineParticle(new ParticleData(Particle.HEART), player.getLocation(), target.getEyeLocation(), 1, 2);
                PlayerData targetData = playerData(target);
                EffectManager.addEffect(target, EffectType.HealingFactor, time, player);
                playSound(target, SoundList.HEAL);
                MultiThread.TaskRun(() -> {
                    double maxHealth = targetData.Status.Health;
                    for (int i = 0; i < time; i += hitRate) {
                        if (targetData.EffectManager.hasEffect(EffectType.HealingFactor)) {
                            if (targetData.Status.Health < maxHealth) {
                                Damage.makeHeal(player, target, skillData.Id, value);
                            }
                            MultiThread.sleepTick(hitRate);
                        } else break;
                    }
                }, skillData.Id);
            } else {
                player.sendMessage("§e対象§aがいません");
                playSound(player, SoundList.NOPE);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void FumiGate(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            int hitRate = Math.toIntExact(Math.round(skillData.ParameterValue(1)*20));
            int count = skillData.ParameterValueInt(2);
            double radius = skillData.ParameterValue(3);
            ParticleData particleData = new ParticleData(Particle.DUST, new Particle.DustOptions(Color.RED, 1));
            Location origin = player.getLocation();

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            MultiThread.TaskRun(() -> {
                for (int i = 0; i < time; i+=hitRate) {
                    for (LivingEntity victim : Function.NearLivingEntity(origin, radius, skillProcess.Predicate())) {
                        EffectManager effectManager = EffectManager.getEffectManager(victim);
                        int perCount = 0;
                        Set<EffectType> effects = new HashSet<>(effectManager.Effect.keySet());
                        for (EffectType effectType : effects) {
                            if (perCount >= count) break;
                            if (effectType.Buff && !effectType.effectRank.isImpossible()) {
                                effectManager.removeEffect(effectType, player);
                                perCount++;
                            }
                        }
                    }
                    MultiThread.sleepTick(hitRate);
                }
            }, skillData.Id);
            MultiThread.TaskRun(() -> {
                for (int i = 0; i <= time; i+=5) {
                    ParticleManager.CirclePointLineParticle(particleData, origin, radius/1.5, 6, 0, 5);
                    ParticleManager.CircleParticle(particleData, origin, radius, 24);
                    MultiThread.sleepTick(5);
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

            Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate()));
            HashMap<EffectType, EffectData> effect = new HashMap<>();
            RuneParameter rune = playerData.Equipment.equippedRune("免疫低下のルーン");
            boolean bool = rune != null;
            int time = bool ? rune.AdditionParameterValueInt(0)*20 : 0;
            for (LivingEntity victim : victims) {
                EffectManager effectManager = EffectManager.getEffectManager(victim);
                if (bool) EffectManager.addEffect(victim, EffectType.ImmuneDepression, time, player);
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

    public void Modafinil(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            ParticleData particleData = new ParticleData(Particle.ELECTRIC_SPARK);

            MultiThread.sleepTick(skillData.CastTime);

            Set<Player> players = new HashSet<>();
            players.add(player);
            if (playerData.Party != null) players.addAll(playerData.Party.Members);
            RuneParameter rune = playerData.Equipment.equippedRune("良薬のルーン");
            boolean bool  = rune != null;
            int time2 = 0;
            if (bool) time2 = rune.AdditionParameterValueInt(0)*20;
            for (Player player : players) {
                if (bool) EffectManager.addEffect(player, EffectType.NonKnockBack, time2, this.player);
                EffectManager.addEffect(player, EffectType.Modafinil, time, this.player);
                ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
                playSound(player, SoundList.HEAL);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
