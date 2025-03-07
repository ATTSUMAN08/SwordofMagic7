package swordofmagic7.Skill.SkillClass;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.FanShapedCollider;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Oracle extends BaseSkillClass {

    public Oracle(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void ArcaneEnergy(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            ParticleData particleData = new ParticleData(Particle.CRIT);

            MultiThread.sleepTick(skillData.CastTime);

            Set<Player> players = new HashSet<>();
            players.add(player);
            if (playerData.Party != null) players.addAll(playerData.Party.Members);
            for (Player player : players) {
                ParticleManager.CylinderParticle(particleData, player.getLocation(), 1, 2, 3, 3);
                EffectManager.addEffect(player, EffectType.ArcaneEnergy, time, this.player);
                playSound(player, SoundList.HEAL);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, "ShieldBash");
    }

    public void DeathVerdict(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(0);
            double angle = radius*10;
            int time = skillData.ParameterValueInt(1)*20;

            for (int i = 0; i < skillData.CastTime; i++) {
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
            ParticleData particleData = new ParticleData(Particle.DUST, new Particle.DustOptions(Color.YELLOW, 1));
            Location origin = player.getLocation();

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                MultiThread.sleepMillis(millis);
            }

            RuneParameter rune = playerData.Equipment.equippedRune("魔法障壁のルーン");
            if (rune != null) {
                int time2 = rune.AdditionParameterValueInt(0)*20;
                Set<Player> players = new HashSet<>();
                players.add(player);
                if (playerData.Party != null) players.addAll(playerData.Party.Members);
                for (Player player : players) {
                    playerData(player).EffectManager.addEffect(EffectType.MagicBarrier, time2);
                }
            }

            MultiThread.TaskRun(() -> {
                for (int i = 0; i < time; i++) {
                    for (Player victim : PlayerList.getNearNonDead(origin, radius)) {
                        if (player != victim && playerData.Party != null && playerData.Party == playerData(victim).Party) {
                            EffectManager.addEffect(victim, EffectType.Invincible, 25, null);
                        }
                    }
                    for (int i2 = 0; i2 < 4; i2++) {
                        ParticleManager.CirclePointLineParticle(particleData, origin, radius, 3, 0, 5);
                        ParticleManager.CircleParticle(particleData, origin, radius, 24);
                        MultiThread.sleepTick(5);
                    }
                }
            }, skillData.Id);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Foretell(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(2);
            int time = skillData.ParameterValueInt(0);
            Location origin = RayTrace.rayLocationBlock(player.getEyeLocation(), 10, true).HitPosition;
            ParticleData particleData = new ParticleData(Particle.DUST, new Particle.DustOptions(Color.AQUA, 1));
            particleData.setVector(Function.VectorDown);

            if (playerData.Equipment.isEquipRune("追尾魔法陣のルーン")) {
                skillProcess.PartyBuffApply(skillData, EffectType.Foretell, particleData, time*20);
            } else {
                for (int i = 0; i <= skillData.CastTime; i++) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 10);
                    MultiThread.sleepMillis(millis);
                }
                MultiThread.TaskRun(() -> {
                    for (int i = 0; i < time; i++) {
                        for (LivingEntity victim : Function.NearLivingEntity(origin, radius, skillProcess.PredicateA_ME())) {
                            EffectManager.addEffect(victim, EffectType.Foretell, 25, null);
                        }
                        for (int i2 = 0; i2 < 4; i2++) {
                            ParticleManager.CirclePointLineParticle(particleData, origin, radius*0.7, 6, 0, 5);
                            ParticleManager.CircleParticle(particleData, origin, radius, 24);
                            MultiThread.sleepTick(5);
                        }
                    }
                }, skillData.Id);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void DivineMight(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.PORTAL);
            int minTime = skillData.ParameterValueInt(0)*20;
            double multiTime = skillData.ParameterValue(1);

            MultiThread.sleepTick(skillData.CastTime);

            double radius = skillData.ParameterValue(1);
            ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 30);
            Set<Player> Targets = new HashSet<>();
            Targets.add(player);
            if (playerData.Party != null) Targets.addAll(playerData.Party.Members);
            for (Player target : Targets) {
                for (Map.Entry<EffectType, EffectData> data : EffectManager.getEffectManager(target).Effect.entrySet()) {
                    if (data.getKey().Buff && !data.getKey().isStatic && !data.getValue().flags) {
                        if (data.getValue().time > minTime) {
                            data.getValue().time *= multiTime;
                        } else {
                            data.getValue().time += minTime;
                        }
                        data.getValue().flags = true;
                    }
                }
                EffectManager.addEffectMessage(player, target, skillData.Display, "§e");
                playSound(target, SoundList.HEAL);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
