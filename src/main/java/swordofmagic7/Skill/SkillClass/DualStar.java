package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetAIState;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;

import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.Heal;

public class DualStar extends BaseSkillClass {

    public DualStar(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void ExtraBuff(SkillData skillData, EffectType effectType) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepMillis(skillData.CastTime);

            PetParameter pet = playerData.getPetSelect();
            if (pet != null) {
                pet.getEffectManager().addEffect(effectType, time);
                ParticleManager.CylinderParticle(new ParticleData(Particle.FIREWORK), pet.entity.getLocation(), 1.5, 1, 3, 3);
                playSound(player, Heal);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Order(SkillData skillData, PetAIState state) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            for (PetParameter pet : playerData.PetSummon) {
                playerData.PetManager.PetAISelect(pet, state);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
