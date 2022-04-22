package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

public class GetExp implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            try {
                playerData.addPlayerExp(Integer.parseInt(args[0]));
            } catch (Exception e) {
                player.sendMessage("§c" + "/getExp <exp>");
            }
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
