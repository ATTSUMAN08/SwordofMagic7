package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;

import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.Heal;

public class DualStar {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public DualStar(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void ExtraAttack(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepMillis(skillData.CastTime);

            PetParameter pet = playerData.getPetSelect();
            if (pet != null) {
                pet.getEffectManager().addEffect(EffectType.ExtraAttack, time);
                ParticleManager.CylinderParticle(new ParticleData(Particle.FIREWORKS_SPARK), pet.entity.getLocation(), 1.5, 1, 3, 3);
                playSound(player, Heal);
            }
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
