package swordofmagic7.Command.Developer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.SomCore.plugin;

public class SomReload implements SomCommand {

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerData(player).saveCloseInventory();
        }
        for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
            if (!hologram.isDeleted()) hologram.delete();
        }
        MultiThread.TaskRunSynchronizedLater(() -> Bukkit.getServer().dispatchCommand(sender, "plugman reload swordofmagic7"), 5);
        return true;
    }
}
