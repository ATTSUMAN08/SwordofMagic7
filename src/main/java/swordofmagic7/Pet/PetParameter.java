package swordofmagic7.Pet;

import com.destroystokyo.paper.entity.Pathfinder;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.StatusParameter;
import swordofmagic7.System;

import java.util.*;

import static swordofmagic7.Data.DataBase.getPetData;
import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.BTTSet;

public class PetParameter implements Cloneable {
    public Player player;
    public PlayerData playerData;

    public UUID petUUID = UUID.randomUUID();
    public UUID uuid;
    public LivingEntity entity;
    public PetData petData;

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
    public EffectManager effectManager = new EffectManager(entity);

    public boolean Summoned = false;

    public LivingEntity target;
    private final Random random = new Random();

    PetParameter() {
    }

    ;

    public PetParameter(Player player, PlayerData playerData, PetData petData, int Level, int MaxLevel, int Exp, double GrowthRate) {
        this.player = player;
        this.playerData = playerData;
        this.petData = petData;
        this.Level = Level;
        this.MaxLevel = MaxLevel;
        this.Exp = Exp;
        this.GrowthRate = GrowthRate;
        updateStatus();
        Stamina = MaxStamina;
        Health = MaxHealth;
        Mana = MaxMana;
    }

    public void addExp(int add) {
        if (MaxLevel > Level) {
            Exp += add;
            boolean levelUp = false;
            while (ReqExp() <= Exp) {
                Exp -= ReqExp();
                Level++;
                updateStatus();
                levelUp = true;
                if (MaxLevel <= Level) Exp = 0;
            }
            if (levelUp)
                BroadCast(playerData.getNick() + "§aさんの§e[" + petData.Display + "§e]§aが§eLv" + Level + "§aになりました");
        }
    }

