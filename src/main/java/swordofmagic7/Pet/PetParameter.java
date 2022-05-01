package swordofmagic7.Pet;

import com.destroystokyo.paper.entity.Pathfinder;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.*;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.StatusParameter;
import swordofmagic7.TextView.TextView;

import java.util.*;

import static swordofmagic7.Data.DataBase.getPetData;
import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Function.*;
import static swordofmagic7.SomCore.plugin;
import static swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetParameter implements Cloneable {
    public Player player;
    public PlayerData playerData;

    public UUID petUUID = UUID.randomUUID();
    public UUID uuid;
    public LivingEntity entity;
    public PetData petData;

    public String Name;
    public int MaxLevel;
    public int Level;
    public int Exp;
    public double GrowthRate;

    public double MaxStamina;
    public double Stamina;
    public double MaxHealth;
    public double HealthRegen;
    public double Health;
    public double MaxMana;
    public double ManaRegen;
    public double Mana;
    public double ATK;
    public double DEF;
    public double HLP;
    public double ACC;
    public double EVA;
    public double CriticalRate;
    public double CriticalResist;
    public PetAIState AIState = PetAIState.Follow;
    public ItemParameter[] Equipment = new ItemParameter[3];
    public HashMap<StatusParameter, Double> EquipmentStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> MultiplyStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> FixedStatus = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseMultiply = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseResistance = new HashMap<>();
    private EffectManager effectManager;

    public boolean Summoned = false;

    public LivingEntity target;

    PetParameter() {}

    ;

    public PetParameter(Player player, PlayerData playerData, PetData petData, int Level, int MaxLevel, int Exp, double GrowthRate) {
        this.player = player;
        this.playerData = playerData;
        this.petData = petData;
        this.Level = Level;
        this.MaxLevel = MaxLevel;
        this.Exp = Exp;
        this.GrowthRate = GrowthRate;
        this.Name = petData.Id;
        updateStatus();
        Stamina = MaxStamina;
        Health = MaxHealth;
        Mana = MaxMana;
    }

    public void addExp(int add) {
        if (MaxLevel > Level && PlayerData.MaxLevel > Level) {
            Exp += add;
            while (ReqExp() <= Exp) {
                Exp -= ReqExp();
                Level++;
                updateStatus();
                sendMessage(player,"§e[" + petData.Display + "§e]§aが§eLv" + Level + "§aになりました§b[" + getSummonId() + "]", SoundList.LevelUp);
                if (MaxLevel <= Level) Exp = 0;
            }

        }
    }

    int ReqExp() {
        double reqExp = Classes.ReqExp(Level);
        reqExp *= GrowthRate/2;
        if (petData.BossPet) reqExp *= 5;
        return (int) Math.round(reqExp);
    }

    private double StatusMultiply() {
        return Math.pow(0.95 + (Level / 20f), 1.2) * GrowthRate;
    }

    double EquipmentStatus(StatusParameter param) {
        if (!EquipmentStatus.containsKey(param)) {
            EquipmentStatus.put(param, 0d);
        }
        return EquipmentStatus.get(param);
    }

    double MultiplyStatus(StatusParameter param) {
        if (!MultiplyStatus.containsKey(param)) {
            MultiplyStatus.put(param, 0d);
        }
        return MultiplyStatus.get(param);
    }

    public int getSummonId() {
        int i = 1;
        for (PetParameter pet : playerData.PetSummon) {
            if (pet == this) return i;
            i++;
        }
        return -1;
    }

    void MultiplyStatusAdd(StatusParameter param, double add) {
        if (MultiplyStatus.containsKey(param)) {
            MultiplyStatus.put(param, MultiplyStatus.getOrDefault(param, 0d) + add);
        }
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public void updateStatus() {
        if (petData.BossPet) MaxLevel = PlayerData.MaxLevel;
        HashMap<StatusParameter, Double> baseMultiplyStatusRev = new HashMap<>();
        HashMap<StatusParameter, Double> multiplyStatusRev = new HashMap<>();
        boolean isNotDummy = !petData.Id.equals("訓練用ダミー");
        for (StatusParameter param : StatusParameter.values()) {
            MultiplyStatus.put(param, 1d);
            EquipmentStatus.put(param, 0d);
            FixedStatus.put(param, 0d);
            multiplyStatusRev.put(param, 1d);
            baseMultiplyStatusRev.put(param, 1d);
        }
        if (isNotDummy && playerData.Equipment.isMainHandEquip(EquipmentCategory.Baton)) {
            ItemParameter item = playerData.Equipment.getEquip(EquipmentSlot.MainHand);
            for (StatusParameter param : StatusParameter.values()) {
                EquipmentStatus.put(param, item.itemEquipmentData.Parameter(playerData.Level).get(param));
            }
        }
        for (DamageCause cause : DamageCause.values()) {
            DamageCauseMultiply.put(cause, 1d);
            DamageCauseResistance.put(cause, 1d);
        }
        if (effectManager != null && effectManager.Effect.size() > 0) {
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
        for (ItemParameter equipment : Equipment) {
            if (equipment != null) {
                for (StatusParameter param : StatusParameter.values()) {
                    EquipmentStatus.put(param, EquipmentStatus.getOrDefault(param, 0d) + equipment.itemEquipmentData.Parameter.getOrDefault(param, 0d));
                }
            }
        }
        double Multiply = StatusMultiply();
        SkillData basicTamer = getSkillData("BasicTamer");
        if (playerData.Skill.hasSkill("BasicTamer")) {
            Multiply *= 1+basicTamer.ParameterValue(1)/100;
        }
        if (isNotDummy && playerData.Equipment.isMainHandEquip(EquipmentCategory.Baton)) {
            Multiply *= (1+playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.Plus/100f);
        }
        MaxStamina = petData.MaxStamina * (Level/50f + 0.98);
        MaxHealth = (petData.MaxHealth * Multiply + EquipmentStatus(StatusParameter.MaxHealth)) * MultiplyStatus(StatusParameter.MaxHealth);
        HealthRegen = (petData.HealthRegen * (Multiply / 10 + 1) + EquipmentStatus(StatusParameter.HealthRegen)) * MultiplyStatus(StatusParameter.HealthRegen);
        MaxMana = (petData.MaxMana * Multiply + EquipmentStatus(StatusParameter.MaxMana)) * MultiplyStatus(StatusParameter.MaxMana);
        ManaRegen = (petData.ManaRegen * (Multiply / 10 + 1) + EquipmentStatus(StatusParameter.ManaRegen)) * MultiplyStatus(StatusParameter.ManaRegen);
        ATK = (petData.ATK * Multiply + EquipmentStatus(StatusParameter.ATK)) * MultiplyStatus(StatusParameter.ATK);
        DEF = (petData.DEF * Multiply + EquipmentStatus(StatusParameter.DEF)) * MultiplyStatus(StatusParameter.DEF);
        HLP = (petData.HLP * Multiply + EquipmentStatus(StatusParameter.HLP)) * MultiplyStatus(StatusParameter.HLP);
        ACC = (petData.ACC * Multiply + EquipmentStatus(StatusParameter.ACC)) * MultiplyStatus(StatusParameter.ACC) * 3;
        EVA = (petData.EVA * Multiply + EquipmentStatus(StatusParameter.EVA)) * MultiplyStatus(StatusParameter.EVA);
        CriticalRate = (petData.CriticalRate * Multiply + EquipmentStatus(StatusParameter.CriticalRate)) * MultiplyStatus(StatusParameter.CriticalRate);
        CriticalResist = (petData.CriticalResist * Multiply + EquipmentStatus(StatusParameter.CriticalResist)) * MultiplyStatus(StatusParameter.CriticalResist);

        if (entity != null) {
            entity.setCustomName(getDisplayName());
        }
    }

    public void spawn() {
        int maxSpawn = playerData.Skill.hasSkill("DualStar") ? 2 : 1;
        if (Summoned) {
            cage();
        } else if (playerData.PetSummon.size() < maxSpawn) {
            spawn(player.getLocation());
        } else {
            sendMessage(player, "§c召喚上限§aです", SoundList.Nope);
        }
        updateStatus();
    }

    public void changeStamina(int stamina) {
        Stamina = Math.min(MaxStamina, Math.max(Stamina+stamina, 0));
    }
    public void changeHealth(int health) {
        Health = Math.min(MaxHealth, Math.max(Health+health, 0));
    }
    public void changeMana(int mana) {
        Mana = Math.min(MaxMana, Math.max(Mana+mana, 0));
    }

    public String getDisplayName() {
        return "§b[" + getSummonId() + "]" + "§e" + petData.Display + "Lv" + Level;
    }

    private boolean spawnCooltime = false;

    public void spawn(Location location) {
        if (spawnCooltime) {
            sendMessage(player, "§a時間をおいてから§b召喚§aしてください", SoundList.Nope);
            return;
        }
        spawnCooltime = true;
        MultiThread.TaskRunLater(() -> {
            spawnCooltime = false;
        }, 40, "spawnCoolTime");
        MultiThread.TaskRunSynchronized(() -> {
            target = null;
            List<String> cancel = new ArrayList<>();
            if (playerData.Level < Level - 10) {
                cancel.add("§aレベルが足りないため召喚出来ません");
            }
            if (cancel.size() > 0) {
                for (String str : cancel) {
                    player.sendMessage(str);
                }
                playSound(player, SoundList.Nope);
                return;
            }
            if (Stamina / MaxStamina < 0.05) {
                player.sendMessage("§e[スタミナ]§aが§e[5%]§a未満のため召喚できません");
                playSound(player, SoundList.Nope);
                return;
            }
            entity = (LivingEntity) location.getWorld().spawnEntity(location, petData.entityType);
            if (effectManager == null) effectManager = new EffectManager(entity, EffectOwnerType.Pet, this);
            uuid = entity.getUniqueId();
            if (petData.disguise != null) {
                Disguise disguise = petData.disguise.clone();
                disguise.setEntity(entity);
                disguise.setDisguiseName(getDisplayName());
                disguise.setDynamicName(true);
                disguise.setCustomDisguiseName(true);
                disguise.startDisguise();
            }

            entity.setCustomName(getDisplayName());
            entity.setCustomNameVisible(true);

            Summoned = true;
            playerData.PetSummon.add(this);
            PetManager.PetSummonedList.put(entity.getUniqueId(), this);
            effectManager.entity = entity;
            player.sendMessage("§e[" + petData.Display + "]§aを§b召喚§aしました§b[" + getSummonId() + "]");
            playSound(player, SoundList.Click);
            for (PetParameter pet : playerData.PetSummon) {
                pet.updateStatus();
            }
            runAI();
        });
    }

    public ItemStack viewPet(String format) {
        if (petData.Icon == null) Log(petData.Id + " -> Icon Error");
        ItemStack item = new ItemStack(petData.Icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(petData.Display));
        List<String> Lore = new ArrayList<>();
        for (String str : petData.Lore) {
            Lore.add("§a§l" + str);
        }
        Lore.add(decoText("ペット情報"));
        if (Summoned) {
            Lore.add(decoLore("状態") + AIState.Display);
            meta.addEnchant(Enchantment.DURABILITY, 0, true);
        } else {
            Lore.add(decoLore("状態") + "ケージ内 [" + AIState.Display + "]");
        }
        Lore.add(decoLore("成長率") + String.format(format, GrowthRate * 100) + "%");
        Lore.add(decoLore("レベル") + Level + "/" + Math.min(PlayerData.MaxLevel, MaxLevel));
        Lore.add(decoLore("経験値") + Exp + "/" + ReqExp());
        Lore.add(decoText("ペットステータス"));
        Lore.add(decoLore("スタミナ") + String.format(format, Stamina) + "/" + String.format(format, MaxStamina));
        Lore.add(decoLore(StatusParameter.MaxHealth.Display) + String.format(format, Health) + "/" + String.format(format, MaxHealth));
        Lore.add(decoLore(StatusParameter.HealthRegen.Display) + String.format(format, HealthRegen));
        Lore.add(decoLore(StatusParameter.MaxMana.Display) + String.format(format, Mana) + "/" + String.format(format, MaxMana));
        Lore.add(decoLore(StatusParameter.ManaRegen.Display) + String.format(format, ManaRegen));
        Lore.add(decoLore(StatusParameter.ATK.Display) + String.format(format, ATK));
        Lore.add(decoLore(StatusParameter.DEF.Display) + String.format(format, DEF));
        Lore.add(decoLore(StatusParameter.ACC.Display) + String.format(format, ACC));
        Lore.add(decoLore(StatusParameter.EVA.Display) + String.format(format, EVA));
        Lore.add(decoLore(StatusParameter.CriticalRate.Display) + String.format(format, CriticalRate));
        Lore.add(decoLore(StatusParameter.CriticalResist.Display) + String.format(format, CriticalResist));
        if (effectManager != null && effectManager.Effect.size() > 0) {
            Lore.add(decoText("バフ・デバフ"));
            for (Map.Entry<EffectType, EffectData> effect : effectManager.Effect.entrySet()) {
                Lore.add(decoLore(effect.getKey().Display) + String.format(playerData.ViewFormat(), effect.getValue().time/20f) + "秒");
            }
        }
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        item.setAmount(Math.min(100, Level));
        return item;
    }

    public TextView getTextView(String format) {
        ItemStack item = viewPet(format);
        StringBuilder hoverText = new StringBuilder(item.getItemMeta().getDisplayName());
        for (String str : item.getLore()) {
            hoverText.append("\n").append(str);
        }
        return new TextView().addText("§e[" + petData.Display + "§bLv" + Level + "§e]").addHover(hoverText.toString()).reset();
    }

    private boolean runAITask;

    void stopAI() {
        runAITask = false;
        if (asyncAITask != null) asyncAITask.cancel();
    }

    public boolean isRunnableAI() {
        return runAITask && entity != null && plugin.isEnabled();
    }

    public BukkitTask asyncAITask;
    void runAI() {
        stopAI();
        if (entity instanceof Mob mob) {
            runAITask = true;
            final Pathfinder pathfinder = mob.getPathfinder();
            asyncAITask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isRunnableAI()) this.cancel();
                    MultiThread.TaskRunSynchronized(() -> {
                        Location location = target != null && AIState.isAttack() ? target.getLocation() : player.getLocation();
                        if (entity != null && location.distance(entity.getLocation()) > 1.5) {
                            mob.lookAt(location);
                            pathfinder.moveTo(location, 1.5d);
                        }
                    });
                    try {
                        if (target == null && AIState.isAttack()) {
                            double radius = 24;
                            List<LivingEntity> targets = SkillProcess.Nearest(entity.getLocation(), Function.NearLivingEntity(player.getLocation(), radius, playerData.Skill.SkillProcess.Predicate()));
                            if (targets.size() > 0) target = targets.get(0);
                        }
                        if (target != null) {
                            if (target.getLocation().distance(entity.getLocation()) > 32 || target.isDead()) {
                                target = null;
                            } else if (target.getLocation().distance(entity.getLocation()) < 2) {
                                if (target instanceof Player player) {
                                    PlayerData playerData = PlayerData.playerData(player);
                                    if (!playerData.PvPMode || playerData.isDead) {
                                        target = null;
                                        return;
                                    }
                                }
                                Damage.makeDamage(entity, target, DamageCause.ATK, "attack", 1, 1);
                            }
                        }
                        if (entity.getLocation().distance(player.getLocation()) > 48) {
                            entity.teleportAsync(player.getLocation());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        cage();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0, 20);
        }
    }

    public void DecreaseStamina(double decrease, double percent) {
        if (random.nextDouble() <= percent) {
            Stamina -= decrease;
            if (Stamina <= 0) {
                dead();
            }
        }
    }

    private void delete() {
        for (PetParameter pet : playerData.PetSummon) {
            pet.updateStatus();
        }
        MultiThread.TaskRunSynchronized(() -> {
            if (entity != null) {
                entity.remove();
                entity = null;
            }
        });
    }

    public void cage() {
        stopAI();
        Summoned = false;
        sendMessage(player, "§e[" + petData.Display + "]§aを§eケージ§aに戻しました§b[" + getSummonId() + "]", SoundList.Click);
        playerData.PetSummon.remove(this);
        PetManager.PetSummonedList.remove(entity.getUniqueId());
        delete();
    }

    public void dead() {
        stopAI();
        delete();
        Summoned = false;
        Stamina = 0;
        sendMessage(player, "§e[" + petData.Display + "]§aを§eケージ§aに戻りました§b[" + getSummonId() + "]", SoundList.Click);
        playerData.PetSummon.remove(this);
        PetManager.PetSummonedList.remove(entity.getUniqueId());
        delete();
    }

    public PetParameter(Player player, PlayerData playerData, String data) {
        this.player = player;
        this.playerData = playerData;
        if (!data.equals("None")) {
            String[] split = data.split(",pet");
            petUUID = UUID.fromString(split[0]);
            split[1] = split[1].replace("Id:", "");
            if (DataBase.PetList.containsKey(split[1])) {
                petData = getPetData(split[1]);
                for (String str : split) {
                    if (str.contains("Name:")) {
                        Name = str.replace("Name:", "");
                    }
                    if (str.contains("Level:")) {
                        Level = Integer.parseInt(str.replace("Level:", ""));
                    }
                    if (str.contains("LevelMax:")) {
                        MaxLevel = Integer.parseInt(str.replace("LevelMax:", ""));
                    }
                    if (str.contains("Exp:")) {
                        Exp = Integer.parseInt(str.replace("Exp:", ""));
                    }
                    if (str.contains("GrowthRate:")) {
                        GrowthRate = Double.parseDouble(str.replace("GrowthRate:", ""));
                    }
                    if (str.contains("Stamina:")) {
                        Stamina = Double.parseDouble(str.replace("Stamina:", ""));
                    }
                    if (str.contains("Health:")) {
                        Health = Double.parseDouble(str.replace("Health:", ""));
                    }
                    if (str.contains("Mana:")) {
                        Mana = Double.parseDouble(str.replace("Mana:", ""));
                    }
                    for (int i = 0; i < 3; i++) {
                        if (str.contains("Equipment" + i + ":")) {
                            Equipment[i] = ItemParameterStack.fromString(str.replace("Equipment" + i + ":", "")).itemParameter;
                        }
                    }
                }
                updateStatus();
            } else {
                Log("§cError NotFoundItemData: " + split[0], true);
            }
        }
    }

    @Override
    public String toString() {
        final String format = "%.1f";
        StringBuilder data = new StringBuilder(
                petUUID
                + ",petId:" + petData.Id
                + ",petId:" + Name
                + ",petLevel:" + Level
                + ",petLevelMax:" + MaxLevel
                + ",petExp:" + Exp
                + ",petGrowthRate:" + String.format("%.4f", GrowthRate)
                + ",petStamina:" + String.format(format, Stamina)
                + ",petHealth:" + String.format(format, Health)
                + ",petMana:" + String.format(format, Mana)
        );

        for (int i = 0; i < 3; i++) {
            if (Equipment[i] != null) {
                data.append(",petEquipment").append(i).append(":").append(new ItemParameterStack(Equipment[i]));
            }
        }
        return data.toString();
    }

    @Override
    public PetParameter clone() {
        try {
            PetParameter clone = (PetParameter) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
