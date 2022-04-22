package swordofmagic7.Command.Builder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

public class FlySpeed implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 1) {
            player.setFlySpeed(Float.parseFloat(args[0]));
        } else {
            player.setFlySpeed(0.2f);
        }
        player.sendMessage("FlySpeed: " + player.getFlySpeed());
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
