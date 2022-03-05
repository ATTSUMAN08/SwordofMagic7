package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Pet.PetManager.ReqPetSelect;
import static swordofmagic7.Skill.SkillProcess.period;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.Heal;
import static swordofmagic7.System.plugin;

public class Tamer {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public Tamer(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }


    public void PetAttack(SkillData skillData) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    LivingEntity target = playerData.PetSelect.target;
                    LivingEntity pet = playerData.PetSelect.entity;
                    if (pet != null && target != null && pet.getLocation().distance(target.getLocation()) <= skillData.Parameter.get(1).Value) {
                        ParticleManager.RandomVectorParticle(new ParticleData(Particle.CRIT), target.getLocation(), 30);
                        Damage.makeDamage(pet, target, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1);
                        playSound(target.getLocation(), SoundList.AttackWeak);
                    }
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void PetHeal(SkillData skillData) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    if (playerData.PetSelect != null && playerData.PetSelect.entity != null) {
                        playerData.PetSelect.changeHealth((int) Math.round(playerData.Status.HLP*skillData.Parameter.get(0).Value/100));
                        ParticleManager.CylinderParticle(new ParticleData(Particle.VILLAGER_HAPPY), playerData.PetSelect.entity.getLocation(), 1, 2, 3, 3);
                        playSound(player, Heal);
                    } else {
                        player.sendMessage(ReqPetSelect);
                        playSound(player, SoundList.Nope);
                    }
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void PetBoost(SkillData skillData) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    if (playerData.PetSelect != null && playerData.PetSelect.entity != null) {
                        playerData.PetSelect.effectManager.addEffect(EffectType.PetBoost, (int) skillData.Parameter.get(0).Value * 20);
                        ParticleManager.CylinderParticle(new ParticleData(Particle.FIREWORKS_SPARK), playerData.PetSelect.entity.getLocation(), 1, 2, 3, 3);
                        playSound(player, Heal);
                    } else {
                        player.sendMessage(ReqPetSelect);
                        playSound(player, SoundList.Nope);
                    }
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}
