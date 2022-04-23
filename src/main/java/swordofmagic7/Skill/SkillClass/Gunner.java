package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Sound.CustomSound.playSound;

public class Gunner extends BaseSkillClass {

    public Gunner(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Aiming(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Aiming, (int) skillData.Parameter.get(0).Value * 20);
            ParticleManager.CylinderParticle(new ParticleData(Particle.SPELL_WITCH), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Rolling(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0)*20;
            RuneParameter rune = playerData.Equipment.equippedRune("トリプルアクセルのルーン");
            if (rune != null) time = rune.AdditionParameterValueInt(0)*20;

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.Invincible, time);
            ParticleManager.CylinderParticle(new ParticleData(Particle.CRIT_MAGIC), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.Heal);
            player.setVelocity(player.getLocation().getDirection().clone().setY(0.5).normalize().multiply(-1));
            skillProcess.SkillRigid(skillData);
        }, "Rolling");
    }
}
