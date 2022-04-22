package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

import java.util.Map;
import java.util.UUID;

public class LoadedPlayer implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        sender.sendMessage("Loaded PlayerData: ");
        for (Map.Entry<UUID, PlayerData> loopData : PlayerData.getPlayerData().entrySet()) {
            sender.sendMessage(loopData.getKey().toString() + ": " + loopData.getValue().player.getName());
        }
        return true;
    }
}
