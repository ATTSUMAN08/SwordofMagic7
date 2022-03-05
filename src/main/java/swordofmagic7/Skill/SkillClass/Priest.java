package swordofmagic7.Skill.SkillClass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Damage.Damage.makeHeal;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.SkillProcess.period;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Priest {

    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;


    public Priest(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void MassHeal(SkillData skillData) {
        skill.setCastReady(false);
        double radius = skillData.ParameterValue(1);
        ParticleManager.CircleParticle(new ParticleData(Particle.VILLAGER_HAPPY), player.getLocation(), radius, 30);
        for (Player target : PlayerList.getNearNonDead(player.getLocation(), radius)) {
            if (skillProcess.isAllies(target) || target == player) {
                makeHeal(player, target, skillData.ParameterValue(0)/100);
                playSound(target, SoundList.Heal);
            }
        }
        skillProcess.SkillRigid(skillData);
    }

    public void HolyBuff(SkillData skillData, ParticleData particleData, EffectType effectType) {
        skill.setCastReady(false);
        double radius = skillData.ParameterValue(1);
        ParticleManager.CircleParticle(particleData, player.getLocation(), radius, 30);
        Set<Player> Targets = PlayerList.getNearNonDead(player.getLocation(), radius);
        if (playerData.Party != null) Targets.addAll(playerData.Party.Members);
        for (Player target : new HashSet<>(Targets)) {
            if (skillProcess.isAllies(target) || target == player) {
                EffectManager.addEffect(target, effectType, (int) (skillData.ParameterValue(0)*20), player);
                playSound(target, SoundList.Heal);
            }
        }
        skillProcess.SkillRigid(skillData);
    }

    public void Monstrance(SkillData skillData) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), 20, 0, 10);
                    Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.PredicateE());
                    EffectManager manager = EffectManager.getEffectManager(ray.HitEntity);
                    if (manager != null) {
                        manager.addEffect(EffectType.Monstrance, (int) (skillData.ParameterValue(0)*20));
                        player.sendMessage("§c" + manager.getOwnerName() + "§aに§c[" + skillData.Display + "]§aを付与しました");
                        playSound(player, SoundList.DeBuff);
                    } else {
                        player.sendMessage("§c対象§aがいません");
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
