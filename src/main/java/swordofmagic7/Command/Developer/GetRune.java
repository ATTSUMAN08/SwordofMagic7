package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.RuneParameter;

import java.util.Map;

import static swordofmagic7.Data.DataBase.getRuneList;
import static swordofmagic7.Data.DataBase.getRuneParameter;

public class GetRune implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            if (getRuneList().containsKey(args[0])) {
                RuneParameter rune = getRuneParameter(args[0]);
                rune.Level = 1;
                if (args.length >= 2) rune.Level = Integer.parseInt(args[1]);
                if (args.length >= 3) rune.Quality = Double.parseDouble(args[2]);
                playerData.RuneInventory.addRuneParameter(rune);
                playerData.RuneInventory.viewRune();
                return true;
            }
        }
        for (Map.Entry<String, RuneParameter> str : DataBase.getRuneList().entrySet()) {
            player.sendMessage(str.getKey());
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
