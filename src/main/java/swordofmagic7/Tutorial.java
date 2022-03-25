package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Tutorial {
    public static HashMap<Player, Integer> TutorialProcess = new HashMap<>();
    public static final String TutorialNonSave = "§c§lチュートリアル中はデータはセーブされません";

    private static final World world  = Bukkit.getWorld("world");
    private static final Location[] TutorialLocation = new Location[5];

    public static void onLoad() {
        TutorialLocation[0] = new Location(world, 1181.5, 65, -259.5);
        TutorialLocation[1] = new Location(world, 1153.5, 65, -259.5);
        TutorialLocation[2] = new Location(world, 1125.5, 65, -259.5);
        TutorialLocation[3] = new Location(world, 1097.5, 65, -259.5);
        TutorialLocation[4] = new Location(world, 1069.5, 65, -259.5);
    }

    public static void tutorialTrigger(Player player, int i) {
        MultiThread.TaskRunSynchronized(() -> {
            if (i == 0 || TutorialProcess.getOrDefault(player, -1)+1 == i) {
                if (i == 0) {
                    BroadCast(playerData(player).getNick() + "§aさんが§eチュートリアル§aを§b開始§aしました");
                    player.sendMessage(TutorialNonSave);
                }
                if (TutorialLocation[i] != null) player.teleportAsync(TutorialLocation[i]);
                TutorialProcess.put(player, i);
                MultiThread.TaskRunSynchronizedLater(() -> playSound(player, SoundList.LevelUp), 1);
                PlayerData playerData = playerData(player);
                player.sendMessage("§eチュートリアル§aが進みました");
                if (i == 4) {
                    TutorialProcess.remove(player);
                    BroadCast(playerData.getNick() + "§aさんが§eチュートリアル§c最低限編§aを§eクリア§aしました");
                }
            }
        });
    }

    public static void tutorialHub(Player player) {
        player.teleportAsync(TutorialLocation[4]);
        MultiThread.TaskRunSynchronizedLater(() -> playSound(player, SoundList.LevelUp), 1);
    }
}
