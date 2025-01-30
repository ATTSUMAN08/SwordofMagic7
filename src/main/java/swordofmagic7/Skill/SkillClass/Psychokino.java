package swordofmagic7.Skill.SkillClass;

import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

public class Psychokino extends BaseSkillClass {
    public Psychokino(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void HeavyGravity(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Raise(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void GravityPole(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            if (!playerData.EffectManager.hasEffect(EffectType.PsychicPressure)) {
                Function.sendMessage(player, "§e[" + EffectType.PsychicPressure.Display + "]§aが§b有効§aではありません", SoundList.NOPE);
                skill.resetSkillCoolTimeWaited(skillData);
            }

            double value = skillData.ParameterValue(0)/100;
            double radius = skillData.ParameterValue(1);

            MultiThread.sleepMillis(skillData.CastTime);

            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void Swap(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepMillis(skillData.CastTime);

            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }

    public void PsychicPressure(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;

            MultiThread.sleepMillis(skillData.CastTime);

            if (playerData.EffectManager.hasEffect(EffectType.PsychicPressure)) {
                playerData.EffectManager.removeEffect(EffectType.PsychicPressure);
                Function.sendMessage(player, "§e[" + EffectType.PsychicPressure + "]§aを§c無効化§aしました", SoundList.TICK);
            } else {
                playerData.EffectManager.addEffect(EffectType.PsychicPressure, 1, value);
                Function.sendMessage(player, "§e[" + EffectType.PsychicPressure + "]§aを§b有効化§aしました", SoundList.TICK);
            }

            skillProcess.SkillRigid(skillData);
        }, skillData.Id);
    }
}
