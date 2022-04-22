package swordofmagic7.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import swordofmagic7.Data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public interface SomTabComplete extends TabCompleter {
    @Override
    default @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        List<String> data = TabComplete(sender, command, args);
        if (data != null) list.addAll(data);
        if (sender instanceof Player player) {
            data = PlayerTabComplete(player, PlayerData.playerData(player), command, args);
            if (data != null) list.addAll(data);
        }
        if (args.length > 0) list.removeIf(tab -> !tab.contains(args[args.length-1]));
        return list;
    }

    List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args);

    List<String> TabComplete(CommandSender sender, Command command, String[] args);
}
