package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
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
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Function.*;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Corsair extends BaseSkillClass {

    public Corsair(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Brutality(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double mana = skillData.ParameterValue(1);

            MultiThread.sleepTick(skillData.CastTime);

            if (playerData.EffectManager.hasEffect(EffectType.Brutality)) {
                playerData.EffectManager.removeEffect(EffectType.Brutality);
                Function.sendMessage(player, "§e[" + EffectType.Brutality + "]§aを§c無効化§aしました", SoundList.Tick);
            } else {
                playerData.EffectManager.addEffect(EffectType.Brutality, 1, mana);
                Function.sendMessage(player, "§e[" + EffectType.Brutality + "]§aを§b有効化§aしました", SoundList.Tick);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void CoveringFire(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.FLAME);
            int time = skillData.ParameterValueInt(0)*20;
            double value = skillData.ParameterValue(1)/100;

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.CoveringFire, time, value);
            ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public int JollyRogerCombo = 1;
    public void JollyRoger(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            JollyRogerCombo = 1;
            ParticleData particleData = new ParticleData(Particle.FLAME);
            int time = skillData.ParameterValueInt(0);
            double radius = skillData.ParameterValue(2);
            int time2 = skillData.ParameterValueInt(3)*20;

            MultiThread.sleepTick(skillData.CastTime);

            skillProcess.SkillRigid(skillData);
            Location origin = player.getLocation().clone();

            int i = 0;
            while (playerWhileCheck(playerData) && i < time && JollyRogerCombo < 100) {
                i++;
                Set<Player> players = new HashSet<>();
                players.add(player);
                if (playerData.Party != null) players.addAll(playerData.Party.Members);
                for (Player player : players) {
                    if (player.getLocation().distance(origin) < radius) {
                        PlayerData playerData = PlayerData.playerData(player);
                        playerData.EffectManager.addEffect(EffectType.JollyRogerCombo, 30, new Object[]{this.playerData});
                        playerData.EffectManager.getData(EffectType.JollyRogerCombo).stack = JollyRogerCombo;
                    }
                }
                ParticleManager.CircleParticle(particleData, origin, radius, 36);
                MultiThread.sleepTick(20);
            }
            if (JollyRogerCombo >= 100) {
                Set<Player> players = new HashSet<>();
                players.add(player);
                if (playerData.Party != null) players.addAll(playerData.Party.Members);
                for (Player player : players) {
                    if (player.getLocation().distance(origin) < radius) {
                        PlayerData.playerData(player).EffectManager.addEffect(EffectType.JollyRoger, time2);
                        ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
                        playSound(player, SoundList.Heal);
                    }
                }
            } else {
                sendMessage(player, "§eコンボ§aを達成できませんでした...", SoundList.Tick);
            }
        }, skillData.Id);
    }

    LivingEntity IronHookEntity;
    public void IronHook(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                ParticleManager.LineParticle(new ParticleData(Particle.SMOKE_NORMAL), playerHandLocation(player), 20, 0, 10);
                EffectManager.addEffect(ray.HitEntity, EffectType.IronHook, time, player);
                IronHookEntity = ray.HitEntity;
                playSound(player, SoundList.DeBuff);
            } else {
                player.sendMessage("§c対象§aがいません");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Keelhauling(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepTick(skillData.CastTime);

            if (IronHookEntity != null && EffectManager.hasEffect(IronHookEntity, EffectType.IronHook)) {
                EffectManager effectManager = EffectManager.getEffectManager(IronHookEntity);
                effectManager.addEffect(EffectType.Keelhauling, time, new Object[]{player.getLocation()});
                effectManager.removeEffect(EffectType.IronHook);
                playSound(player, SoundList.DeBuff);
            } else {
                player.sendMessage("§c対象§aがいません");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            IronHookEntity = null;
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
