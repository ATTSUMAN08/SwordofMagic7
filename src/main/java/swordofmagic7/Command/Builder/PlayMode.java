package swordofmagic7.Command.Builder;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

public class PlayMode implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        playerData.PlayMode = !playerData.PlayMode;
        if (playerData.PlayMode) {
            player.setGameMode(GameMode.SURVIVAL);
            player.closeInventory();
        } else {
            player.setGameMode(GameMode.CREATIVE);
            player.getInventory().clear();
        }
        player.sendMessage("§ePlayMode: " + playerData.PlayMode);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
