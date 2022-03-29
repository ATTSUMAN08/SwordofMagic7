package swordofmagic7.Mob;

import com.destroystokyo.paper.entity.Pathfinder;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectOwnerType;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.PlayerList;
import swordofmagic7.Quest.QuestData;
import swordofmagic7.Quest.QuestProcess;
import swordofmagic7.Quest.QuestReqContentKey;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.*;

public class EnemyData {
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
        MultiThread.TaskRunSynchronized(() -> {
            entity = (LivingEntity) Bukkit.getEntity(uuid);
        }, "UpdateEntity: " + uuid);
    }

    private final Set<Player> Involved = new HashSet<>();
    private final HashMap<LivingEntity, Double> Priority = new HashMap<>();
    public Location SpawnLocation;
    public LivingEntity target;
    public Location overrideTargetLocation;
    private boolean isDead = false;

    public boolean isDead() {
        return isDead || entity == null;
    }

    public boolean isAlive() {
        return !isDead && entity != null;
    }

    public EnemyData(LivingEntity entity, MobData baseData, int level) {
        this.entity = entity;
        mobData = baseData;
        Level = level;
        effectManager = new EffectManager(entity, EffectOwnerType.Enemy, this);

        statusUpdate();

        String DisplayName = getDecoDisplay();
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

    public String getDecoDisplay() {
        return "§c§l《" + mobData.Display + " Lv" + Level + "》";
    }

    private final java.util.function.Predicate<LivingEntity> Predicate = entity -> entity.getType() == EntityType.PLAYER;

    private boolean runAITask = true;

    void stopAI() {
        runAITask = false;
    }

    public boolean isRunnableAI() {
        return runAITask && isAlive() && plugin.isEnabled();
    }

    public Location LastLocation;
    void runAI() {
        stopAI();
        SpawnLocation = entity.getLocation();
        LastLocation = SpawnLocation;
        if (entity instanceof Mob mob) {
            MultiThread.TaskRun(() ->{
                runAITask = true;
                if (DefenseAI != null) {
                    while (isRunnableAI()) {
                        mob.lookAt(DefenseAI);
                        Pathfinder pathfinder = mob.getPathfinder();
                        pathfinder.moveTo(DefenseAI, mobData.Mov);
                        LastLocation = entity.getLocation();
                        MultiThread.sleepTick(10);
                    }
                } else {
                    while (isRunnableAI()) {
                        Location targetLocation = null;
                        if (overrideTargetLocation != null) {
                            targetLocation = overrideTargetLocation;
                        } else if (target != null) {
                            targetLocation = target.getEyeLocation();
                        }
                        Location finalTargetLocation = targetLocation;
                        MultiThread.TaskRunSynchronized(() -> {
                            if (finalTargetLocation != null) {
                                mob.lookAt(finalTargetLocation);
                                Pathfinder pathfinder = mob.getPathfinder();
                                pathfinder.moveTo(finalTargetLocation, mobData.Mov);
                            }
                        }, "PathFindMove: " + uuid);

                        double topPriority = 0;
                        for (Map.Entry<LivingEntity, Double> priority : new HashMap<>(Priority).entrySet()) {
                            double Priority = priority.getValue();
                            LivingEntity priorityTarget = priority.getKey();
                            if (entity.getLocation().distance(priorityTarget.getLocation()) < mobData.Search) {
                                if (priorityTarget instanceof Player player) {
                                    if (Function.isAlive(player)) {
                                        PlayerData targetData = playerData(player);
                                        if (targetData.EffectManager.hasEffect(EffectType.Teleportation)) {
                                            Priority = 0;
                                            this.Priority.put(priority.getKey(), 0d);
                                        } else if (targetData.EffectManager.hasEffect(EffectType.Covert)) {
                                            Priority = 0;
                                            if (target == player) target = null;
                                        }
                                    } else {
                                        this.Priority.remove(priorityTarget);
                                    }
                                }
                            } else {
                                this.Priority.remove(priorityTarget);
                            }
                            if (topPriority < Priority) {
                                target = priorityTarget;
                                topPriority = Priority;
                            }
                        }

                        if (target == null && mobData.Hostile) {
                            List<LivingEntity> Targets = SkillProcess.Nearest(entity.getLocation(), PlayerList.getNearLivingEntity(entity.getLocation(), mobData.Search));
                            if (Targets.size() > 0) Priority.put(Targets.get(0), 1d);
                        }

                        if (target != null) {
                            if (target instanceof Player player && !Function.isAlive(player)) {
                                Priority.remove(target);
                                target = null;
                            } else {
                                skillManager.tickSkillTrigger();
                                final Location TargetLocation = target.getLocation();
                                final Location EntityLocation = entity.getLocation();
                                double x1 = EntityLocation.getX();
                                double y1 = EntityLocation.getY();
                                double z1 = EntityLocation.getZ();
                                double x2 = TargetLocation.getX();
                                double y2 = TargetLocation.getY();
                                double z2 = TargetLocation.getZ();
                                if (Math.abs(x1 - x2) <= mobData.Reach && Math.abs(z1 - z2) <= mobData.Reach && Math.abs(y1 - y2) < 16) {
                                    Damage.makeDamage(entity, target, DamageCause.ATK, "attack", 1, 1);
                                }
                            }
                        }
                        if (SpawnLocation.distance(entity.getLocation()) > mobData.Search) entity.teleportAsync(SpawnLocation);
                        MultiThread.sleepTick(10);
                    }
                }
            }, "EnemyAI: " + uuid);
        }
    }

    public void addPriority(LivingEntity entity, double addPriority) {
        Priority.put(entity, Priority.getOrDefault(entity, 0d) + addPriority);
    }

    public void resetPriority() {
        Priority.clear();
    }

    public void delete() {
        isDead = true;
        stopAI();
        MultiThread.TaskRunSynchronized(() -> {
            if (entity != null) {
                MobManager.getEnemyTable().remove(entity.getUniqueId());
                entity.setHealth(0);
                entity.remove();
            }
        }, "EnemyDelete: " + uuid);
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
        stopAI();
        if (entity != null) {
            MobManager.getEnemyTable().remove(entity.getUniqueId());
            ParticleManager.RandomVectorParticle(new ParticleData(Particle.FIREWORKS_SPARK, 0.22f), entity.getLocation(), 100);
            playSound(entity.getLocation(), SoundList.Death);
            Involved.addAll(PlayerList.getNear(entity.getLocation(), 32));

            MultiThread.TaskRunSynchronized(() -> {
                entity.setHealth(0);
                entity.remove();
            }, "EnemyDead: " + uuid);
        }

        int exp = (int) Math.floor(Exp);
        int classExp = (int) Math.floor(ClassExp);
        List<DropItemData> DropItemTable = new ArrayList<>(mobData.DropItemTable);
        DropItemTable.add(new DropItemData(getItemParameter("生命の雫"), 0.0001));
        DropItemTable.add(new DropItemData(getItemParameter("強化石"), 0.05));
        for (Player player : Involved) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                playerData.statistics.enemyKill(mobData.Id);
                Classes classes = playerData.Classes;
                List<ClassData> classList = new ArrayList<>();
                for (ClassData classData : classes.classSlot) {
                    if (classData != null && !classData.ProductionClass) {
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
                            if ((dropData.Percent <= 0.01 && mobData.enemyType.isBoss()) || (dropData.Percent <= 0.001 && mobData.enemyType.isNormal())) {
                                BroadCast(playerData.getNick() + "§aさんが§e[" + dropData.itemParameter.Display + "§ax" + amount + "§e]§aを§e獲得§aしました", SoundList.Tick);
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
                        Function.sendMessage(player, "§e[" + mobData.Id + "]§aを§b懐柔§aしました", SoundList.Tick);
                    }
                }
                Location loc = entity.getLocation().clone().add(0, 1 + Holo.size() * 0.25, 0);
                MultiThread.TaskRunSynchronized(() -> {
                    Hologram hologram = createHologram("DropHologram:" + UUID.randomUUID(), loc);
                    VisibilityManager visibilityManager = hologram.getVisibilityManager();
                    visibilityManager.setVisibleByDefault(false);
                    visibilityManager.showTo(player);
                    for (String holo : Holo) {
                        hologram.appendTextLine(holo);
                    }
                    MultiThread.TaskRunSynchronizedLater(hologram::delete, 50, "EnemyKillRewardHoloDelete: " + uuid + "/" + player.getName());
                    playerData.viewUpdate();
                }, "EnemyKillRewardHolo" + uuid);
            }
        }
    }
}
