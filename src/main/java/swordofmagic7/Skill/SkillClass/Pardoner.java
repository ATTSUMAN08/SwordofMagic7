package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Map;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.RodAttack;

public class Pardoner extends BaseSkillClass {

    public Pardoner(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Indulgendia(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            int time = skillData.ParameterValueInt(1) * 20;
            double heal = playerData.Status.HLP * skillData.ParameterValue(0) / 100;
            double radius = skillData.ParameterValue(1);
            ParticleManager.CircleParticle(new ParticleData(Particle.VILLAGER_HAPPY), player.getLocation(), radius, 30);
            for (Player target : PlayerList.getNearNonDead(player.getLocation(), radius)) {
                if (skillProcess.isAllies(target) || target == player) {
                    playerData(target).EffectManager.addEffect(EffectType.Indulgendia, time, heal);
                    playSound(target, SoundList.Heal);
                }
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void DiscernEvil(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            int min = skillData.ParameterValueInt(0) * 20;
            double multiply = skillData.ParameterValue(1);
            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                ParticleManager.LineParticle(new ParticleData(Particle.SPELL_WITCH), playerHandLocation(player), 20, 0, 10);
                LivingEntity target = ray.HitEntity;
                for (Map.Entry<EffectType, EffectData> data : EffectManager.getEffectManager(target).Effect.entrySet()) {
                    if (!data.getKey().Buff && !data.getKey().isStatic && !data.getValue().flags) {
                        if (data.getValue().time > min) {
                            data.getValue().time *= multiply;
                        } else {
                            data.getValue().time += min;
                        }
                        data.getValue().flags = true;
                        sendMessage(player, "§c[" + data.getKey().Display + "]§aを延長しました");
                    }
                }
                playSound(player, RodAttack);
            } else {
                player.sendMessage("§e対象§aがいません");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, "DiscernEvil");
    }

    public void Forgiveness(SkillData skillData, double length) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 1, skillProcess.PredicateA());
            Player target;
            if (ray.isHitEntity()) {
                target = (Player) ray.HitEntity;
                ParticleManager.LineParticle(new ParticleData(Particle.FIREWORKS_SPARK), player.getEyeLocation(), target.getEyeLocation(), 0, 10);
            } else {
                target = player;
            }
            ParticleManager.CylinderParticle(new ParticleData(Particle.FIREWORKS_SPARK), target.getLocation(), 1, 2, 3, 3);
            PlayerData targetData = playerData(target);
            boolean cured = false;
            for (Map.Entry<EffectType, EffectData> effect : targetData.EffectManager.Effect.entrySet()) {
                EffectType effectType = effect.getKey();
                if (!effectType.Buff && effectType.effectRank.isHigh()) {
                    cured = true;
                    targetData.EffectManager.removeEffect(effectType, player);
                    break;
                }
            }
            if (!cured) for (Map.Entry<EffectType, EffectData> effect : targetData.EffectManager.Effect.entrySet()) {
                EffectType effectType = effect.getKey();
                if (!effectType.Buff && effectType.effectRank.isNormal()) {
                    cured = true;
                    targetData.EffectManager.removeEffect(effectType, player);
                    break;
                }
            }
            if (!cured) {
                player.sendMessage("§e対象§aに§cデバフ§aが付与されていません");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            } else {
                playSound(player, SoundList.Heal);
                playSound(target, SoundList.Heal);
            }
            skillProcess.SkillRigid(skillData);
        }, "Forgiveness");
    }

    public void Indulgence(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0) * 20;
            int stack = skillData.ParameterValueInt(1);
            EffectType effectType = playerData.Equipment.isEquipRune("プロフェシーのルーン") ? EffectType.Profesy : EffectType.Indulgence;

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(new ParticleData(Particle.SPELL_WITCH), playerHandLocation(player), 20, 0, 10);
            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.PredicateA());
            Player target;
            if (ray.isHitEntity()) {
                target = (Player) ray.HitEntity;
            } else {
                target = player;
            }

            EffectManager.addEffect(target, effectType, time, stack, player);
            playSound(target, RodAttack);
            skillProcess.SkillRigid(skillData);
        }, "Indulgence");
    }
}
