package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

public class LoadedPlayer implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        sender.sendMessage("Loaded PlayerData: ");
        for (PlayerData playerData : PlayerData.getPlayerData().values()) {
            sender.sendMessage(playerData.player.getUniqueId() + ": " + playerData.player.getName());
        }
        return true;
    }
}
