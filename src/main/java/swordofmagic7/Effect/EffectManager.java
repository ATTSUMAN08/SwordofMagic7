package swordofmagic7.Effect;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.Log;
import static swordofmagic7.System.BTTSet;
import static swordofmagic7.System.plugin;

public class EffectManager {
    public LivingEntity entity;
    public final EffectOwnerType ownerType;
    public PetParameter petParameter;
    public EnemyData enemyData;

    public HashMap<EffectType, EffectData> Effect = new HashMap<>();

    public EffectManager(LivingEntity entity, EffectOwnerType ownerType) {
        this.entity = entity;
        this.ownerType = ownerType;
        BTTSet(new BukkitRunnable() {
            @Override
            public void run() {
                if ((entity instanceof Player player && !player.isOnline()) || (entity != null && entity.isDead())) {
                    this.cancel();
                    Effect.clear();
                    return;
                }
                if (Effect.size() > 0){
                    HashMap<EffectType, EffectData> clone = new HashMap<>(Effect);
                    for (Map.Entry<EffectType, EffectData> effect : clone.entrySet()) {
                        effect.getValue().time -= 2;
                        if (effect.getKey() == EffectType.Stun) {
                            if (entity != null) {
                                entity.setVelocity(Function.VectorDown);
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    entity.removePotionEffect(PotionEffectType.SLOW);
                                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3, 127, false, false));
                                });
                            }
                        }
                        if (effect.getValue().time <= 0) {
                            Effect.remove(effect.getKey());
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 2), "EffectManager");
    }

    public boolean hasEffect(EffectType effect) {
        return Effect.containsKey(effect);
    }

    public void addEffect(EffectType effectType, int time) {
        if (effectType == EffectType.Stun && hasEffect(EffectType.PainBarrier)) {
            if (entity instanceof Player) entity.sendMessage("§e[" + EffectType.PainBarrier.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました");
            return;
        }
        Effect.put(effectType, new EffectData(time));
        statusUpdate();
    }

    public void removeEffect(EffectType effectType) {
        Effect.remove(effectType);
        statusUpdate();
    }

    public void statusUpdate() {
        switch (ownerType) {
            case Pet -> petParameter.updateStatus();
            case Player -> playerData((Player) entity).Status.StatusUpdate();
            case Enemy -> enemyData.statusUpdate();
        }
    }

    public static EffectManager getEffectManager(LivingEntity entity) {
        if (entity == null) {
            return null;
        } else if (entity instanceof Player player) {
            return playerData(player).EffectManager;
        } else if (MobManager.isEnemy(entity)) {
            return MobManager.getEnemyTable().get(entity.getUniqueId()).effectManager;
        } else if (PetManager.isPet(entity)) {
            return PetManager.PetParameter(entity).effectManager;
        }
        return null;
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, Player player) {
        EffectManager manager = getEffectManager(entity);
        if (manager != null) {
            manager.addEffect(effectType, time);
            String color;
            if (effectType.Buff) {
                color = "§e";
            } else {
                color = "§c";
            }
            player.sendMessage(color + manager.getOwnerName() + "§aに" + color + "[" + effectType.Display + "]§aを付与しました");
            if (entity instanceof Player target && player != target) target.sendMessage(color + playerData(player).getNick() + "§aから" + color + "[" + effectType.Display + "]§aを付与されました");
        }
    }

    public static void removeEffect(LivingEntity entity, EffectType effectType) {
        EffectManager manager = getEffectManager(entity);
        if (manager != null) {
            manager.removeEffect(effectType);
        }
    }

    public static boolean hasEffect(LivingEntity entity, EffectType effectType) {
        EffectManager manager = getEffectManager(entity);
        if (manager != null) {
            return manager.hasEffect(effectType);
        }
        return false;
    }

    public String getOwnerName() {
        return getOwnerName(entity);
    }

    public static String getOwnerName(LivingEntity entity) {
        if (entity == null) {
            return null;
        } else if (entity instanceof Player player) {
            return playerData(player).getNick();
        } else if (MobManager.isEnemy(entity)) {
            return MobManager.getEnemyTable().get(entity.getUniqueId()).mobData.Display;
        } else if (PetManager.isPet(entity)) {
            return PetManager.PetParameter(entity).petData.Display;
        }
        return null;
    }
}