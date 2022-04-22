package swordofmagic7.Command.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Function.sendMessage;

public class ItemInfo implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("Amount")) {
                sendMessage(player, "§aItemListSize: " + ItemList.size());
            } else if (ItemList.containsKey(args[0])) {
                ItemParameter item = getItemParameter(args[0]);
                List<String> list = new ArrayList<>();
                list.add(decoText(item.Display));
                list.addAll(ItemInfoData.get(item.Id));
                sendMessage(player, list);
            } else player.sendMessage("§a存在しない§eアイテム§aです");
        } else {
            player.sendMessage("§e/itemInfo <ItemID>");
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
        List<String> complete = new ArrayList<>();
        if (args.length == 1) for (ItemParameter item : ItemList.values()) {
            if (!item.isHide) complete.add(item.Id);
        }
        return complete;
    }
}
