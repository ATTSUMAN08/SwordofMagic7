package swordofmagic7.Command.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.TagGame;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.sendMessage;

public class TagGameCommand implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("join")) {
                TagGame.join(player);
            } else if (args[0].equalsIgnoreCase("leave")) {
                TagGame.leave(player);
            } else if (args[0].equalsIgnoreCase("start") && TagGame.Master == player) {
                TagGame.startTagGame();
            } else if (args[0].equalsIgnoreCase("reset") && TagGame.Master == player) {
                TagGame.resetTagGame();
            } else if (args[0].equalsIgnoreCase("master")) {
                if (TagGame.Master == null || !TagGame.Master.isOnline()) {
                    TagGame.Master = player;
                    sendMessage(player, "§eゲームマスター§aになりました");
                } else {
                    sendMessage(player, TagGame.Master.getDisplayName() + "§aが§eゲームマスター§aです");
                }
            }
        } else {
            for (String str : TagGame.info()) {
                player.sendMessage(str);
            }
            player.sendMessage("§e/tagGame [join/leave]");
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        complete.add("join");
        complete.add("leave");
        if (player.isOp()) {
            complete.add("master");
            complete.add("start");
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
