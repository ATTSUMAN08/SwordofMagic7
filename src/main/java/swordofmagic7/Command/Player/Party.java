package swordofmagic7.Command.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Command.TabComplete.PlayerTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Party.PartyManager;

import java.util.ArrayList;
import java.util.List;

public class Party implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        PartyManager.partyCommand(player, playerData, args);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        if (args.length == 1) {
            if (PartyManager.PartyInvites.containsKey(player)) {
                complete.add("accept");
                complete.add("decline");
            }
            if (playerData.Party != null) {
                complete.add("leave");
                complete.add("info");
                if (playerData.Party.Leader == player) {
                    complete.add("invite");
                    complete.add("promote");
                    complete.add("kick");
                    complete.add("lore");
                    complete.add("toggle");
                }
            } else {
                complete.add("create");
                complete.add("join");
                complete.add("list");
            }
        } else if (args.length >= 2) {
            if (Function.StringEqual(args[0], new String[]{"invite","promote","kick"})) {
                return PlayerTabComplete.complete();
            }
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
