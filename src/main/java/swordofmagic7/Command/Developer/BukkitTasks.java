package swordofmagic7.Command.Developer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

public class BukkitTasks implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        sender.sendMessage("PendingTask: " + Bukkit.getScheduler().getPendingTasks().size());
        sender.sendMessage("ActiveTask: " + Bukkit.getScheduler().getActiveWorkers().size());
        return true;
    }
}
