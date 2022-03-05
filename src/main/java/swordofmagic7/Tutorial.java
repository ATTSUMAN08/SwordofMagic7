package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.DataBase.SpawnLocation;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Tutorial {
    public static HashMap<Player, Integer> TutorialProcess = new HashMap<>();
    public static final String TutorialNonSave = "§c§lチュートリアル中はデータはセーブされません";

    private static final World world  = Bukkit.getWorld("world");
    private static final Location[] TutorialLocation = new Location[5];

    public static void onLoad() {
        TutorialLocation[0] = new Location(world, 1220, -4, -718);
        TutorialLocation[2] = new Location(world, 1202, -1, -690);
    }

    public static void tutorialTrigger(Player player, int i) {
        if (i == 0 || TutorialProcess.getOrDefault(player, -1)+1 == i) {
            if (i == 0) player.sendMessage(TutorialNonSave);
            if (TutorialLocation[i] != null) player.teleportAsync(TutorialLocation[i]);
            TutorialProcess.put(player, i);
            player.sendMessage("§eチュートリアル[" + i + "/" + (TutorialLocation.length-1) + "]");
            Bukkit.getScheduler().runTaskLater(plugin, () -> playSound(player, SoundList.LevelUp), 1);
            if (i == TutorialLocation.length-1) tutorialEnd(player);
        }
    }

    static void tutorialEnd(Player player) {
        TutorialProcess.remove(player);
        player.sendMessage("§eチュートリアルをクリアしました");
        BroadCast(playerData(player).getNick() + "§aさんが§eチュートリアル§aを§eクリア§aしました");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.teleportAsync(SpawnLocation);
        }, 20);
    }
}
