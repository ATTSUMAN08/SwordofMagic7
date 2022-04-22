package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Data.DataBase.getClassData;
import static swordofmagic7.Data.DataBase.getClassList;

public class GetClassExp implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 2 && getClassList().containsKey(args[1])) {
            try {
                playerData.Classes.addClassExp(getClassData(args[1]), Integer.parseInt(args[0]));
            } catch (Exception e) {
                player.sendMessage("§c" + "/getClassExp <exp> <class>");
            }
        } else {
            player.sendMessage("§c" + "/getClassExp <exp> <class>");
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
