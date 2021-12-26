package swordofmagic7;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

enum SoundList {
    Click("custom.menu.click", 1, SoundCategory.PLAYERS),
    Nope(Sound.BLOCK_NOTE_BLOCK_HARP, 0, SoundCategory.PLAYERS),
    LevelUp(Sound.ENTITY_PLAYER_LEVELUP, 1, SoundCategory.PLAYERS),
    MenuOpen("custom.menu.open", 1, SoundCategory.PLAYERS),
    MenuClose("custom.menu.close", 1, SoundCategory.PLAYERS),
    GunAttack(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, SoundCategory.PLAYERS),
    RodAttack(Sound.ENTITY_ENDER_EYE_LAUNCH, 1, SoundCategory.PLAYERS),
    AttackSweep(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, SoundCategory.PLAYERS),
    AttackWeak(Sound.ENTITY_PLAYER_ATTACK_WEAK, 1, SoundCategory.PLAYERS),
    Shoot(Sound.ENTITY_WITHER_SHOOT, 1, SoundCategory.PLAYERS),
    Warp(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, SoundCategory.PLAYERS),
    Death("custom.object.break", 1, SoundCategory.PLAYERS),
    ;

    Object data;
    float pitch;
    SoundCategory category;

    SoundList(Object data, float pitch, SoundCategory category) {
        this.data = data;
        this.pitch = pitch;
        this.category = category;
    }

    boolean isSound() {
        return data instanceof Sound;
    }

    boolean isCustomSound() {
        return data instanceof String;
    }
}

class CustomSound {
    static void playSound(Player player, SoundList sound) {
        playSound(player, player.getLocation(), sound);
    }

    static void playSound(Player player, Location location, SoundList sound) {
        if (sound.data instanceof Sound data) {
            player.playSound(location, data, sound.category, 1, sound.pitch);
        }
        if (sound.data instanceof String data) {
            player.playSound(location, data, sound.category, 1, sound.pitch);
        }
    }

    static void playSound(Location location, SoundList sound) {
        final World world = location.getWorld();
        if (sound.data instanceof Sound data) {
            world.playSound(location, data, sound.category, 1, sound.pitch);
        }
        if (sound.data instanceof String data) {
            world.playSound(location, data, sound.category, 1, sound.pitch);
        }
    }

    static void playSound(Player player, SoundList sound, int count, int wait) {
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

    static void playSound(Location location, SoundList sound, int count, int wait) {
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
