package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PlayerList {
    private static final List<Player> PlayerList = new ArrayList<>();

    static void load() {
        PlayerList.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOnline()) {
                PlayerList.add(player);
            }
        }
    }

    public static List<Player> get() {
        return PlayerList;
    }

    public static List<Player> getNear(Location loc, double radius) {
        List<Player> List = new ArrayList<>();
        for (Player player : PlayerList) {
            if (player.getLocation().distance(loc) <= radius) List.add(player);
        }
        return List;
    }

    public static List<LivingEntity> getNearLivingEntity(Location loc, double radius) {
        List<LivingEntity> List = new ArrayList<>();
        for (Player player : PlayerList) {
            if (player.getLocation().distance(loc) <= radius) List.add(player);
        }
        return List;
    }
}
