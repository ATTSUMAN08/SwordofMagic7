package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Map;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.WarpGateList;
import static swordofmagic7.DataBase.playerData;
import static swordofmagic7.Function.Log;
import static swordofmagic7.System.plugin;

public class MapManager {
    static void TeleportGateSelector(Player player) {
        Location pLoc = player.getLocation();
        for (Map.Entry<String, WarpGateParameter> entry : WarpGateList.entrySet()) {
            if (entry.getValue().Location.distance(pLoc) < 2) {
                if (WarpGateList.containsKey(entry.getValue().Target)) {
                    entry.getValue().usePlayer(player);
                } else {
                    Log("§cError NotFundWarpGate: " + entry.getValue().Target + " at " + entry.getKey());
                }
            }
        }
    }
}


class MapData {
    String Display;
    int Level = 0;
    boolean Safe;

    void enter(Player player) {
        playerData(player).Map = this;
        player.sendTitle(Display, "§e推奨Lv" + Level, 20, 40, 20);
    }
}

class WarpGateParameter {
    Location Location;
    String Target;
    MapData NextMap;

    void usePlayer(Player player) {
        NextMap.enter(player);
        player.teleportAsync(WarpGateList.get(Target).Location);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            playSound(player, SoundList.Warp);
        }, 1);
    }
}