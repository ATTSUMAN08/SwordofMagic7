package swordofmagic7.Effect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Data.PlayerData;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.System.BTTSet;
import static swordofmagic7.System.plugin;

public class EffectManager {
    public final LivingEntity entity;

    HashMap<EffectType, EffectData> Effect = new HashMap<>();

    public EffectManager(LivingEntity entity) {
        this.entity = entity;
        BTTSet(new BukkitRunnable() {
            @Override
            public void run() {
                if ((entity instanceof Player player && !player.isOnline()) || (entity != null && entity.isDead())) this.cancel();
                for (Map.Entry<EffectType, EffectData> effect : Effect.entrySet()) {
                    effect.getValue().time -= 5;
                    if (effect.getValue().time <= 0) {
                        Effect.remove(effect.getKey());
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 5), "EffectManager");
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