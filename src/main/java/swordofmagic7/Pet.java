package swordofmagic7;

import com.destroystokyo.paper.entity.Pathfinder;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Predicate;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.MobManager.EnemyTable;
import static swordofmagic7.RayTrace.rayLocationEntity;
import static swordofmagic7.SoundList.Click;
import static swordofmagic7.SoundList.Nope;

enum PetSkillType {
    Attack,
    Support,
    Both,
}

enum PetAIState {
    Attack("攻撃"),
    Follow("追従"),
    Support("サポート"),
    ;

    String Display;

    PetAIState(String Display) {
        this.Display = Display;
    }

    boolean isAttack() {
        return this == Attack;
    }

    boolean isFollow() {
        return this == Follow;
    }
}

class PetData {
    String Id;
    EntityType entityType;
    MobDisguise disguise;

    String Display;
    List<String> Lore;
    Material Icon;
    int Tier;
    double MaxStamina;
    double MaxHealth;
    double HealthRegen;
    double MaxMana;
    double ManaRegen;
    double ATK;
    double DEF;
    double ACC;
    double EVA;
    double CriticalRate;
    double CriticalResist;
}

class PetParameter implements Cloneable{
    Player player;
    PlayerData playerData;

    UUID uuid;
    LivingEntity entity;
    PetData petData;

    int MaxLevel;
    int Level;
    int Exp;
    double GrowthRate;

    double MaxStamina;
    double Stamina;
    double MaxHealth;
    double HealthRegen;
    double Health;
    double MaxMana;
    double ManaRegen;
    double Mana;
    double ATK;
    double DEF;
    double ACC;
    double EVA;
    double CriticalRate;
    double CriticalResist;
    PetAIState AIState = PetAIState.Follow;
    HashMap<AttributeType, Integer> Attribute = new HashMap<>();
    List<RuneParameter> Rune = new ArrayList<>();
    HashMap<StatusParameter, Double> RuneStatus = new HashMap<>();

    boolean Summoned = false;

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

    PetParameter() {};

    PetParameter(Player player, PlayerData playerData, PetData petData, int Level, int MaxLevel, int Exp, double GrowthRate) {
        this.player = player;
        this.playerData = playerData;
        this.petData = petData;
        this.Level = Level;
        this.MaxLevel = MaxLevel;
        this.Exp = Exp;
        this.GrowthRate = GrowthRate;
        updateStatus();
    }

