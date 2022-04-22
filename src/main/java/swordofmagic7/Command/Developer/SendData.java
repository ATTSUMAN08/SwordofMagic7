package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Client;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Function.Log;

public class SendData implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        if (args.length == 1) {
            Client.send(args[0]);
        } else {
            Log("/sendData <text>");
        }
        return true;
    }
}
