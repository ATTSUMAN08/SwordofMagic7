package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;

public class SkillCTReset implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        for (String skillData : playerData.Skill.SkillCoolTime.keySet()) {
            MultiThread.TaskRunSynchronizedLater(() -> {
                playerData.Skill.resetSkillCoolTime(skillData);
            }, 1);
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
