package swordofmagic7.Skill.SkillClass;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

public class Corsair {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public Corsair(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    private boolean Brutality = false;
    public void Brutality(SkillData skillData, double length) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            if (playerData.EffectManager.hasEffect(EffectType.Brutality)) {
                playerData.EffectManager.removeEffect(EffectType.Brutality);
                Function.sendMessage(player, "§e[" + EffectType.Brutality + "]§aを§b有効§aにしました", SoundList.Tick);
            } else {
                playerData.EffectManager.addEffect(EffectType.Brutality);
                Function.sendMessage(player, "§e[" + EffectType.Brutality + "]§aを§c向こう§aにしました", SoundList.Tick);
            }
            skillProcess.SkillRigid(skillData);
        }, "Heal");
    }
}
