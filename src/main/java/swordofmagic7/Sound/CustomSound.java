package swordofmagic7.Sound;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import swordofmagic7.MultiThread.MultiThread;

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
        MultiThread.TaskRun(() -> {
            for (int i = 0; i < count; i++) {
                playSound(player, player.getLocation(), sound);
                MultiThread.sleepTick(wait);
            }
        }, "playSound");
    }

    public static void playSound(Location location, SoundList sound, int count, int wait) {
        MultiThread.TaskRun(() -> {
            for (int i = 0; i < count; i++) {
                playSound(location, sound);
                MultiThread.sleepTick(wait);
            }
        }, "playSound");
    }
}
