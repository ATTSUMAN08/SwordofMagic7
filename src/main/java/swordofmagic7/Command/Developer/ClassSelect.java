package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Data.DataBase.getClassData;

public class ClassSelect implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        try {
            playerData.Classes.classSlot[Integer.parseInt(args[0])] = getClassData(args[1]);
        } catch (Exception e) {
            player.sendMessage("/classSelect <slot> <class>");
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
