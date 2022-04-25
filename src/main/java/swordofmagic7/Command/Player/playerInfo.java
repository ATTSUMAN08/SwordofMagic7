package swordofmagic7.Command.Player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Command.TabComplete.PlayerTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;

import java.util.List;

import static swordofmagic7.Function.CheckBlockPlayer;
import static swordofmagic7.Function.sendMessage;

public class playerInfo implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        Player target = player;
        if (args.length == 1 && Bukkit.getPlayer(args[0]) != null) {
            target = Bukkit.getPlayer(args[0]);
        }
        if (target != null && target.isOnline()) {
            if (CheckBlockPlayer(player, target)) return true;
            playerData.Menu.StatusInfo.StatusInfoView(target);
        } else {
            sendMessage(player, "§c無効なプレイヤーです", SoundList.Nope);
        }
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
