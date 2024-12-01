package swordofmagic7.Command.Developer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;

import static swordofmagic7.Data.PlayerData.playerData;

public class SomReload implements SomCommand {

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerData(player).saveCloseInventory();
        }
        MultiThread.TaskRunSynchronizedLater(() -> Bukkit.getServer().dispatchCommand(sender, "plugman reload swordofmagic7"), 5);
        return true;
    }
}
