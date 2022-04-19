package swordofmagic7.Skill;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;

public class BaseSkillClass {
    public final SkillProcess skillProcess;
    public final Player player;
    public final PlayerData playerData;
    public final Skill skill;

    public BaseSkillClass(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }
}
