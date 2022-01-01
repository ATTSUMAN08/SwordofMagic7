package swordofmagic7.Effect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Data.PlayerData;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.System.BTTSet;

public class EffectManager {
    private final Player player;
    private final PlayerData playerData;
    private final Plugin plugin;

    HashMap<EffectType, EffectData> Effect = new HashMap<>();

    public EffectManager(Player player, PlayerData playerData, Plugin plugin) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;

        BTTSet(new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) this.cancel();
                for (Map.Entry<EffectType, EffectData> effect : Effect.entrySet()) {
                    effect.getValue().time -= 5;
                    if (effect.getValue().time <= 0) {
                        Effect.remove(effect.getKey());
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 5), "EffectManager:" + player.getName());
    }

    public boolean hasEffect(EffectType effect) {
        return Effect.containsKey(effect);
    }

    public void addEffect(EffectType effectType, int time) {
        Effect.put(effectType, new EffectData(time));
    }

    public void removeEffect(EffectType effectType) {
        Effect.remove(effectType);
    }
}