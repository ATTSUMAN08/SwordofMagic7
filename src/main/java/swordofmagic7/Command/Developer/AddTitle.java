package swordofmagic7.Command.Developer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Command.TabComplete.PlayerTabComplete;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.TitleData;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.PlayerData.playerData;

public class AddTitle implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        Player target = null;
        if (sender instanceof Player player) target = player;
        if (args.length == 2) {
            target = Bukkit.getPlayer(args[1]);
        }
        if (target != null && target.isOnline()) {
            PlayerData targetData = playerData(target);
            targetData.titleManager.addTitle(args[0]);
        } else {
            sender.sendMessage("§c無効なプレイヤーです");
        }
        return true;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        if (args.length == 1) {
            for (TitleData titleData : DataBase.TitleDataList.values()) {
                complete.add(titleData.Id);
            }
        } else if (args.length == 2) {
            return PlayerTabComplete.complete();
        }
        return complete;
    }
}
