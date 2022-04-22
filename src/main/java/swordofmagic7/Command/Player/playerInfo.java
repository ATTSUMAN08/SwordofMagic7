package swordofmagic7.Command.Player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Command.TabComplete.PlayerTabComplete;
import swordofmagic7.Data.PlayerData;

import java.util.List;

public class playerInfo implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        Player target = player;
        if (args.length == 1 && Bukkit.getPlayer(args[0]) != null) {
            target = Bukkit.getPlayer(args[0]);
        }
        playerData.Menu.StatusInfo.StatusInfoView(target);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return PlayerTabComplete.complete();
    }
}
