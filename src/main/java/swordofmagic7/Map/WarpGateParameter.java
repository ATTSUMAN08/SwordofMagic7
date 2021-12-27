package swordofmagic7.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Data.DataBase.WarpGateList;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class WarpGateParameter {
    public Location Location;
    public String Target;
    public MapData NextMap;

    void usePlayer(Player player) {
        NextMap.enter(player);
        player.teleportAsync(WarpGateList.get(Target).Location);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            playSound(player, SoundList.Warp);
        }, 1);
    }
}
