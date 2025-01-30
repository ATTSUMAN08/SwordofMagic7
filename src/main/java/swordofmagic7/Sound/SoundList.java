package swordofmagic7.Sound;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public enum SoundList {
    CLICK(Sound.UI_BUTTON_CLICK, 1, SoundCategory.PLAYERS),
    NOPE(Sound.BLOCK_NOTE_BLOCK_HARP, 0, SoundCategory.PLAYERS),
    LEVEL_UP(Sound.ENTITY_PLAYER_LEVELUP, 1, SoundCategory.PLAYERS),
    MENU_OPEN("custom.menu.open", 1, SoundCategory.PLAYERS),
    MENU_CLOSE("custom.menu.close", 1, SoundCategory.PLAYERS),
    GUN_ATTACK(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, SoundCategory.PLAYERS),
    ROD_ATTACK(Sound.ENTITY_ENDER_EYE_LAUNCH, 1, SoundCategory.PLAYERS),
    ATTACK_SWEEP(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, SoundCategory.PLAYERS),
    ATTACK_WEAK(Sound.ENTITY_PLAYER_ATTACK_WEAK, 1, SoundCategory.PLAYERS),
    SHOOT(Sound.ENTITY_WITHER_SHOOT, 1, SoundCategory.PLAYERS),
    WARP(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, SoundCategory.PLAYERS),
    DEATH("custom.object.break", 1, SoundCategory.PLAYERS),
    DUNGEON_TRIGGER(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, SoundCategory.PLAYERS),
    TICK(Sound.BLOCK_LEVER_CLICK, 1, SoundCategory.PLAYERS),
    HEAL(Sound.ENTITY_ENDER_EYE_DEATH, 1, SoundCategory.PLAYERS),
    ACCEPT(Sound.BLOCK_NOTE_BLOCK_HARP, 1, SoundCategory.PLAYERS),
    HOWL(Sound.ENTITY_HOGLIN_ANGRY, 0, SoundCategory.PLAYERS),
    EXPLOSION(Sound.ENTITY_GENERIC_EXPLODE, 1, SoundCategory.PLAYERS),
    FAILED(Sound.ENTITY_IRON_GOLEM_DEATH, 1, SoundCategory.PLAYERS),
    DEBUFF(Sound.AMBIENT_UNDERWATER_EXIT, 1, SoundCategory.PLAYERS),
    FIRE(Sound.ENTITY_BLAZE_SHOOT, 1, SoundCategory.PLAYERS),
    ROCK(Sound.ENTITY_IRON_GOLEM_HURT, 1.5f, SoundCategory.PLAYERS),
    EAT(Sound.ENTITY_HORSE_EAT, 1, SoundCategory.PLAYERS),
    SHUN(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2, SoundCategory.PLAYERS),
    COUNTER(Sound.BLOCK_ANVIL_HIT, 2, SoundCategory.PLAYERS),
    SLIME(Sound.ENTITY_SLIME_ATTACK, 1, SoundCategory.PLAYERS),
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