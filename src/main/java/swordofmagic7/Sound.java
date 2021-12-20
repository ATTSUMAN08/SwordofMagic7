package swordofmagic7;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

enum SoundList {
    Click(Sound.BLOCK_LEVER_CLICK, 1, SoundCategory.PLAYERS),
    Nope(Sound.BLOCK_NOTE_BLOCK_HARP, 0, SoundCategory.PLAYERS),
    LevelUp(Sound.ENTITY_PLAYER_LEVELUP, 1, SoundCategory.PLAYERS),
    MenuOpen(Sound.BLOCK_ENDER_CHEST_OPEN, 1, SoundCategory.PLAYERS),
    MenuClose(Sound.BLOCK_ENDER_CHEST_CLOSE, 1, SoundCategory.PLAYERS),
    GunAttack(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, SoundCategory.PLAYERS),
    RodAttack(Sound.ENTITY_ENDER_EYE_LAUNCH, 1, SoundCategory.PLAYERS),
    AttackSweep(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, SoundCategory.PLAYERS),
    AttackWeak(Sound.ENTITY_PLAYER_ATTACK_WEAK, 1, SoundCategory.PLAYERS),
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

final class CustomSound {

    static void playSound(Player player, SoundList sound) {
        playSound(player, player.getLocation(), sound);
    }
    static void playSound(Player player, Location location, SoundList sound) {
        if (sound.data instanceof Sound data) player.playSound(location, data, sound.category, 1, sound.pitch);
        if (sound.data instanceof String data) player.playSound(location, data, sound.category, 1, sound.pitch);
    }

    static void playSound(Location location, SoundList sound) {
        final World world = location.getWorld();
        if (sound.data instanceof Sound data) world.playSound(location, data, sound.category, 1, sound.pitch);
        if (sound.data instanceof String data) world.playSound(location, data, sound.category, 1, sound.pitch);
    }
}
