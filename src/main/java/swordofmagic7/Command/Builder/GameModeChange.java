package swordofmagic7.Command.Builder;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

public class GameModeChange implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 0) {
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                player.setGameMode(GameMode.SURVIVAL);
            } else {
                player.setGameMode(GameMode.CREATIVE);
            }
        } else {
            if (args[0].equalsIgnoreCase("0")) {
                player.setGameMode(GameMode.SURVIVAL);
            } else if (args[0].equalsIgnoreCase("1")) {
                player.setGameMode(GameMode.CREATIVE);
            } else if (args[0].equalsIgnoreCase("2")) {
                player.setGameMode(GameMode.ADVENTURE);
            } else if (args[0].equalsIgnoreCase("3")) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
