package swordofmagic7.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;

import static swordofmagic7.Data.DataBase.WarpGateList;
import static swordofmagic7.Function.Log;

public class MapManager {
    public static void TeleportGateSelector(Player player) {
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


