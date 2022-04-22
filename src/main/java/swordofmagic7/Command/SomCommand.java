package swordofmagic7.Command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Function.Log;

public interface SomCommand extends CommandExecutor {
    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player && PlayerCommand(player, PlayerData.playerData(player), args)) return true;
        return Command(sender, args);
    }

    boolean PlayerCommand(Player player, PlayerData playerData, String[] args);

    boolean Command(CommandSender sender, String[] args);

    static void register(String command, SomCommand executor) {
        try {
            Bukkit.getPluginCommand(command).setExecutor(executor);
            if (executor instanceof SomTabComplete tabComplete) tabComplete(command, tabComplete);
        } catch (Exception e) {
            e.printStackTrace();
            Log("§cCommandExecutor Error -> " + command);
        }
    }

    static void tabComplete(String command, TabCompleter tabComplete) {
        try {
            Bukkit.getPluginCommand(command).setTabCompleter(tabComplete);
        } catch (Exception e) {
            Log("§cTabCompleter Error -> " + command);
        }
    }
}
