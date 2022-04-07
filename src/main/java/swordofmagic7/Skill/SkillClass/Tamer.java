package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.Heal;

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
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            PetParameter pet = playerData.getPetSelectCheckTarget();
            if (pet != null) {
                LivingEntity target = pet.target;
                if (pet.entity.getLocation().distance(target.getLocation()) <= skillData.Parameter.get(1).Value) {
                    ParticleManager.RandomVectorParticle(new ParticleData(Particle.CRIT), target.getLocation(), 30);
                    Damage.makeDamage(pet.entity, target, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1);
                    playSound(target.getLocation(), SoundList.AttackWeak);
                }
            }
            skillProcess.SkillRigid(skillData);
        }, "PetAttack");
    }

    public void PetHeal(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            PetParameter pet = playerData.getPetSelect();
            if (pet != null) {
                pet.changeHealth((int) Math.round(playerData.Status.HLP * skillData.Parameter.get(0).Value / 100));
                ParticleManager.CylinderParticle(new ParticleData(Particle.VILLAGER_HAPPY), pet.entity.getLocation(), 1.5, 1, 3, 3);
                playSound(player, Heal);
            }
            skillProcess.SkillRigid(skillData);
        }, "PetHeal");
    }

    public void PetBoost(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepMillis(skillData.CastTime);

            PetParameter pet = playerData.getPetSelect();
            if (pet != null) {
                pet.getEffectManager().addEffect(EffectType.PetBoost, time);
                ParticleManager.CylinderParticle(new ParticleData(Particle.FIREWORKS_SPARK), pet.entity.getLocation(), 1.5, 1, 3, 3);
                playSound(player, Heal);
            }
            skillProcess.SkillRigid(skillData);
        }, "PetBoost");
    }
}
