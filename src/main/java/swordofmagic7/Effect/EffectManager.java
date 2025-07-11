package swordofmagic7.Effect;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Particle.ParticleManager.ShapedParticle;
import static swordofmagic7.Particle.ParticleManager.spawnParticle;
import static swordofmagic7.Skill.SkillProcess.FanShapedCollider;
import static swordofmagic7.Skill.SkillProcess.particleActivate;
import static net.somrpg.swordofmagic7.SomCore.instance;

public class EffectManager {
    public LivingEntity entity;
    public final EffectOwnerType ownerType;
    public PlayerData playerData;
    public PetParameter petParameter;
    public EnemyData enemyData;
    public boolean isRunnable = true;

    public boolean isCrowdControl = false;
    public boolean isSkillsNotAvailable = false;
    public boolean isInvincible = false;
    public boolean isSlow = false;
    public boolean isBlind = false;
    public Location isFixed;

    public static final Long period = 5L;
    private int stickyCount = 0;
    private int dissolutionTimer = 0;

    public Map<EffectType, EffectData> Effect = new HashMap<>();

    public EffectManager(LivingEntity entity, EffectOwnerType ownerType, Object ownerData) {
        this.entity = entity;
        this.ownerType = ownerType;
        switch (ownerType) {
            case Player -> playerData = (PlayerData) ownerData;
            case Pet -> petParameter = (PetParameter) ownerData;
            case Enemy -> enemyData = (EnemyData) ownerData;
        }
    }

