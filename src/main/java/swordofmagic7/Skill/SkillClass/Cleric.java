package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Map;

import static swordofmagic7.Damage.Damage.makeHeal;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.Heal;

public class Cleric extends BaseSkillClass {

    public Cleric(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Heal(SkillData skillData, double length) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 1, skillProcess.PredicateA().and(entity -> entity instanceof Player player && playerData(player).Status.Health < playerData(player).Status.MaxHealth));
            Player target;
            if (ray.isHitEntity()) {
                target = (Player) ray.HitEntity;
                ParticleManager.LineParticle(new ParticleData(Particle.VILLAGER_HAPPY), player.getEyeLocation(), target.getEyeLocation(), 0, 10);
            } else {
                target = player;
            }
            PlayerData targetData = playerData(target);
            if (targetData.Status.Health < targetData.Status.MaxHealth) {
                ParticleManager.CylinderParticle(new ParticleData(Particle.VILLAGER_HAPPY), target.getLocation(), 1, 2, 3, 3);
                makeHeal(player, target, skillData.Id, value);
                playSound(player, SoundList.Heal);
                playSound(target, SoundList.Heal);
            } else {
                player.sendMessage("§e対象§aの§cHP§aが§e最大§aです");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, "Heal");
    }

    public void Cure(SkillData skillData, double length) {
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
        }, "Cure");
    }

    public void Fade(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Invincible, 20);
            if (playerData.Party != null) {
                for (Player player : playerData.Party.Members) {
                    playerData(player).EffectManager.addEffect(EffectType.Covert, skillData.ParameterValueInt(0) * 20);
                    playSound(player, SoundList.Shoot);
                }
            } else {
                playerData.EffectManager.addEffect(EffectType.Covert, skillData.ParameterValueInt(0) * 20);
                playSound(player, SoundList.Shoot);
            }
            skillProcess.SkillRigid(skillData);
        }, "Fade");
    }

    public void Resurrection(SkillData skillData, double length) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 1, skillProcess.PredicateA2());
            if (ray.isHitEntity()) {
                Player target = (Player) ray.HitEntity;
                PlayerData targetData = playerData(target);
                ParticleManager.LineParticle(new ParticleData(Particle.END_ROD), player.getEyeLocation(), target.getEyeLocation(), 0, 10);
                ParticleManager.CylinderParticle(new ParticleData(Particle.END_ROD), target.getLocation(), 1, 2, 3, 3);
                targetData.revival();
                playSound(target.getLocation(), Heal);
                RuneParameter rune = playerData.Equipment.equippedRune("再生促進のルーン");
                if (rune != null) {
                    double value = rune.AdditionParameterValue(1)/100;
                    int time = rune.AdditionParameterValueInt(0)*20;
                    targetData.Status.Health = targetData.Status.MaxHealth;
                    targetData.changeShield(targetData.Status.MaxHealth*value, time);
                } else {
                    targetData.Status.Health = targetData.Status.MaxHealth/2;
                }
            } else {
                player.sendMessage("§b[蘇生対象]§aを選択してください");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, "Resurrection");
    }
}
