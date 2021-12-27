package swordofmagic7.Pet;

import com.destroystokyo.paper.entity.Pathfinder;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
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
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.StatusParameter;
import swordofmagic7.System;

import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetParameter implements Cloneable {
    public Player player;
    public PlayerData playerData;

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
    public double ACC;
    public double EVA;
    public double CriticalRate;
    public double CriticalResist;
    public PetAIState AIState = PetAIState.Follow;
    public List<RuneParameter> Rune = new ArrayList<>();
    public HashMap<StatusParameter, Double> RuneStatus = new HashMap<>();

    public boolean Summoned = false;

    List<RuneParameter> getRune() {
        return new ArrayList<>(Rune);
    }

    int getRuneSize() {
        return getRune().size();
    }

    RuneParameter getRune(int i) {
        return getRune().get(i);
    }

    void addRune(RuneParameter rune) {
        List<RuneParameter> List = getRune();
        List.add(rune);
        Rune = List;
    }

    void removeRune(int i) {
        List<RuneParameter> List = getRune();
        List.remove(i);
        Rune = List;
    }

    Entity target;
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
                BroadCast("§e" + playerData.Nick + "§aさんの§e[" + petData.Display + "§e]§aが§eLv" + Level + "§aになりました");
        }
    }

    int ReqExp() {
        double reqExp = playerData.Classes.ReqExp(Level, petData.Tier);
        reqExp *= GrowthRate;
        return (int) Math.round(reqExp);
    }

    private double StatusMultiply() {
        return Math.pow(0.8 + (Level / 5f), 1.2) * GrowthRate;
    }

    double RuneStatus(StatusParameter param) {
        RuneStatus.putIfAbsent(param, 0d);
        return RuneStatus.get(param);
    }

    void updateStatus() {
        for (StatusParameter param : StatusParameter.values()) {
            RuneStatus.put(param, 0d);
        }
        for (RuneParameter rune : Rune) {
            for (StatusParameter param : StatusParameter.values()) {
                RuneStatus.put(param, RuneStatus.get(param) + rune.Parameter.get(param));
            }
        }
        double Multiply = StatusMultiply();
        MaxStamina = petData.MaxStamina * (Multiply / 10 + 1);
        MaxHealth = petData.MaxHealth * Multiply + RuneStatus(StatusParameter.MaxHealth);
        HealthRegen = petData.HealthRegen * (Multiply / 10 + 1) + RuneStatus(StatusParameter.HealthRegen);
        MaxMana = petData.MaxMana * Multiply + RuneStatus(StatusParameter.MaxMana);
        ManaRegen = petData.ManaRegen * (Multiply / 10 + 1) + RuneStatus(StatusParameter.ManaRegen);
        ATK = petData.ATK * Multiply + RuneStatus(StatusParameter.ATK);
        DEF = petData.DEF * Multiply + RuneStatus(StatusParameter.DEF);
        ACC = petData.ACC * Multiply + RuneStatus(StatusParameter.ACC);
        EVA = petData.EVA * Multiply + RuneStatus(StatusParameter.EVA);
        CriticalRate = petData.CriticalRate * Multiply + RuneStatus(StatusParameter.CriticalRate);
        CriticalResist = petData.CriticalResist * Multiply + RuneStatus(StatusParameter.CriticalResist);

        if (entity != null) {
            String DisplayName = "§e§l《" + petData.Display + "Lv" + Level + "》";
            entity.setCustomName(DisplayName);
        }
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

    void runAI() {
        stopAI();
        if (entity instanceof Mob mob) {
            runPathfinderTask = Bukkit.getScheduler().runTaskTimer(System.plugin, () -> {
                Vector vector;
                Location location;
                if (target != null && AIState.isAttack()) {
                    vector = target.getLocation().toVector().subtract(entity.getLocation().toVector());
                    location = target.getLocation();
                    entity.getLocation().setDirection(vector);
                    Pathfinder pathfinder = mob.getPathfinder();
                    pathfinder.moveTo(location, 1.5d);

                } else {
                    vector = player.getLocation().toVector().subtract(entity.getLocation().toVector());
                    entity.getLocation().setDirection(vector);
                    if (entity.getLocation().distance(player.getLocation()) > 5) {
                        location = player.getLocation().add(player.getLocation().getDirection().setY(0).normalize());
                        Pathfinder pathfinder = mob.getPathfinder();
                        pathfinder.moveTo(location, 1.5d);
                    }
                }
            }, 0, 10);
            runAITask = Bukkit.getScheduler().runTaskTimer(System.plugin, () -> {
                if (entity.getLocation().distance(player.getLocation()) > 64) {
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
                    if (target.getLocation().distance(entity.getLocation()) < 2) {
                        Damage.makeDamage(entity, (LivingEntity) target, DamageCause.ATK, "attack", 1, 1);
                    }
                    if (target instanceof Player player) {
                        if (player.getGameMode() != GameMode.SURVIVAL) {
                            target = null;
                        }
                    } else if (MobManager.isEnemy(target)) {
                        if (MobManager.EnemyTable(target.getUniqueId()).isDead) {
                            target = null;
                        }
                    } else if (target.getLocation().distance(entity.getLocation()) > 32) {
                        target = null;
                    }
                }
            }, 0, 20);
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
            String[] split = data.split(",");
            if (DataBase.PetList.containsKey(split[0])) {
                petData = getPetData(split[0]);
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
                    if (str.contains("Rune:")) {
                        addRune(stringToRune(str.replace("Rune:", "")));
                    }
                }
                updateStatus();
            } else {
                Log("§cError NotFoundItemData: " + split[0]);
            }
        }
    }

    public String toString() {
        final String format = "%.1f";
        StringBuilder data = new StringBuilder(
                petData.Id + ",Level:" + Level + ",LevelMax:" + MaxLevel + ",Exp:" + Exp
                        + ",GrowthRate:" + String.format("%.4f", GrowthRate)
                        + ",Stamina:" + String.format(format, Stamina)
                        + ",Health:" + String.format(format, Health)
                        + ",Mana:" + String.format(format, Mana));
        for (RuneParameter runeParameter : getRune()) {
            if (!runeToString(runeParameter).equals("None"))
                data.append(",Rune:").append(runeToString(runeParameter));
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
