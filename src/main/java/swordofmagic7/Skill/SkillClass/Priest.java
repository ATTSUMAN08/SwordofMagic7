package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Damage.Damage.makeHeal;
import static swordofmagic7.Function.*;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Priest extends BaseSkillClass {

    public Priest(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void MassHeal(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            double radius = skillData.ParameterValue(1);
            ParticleManager.CircleParticle(new ParticleData(Particle.VILLAGER_HAPPY), player.getLocation(), radius, 30);
            for (Player target : PlayerList.getNearNonDead(player.getLocation(), radius)) {
                if (skillProcess.isAllies(target) || target == player) {
                    makeHeal(player, target, skillData.Id, skillData.ParameterValue(0) / 100);
                    playSound(target, SoundList.Heal);
                }
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void HolyBuff(SkillData skillData, ParticleData particleData, EffectType effectType) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            double radius = skillData.ParameterValue(1);
            ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 30);
            Set<Player> Targets = PlayerList.getNearNonDead(player.getLocation(), radius);
            if (playerData.Party != null) Targets.addAll(playerData.Party.Members);
            for (Player target : Targets) {
                if (skillProcess.isAllies(target) || target == player) {
                    EffectManager.addEffect(target, effectType, skillData.ParameterValueInt(0)*20, player);
                    playSound(target, SoundList.Heal);
                }
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Monstrance(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), 20, 0, 10);
                EffectManager.addEffect(ray.HitEntity, EffectType.Monstrance, skillData.ParameterValueInt(0) * 20, player);
                playSound(player, SoundList.DeBuff);
            } else {
                sendMessage(player, "§c対象§aがいません", SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Revive(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.VILLAGER_HAPPY);
            int time = skillData.ParameterValueInt(0)*20;
            int time2 = 0;
            RuneParameter rune = playerData.Equipment.equippedRune("祝福のルーン");
            if (rune != null) time2 = rune.AdditionParameterValueInt(0)*20;

            MultiThread.sleepTick(skillData.CastTime);

            double radius = skillData.ParameterValue(1);
            ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 30);
            Set<Player> Targets = PlayerList.getNearNonDead(player.getLocation(), radius);
            if (playerData.Party != null) Targets.addAll(playerData.Party.Members);
            for (Player target : Targets) {
                if (skillProcess.isAllies(target) || target == player) {
                    EffectManager.addEffect(target, EffectType.Revive, time, player, time2);
                    playSound(target, SoundList.Heal);
                }
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
