package swordofmagic7.Skill.SkillClass;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;

public class SkillBase {

    public final Player player;
    public final PlayerData playerData;
    public final Skill skill;

    public SkillBase(SkillData skillData, SkillProcess skillProcess) {
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }
}
