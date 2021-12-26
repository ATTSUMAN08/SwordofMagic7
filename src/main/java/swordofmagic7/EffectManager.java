package swordofmagic7;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

enum EffectType {
    Stun("スタン")
    ;

    String Display;

    EffectType(String Display) {
        this.Display = Display;
    }
}

class EffectData {
    int time;

    EffectData(int time) {
        this.time = time;
    }
}

public class EffectManager {
    private final Player player;
    private final PlayerData playerData;

    HashMap<EffectType, EffectData> Effect = new HashMap<>();

    EffectManager(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    boolean hasEffect(EffectType effect) {
        return Effect.containsKey(effect);
    }

    void addEffect(EffectType effectType, int time) {
        Effect.put(effectType, new EffectData(time));
    }

    void removeEffect(EffectType effectType) {
        Effect.remove(effectType);
    }
}