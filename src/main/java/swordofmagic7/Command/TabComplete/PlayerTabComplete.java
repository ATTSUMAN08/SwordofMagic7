package swordofmagic7.Command.TabComplete;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class PlayerTabComplete implements SomTabComplete {

    public static List<String> complete() {
        List<String> list = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            list.add(player.getName());
        }
        return list;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {

        return complete();
    }
}