    void addExp(int add) {
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
            if (levelUp) BroadCast("§e" + playerData.Nick + "§aさんの§e[" + petData.Display + "§e]§aが§eLv" + Level + "§aになりました");
        }
    }

    int ReqExp() {
        double reqExp = playerData.Classes.ReqExp(Level, petData.Tier);
        reqExp *= GrowthRate;
        return (int) Math.round(reqExp);
    }

    private double StatusMultiply() {
        return Math.pow(0.8+(Level/5f), 1.2) * GrowthRate;
    }

    double RuneStatus(StatusParameter param) {
        RuneStatus.putIfAbsent(param, 0d);
        return RuneStatus.get(param);
    }

    void updateStatus() {
        for (AttributeType attr : AttributeType.values()) {
            Attribute.put(attr, 0);
        }
        for (StatusParameter param : StatusParameter.values()) {
            RuneStatus.put(param, 0d);
        }
        for (RuneParameter rune : Rune) {
            for (StatusParameter param : StatusParameter.values()) {
                RuneStatus.put(param, RuneStatus.get(param) + rune.Parameter.get(param));
            }
        }
        double Multiply = StatusMultiply();
        MaxStamina = petData.MaxStamina * (Multiply/10+1);
        MaxHealth = petData.MaxHealth * Multiply + RuneStatus(StatusParameter.MaxHealth);
        HealthRegen = petData.HealthRegen * (Multiply/10+1) + RuneStatus(StatusParameter.HealthRegen);
        MaxMana = petData.MaxMana * Multiply + RuneStatus(StatusParameter.MaxMana);
        ManaRegen = petData.ManaRegen * (Multiply/10+1) + RuneStatus(StatusParameter.ManaRegen);
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

    void spawn(Location location) {
        target = null;
        if (Stamina/MaxStamina < 0.05) {
            player.sendMessage("§e[スタミナ]§aが§e[5%]§a未満のため召喚できません");
            playSound(player, Nope);
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
        playSound(player, Click);
        runAI();
    }

    ItemStack viewPet(String format) {
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
        Lore.add(decoLore("成長率") + String.format(format, GrowthRate*100) + "%");
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
                    List<LivingEntity> targets = (List<LivingEntity>) player.getLocation().getNearbyLivingEntities(radius, radius/2, playerData.Skill.SkillProcess.Predicate());
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
                        if (EnemyTable(target.getUniqueId()).isDead) {
                            target = null;
                        }
                    } else if (target.getLocation().distance(entity.getLocation()) > 32) {
                        target = null;
                    }
                }
            }, 0, 20);
        }
    }

    void DecreaseStamina(double decrease, double percent) {
        if (random.nextDouble() <= percent) {
            Stamina -= decrease;
            if (Stamina <= 0) {
                dead();
            }
        }
    }

    void cage() {
        stopAI();
        Bukkit.getScheduler().runTask(System.plugin, () -> {
            entity.remove();
            entity = null;
        });
        Summoned = false;
        player.sendMessage("§e[" + petData.Display + "]§aを§eケージ§aに戻しました");
        playerData.PetSummon.remove(this);
        PetManager.PetSummonedList.remove(entity.getUniqueId());
        playSound(entity.getLocation(), Click);
    }
    void dead() {
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

    PetParameter(Player player, PlayerData playerData, String data) {
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



class PetManager {
    final static HashMap<String, PetSkillType> PersonalityA = new HashMap<>();
    final static HashMap<String, Double> PersonalityB = new HashMap<>();
    final static HashMap<String, Boolean> PersonalityC = new HashMap<>();
    final static HashMap<UUID, PetParameter> PetSummonedList = new HashMap<>();

    static Predicate<Entity> PredicatePet(Player player) {
        return entity -> entity != player && isPet((LivingEntity) entity) && PetParameter((LivingEntity) entity).player == player;
    }

    static boolean isPet(LivingEntity entity) {
        return PetSummonedList.containsKey(entity.getUniqueId());
    }

    static PetParameter PetParameter(LivingEntity entity) {
        if (isPet(entity)) {
            return PetSummonedList.get(entity.getUniqueId());
        }
        Log("§cNon-PetParameter: " + entity.getName(), true);
        return new PetParameter();
    }

    PetManager() {
        PersonalityA.put("イケイケの", PetSkillType.Both);
        PersonalityA.put("グイグイの", PetSkillType.Both);
        PersonalityA.put("陽気な", PetSkillType.Both);
        PersonalityA.put("バリバリの", PetSkillType.Both);
        PersonalityA.put("ボチボチの", PetSkillType.Both);
        PersonalityA.put("気まぐれな", PetSkillType.Both);
        PersonalityA.put("宝の持ち腐れ", PetSkillType.Both);
        PersonalityA.put("救世主の", PetSkillType.Support);
        PersonalityA.put("親切な", PetSkillType.Support);
        PersonalityA.put("奇跡を与える", PetSkillType.Support);
        PersonalityA.put("生の亡者の", PetSkillType.Support);
        PersonalityA.put("攻撃的な", PetSkillType.Attack);
        PersonalityA.put("勇敢な", PetSkillType.Attack);
        PersonalityA.put("破滅へいざなう", PetSkillType.Attack);
        PersonalityA.put("賢者を目指す", PetSkillType.Attack);

        PersonalityB.put("きままな", 32d);
        PersonalityB.put("やる気マンマン", 28d);
        PersonalityB.put("神の如き", 24d);
        PersonalityB.put("ルンルン気分で", 18d);
        PersonalityB.put("深淵より生まれし", 18d);
        PersonalityB.put("超燃え滾る", 17d);
        PersonalityB.put("深緑を護る", 17d);
        PersonalityB.put("光とともに", 17d);
        PersonalityB.put("燃え滾る", 16d);
        PersonalityB.put("闇を支配する", 16d);
        PersonalityB.put("豪炎の使徒", 16d);
        PersonalityB.put("悪魔の如き", 15d);
        PersonalityB.put("光の如き", 15d);
        PersonalityB.put("煌めき羽ばたく", 15d);
        PersonalityB.put("闇へいざなう", 15d);
        PersonalityB.put("暗黒を纏う", 15d);
        PersonalityB.put("ドSな", 14d);
        PersonalityB.put("雑草魂で", 14d);
        PersonalityB.put("情熱を秘めた", 14d);
        PersonalityB.put("雪原を駆ける", 14d);
        PersonalityB.put("ふわふわと", 13d);
        PersonalityB.put("ガッツのある", 13d);
        PersonalityB.put("常夏気分で", 12d);
        PersonalityB.put("吹雪の如き", 12d);
        PersonalityB.put("稲妻の如き", 11d);
        PersonalityB.put("甘えんぼうで", 10d);
        PersonalityB.put("天使の如き", 9d);
        PersonalityB.put("恋人みたいで", 8d);
        PersonalityB.put("気にはしている", 7d);
        PersonalityB.put("ツンデレの", 6d);

        PersonalityC.put("頑固な", false);
        PersonalityC.put("べったりな", false);
        PersonalityC.put("手当たり次第の", true);
        PersonalityC.put("従順な", true);
        PersonalityC.put("ビビリな", false);
        PersonalityC.put("貫禄のある", false);
    }

    static void PetSelect(Player player, LivingEntity entity) {
        PlayerData playerData = playerData(player);
        if (PetManager.isPet(entity)) {
            PetParameter pet = PetManager.PetParameter(entity);
            if (pet.player == player) {
                playerData.PetSelect = pet;
                player.sendMessage("§e[" + pet.petData.Display + "]§aを選択しました");
                playSound(player, Click);
            } else {
                player.sendMessage("§a自身の§e[ペット]§aを選択してください");
                playSound(player, Nope);
            }
        } else {
            player.sendMessage("§e[ペット]§aを選択してください");
            playSound(player, Nope);
        }
    }

    static boolean usingBaton(Player player) {
        PlayerData playerData = playerData(player);
        if (playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
            EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).EquipmentCategory;
            if (category == EquipmentCategory.Baton) {
                return true;
            }
        }
        return false;
    }

    static void PetAISelect(Player player) {
        PlayerData playerData = playerData(player);
        if (playerData.PetSelect != null && PetManager.isPet(playerData.PetSelect.entity)) {
            PetParameter pet = playerData.PetSelect;
            switch (pet.AIState) {
                case Follow -> {
                    pet.AIState = PetAIState.Attack;
                }
                case Attack -> {
                    pet.AIState = PetAIState.Support;
                    pet.target = null;
                }
                case Support -> {
                    pet.AIState = PetAIState.Follow;
                    pet.target = null;
                }
            }
            player.sendMessage("§e[" + pet.petData.Display + "]§aに§b[" + pet.AIState.Display + "]§aを指示しました");
            playSound(player, Click);
        } else {
            player.sendMessage("§a指揮する§e[ペット]§aを選択してください");
            playSound(player, Nope);
        }
    }

    static void PetAITarget(Player player) {
        PlayerData playerData = playerData(player);
        if (playerData.PetSelect != null) {
            switch (playerData.PetSelect.AIState) {
                case Attack -> {
                    Ray ray = RayTrace.rayLocationEntity(player.getEyeLocation(), 24, 1, playerData.Skill.SkillProcess.PredicateE());
                    if (ray.isHitEntity()) {
                        String Display = null;
                        if (ray.HitEntity instanceof Player target) {
                            Display = target.getDisplayName();
                        } else if (MobManager.isEnemy(ray.HitEntity)){
                            Display = EnemyTable(ray.HitEntity.getUniqueId()).mobData.Display;
                        } else if (PetManager.isPet(ray.HitEntity)) {
                            Display = PetManager.PetParameter(ray.HitEntity).petData.Display;
                        } else {
                            player.sendMessage("§c[攻撃対象]§aを選択してください");
                            playSound(player, SoundList.Nope);
                        }
                        if (Display != null) {
                            playerData.PetSelect.target = ray.HitEntity;
                            player.sendMessage("§c[" + Display + "]§aを§c[攻撃対象]§aにしました");
                            playSound(player, SoundList.Click);
                        }
                    } else {
                        player.sendMessage("§c[攻撃対象]§aを選択してください");
                        playSound(player, SoundList.Nope);
                    }
                }
            }
        } else {
            player.sendMessage("§a指揮する§e[ペット]§aを選択してください");
            playSound(player, Nope);
        }
    }
}
