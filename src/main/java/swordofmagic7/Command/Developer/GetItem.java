package swordofmagic7.Command.Developer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.TextView.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.sendMessage;

public class GetItem implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        PlayerData targetData = sender instanceof Player player ? playerData(player) : null;
        /*if (args.length >= 3) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target.isOnline()) targetData = playerData(target);
        }*/
        if (targetData != null) {
            if (getItemList().containsKey(args[0])) {
                ItemParameterStack stack = new ItemParameterStack(getItemParameter(args[0]));
                int amount = 1;
                if (args.length >= 2) amount = Integer.parseInt(args[1]);
                if (args.length >= 3 && stack.itemParameter.Category.isEquipment()) {
                    stack.itemParameter.itemEquipmentData.Plus = Integer.parseInt(args[2]);
                }
                stack.Amount = amount;
                targetData.ItemInventory.addItemParameter(stack);
                targetData.ItemInventory.viewInventory();
                TextView textView = stack.itemParameter.getTextView(stack.Amount, targetData.ViewFormat());
                textView.addText("§aを§e獲得§aしました");
                targetData.player.sendMessage(textView.toComponent());
                return true;
            }
        } else {
            sender.sendMessage("§c無効なプレイヤーです");
        }
        for (Map.Entry<String, ItemParameter> str : getItemList().entrySet()) {
            sender.sendMessage(str.getKey());
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
        if (args.length == 1) for (ItemParameter item : ItemList.values()) {
            complete.add(item.Id);
        }
        return complete;
    }
}
