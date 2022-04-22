package swordofmagic7.Command.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Market.Market;

import java.util.ArrayList;
import java.util.List;

public class MarketCommand implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        Market.marketCommand(playerData, args);
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
            complete.add("buy");
            complete.add("sell");
            complete.add("cancel");
            complete.add("info");
            complete.add("collect");
            complete.add("price");
        } else if (args.length == 2) {
            if (Function.StringEqual(args[1], "collect")) {
                complete.add("confirm");
            }
        }
        return complete;
    }
}