    int ReqExp() {
        double reqExp = playerData.Classes.ReqExp(Level, petData.Tier);
        reqExp *= GrowthRate;
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

    public void updateStatus() {
        for (StatusParameter param : StatusParameter.values()) {
            EquipmentStatus.put(param, 0d);
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
        if (playerData.Classes.getPassiveSkillList().contains(basicTamer)) {
            Multiply *= 1+basicTamer.Parameter.get(1).Value/100;
        }
        MaxStamina = petData.MaxStamina * (Level/50f + 0.98);
        MaxHealth = petData.MaxHealth * Multiply + EquipmentStatus(StatusParameter.MaxHealth);
        HealthRegen = petData.HealthRegen * (Multiply / 10 + 1) + EquipmentStatus(StatusParameter.HealthRegen);
        MaxMana = petData.MaxMana * Multiply + EquipmentStatus(StatusParameter.MaxMana);
        ManaRegen = petData.ManaRegen * (Multiply / 10 + 1) + EquipmentStatus(StatusParameter.ManaRegen);
        ATK = petData.ATK * Multiply + EquipmentStatus(StatusParameter.ATK);
        DEF = petData.DEF * Multiply + EquipmentStatus(StatusParameter.DEF);
        HLP = petData.HLP * Multiply + EquipmentStatus(StatusParameter.HLP);
        ACC = petData.ACC * Multiply + EquipmentStatus(StatusParameter.ACC);
        EVA = petData.EVA * Multiply + EquipmentStatus(StatusParameter.EVA);
        CriticalRate = petData.CriticalRate * Multiply + EquipmentStatus(StatusParameter.CriticalRate);
        CriticalResist = petData.CriticalResist * Multiply + EquipmentStatus(StatusParameter.CriticalResist);

        if (entity != null) {
            String DisplayName = "§e§l《" + petData.Display + "Lv" + Level + "》";
            entity.setCustomName(DisplayName);
        }
    }

    public void spawn() {
        if (playerData.PetSummon.size() == 0) {
            spawn(player.getLocation());
        } else if (Summoned) {
            cage();
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


    public void spawn(Location location) {
        target = null;
        if (Stamina / MaxStamina < 0.05) {
            player.sendMessage("§e[スタミナ]§aが§e[5%]§a未満のため召喚できません");
            playSound(player, SoundList.Nope);
            return;
        }
        entity = (LivingEntity) location.getWorld().spawnEntity(location, petData.entityType);
        uuid = entity.getUniqueId();
        String DisplayName = "§e§l《" + petData.Display + "Lv" + Level + "》";
        if (petData.disguise != null) {
            Disguise disguise = petData.disguise.clone();
            disguise.setEntity(entity);
            disguise.setDisguiseName(DisplayName);
            disguise.setDynamicName(true);
            disguise.setCustomDisguiseName(true);
            disguise.startDisguise();
        }

        entity.setCustomName(DisplayName);
        entity.setCustomNameVisible(true);

        Summoned = true;
        playerData.PetSummon.add(this);
        PetManager.PetSummonedList.put(entity.getUniqueId(), this);
        player.sendMessage("§e[" + petData.Display + "]§aを§b召喚§aしました");
        playSound(player, SoundList.Click);
        runAI();
    }

    public ItemStack viewPet(String format) {
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
        Lore.add(decoLore("レベル") + Level + "/" + MaxLevel);
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
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }


    private BukkitTask runAITask;
    private BukkitTask runPathfinderTask;

    void stopAI() {
        if (runAITask != null) runAITask.cancel();
        if (runPathfinderTask != null) runPathfinderTask.cancel();
    }

    public Location LastLocation;
    void runAI() {
        stopAI();
        LastLocation = entity.getLocation();
        if (entity instanceof Mob mob) {
            runPathfinderTask = Bukkit.getScheduler().runTaskTimer(System.plugin, () -> {
                Vector vector;
                Location location;
                double range;
                if (target != null && AIState.isAttack()) {
                    location = target.getLocation();
                    range = 2;
                } else {
                    location = player.getEyeLocation();
                    range = 4;
                }
                vector = location.toVector().subtract(entity.getLocation().toVector());
                mob.lookAt(location);
                Pathfinder pathfinder = mob.getPathfinder();
                pathfinder.moveTo(location, 1.5d);
                if (LastLocation.distance(entity.getLocation()) < 0.5 && location.distance(entity.getLocation()) > range) {
                    entity.setVelocity(vector.normalize().multiply(0.5).setY(0.5));
                }
                LastLocation = entity.getLocation();
            }, 0, 10);
            runAITask = Bukkit.getScheduler().runTaskTimer(System.plugin, () -> {
                if (entity.getLocation().distance(player.getLocation()) > 32) {
                    entity.teleportAsync(player.getLocation());
                }

                if (target == null && AIState.isAttack()) {
                    double radius = 24;
                    List<LivingEntity> targets = (List<LivingEntity>) player.getLocation().getNearbyLivingEntities(radius, radius / 2, playerData.Skill.SkillProcess.Predicate());
                    double distance = radius;
                    for (LivingEntity entity : targets) {
                        if (playerData.Skill.SkillProcess.isEnemy(entity) && entity.getLocation().distance(player.getLocation()) < distance) {
                            distance = entity.getLocation().distance(player.getLocation());
                            target = entity;
                        }
                    }
                }
                if (target != null) {
                    if ((target.getLocation().distance(entity.getLocation()) > 32)
                    || (MobManager.isEnemy(target) && MobManager.EnemyTable(target.getUniqueId()).isDead)
                    || (target instanceof Player player && player.getGameMode() != GameMode.SURVIVAL)
                    || target.isDead()) {
                        target = null;
                    } else if (target.getLocation().distance(entity.getLocation()) < 2) {
                        Damage.makeDamage(entity, target, DamageCause.ATK, "attack", 1, 1);
                    }
                }
            }, 0, 20);
            BTTSet(runPathfinderTask, "PetPathfinder");
            BTTSet(runAITask, "PetAI");
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

    public void cage() {
        stopAI();
        Bukkit.getScheduler().runTask(System.plugin, () -> {
            entity.remove();
            entity = null;
        });
        Summoned = false;
        player.sendMessage("§e[" + petData.Display + "]§aを§eケージ§aに戻しました");
        playerData.PetSummon.remove(this);
        PetManager.PetSummonedList.remove(entity.getUniqueId());
        playSound(entity.getLocation(), SoundList.Click);
    }

    public void dead() {
        stopAI();
        Bukkit.getScheduler().runTask(System.plugin, () -> {
            entity.remove();
            entity = null;
        });
        Summoned = false;
        Stamina = 0;
        player.sendMessage("§e[" + petData.Display + "]§aが§eケージ§aに戻りました");
        playerData.PetSummon.remove(this);
        PetManager.PetSummonedList.remove(entity.getUniqueId());
        playSound(entity.getLocation(), SoundList.Death);
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
