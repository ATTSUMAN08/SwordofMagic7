package swordofmagic7.Effect;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;

import java.util.HashMap;

public class EffectManager {
    private final Player player;
    private final PlayerData playerData;

    HashMap<EffectType, EffectData> Effect = new HashMap<>();

    public EffectManager(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public boolean hasEffect(EffectType effect) {
        return Effect.containsKey(effect);
    }

    void addEffect(EffectType effectType, int time) {
        Effect.put(effectType, new EffectData(time));
    }

    void removeEffect(EffectType effectType) {
        Effect.remove(effectType);
    }
}