package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Tutorial {
    public static HashMap<Player, Integer> TutorialProcess = new HashMap<>();
    public static List<TutorialData> TutorialList = new ArrayList<>();
    public static final String TutorialNonSave = "§c§lチュートリアル中はデータはセーブされません";

    private static final World world  = Bukkit.getWorld("world");
    private static final Location[] TutorialLocation = new Location[64];

    public static void onLoad() {
        Location clear = new Location(world, 1069.5, 65, -259.5);;
        TutorialLocation[0] = new Location(world, 1181.5, 65, -259.5);
        TutorialLocation[1] = new Location(world, 1153.5, 65, -259.5);
        TutorialLocation[2] = clear;

        TutorialLocation[4] = clear;

        TutorialLocation[5] = new Location(world, 1125.5, 65, -259.5);
        TutorialLocation[6] = new Location(world, 1097.5, 65, -259.5);
        TutorialLocation[7] = clear;

        TutorialList.add(new TutorialData("§c最低限編", 0, 2));
        TutorialList.add(new TutorialData("§bスキル編", 5, 7));
    }

    public static void tutorialTrigger(Player player, int i) {
        MultiThread.TaskRunSynchronized(() -> {
            PlayerData playerData = playerData(player);
            boolean start = false;
            for (TutorialData data : TutorialList) {
                if (data.start == i) {
                    BroadCast(playerData.getNick() + "§aさんが§eチュートリアル" + data.Display + "§aを§b開始§aしました", false);
                    if (playerData.Level == 1) player.sendMessage(TutorialNonSave);
                    start = true;
                }
            }
            if (start || TutorialProcess.getOrDefault(player, -1)+1 == i) {
                if (TutorialLocation[i] != null) player.teleportAsync(TutorialLocation[i]);
                TutorialProcess.put(player, i);
                MultiThread.TaskRunSynchronizedLater(() -> playSound(player, SoundList.LevelUp), 1);
                for (TutorialData data : TutorialList) {
                    if (data.end == i) {
                        TutorialProcess.remove(player);
                        BroadCast(playerData.getNick() + "§aさんが§eチュートリアル" + data.Display + "§aを§eクリア§aしました", false);
                    }
                }
            }
        });
    }

    public static void tutorialHub(Player player) {
        player.teleportAsync(TutorialLocation[4]);
        MultiThread.TaskRunSynchronizedLater(() -> playSound(player, SoundList.LevelUp), 1);
    }
}

class TutorialData {
    final String Display;
    final int start;
    final int end;
    TutorialData(String Display, int start, int end) {
        this.Display = Display;
        this.start = start;
        this.end = end;
    }
}