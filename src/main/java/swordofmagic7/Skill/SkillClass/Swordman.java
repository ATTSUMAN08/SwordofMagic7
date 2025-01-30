package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Sound.CustomSound.playSound;

public class Swordman extends BaseSkillClass {

    public Swordman(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void PainBarrier(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;

            MultiThread.sleepMillis(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.PainBarrier, time);
            if (playerData.Equipment.isEquipRune("仁王立ちのルーン")) playerData.EffectManager.addEffect(EffectType.NonKnockBack, time);
            ParticleManager.CylinderParticle(new ParticleData(Particle.WITCH), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.HEAL);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Feint(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Invincible, (int) skillData.Parameter.get(0).Value*20);
            ParticleManager.CylinderParticle(new ParticleData(Particle.CRIT), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.HEAL);
            skillProcess.SkillRigid(skillData);
        }, "Feint");
    }
}
