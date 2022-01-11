package swordofmagic7.Mob;

import com.destroystokyo.paper.entity.Pathfinder;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.PlayerList;
import swordofmagic7.Quest.QuestData;
import swordofmagic7.Quest.QuestProcess;
import swordofmagic7.Quest.QuestReqContentKey;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.System;

import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.BTTSet;

public class EnemyData {
    private final Plugin plugin = System.plugin;
    private final Random random = new Random();

    public UUID uuid;
    public LivingEntity entity;
    public MobData mobData;
    public int Level;
    public double MaxHealth;
    public double Health;
    public double ATK;
    public double DEF;
    public double ACC;
    public double EVA;
    public double CriticalRate;
    public double CriticalResist;
    public double Exp;

    public final EnemySkillManager skillManager = new EnemySkillManager(this);
    public final EffectManager effectManager;
    public int HitCount = 0;

    public void updateEntity() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            entity = (LivingEntity) Bukkit.getEntity(uuid);
        });
    }

    private final List<Player> Involved = new ArrayList<>();
    private final HashMap<LivingEntity, Double> Priority = new HashMap<>();
    Location SpawnLocation;
    LivingEntity target;
    public boolean isDead = false;

    public EnemyData(LivingEntity entity) {
        this.entity = entity;
        effectManager = new EffectManager(entity);
    }

    void Involved(Player player) {
        if (!Involved.contains(player)) {
            Involved.add(player);
        }
    }

    private final java.util.function.Predicate<LivingEntity> Predicate = entity -> entity.getType() == EntityType.PLAYER;

    private BukkitTask runAITask;
    private BukkitTask runPathfinderTask;

    void stopAI() {
        if (runAITask != null) runAITask.cancel();
        if (runPathfinderTask != null) runPathfinderTask.cancel();
    }

    public Location LastLocation;
    void runAI() {
        stopAI();
        SpawnLocation = entity.getLocation();
        LastLocation = SpawnLocation;
        if (entity instanceof Mob mob) {
            runPathfinderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (target != null) {
                    Vector vector = target.getEyeLocation().toVector().subtract(entity.getLocation().toVector());
                    mob.lookAt(target);
                    Pathfinder pathfinder = mob.getPathfinder();
                    pathfinder.moveTo(target, mobData.Mov);
                    if (LastLocation.distance(entity.getLocation()) < 0.5 && target.getLocation().distance(entity.getLocation()) > mobData.Reach) {
                        entity.setVelocity(vector.normalize().multiply(0.5).setY(0.5));
                    }
                }
                LastLocation = entity.getLocation();
            }, 0, 10);
            runAITask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                double topPriority = 0;
                for (Map.Entry<LivingEntity, Double> priority : Priority.entrySet()) {
                    if (topPriority < priority.getValue()) {
                        target = priority.getKey();
                        topPriority = priority.getValue();
                    }
                }

                if (target == null && mobData.Hostile) {
                    for (Player player : PlayerList.getNear(entity.getLocation(), 48)) {
                        target = player;
                        break;
                    }
                }

                if (target != null) {
                    if (target instanceof Player player && player.getGameMode() != GameMode.SURVIVAL) {
                        Priority.remove(target);
                        target = null;
                    } else {
                        skillManager.tickSkillTrigger();
                        final Location TargetLocation = target.getLocation();
                        final Location EntityLocation = entity.getLocation();
                        if (EntityLocation.distance(TargetLocation) <= mobData.Reach) {
                            Damage.makeDamage(entity, target, DamageCause.ATK, "attack", 1, 1);
                        }
                    }
                }

                if (SpawnLocation.distance(entity.getLocation()) > 64) entity.teleportAsync(SpawnLocation);
            }, 0, 20);
            BTTSet(runAITask, "EnemyAI:" + mobData.Id);
            BTTSet(runPathfinderTask, "EnemyPathfinder" + mobData.Id);
        }
    }

    public void addPriority(LivingEntity entity, double addPriority) {
        Priority.put(entity, Priority.getOrDefault(entity, 0d) + addPriority);
    }

    public void delete() {
        isDead = true;
        stopAI();
        MobManager.getEnemyTable().remove(entity.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            entity.setHealth(0);
            entity.remove();
        });
    }

    public void dead() {
        if (isDead) return;
        isDead = true;
        MobManager.getEnemyTable().remove(entity.getUniqueId());
        ParticleManager.RandomVectorParticle(new ParticleData(Particle.FIREWORKS_SPARK, 0.22f), entity.getLocation(), 100);
        playSound(entity.getLocation(), SoundList.Death);
        stopAI();

        Bukkit.getScheduler().runTask(plugin, () -> {
            entity.setHealth(0);
            entity.remove();
        });

        int exp = (int) Math.floor(Exp);
        for (Player player : PlayerList.getNear(entity.getLocation(), 32)) {
            Involved(player);
        }
        List<DropItemData> DropItemTable = new ArrayList<>(mobData.DropItemTable);
        DropItemTable.add(new DropItemData(getItemParameter("生命の雫"), 0.0001));
        DropItemTable.add(new DropItemData(getItemParameter("強化石"), 0.03));
        for (Player player : Involved) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                Classes classes = playerData.Classes;
                List<ClassData> classList = new ArrayList<>();
                for (ClassData classData : classes.classTier) {
                    if (classData != null) {
                        classList.add(classData);
                    } else break;
                }
                int expSplit = Math.round((float) exp / classList.size());
                for (ClassData classData : classList) {
                    classes.addExp(classData, expSplit);
                }
                for (PetParameter pet : playerData.PetSummon) {
                    pet.addExp(exp);
                }
                List<String> Holo = new ArrayList<>();
                Holo.add("§e§lEXP §a§l+" + exp);
                for (DropItemData dropData : DropItemTable) {
                    if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                        if (random.nextDouble() <= dropData.Percent) {
                            int amount;
                            if (dropData.MaxAmount != dropData.MinAmount) {
                                amount = random.nextInt(dropData.MaxAmount - dropData.MinAmount) + dropData.MinAmount;
                            } else {
                                amount = dropData.MinAmount;
                            }
                            playerData.ItemInventory.addItemParameter(dropData.itemParameter.clone(), amount);
                            Holo.add("§b§l[+]§e§l" + dropData.itemParameter.Display + "§a§lx" + amount);
                            if (playerData.DropLog.isItem()) {
                                player.sendMessage("§b[+]§e" + dropData.itemParameter.Display + "§ax" + amount);
                            }
                        }
                    }
                }
                for (DropRuneData dropData : mobData.DropRuneTable) {
                    if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                        if (random.nextDouble() <= dropData.Percent) {
                            RuneParameter runeParameter = dropData.runeParameter.clone();
                            runeParameter.Quality = random.nextDouble();
                            runeParameter.Level = Level;
                            playerData.RuneInventory.addRuneParameter(runeParameter);
                            Holo.add("§b§l[+]§e§l" + runeParameter.Display);
                            if (playerData.DropLog.isRune()) {
                                player.sendMessage("§b[+]§e" + runeParameter.Display);
                            }
                        }
                    }
                }
                for (Map.Entry<QuestData, QuestProcess> data : playerData.QuestManager.QuestList.entrySet()) {
                    QuestData questData = data.getKey();
                    if (questData.type.isEnemy()) {
                        for (Map.Entry<QuestReqContentKey, Integer> reqContent : questData.ReqContent.entrySet()) {
                            QuestReqContentKey key = reqContent.getKey();
                            if (key.mainKey.equalsIgnoreCase(mobData.Id) && Level >= key.intKey[0]) {
                                playerData.QuestManager.processQuest(questData, key, 1);
                            }
                        }
                    }
                }
                if (classes.classTier[1] == getClassData("Tamer") && getPetList().containsKey(mobData.Id)) {
                    if (random.nextDouble() <= 0.01) {
                        PetParameter pet = new PetParameter(player, playerData, getPetData(mobData.Id), Level, Level+30, 0, random.nextDouble()+0.5);
                        playerData.PetInventory.addPetParameter(pet);
                        BroadCast(playerData.getNick() + "§aさんが§e[" + mobData.Id + "]§aを§b懐柔§aしました");
                    }
                }
                Location loc = entity.getLocation().clone().add(0, 1 + Holo.size() * 0.25, 0);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Hologram hologram = HologramsAPI.createHologram(plugin, loc);
                    VisibilityManager visibilityManager = hologram.getVisibilityManager();
                    visibilityManager.setVisibleByDefault(false);
                    visibilityManager.showTo(player);
                    for (String holo : Holo) {
                        hologram.appendTextLine(holo);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, hologram::delete, 50);
                    playerData.viewUpdate();
                });
            }
        }
    }
}
