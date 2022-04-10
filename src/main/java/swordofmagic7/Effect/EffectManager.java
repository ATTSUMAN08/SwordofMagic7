package swordofmagic7.Effect;

import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.SomCore.plugin;

public class EffectManager {
    public LivingEntity entity;
    public final EffectOwnerType ownerType;
    public PlayerData playerData;
    public PetParameter petParameter;
    public EnemyData enemyData;
    public boolean isRunnable = true;

    public HashMap<EffectType, EffectData> Effect = new HashMap<>();

    public EffectManager(LivingEntity entity, EffectOwnerType ownerType, Object ownerData) {
        this.entity = entity;
        this.ownerType = ownerType;
        switch (ownerType) {
            case Player -> playerData = (PlayerData) ownerData;
            case Pet -> petParameter = (PetParameter) ownerData;
            case Enemy -> enemyData = (EnemyData) ownerData;
        }
        MultiThread.TaskRun(() -> {
            while (isRunnable && plugin.isEnabled() && ((ownerType.isPlayer() && playerData.player.isOnline())
                    || (ownerType.isEnemy() && enemyData.isAlive())
                    || (ownerType.isPet() && petParameter.player.isOnline()))) {
                if (Effect.size() > 0) {
                    for (Map.Entry<EffectType, EffectData> effect : new HashMap<>(Effect).entrySet()) {
                        effect.getValue().time -= 2;
                        EffectType effectType = effect.getKey();
                        if (entity != null) {

                        }
                        if (ownerType == EffectOwnerType.Player) {
                            if (effectType == EffectType.Indulgendia && Math.floorMod(effect.getValue().time, 20) == 0) {
                                playerData.changeHealth(effect.getValue().doubleData[0]);
                            }
                        }
                        if (effect.getValue().time <= 0 || effect.getValue().stack < 1) {
                            removeEffect(effectType);
                        }
                    }
                }
                if (entity != null) {
                    MultiThread.TaskRunSynchronized(() -> {
                        boolean isCrowdControl = false;
                        boolean isSlow = false;
                        boolean isBlind = false;
                        for (EffectType effectType : Effect.keySet()) {
                            if (effectType.isCrowdControl()) isCrowdControl = true;
                            if (effectType.isSlow()) isSlow = true;
                            if (effectType.isBlind()) isBlind = true;
                        }
                        if (isCrowdControl) {
                            if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) return;
                            if (!ownerType.isEnemy() || !enemyData.mobData.enemyType.isIgnoreCrowdControl()) {
                                entity.removePotionEffect(PotionEffectType.SLOW);
                                entity.removePotionEffect(PotionEffectType.JUMP);
                                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 255, false, false, false));
                                entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 2, 255, false, false, false));
                                entity.setVelocity(Function.VectorDown);
                            }
                        }
                        if (isSlow) {
                            entity.removePotionEffect(PotionEffectType.SLOW);
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 2, false, false));
                        }
                        if (isBlind) {
                            entity.removePotionEffect(PotionEffectType.BLINDNESS);
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 0, false, false));
                        }
                    }, "EffectManagerTimer");
                }
                MultiThread.sleepTick(2);
            }
        }, "EffectManager");
    }

    public void stunVelocity(LivingEntity entity, EffectType effectType) {
        if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) return;
        entity.setVelocity(Function.VectorDown);
    }

    public boolean hasEffect(EffectType effect) {
        return Effect.containsKey(effect);
    }

    public boolean addEffect(EffectType effectType, int time) {
        return addEffect(effectType, time, null, 1);
    }

    public boolean addEffect(EffectType effectType, int time, double doubleData) {
        return addEffect(effectType, time, new double[]{doubleData}, 1);
    }

    public boolean addEffect(EffectType effectType, int time, double[] doubleData, int stack) {
        if (hasEffect(EffectType.Stop)) {
            if (entity instanceof Player player) Function.sendMessage(player, "§e[" + EffectType.Stop.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.Tick);
            return false;
        }
        if (effectType == EffectType.Stun && hasEffect(EffectType.PainBarrier)) {
            if (entity instanceof Player player) Function.sendMessage(player, "§e[" + EffectType.PainBarrier.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.Tick);
            return false;
        }
        if (!effectType.Buff && effectType.effectRank.isNormal() && hasEffect(EffectType.Indulgence)) {
            Effect.get(EffectType.Indulgence).stack--;
            if (Effect.get(EffectType.Indulgence).stack < 1) Effect.remove(EffectType.Indulgence);
            if (entity instanceof Player player) Function.sendMessage(player, "§e[" + EffectType.Indulgence.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.Tick);
            return false;
        }
        EffectData effectData;
        if (Effect.containsKey(effectType)) {
            effectData = Effect.get(effectType);
            effectData.time = Math.max(effectData.time, time);
            if (effectData.stack+stack >= effectType.MaxStack) {
                effectData.stack = effectType.MaxStack;
            } else effectData.stack += stack;
        } else {
            effectData = new EffectData(effectType, time);
            effectData.stack = stack;
        }
        effectData.doubleData = doubleData;
        Effect.put(effectType, effectData);
        statusUpdate(effectType);
        return true;
    }

    public void removeEffect(EffectType effectType) {
        Effect.remove(effectType);
        statusUpdate(effectType);
    }

    public void removeEffect(EffectType effectType, Player player) {
        Effect.remove(effectType);
        switch (ownerType) {
            case Player -> {
                if (entity instanceof Player target) {
                    player.sendMessage(playerData(target).getNick() + "§aの" + effectType.color() + "[" + effectType.Display + "]§aを§b解除§aしました");
                    target.sendMessage(playerData.getNick() + "§aが" + effectType.color() + "[" + effectType.Display + "]§aを§b解除§aしました");
                }
            }
            case Pet -> player.sendMessage("§e" + petParameter.petData.Display + "§aの" + effectType.color() + "[" + effectType.Display + "]§aを§b解除§aしました");
            case Enemy -> player.sendMessage("§e" + enemyData.mobData.Display + "§aの" + effectType.color() + "[" + effectType.Display + "]§aを§b解除§aしました");
        }

    }

    public EffectData getData(EffectType effectType) {
        return Effect.get(effectType);
    }

    public void clearEffect() {
        Effect.clear();
    }

    public void statusUpdate(EffectType effectType) {
        if (effectType.isUpdateStatus) {
            switch (ownerType) {
                case Pet -> petParameter.updateStatus();
                case Player -> playerData.Status.StatusUpdate();
                case Enemy -> enemyData.statusUpdate();
            }
        }
    }

    public static EffectManager getEffectManager(LivingEntity entity) {
        if (entity == null) {
            return null;
        } else if (entity instanceof Player player) {
            return playerData(player).EffectManager;
        } else if (MobManager.isEnemy(entity)) {
            return MobManager.EnemyTable(entity.getUniqueId()).effectManager;
        } else if (PetManager.isPet(entity)) {
            return PetManager.PetParameter(entity).getEffectManager();
        }
        return null;
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, Player player) {
        addEffect(entity, effectType, time, 1, player);
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, int stack, Player player) {
        EffectManager manager = getEffectManager(entity);
        if (manager != null) {
            boolean isSendMessage = !manager.hasEffect(effectType);
            if (manager.addEffect(effectType, time, null, stack)) {
                if (player != null && isSendMessage) addEffectMessage(player, entity, effectType);
            } else {
                if (player != null) player.sendMessage(effectType.color() + "[" + effectType.Display + "]§aが無効化されました");
            }
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

    public boolean isCrowdControl() {
        for (EffectType effectType : Effect.keySet()) {
            if (effectType.isCrowdControl()) return true;
        }
        return false;
    }

    public boolean isSkillsNotAvailable() {
        for (EffectType effectType : Effect.keySet()) {
            if (effectType.isSkillsNotAvailable()) return true;
        }
        return false;
    }

    public boolean isInvincible() {
        return hasEffect(EffectType.Invincible) || hasEffect(EffectType.Stop);
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
            return MobManager.EnemyTable(entity.getUniqueId()).mobData.Display;
        } else if (PetManager.isPet(entity)) {
            return PetManager.PetParameter(entity).petData.Display;
        }
        return null;
    }

    public static void addEffectMessage(Player player, LivingEntity entity, EffectType effectType) {
        addEffectMessage(player, entity, effectType.Display, effectType.color());
    }

    public static void addEffectMessage(Player player, LivingEntity entity, String Display, String color) {
        if (player == null) return;
        player.sendMessage(color + EffectManager.getOwnerName(entity) + "§aに" + color + "[" + Display + "]§aを付与しました");
        if (entity instanceof Player target && player != entity) target.sendMessage(color + playerData(player).getNick() + "§aから" + color + "[" + Display + "]§aを付与されました");
    }
}