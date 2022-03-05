package swordofmagic7.Mob;

import com.destroystokyo.paper.entity.Pathfinder;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import me.libraryaddict.disguise.disguisetypes.Disguise;
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
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectOwnerType;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.PlayerList;
import swordofmagic7.Quest.QuestData;
import swordofmagic7.Quest.QuestProcess;
import swordofmagic7.Quest.QuestReqContentKey;
import swordofmagic7.Skill.SkillProcess;
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

    public static final int AIRadius = 48;

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
    public double ClassExp;

    public final EnemySkillManager skillManager = new EnemySkillManager(this);
    public final EffectManager effectManager;
    public int HitCount = 0;

    public Location DefenseAI;

    public void updateEntity() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            entity = (LivingEntity) Bukkit.getEntity(uuid);
        });
    }

    private final Set<Player> Involved = new HashSet<>();
    private final HashMap<LivingEntity, Double> Priority = new HashMap<>();
    public Location SpawnLocation;
    public LivingEntity target;
    public Location overrideTargetLocation;
    public boolean isDead = false;

    public EnemyData(LivingEntity entity, MobData baseData, int level) {
        this.entity = entity;
        mobData = baseData;
        Level = level;
        effectManager = new EffectManager(entity, EffectOwnerType.Enemy);
        effectManager.enemyData = this;

        statusUpdate();

        String DisplayName = "§c§l《" + mobData.Display + " Lv" + Level + "》";
        entity.setCustomNameVisible(true);
        entity.setCustomName(DisplayName);
        entity.setMaxHealth(MaxHealth);
        entity.setHealth(entity.getMaxHealth());
        if (mobData.disguise != null) {
            Disguise disguise = mobData.disguise.clone();
            disguise.setEntity(entity);
            disguise.setDisguiseName(DisplayName);
            disguise.setDynamicName(true);
            disguise.setCustomDisguiseName(true);
            disguise.startDisguise();
        }
        Health = MaxHealth;
        entity.setNoDamageTicks(0);
        uuid = entity.getUniqueId();
    }

    public static double StatusMultiply(int level) {
        return Math.pow(0.74+(level/3f), 1.4);
    }

    public void statusUpdate() {
        double multiply = StatusMultiply(Level);
        MaxHealth = mobData.Health * multiply;
        ATK = mobData.ATK * multiply;
        DEF = mobData.DEF * multiply;
        ACC = mobData.ACC * multiply;
        EVA = mobData.EVA * multiply;
        CriticalRate = mobData.CriticalRate * multiply;
        CriticalResist = mobData.CriticalResist * multiply;
        Exp = mobData.Exp * multiply;
        ClassExp = mobData.Exp;

        if (effectManager.hasEffect(EffectType.Monstrance)) {
            EVA *= 1-getSkillData("Monstrance").ParameterValue(0)/100;
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
                Location targetLocation = null;
                if (overrideTargetLocation != null) {
                    targetLocation = overrideTargetLocation;
                } else if (target != null) {
                    targetLocation = target.getEyeLocation();
                }
                if (targetLocation != null) {
                    Vector vector = targetLocation.toVector().subtract(entity.getLocation().toVector());
                    mob.lookAt(targetLocation);
                    Pathfinder pathfinder = mob.getPathfinder();
                    pathfinder.moveTo(targetLocation, mobData.Mov);
                    /*
                    if (LastLocation.distance(entity.getLocation()) < 0.5 && targetLocation.distance(entity.getLocation()) > mobData.Reach) {
                        entity.setVelocity(vector.normalize().multiply(0.5).setY(0.5));
                    }
                     */
                }

                LastLocation = entity.getLocation();
            }, 0, 10);
            runAITask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                double topPriority = 0;
                for (Map.Entry<LivingEntity, Double> priority : Priority.entrySet()) {
                    double Priority = priority.getValue();
                    LivingEntity entity = priority.getKey();
                    if (entity.getLocation().distance(entity.getLocation()) < AIRadius) {
                        if (priority.getKey() instanceof Player player) {
                            if (player.isOnline()) {
                                if (playerData(player).EffectManager.hasEffect(EffectType.Teleportation)) {
                                    Priority = 0;
                                    this.Priority.put(priority.getKey(), 0d);
                                } else if (playerData(player).EffectManager.hasEffect(EffectType.Covert)) {
                                    Priority = 0;
                                    if (target == player) target = null;
                                }
                            } else {
                                this.Priority.remove(entity);
                            }
                        }
                    } else {
                        this.Priority.remove(entity);
                    }
                    if (topPriority < Priority) {
                        target = priority.getKey();
                        topPriority = Priority;
                    }
                }

                if (target == null && mobData.Hostile) {
                    List<LivingEntity> Targets = SkillProcess.Nearest(entity.getLocation(), PlayerList.getNearLivingEntity(entity.getLocation(), AIRadius));
                    if (Targets.size() > 0) Priority.put(Targets.get(0), 1d);
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

                if (DefenseAI != null) {
                    runAITask.cancel();
                    runPathfinderTask.cancel();
                    runPathfinderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        mob.lookAt(DefenseAI);
                        Pathfinder pathfinder = mob.getPathfinder();
                        pathfinder.moveTo(DefenseAI, mobData.Mov);
                        LastLocation = entity.getLocation();
                    }, 0, 10);
                }
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

    public static int decayExp(int exp, int playerLevel, int mobLevel) {
        if (playerLevel < mobLevel + 15) {
            return exp;
        } else if (playerLevel > mobLevel + 15 && mobLevel + 30 > playerLevel) {
            double decay = ((mobLevel + 30) - playerLevel)/15f;
            return (int) Math.round(exp * decay);
        } else {
            return 1;
        }
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
        int classExp = (int) Math.floor(ClassExp);
        Involved.addAll(PlayerList.getNear(entity.getLocation(), 32));
        List<DropItemData> DropItemTable = new ArrayList<>(mobData.DropItemTable);
        DropItemTable.add(new DropItemData(getItemParameter("生命の雫"), 0.0001));
        DropItemTable.add(new DropItemData(getItemParameter("強化石"), 0.03));
        for (Player player : Involved) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                Classes classes = playerData.Classes;
                List<ClassData> classList = new ArrayList<>();
                for (ClassData classData : classes.classSlot) {
                    if (classData != null) {
                        classList.add(classData);
                    }
                }
                playerData.addPlayerExp(decayExp(exp, playerData.Level, Level));
                for (ClassData classData : classList) {
                    classes.addClassExp(classData, classExp);
                }
                for (PetParameter pet : playerData.PetSummon) {
                    pet.addExp(decayExp(exp, pet.Level, Level));
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
                                player.sendMessage("§b[+]§e" + runeParameter.Display + " §e[レベル:" + Level + "] [品質:" + String.format(playerData.ViewFormat(), runeParameter.Quality*100) + "%]");
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
                if (playerData.Skill.hasSkill("Pleasure") && getPetList().containsKey(mobData.Id)) {
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
