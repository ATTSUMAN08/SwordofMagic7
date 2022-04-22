package swordofmagic7.Command.Developer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.SomCore.BukkitTaskTag;

public class BukkitTasks implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        HashMap<String, Integer> sync = new HashMap<>();
        HashMap<String, Integer> async = new HashMap<>();
        if (BukkitTaskTag != null) {
            BukkitTaskTag.keySet().removeIf(BukkitTask::isCancelled);
            for (Map.Entry<BukkitTask, String> task : BukkitTaskTag.entrySet()) {
                String[] split = task.getValue().split(":");
                if (task.getKey().isSync()) sync.merge(task.getValue(), 1, Integer::sum);
                else async.merge(task.getValue(), 1, Integer::sum);
            }
        }
        sender.sendMessage("PendingTask: " + Bukkit.getScheduler().getPendingTasks().size());
        sender.sendMessage("TaggedTask: " + BukkitTaskTag.size());
        sender.sendMessage("AsyncTask: " + async.size());
        for (Map.Entry<String, Integer> tagCount : async.entrySet()) {
            sender.sendMessage("・" + tagCount.getKey() + ": " + tagCount.getValue());
        }
        sender.sendMessage("SyncTask: " + sync.size());
        for (Map.Entry<String, Integer> tagCount : sync.entrySet()) {
            sender.sendMessage("・" + tagCount.getKey() + ": " + tagCount.getValue());
        }
        return true;
    }
}
