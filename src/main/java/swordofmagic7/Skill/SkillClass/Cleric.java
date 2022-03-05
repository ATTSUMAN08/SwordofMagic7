package swordofmagic7.Skill.SkillClass;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Map;

import static swordofmagic7.Damage.Damage.makeHeal;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.SkillProcess.period;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.Heal;
import static swordofmagic7.System.plugin;

public class Cleric {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;


    public Cleric(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void Heal(SkillData skillData, double length) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    Ray ray = rayLocationEntity(player.getEyeLocation(), length, 1, skillProcess.PredicateA());
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
                        makeHeal(player, target, skillData.Parameter.get(0).Value/100);
                        playSound(player, SoundList.Heal);
                        playSound(target, SoundList.Heal);
                    } else {
                        player.sendMessage("§e対象§aの§cHP§aが§e最大§aです");
                        playSound(player, SoundList.Nope);
                        skill.resetSkillCoolTimeWaited(skillData);
                    }
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void Cure(SkillData skillData, double length) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
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
                        if (!effectType.Buff) {
                            cured = true;
                            targetData.EffectManager.removeEffect(effectType);
                            player.sendMessage(targetData.getNick() + "§aさんの§c[" + effectType.Display + "]§aを§b解除§aしました");
                            target.sendMessage(playerData.getNick() + "§aさんが§c[" + effectType.Display + "]§aを§b解除§aしました");
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
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void Fade(SkillData skillData) {
        skill.setCastReady(false);
        playerData.EffectManager.addEffect(EffectType.Invincible, 20);
        if (playerData.Party != null) {
            for (Player player : playerData.Party.Members) {
                playerData(player).EffectManager.addEffect(EffectType.Covert, (int) (skillData.Parameter.get(0).Value*20));
                playSound(player, SoundList.Shoot);
            }
        } else {
            playerData.EffectManager.addEffect(EffectType.Covert, (int) (skillData.Parameter.get(0).Value*20));
            playSound(player, SoundList.Shoot);
        }
        skillProcess.SkillRigid(skillData);
    }

    public void Resurrection(SkillData skillData, double length) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    Ray ray = rayLocationEntity(player.getEyeLocation(), length, 1, skillProcess.PredicateA2());
                    if (ray.isHitEntity()) {
                        Player target = (Player) ray.HitEntity;
                        ParticleManager.LineParticle(new ParticleData(Particle.END_ROD), player.getEyeLocation(), target.getEyeLocation(), 0, 10);
                        ParticleManager.CylinderParticle(new ParticleData(Particle.END_ROD), target.getLocation(), 1, 2, 3, 3);
                        playerData(target).revival();
                        playSound(target.getLocation(), Heal);
                    } else {
                        player.sendMessage("§b[蘇生対象]§aを選択してください");
                        playSound(player, SoundList.Nope);
                        skill.resetSkillCoolTimeWaited(skillData);
                    }
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}
