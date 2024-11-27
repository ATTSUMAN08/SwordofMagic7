package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Sound.CustomSound.playSound;

public class Ranger extends BaseSkillClass {

    public Ranger(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void ChainAttack(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.CRIT);

            MultiThread.sleepMillis(skillData.CastTime);

            PetParameter pet = playerData.getPetSelectCheckTarget();
            if (pet != null) {
                LivingEntity target = pet.target;
                Set<LivingEntity> victims = Function.NearLivingEntity(target.getLocation(), skillData.ParameterValue(2), skillProcess.Predicate());
                for (LivingEntity victim : victims) {
                    ParticleManager.LineParticle(particleData, target.getEyeLocation(), victim.getEyeLocation(), 0.5, 3);
                    Damage.makeDamage(pet.entity, victim, DamageCause.ATK, skillData.Id, skillData.ParameterValue(0) / 100, 1);
                    MultiThread.sleepTick(2);
                }
                playSound(player, SoundList.AttackWeak);
            }
            skillProcess.SkillRigid(skillData);
        }, "PetAttack");
    }
}
