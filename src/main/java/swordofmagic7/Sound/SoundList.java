package swordofmagic7.Sound;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public enum SoundList {
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
    DungeonTrigger(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, SoundCategory.PLAYERS),
    Tick(Sound.BLOCK_LEVER_CLICK, 1, SoundCategory.PLAYERS),
    Heal(Sound.ENTITY_ENDER_EYE_DEATH, 1, SoundCategory.PLAYERS),
    Accept(Sound.BLOCK_NOTE_BLOCK_HARP, 1, SoundCategory.PLAYERS),
    Howl(Sound.ENTITY_HOGLIN_ANGRY, 0, SoundCategory.PLAYERS),
    Explosion(Sound.ENTITY_GENERIC_EXPLODE, 1, SoundCategory.PLAYERS),
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