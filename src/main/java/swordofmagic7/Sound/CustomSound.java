package swordofmagic7.Sound;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.System;

public class CustomSound {
    public static void playSound(Player player, SoundList sound) {
        playSound(player, player.getLocation(), sound);
    }

    public static void playSound(Player player, Location location, SoundList sound) {
        if (sound.data instanceof Sound data) {
            player.playSound(location, data, sound.category, 1, sound.pitch);
        }
        if (sound.data instanceof String data) {
            player.playSound(location, data, sound.category, 1, sound.pitch);
        }
    }

    public static void playSound(Location location, SoundList sound) {
        final World world = location.getWorld();
        if (sound.data instanceof Sound data) {
            world.playSound(location, data, sound.category, 1, sound.pitch);
        }
        if (sound.data instanceof String data) {
            world.playSound(location, data, sound.category, 1, sound.pitch);
        }
    }

    public static void playSound(Player player, SoundList sound, int count, int wait) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i < count) {
                    playSound(player, player.getLocation(), sound);
                } else {
                    this.cancel();
                }
                i++;
            }
        }.runTaskTimerAsynchronously(System.plugin, 0, wait);
    }

    public static void playSound(Location location, SoundList sound, int count, int wait) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i < count) {
                    playSound(location, sound);
                } else {
                    this.cancel();
                }
                i++;
            }
        }.runTaskTimerAsynchronously(System.plugin, 0, wait);
    }
}
