package swordofmagic7.Mob;

import com.destroystokyo.paper.entity.Pathfinder;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Client;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.*;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetData;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.PlayerList;
import swordofmagic7.Quest.QuestData;
import swordofmagic7.Quest.QuestProcess;
import swordofmagic7.Quest.QuestReqContentKey;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.StatusParameter;
import swordofmagic7.TextView.TextView;

import java.util.*;

import static swordofmagic7.Classes.Classes.ReqExp;
import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.SomCore.*;
import static swordofmagic7.Sound.CustomSound.playSound;

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
    public double MovementMultiply = 1;

    public HashMap<StatusParameter, Double> MultiplyStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> FixedStatus = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseMultiply = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseResistance = new HashMap<>();

    public final EnemySkillManager skillManager;
    public final EffectManager effectManager;
    public int HitCount = 0;

    private final Set<Player> Involved = new HashSet<>();
    private final HashMap<LivingEntity, Double> Priority = new HashMap<>();
    public Location SpawnLocation;
    public LivingEntity target;
    public Location overrideTargetLocation;
    public Location nonTargetLocation;
    public boolean isDefenseBattle = false;
    private boolean isDead = false;

    public boolean isDead() {
        return isDead || entity == null;
    }

    public boolean isAlive() {
        return !isDead();
    }

    public EnemyData(LivingEntity entity, MobData baseData, int level) {
        this.entity = entity;
        mobData = baseData;
        Level = level;
        effectManager = new EffectManager(entity, EffectOwnerType.Enemy, this);
        skillManager = new EnemySkillManager(this);

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
        double value = Math.pow(0.74+(level/3f), 1.45);
        for (int i = 0; i < Math.ceil((level-50)/10f); i++) {
            value = Math.pow(value, 1.03);
        }
        return value;
    }
    public static double StatusMultiply2(int level) {
        return level >= 30 ? Math.pow(StatusMultiply(level), 1.1) : StatusMultiply(level);
    }
    public static double StatusMultiply3(int level) {
        return level >= 30 ? Math.pow(StatusMultiply(level), 1.04) : StatusMultiply(level);
    }

    public String viewHealthString() {
        return String.format("%.0f", Health) + " (" + String.format("%.2f", Health / MaxHealth *100) + "%)";
    }

    public void statusUpdate() {
        double multiply = StatusMultiply(Level);
        double multiply2 = StatusMultiply2(Level);
        double multiply3 = StatusMultiply3(Level);

        HashMap<StatusParameter, Double> baseMultiplyStatusRev = new HashMap<>();
        HashMap<StatusParameter, Double> multiplyStatusRev = new HashMap<>();
        for (StatusParameter param : StatusParameter.values()) {
            MultiplyStatus.put(param, 1d);
            FixedStatus.put(param, 0d);
            multiplyStatusRev.put(param, 1d);
            baseMultiplyStatusRev.put(param, 1d);
        }
        for (DamageCause cause : DamageCause.values()) {
            DamageCauseMultiply.put(cause, 1d);
            DamageCauseResistance.put(cause, 1d);
        }
        if (effectManager.Effect.size() > 0) {
            for (Map.Entry<EffectType, EffectData> data : effectManager.Effect.entrySet()) {
                EffectType effectType = data.getKey();
                EffectData effectData = data.getValue();
                for (StatusParameter param : StatusParameter.values()) {
                    double multiplyStatusAdd = EffectDataBase.EffectStatus(effectType).MultiplyStatus.getOrDefault(param, 0d) * effectData.stack;
                    double baseMultiplyStatusAdd = EffectDataBase.EffectStatus(effectType).BaseMultiplyStatus.getOrDefault(param, 0d) * effectData.stack;
                    if (multiplyStatusAdd >= 0) MultiplyStatus.merge(param, multiplyStatusAdd, Double::sum);
                    else {
                        multiplyStatusRev.put(param, multiplyStatusRev.get(param) * Math.pow(1 + multiplyStatusAdd, effectData.stack));
                    }
                    if (baseMultiplyStatusAdd >= 0) MultiplyStatus.merge(param, baseMultiplyStatusAdd, Double::sum);
                    else {
                        baseMultiplyStatusRev.put(param, baseMultiplyStatusRev.get(param) * Math.pow(1 + baseMultiplyStatusAdd, effectData.stack));
                    }
                }
                for (DamageCause cause : DamageCause.values()) {
                    DamageCauseMultiply.merge(cause, EffectDataBase.EffectStatus(effectType).DamageCauseMultiply.getOrDefault(cause, 0d) * effectData.stack, Double::sum);
                    DamageCauseResistance.merge(cause, EffectDataBase.EffectStatus(effectType).DamageCauseResistance.getOrDefault(cause, 0d) * effectData.stack, Double::sum);
                }
            }
            for (StatusParameter param : StatusParameter.values()) {
                MultiplyStatus.put(param, MultiplyStatus.get(param) * multiplyStatusRev.get(param));
                MultiplyStatus.put(param, MultiplyStatus.get(param) * baseMultiplyStatusRev.get(param));
            }
        }

        MaxHealth = mobData.Health * multiply2 * statusMultiply(StatusParameter.MaxHealth);
        ATK = mobData.ATK * Math.pow(multiply, 1.11) * statusMultiply(StatusParameter.ATK);
        DEF = mobData.DEF * multiply3 * statusMultiply(StatusParameter.DEF);
        ACC = mobData.ACC * multiply * statusMultiply(StatusParameter.ACC);
        EVA = mobData.EVA * multiply * statusMultiply(StatusParameter.EVA);
        CriticalRate = mobData.CriticalRate * multiply * statusMultiply(StatusParameter.CriticalRate);
        CriticalResist = mobData.CriticalResist * multiply3 * statusMultiply(StatusParameter.CriticalResist);
        Exp = mobData.Exp * multiply;
        ClassExp = mobData.Exp;
    }

    public static List<String> enemyLore(MobData mobData, int Level) {
        double multiply = StatusMultiply(Level);
        double multiply2 = StatusMultiply2(Level);
        double multiply3 = StatusMultiply3(Level);
        String format = "%.0f";
        List<String> lore = new ArrayList<>();
        lore.add(decoText("§3§lステータス"));
        lore.add(decoLore("体力") + String.format(format, mobData.Health*multiply2));
        lore.add(decoLore("攻撃力") + String.format(format, mobData.ATK * multiply));
        lore.add(decoLore("防御力") + String.format(format, mobData.DEF * multiply3));
        lore.add(decoLore("命中") + String.format(format, mobData.ACC * multiply));
        lore.add(decoLore("回避") + String.format(format, mobData.EVA * multiply));
        lore.add(decoLore("クリティカル発生") + String.format(format, mobData.CriticalRate * multiply));
        lore.add(decoLore("クリティカル耐性") + String.format(format, mobData.CriticalResist * multiply3));
        lore.add(decoLore("キャラ経験値") + String.format(format, mobData.Exp * multiply));
        lore.add(decoLore("クラス経験値") + String.format(format, mobData.Exp));
        return lore;
    }

    private double statusMultiply(StatusParameter status) {
        return MultiplyStatus.getOrDefault(status, 1d);
    }

    public String getDecoDisplay() {
        return (mobData.Hostile ? "§c§l" : "§6§l") + "《" + mobData.Display + " Lv" + Level + "》";
    }

    private final java.util.function.Predicate<LivingEntity> Predicate = entity -> entity.getType() == EntityType.PLAYER;

    private boolean runAITask = true;

    void stopAI() {
        runAITask = false;
        if (asyncAITask != null) asyncAITask.cancel();
    }

    public boolean isRunnableAI() {
        return runAITask && isAlive() && plugin.isEnabled();
    }

    public Location LastLocation;
    private Location NextLocation;
    public BukkitTask asyncAITask;
    void runAI() {
        stopAI();
        SpawnLocation = entity.getLocation();
        LastLocation = SpawnLocation;
        if (entity instanceof Mob mob) {
            Pathfinder pathfinder = mob.getPathfinder();
            runAITask = true;
            asyncAITask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isRunnableAI()) this.cancel();

                    Location targetLocation = null;
                    if (overrideTargetLocation != null) {
                        targetLocation = overrideTargetLocation;
                    } else if (target != null) {
                        targetLocation = target.getLocation();
                    } else if (nonTargetLocation != null) {
                        targetLocation = nonTargetLocation;
                    }
                    if (targetLocation != null) {
                        NextLocation = targetLocation;
                        MultiThread.TaskRunSynchronized(() -> {
                            if (NextLocation != null && entity.getLocation().distance(NextLocation) > mobData.Reach) {
                                mob.lookAt(NextLocation);
                                if (target != null) mob.setTarget(target);
                                pathfinder.moveTo(NextLocation, mobData.Mov*MovementMultiply);
                                LastLocation = entity.getLocation();
                            }
                        });
                    }

                    Priority.entrySet().removeIf(entry -> (
                            entry.getKey() instanceof Player player && !Function.isAlive(player))
                            || entry.getKey().getLocation().distance(entity.getLocation()) > mobData.Search
                            || entry.getKey().isDead());
                    if (effectManager.hasEffect(EffectType.Capote)) {
                        target = (LivingEntity) effectManager.getData(EffectType.Capote).getObject(0);
                    } else {
                        double topPriority = 0;
                        for (Map.Entry<LivingEntity, Double> priority : Priority.entrySet()) {
                            double priorityValue = priority.getValue();
                            LivingEntity priorityTarget = priority.getKey();
                            if (priorityTarget instanceof Player player) {
                                PlayerData targetData = playerData(player);
                                if (targetData.EffectManager.hasEffect(EffectType.Teleportation)) {
                                    priorityValue = 0;
                                    Priority.put(priority.getKey(), 0d);
                                }
                                if (targetData.EffectManager.hasEffect(EffectType.Covert)) {
                                    priorityValue = 0;
                                    if (target == player) target = null;
                                }
                                if (targetData.EffectManager.hasEffect(EffectType.HatePriority)) {
                                    target = player;
                                    break;
                                }
                            }
                            if (topPriority < priorityValue) {
                                target = priorityTarget;
                                topPriority = priorityValue;
                            }
                        }
                    }

                    if (target == null && mobData.Hostile) {
                        Set<Player> Targets = PlayerList.getNear(entity.getLocation(), mobData.Search);
                        int topLevel = 0;
                        LivingEntity target = null;
                        for (Player player : Targets) {
                            PlayerData playerData = playerData(player);
                            if (playerData.Level > topLevel) {
                                topLevel = playerData.Level;
                                target = player;
                            }
                        }
                        if (target != null) Priority.put(target, 1d);
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
                    if (!isDefenseBattle && !mobData.enemyType.isBoss() && !mobData.NoAI && !mobData.Invisible) {
                        if (PlayerList.getNear(entity.getLocation(), 64).size() == 0 || SpawnLocation.distance(entity.getLocation()) > mobData.Search + 64) {
                            delete();
                        }
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0, 20);
        }
    }

    private final Map<LivingEntity, Double> TotalDamageTable = new HashMap<>();
    public void addPriority(LivingEntity entity, double addPriority) {
        Priority.put(entity, Priority.getOrDefault(entity, 0d) + addPriority);
        if (entity instanceof Player player) Involved.add(player);
    }
    public void addDamage(LivingEntity entity, double damage) {
        TotalDamageTable.put(entity, TotalDamageTable.getOrDefault(entity, 0d) + damage);
    }

    public void resetPriority() {
        Priority.clear();
    }

    public void delete() {
        isDead = true;
        stopAI();
        if (entity != null) MobManager.EnemyTable.remove(entity.getUniqueId().toString());
        MultiThread.TaskRunSynchronized(() -> {
            entity.setHealth(0);
            entity.remove();
        }, "EnemyDelete");
    }

    public static int decayExp(int exp, int playerLevel, int mobLevel) {
        if (playerLevel < mobLevel + 20) {
            return exp;
        } else if (playerLevel > mobLevel + 20 && mobLevel + 40 < playerLevel) {
            double decay = ((mobLevel + 40) - playerLevel)/20f;
            return (int) Math.max(Math.round(exp * decay), 1);
        } else {
            return 1;
        }
    }

    public synchronized void dead() {
        if (isDead) return;
        isDead = true;
        MultiThread.TaskRun(() -> {
            if (entity != null) {
                ParticleManager.RandomVectorParticle(new ParticleData(Particle.FIREWORKS_SPARK, 0.22f), entity.getLocation(), 50);
                playSound(entity.getLocation(), SoundList.Death);
                Involved.addAll(PlayerList.getNear(entity.getLocation(), 32));
            }
            delete();

            int exp = (int) Math.floor(Exp);
            int classExp = (int) Math.floor(ClassExp);
            List<DropItemData> DropItemTable = new ArrayList<>(mobData.DropItemTable);
            DropItemTable.add(new DropItemData(getItemParameter("生命の雫"), 0.0001));
            DropItemTable.add(new DropItemData(getItemParameter("強化石"), 0.05));

            if (mobData.DamageRanking) {
                List<Map.Entry<LivingEntity, Double>> DamageTable = new ArrayList<>(TotalDamageTable.entrySet());
                List<Map.Entry<LivingEntity, Double>> PriorityTable = new ArrayList<>(Priority.entrySet());
                DamageTable.sort((obj1, obj2) -> obj2.getValue().compareTo(obj1.getValue()));
                PriorityTable.sort((obj1, obj2) -> obj2.getValue().compareTo(obj1.getValue()));
                List<String> message = new ArrayList<>();
                message.add(decoText("ダメージランキング"));
                String name = PriorityTable.get(0).getKey().getName();
                if (PriorityTable.get(0).getKey() instanceof Player player) name = PlayerData.playerData(player).getNick();
                message.add("§7・§eTOPヘイト値§7: §e" + name + " §b-> §c" + String.format("%.0f", PriorityTable.get(0).getValue()));
                int i = 1;
                for (Map.Entry<LivingEntity, Double> entry : DamageTable) {
                    name = entry.getKey().getName();
                    if (entry.getKey() instanceof Player player) name = PlayerData.playerData(player).getNick();
                    message.add("§7・§e" + i + "位§7: §e" + name + " §b-> §c" + String.format("%.0f", entry.getValue()));
                    if (i >= 5) break;
                    i++;
                }
                for (Player player : PlayerList.getNear(entity.getLocation(), 32)) {
                    if (player.isOnline()) {
                        List<String> perMessage = new ArrayList<>(message);
                        perMessage.add(decoText("個人戦績"));
                        perMessage.add(decoLore("ダメージ") + "§c" + String.format("%.0f", TotalDamageTable.get(player)));
                        perMessage.add(decoLore("ヘイト値") + "§c" + String.format("%.0f", Priority.get(player)));
                        sendMessage(player, perMessage, SoundList.Tick);
                    }
                }
            }
            for (Player player : Involved) {
                if (player.isOnline()) {
                    PlayerData playerData = playerData(player);
                    if (!isEventServer() || !playerData.isAFK()) {
                        double percentMultiply = 1; //playerData.isAFK() ? 0.3 : 1;
                        playerData.statistics.enemyKill(mobData);
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
                        Holo.add("§e§lEXP §a§l+" + exp + " §7(" + String.format(format, (double) exp / ReqExp(Level) * 100) + "%)");
                        if (!isDefenseBattle) {
                            for (DropItemData dropData : DropItemTable) {
                                if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                                    if (random.nextDouble() <= dropData.Percent * percentMultiply) {
                                        int amount;
                                        if (dropData.MaxAmount != dropData.MinAmount) {
                                            amount = random.nextInt(dropData.MaxAmount - dropData.MinAmount) + dropData.MinAmount;
                                        } else {
                                            amount = dropData.MinAmount;
                                        }
                                        playerData.ItemInventory.addItemParameter(dropData.itemParameter.clone(), amount);
                                        Holo.add("§b§l[+]§e§l" + dropData.itemParameter.Display + "§a§lx" + amount);
                                        if (playerData.DropLog.isItem() || (dropData.Percent <= 0.05 && playerData.DropLog.isRare()))
                                            ItemGetLog(player, dropData.itemParameter, amount);
                                        if ((dropData.Percent <= 0.01 && mobData.enemyType.isBoss()) || (dropData.Percent <= 0.001 && mobData.enemyType.isNormal())) {
                                            TextView text = new TextView(playerData.getNick() + "§aさんが");
                                            text.addView(dropData.itemParameter.getTextView(amount, playerData.ViewFormat()));
                                            text.addText("§aを§e獲得§aしました");
                                            text.setSound(SoundList.Tick);
                                            Client.sendDisplay(player, text);
                                        }
                                    }
                                }
                            }
                            for (DropRuneData dropData : mobData.DropRuneTable) {
                                if ((dropData.MinLevel == 0 && dropData.MaxLevel == 0) || (dropData.MinLevel <= Level && Level <= dropData.MaxLevel)) {
                                    if (random.nextDouble() <= dropData.Percent * percentMultiply) {
                                        RuneParameter runeParameter = dropData.runeParameter.clone();
                                        if (runeParameter.isSpecial) {
                                            runeParameter.Level = 1;
                                            runeParameter.Quality = 1;
                                        } else {
                                            runeParameter.Level = Level;
                                            runeParameter.Quality = random.nextDouble();
                                        }
                                        if (!mobData.enemyType.isBoss() && (playerData.RuneQualityFilter > runeParameter.Quality || playerData.RuneIdFilter.contains(runeParameter.Id))) {
                                            playerData.RuneShop.addRuneCrashed(runeParameter);
                                            playerData.ItemInventory.addItemParameter(playerData.RuneShop.RunePowder, 1);
                                            Holo.add("§b§l[+]§e§l" + playerData.RuneShop.RunePowder.Display);
                                            if (playerData.DropLog.isItem()) {
                                                ItemGetLog(player, playerData.RuneShop.RunePowder, 1);
                                            }
                                        } else {
                                            playerData.RuneInventory.addRuneParameter(runeParameter);
                                            Holo.add("§b§l[+]§e§l" + runeParameter.Display);
                                            if (playerData.DropLog.isRune() || (dropData.Percent <= 0.05 && playerData.DropLog.isRare())) {
                                                player.sendMessage("§b[+]§e" + runeParameter.Display + " §e[レベル:" + Level + "] [品質:" + String.format(playerData.ViewFormat(), runeParameter.Quality * 100) + "%]");
                                            }
                                            if (runeParameter.isSpecial) {
                                                TextView text = new TextView(playerData.getNick() + "§aさんが");
                                                text.addView(dropData.runeParameter.getTextView(playerData.ViewFormat()));
                                                text.addText("§aを§e獲得§aしました");
                                                text.setSound(SoundList.Tick);
                                                Client.sendDisplay(player, text);
                                            }
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
                                PetData petData = getPetData(mobData.Id);
                                if (petData.BossPet) {
                                    if (random.nextDouble() <= 0.0005 * percentMultiply) {
                                        PetParameter pet = new PetParameter(player, playerData, petData, Level, PlayerData.MaxLevel, 0, 2);
                                        playerData.PetInventory.addPetParameter(pet);
                                        TextView text = new TextView(playerData.getNick() + "§aさんが§e[" + mobData.Id + "]§aを§b懐柔§aしました");
                                        text.setSound(SoundList.Tick);
                                        Client.sendDisplay(player, text);
                                    }
                                } else {
                                    if (random.nextDouble() <= 0.01 * percentMultiply) {
                                        PetParameter pet = new PetParameter(player, playerData, petData, Level, Math.min(Level + 10, PlayerData.MaxLevel), 0, random.nextDouble() + 0.5);
                                        playerData.PetInventory.addPetParameter(pet);
                                        Function.sendMessage(player, "§e[" + mobData.Id + "]§aを§b懐柔§aしました", SoundList.Tick);
                                    }
                                }
                            }
                            if (!playerData.isAFK()) {
                                playerData.viewUpdate();
                                Location loc = entity.getLocation().clone().add(0, 1 + Holo.size() * 0.25, 0);
                                MultiThread.TaskRunSynchronized(() -> {
                                    Hologram hologram = createHologram(loc);
                                    VisibilityManager visibilityManager = hologram.getVisibilityManager();
                                    visibilityManager.setVisibleByDefault(false);
                                    visibilityManager.showTo(player);
                                    for (String holo : Holo) {
                                        hologram.appendTextLine(holo);
                                    }
                                    MultiThread.TaskRunSynchronizedLater(hologram::delete, 50, "EnemyKillRewardHoloDelete");
                                }, "EnemyKillRewardHolo");
                            }
                        }
                    }
                }
            }
        }, "EnemyDead");
    }
}