    public void onTaskRun() {
        try {
            isCrowdControl = false;
            isSkillsNotAvailable = false;
            isInvincible = false;
            isSlow = false;
            isBlind = false;
            isFixed = null;
            if (!Effect.isEmpty()) {
                for (Map.Entry<EffectType, EffectData> effect : new HashMap<>(Effect).entrySet()) {
                    EffectType effectType = effect.getKey();
                    EffectData effectData = effect.getValue();
                    if (!effectType.isToggle) effectData.time -= period;
                    if (effectType.isCrowdControl()) isCrowdControl = true;
                    if (effectType.isSkillsNotAvailable()) isSkillsNotAvailable = true;
                    if (effectType.isInvincible()) isInvincible = true;
                    if (effectType.isSlow()) isSlow = true;
                    if (effectType.isBlind()) isBlind = true;
                    if (effectType.isFixed()) isFixed = (Location) effectData.getObject(0);
                    if (entity instanceof Player player) {
                        switch (effectType) {
                            case Indulgendia -> {
                                if (Math.floorMod(effectData.time, 20) == 0) {
                                    double health = effectData.getDouble(0);
                                    if (playerData(player).PvPMode) health /= Damage.PvPHealDecay;
                                    playerData.changeHealth(health);
                                }
                            }
                            case Brutality -> {
                                double mana = effectData.getDouble(0) / 20 * period;
                                playerData.changeMana(-mana);
                                if (playerData.Status.Mana < mana) {
                                    removeEffect(EffectType.Brutality);
                                    sendMessage(player, "§cマナ枯渇§aのため§e[" + effectType.Display + "]§aを§c無効化§aしました", SoundList.TICK);
                                }
                            }
                            case SubzeroShield -> {
                                if (!playerData.Equipment.isOffHandEquip(EquipmentCategory.Shield)) removeEffect(effectType);
                            }
                            case PsychicPressure -> {
                                double value = effectData.getDouble(0);
                                double radius = 5;
                                double angle = 90;
                                ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
                                Set<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, playerData.Skill.SkillProcess.Predicate(), false);
                                Damage.makeDamage(player, victims, DamageCause.MAT, "PsychicPressure", value, 1, 1);
                                ShapedParticle(new ParticleData(Particle.REVERSE_PORTAL), player.getLocation(), radius, angle, angle/2, 1, true);
                            }
                            case Sticky -> {
                                for (int i = 0; i < 3; i++) {
                                    spawnParticle(new ParticleData(Particle.ITEM_SLIME), player.getLocation());
                                }
                                if (effectData.stack == 15) {
                                    removeEffect(EffectType.Sticky);
                                    playerData.dead();
                                } else if (effectData.stack >= 12) {
                                    MultiThread.TaskRunSynchronized(() -> {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));
                                    });
                                } else if (effectData.stack >= 9) {
                                    MultiThread.TaskRunSynchronized(() -> {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false));
                                    });
                                } else if (effectData.stack >= 3) {
                                    MultiThread.TaskRunSynchronized(() -> {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, false, false));
                                    });
                                }
                                stickyCount++;
                                if (stickyCount >= 12) {
                                    stickyCount = 0;
                                    if (player.getLocation().getBlock().getType() == Material.WATER) {
                                        effectData.stack = Math.max(0, effectData.stack - 3);
                                    } else {
                                        effectData.stack = Math.max(0, effectData.stack - 1);
                                    }
                                }
                            }
                            case Dissolution -> {
                                int dissolutionStack = effectData.stack;
                                dissolutionTimer += 1;

                                if (dissolutionTimer == 4) {
                                    playerData.changeHealth(-(playerData.Status.MaxHealth * (0.01 * dissolutionStack)));
                                } else if (dissolutionTimer >= 12) {
                                    dissolutionTimer = 0;
                                    if (player.getLocation().getBlock().getType() != Material.WATER) {
                                        effectData.stack = Math.max(0, effectData.stack - 1);
                                    }
                                }
                            }
                        }
                    }
                    if (effectData.time <= 0 || effectData.stack < 1) {
                        removeEffect(effectType);
                    }
                }
            }
            if (entity != null) {
                MultiThread.TaskRunSynchronized(() -> {
                    if (!ownerType.isEnemy() || !enemyData.mobData.enemyType.isIgnoreCrowdControl()) {
                        if (isFixed != null) entity.teleportAsync(isFixed, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS);
                        if (isCrowdControl) {
                            entity.removePotionEffect(PotionEffectType.SLOWNESS);
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5, 255, false, false, false));
                        }
                    }
                    if (isSlow) {
                        entity.removePotionEffect(PotionEffectType.SLOWNESS);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5, 2, false, false));
                    }
                    if (isBlind) {
                        entity.removePotionEffect(PotionEffectType.BLINDNESS);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 0, false, false));
                    }
                }, "EffectManagerTimer");
                if (!ownerType.isEnemy() || !enemyData.mobData.enemyType.isIgnoreCrowdControl()) {
                    if (isCrowdControl) for (int i = 0; i < period; i++) {
                        if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) break;
                        entity.setVelocity(Function.VectorDown);
                        //MultiThread.sleepTick(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasEffect(EffectType effect) {
        return Effect.containsKey(effect);
    }

    public boolean addEffect(EffectType effectType) {
        return addEffect(effectType, 1);
    }

    public boolean addEffect(EffectType effectType, int time) {
        return addEffect(effectType, time, null, 1);
    }

    public boolean addEffect(EffectType effectType, int time, Location locationData) {
        return addEffect(effectType, time, new Object[]{locationData}, 1);
    }


    public boolean addEffect(EffectType effectType, int time, double doubleData) {
        return addEffect(effectType, time, new Object[]{doubleData}, 1);
    }

    public boolean addEffect(EffectType effectType, int time, int intData) {
        return addEffect(effectType, time, new Object[]{intData}, 1);
    }


    public boolean addEffect(EffectType effectType, int time, double doubleData, int stack) {
        return addEffect(effectType, time, new Object[]{doubleData}, stack);
    }

    public boolean addEffect(EffectType effectType, int time, Object[] objectData) {
        return addEffect(effectType, time, objectData, 1);
    }

    public boolean addEffect(EffectType effectType, int time, Object[] objectData, int stack) {
        EffectType effectType2 = EffectType.Stop;
        if (hasEffect(effectType2)) {
            if (entity instanceof Player player) sendMessage(player, "§e[" + effectType2.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.TICK);
            return false;
        }
        effectType2 = EffectType.PainBarrier;
        if (hasEffect(effectType2) && effectType == EffectType.Stun) {
            if (entity instanceof Player player) sendMessage(player, "§e[" + effectType2.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.TICK);
            return false;
        }
        effectType2 = EffectType.SubzeroShield;
        if (hasEffect(effectType2) && effectType == EffectType.Freeze) {
            if (entity instanceof Player player) sendMessage(player, "§e[" + effectType2.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.TICK);
            return false;
        }
        if (!effectType.Buff && effectType.effectRank.isNormal()) {
            effectType2 = EffectType.Indulgence;
            if (hasEffect(effectType2)) {
                Effect.get(effectType2).stack--;
                if (Effect.get(effectType2).stack < 1) Effect.remove(effectType2);
                if (entity instanceof Player player) sendMessage(player, "§e[" + effectType2.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.TICK);
                return false;
            }
            effectType2 = EffectType.Profesy;
            if (hasEffect(effectType2)) {
                if (entity instanceof Player player) sendMessage(player, "§e[" + effectType2.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.TICK);
                return false;
            }
            effectType2 = EffectType.BeakMask;
            if (hasEffect(effectType2) && effectType != EffectType.Stun && effectType != EffectType.Slow) {
                if (entity instanceof Player player) sendMessage(player, "§e[" + effectType2.Display + "]§aの効果より§c[" + effectType.Display + "]§aを無効化しました", SoundList.TICK);
                return false;
            }
        }
        EffectData effectData;
        if (Effect.containsKey(effectType)) {
            effectData = Effect.get(effectType);
            effectData.time = Math.max(effectData.time, time);
            effectData.stack += stack;
        } else {
            effectData = new EffectData(effectType, time);
            effectData.stack = stack;
        }
        effectData.stack = Math.min(effectData.stack, effectType.MaxStack);
        effectData.objectData = objectData;
        Effect.put(effectType, effectData);
        statusUpdate(effectType);
        return true;
    }

    public void removeEffect(EffectType effectType) {
        Effect.remove(effectType);
        statusUpdate(effectType);
    }

    public void removeEffect(EffectType effectType, Player player) {
        removeEffect(effectType);
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

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, Player player, double doubleData) {
        addEffect(entity, effectType, time, 1, player, new Object[]{doubleData});
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, Player player, int intData) {
        addEffect(entity, effectType, time, 1, player, new Object[]{intData});
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, Player player, Location locationData) {
        addEffect(entity, effectType, time, 1, player, new Object[]{locationData});
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, int stack, Player player) {
        addEffect(entity, effectType, time, stack, player, null);
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, Player player, Object[] objectData) {
        addEffect(entity, effectType, time, 1, player, objectData);
    }

    public static void addEffect(LivingEntity entity, EffectType effectType, int time, int stack, Player player, Object[] objectData) {
        EffectManager manager = getEffectManager(entity);
        if (manager != null) {
            boolean isSendMessage = !manager.hasEffect(effectType);
            if (manager.addEffect(effectType, time, objectData, stack)) {
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

    public static void addEffectMessage(Player player, LivingEntity entity, EffectType effectType, String suffix) {
        if (playerData(player).EffectLog) addEffectMessage(player, entity, effectType.Display, effectType.color(), suffix);
    }

    public static void addEffectMessage(Player player, LivingEntity entity, EffectType effectType) {
        if (playerData(player).EffectLog) addEffectMessage(player, entity, effectType.Display, effectType.color(), "");
    }

    public static void addEffectMessage(Player player, LivingEntity entity, String Display, String color) {
        addEffectMessage(player, entity, Display, color, "");
    }

    public static void addEffectMessage(Player player, LivingEntity entity, String Display, String color, String suffix) {
        if (player == null) return;
        if (playerData(player).EffectLog) player.sendMessage(color + EffectManager.getOwnerName(entity) + "§aに" + color + "[" + Display + "]§aを付与しました " + suffix);
        if (entity instanceof Player target && player != entity && !playerData(target).EffectLog) target.sendMessage(color + playerData(player).getNick() + "§aから" + color + "[" + Display + "]§aを付与されました " + suffix);
    }
}