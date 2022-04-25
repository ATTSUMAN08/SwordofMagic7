package swordofmagic7.Command.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.RuneParameter;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.RuneList;
import static swordofmagic7.Function.sendMessage;

public class RuneFilter implements SomCommand, SomTabComplete {

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        try {
            if (args[0].equalsIgnoreCase("Quality") || args[0].equalsIgnoreCase("Q")) {
                double value = Double.parseDouble(args[1])/100;
                if (0 <= value && value <= 100) {
                    playerData.RuneQualityFilter = value;
                    sendMessage(player, "§eルーンフィルター[品質] §b-> §a" + value*100 + "%");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("Id")) {
                String runeId = args[1];
                if (DataBase.getRuneList().containsKey(runeId)) {
                    if (playerData.RuneIdFilter.contains(runeId)) {
                        playerData.RuneIdFilter.remove(runeId);
                    } else {
                        playerData.RuneIdFilter.add(runeId);
                    }
                    sendMessage(player, "§eルーンフィルター[ID:" + runeId + "] §b-> §a" + (playerData.RuneIdFilter.contains(runeId) ? "§b有効" : "§c無効"));
                    return true;
                } else {
                    sendMessage(player, "§a存在しない§eルーン§aです");
                }
            }
        } catch (Exception ignored) {}
        sendMessage(player, "§e/runeFilter Quality <0~100>");
        sendMessage(player, "§e/runeFilter Id <RuneId>");
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
        List<String> complete = new ArrayList<>();
        if (args.length == 1) {
            complete.add("Id");
            complete.add("Quality");
        } else if (args.length == 2) {
            for (RuneParameter rune : RuneList.values()) {
                if (!rune.isHide) complete.add(rune.Id);
            }
        }
        return complete;
    }
}
