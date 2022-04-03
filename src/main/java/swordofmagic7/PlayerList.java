package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Pet.PetParameter;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;

public final class PlayerList {
    public static final Set<Player> PlayerList = new HashSet<>();

    static void load() {
        PlayerList.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOnline()) {
                PlayerList.add(player);
            }
        }
    }

    public static Set<Player> get() {
        return PlayerList;
    }

    public static Set<Player> getNear(Location loc, double radius) {
        Set<Player> List = new HashSet<>();
        try {
            for (Player player : PlayerList) {
                if (player.isOnline()) {
                    if (player.getLocation().distance(loc) <= radius) List.add(player);
                }
            }
            return List;
        } catch (Exception e) {
            return List;
        }
    }

    public static Set<Player> getNearNonDead(Location loc, double radius) {
        Set<Player> List = new HashSet<>();
        for (Player player : PlayerList) {
            if (player.isOnline() && player.getGameMode() != GameMode.SPECTATOR) {
                if (player.getLocation().distance(loc) <= radius) List.add(player);
            }
        }
        return List;
    }

    public static Set<LivingEntity> getNearLivingEntity(Location loc, double radius) {
        Set<LivingEntity> List = new HashSet<>();
        for (Player player : PlayerList) {
            if (Function.isAlive(player)) {
                if (player.getLocation().distance(loc) <= radius) List.add(player);
                for (PetParameter pet : playerData(player).PetSummon) {
                    try {
                        if (pet.entity.getLocation().distance(loc) <= radius) List.add(pet.entity);
                    } catch (Exception ignored) {}
                }
            }
        }
        return List;
    }
}
