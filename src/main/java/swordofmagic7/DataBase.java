package swordofmagic7;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.w3c.dom.Attr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.StatusParameter.*;

public final class DataBase {
    Plugin plugin;
    static final String DataBasePath = "M:\\Minecraft\\Server\\SwordofMagic7\\DataBase\\";
    static final String format = "%.3f";
    static final ItemStack AirItem = new ItemStack(Material.AIR);
    static final ItemStack FlameItem = new ItemStackData(Material.GRAY_STAINED_GLASS_PANE, colored("&7&l空スロット")).view();
    static final ItemStack ShopFlame = new ItemStackData(Material.BROWN_STAINED_GLASS_PANE, " ").view();
    static final ItemStack UpScrollItem = UpScrollItem();
    static final ItemStack DownScrollItem = DownScrollItem();
    static final String itemInformation = decoText("&3&lアイテム情報");
    static final String itemParameter = decoText("&3&lパラメーター");
    static final String itemModule = decoText("&3&lモジュール");
    private static final HashMap<Player, PlayerData> playerData = new HashMap<>();
    private static final HashMap<String, ItemParameter> ItemList = new HashMap<>();
    private static final HashMap<String, ModuleParameter> ModuleList = new HashMap<>();
    private static final HashMap<String, ClassData> ClassList = new HashMap<>();
    private static final HashMap<String, SkillData> SkillDataList = new HashMap<>();
    private static final HashMap<String, SkillData> SkillDataDisplayList = new HashMap<>();
    private static final HashMap<String, MobData> MobList = new HashMap<>();
    static final HashMap<String, ShopData> ShopList = new HashMap<>();

    static ItemStack UpScrollItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner("MHF_ArrowUp");
        meta.setDisplayName(colored("&e&l上にスクロール"));
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack DownScrollItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner("MHF_ArrowDown");
        meta.setDisplayName(colored("&e&l下にスクロール"));
        item.setItemMeta(meta);
        return item;
    }

    public DataBase(Plugin plugin) {
        this.plugin = plugin;
        DataLoad();
    }

    static void DataLoad() {
        File itemDirectories = new File(DataBasePath, "ItemData/");
        File[] itemFile = itemDirectories.listFiles();
        for (File file : itemFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            ItemParameter itemParameter = new ItemParameter();
            itemParameter.Display = data.getString("Display");
            itemParameter.Lore = data.getStringList("Lore");
            itemParameter.Sell = data.getInt("Sell");
            itemParameter.Category = ItemCategory.Item.getItemCategory(data.getString("Category"));
            itemParameter.EquipmentCategory = EquipmentCategory.Blade.getEquipmentCategory(data.getString("EquipmentCategory"));
            if (data.isSet("Material")) {
                itemParameter.Icon = Material.getMaterial(data.getString("Material", "BARRIER"));
            } else if (itemParameter.Category == ItemCategory.Equipment) {
                itemParameter.Icon = itemParameter.EquipmentCategory.material;
            }
            itemParameter.EquipmentSlot = EquipmentSlot.MainHand.getEquipmentSlot(data.getString("EquipmentSlot"));
            if (data.isSet("MaxMana")) itemParameter.Parameter.put(MaxMana, data.getDouble("MaxMana"));
            if (data.isSet("ManaRegen")) itemParameter.Parameter.put(ManaRegen, data.getDouble("ManaRegen"));
            if (data.isSet("ATK")) itemParameter.Parameter.put(ATK, data.getDouble("ATK"));
            if (data.isSet("DEF")) itemParameter.Parameter.put(DEF, data.getDouble("DEF"));
            if (data.isSet("SkillCastTime")) itemParameter.Parameter.put(SkillCastTime, data.getDouble("SkillCastTime"));
            if (data.isSet("SkillRigidTime")) itemParameter.Parameter.put(SkillRigidTime, data.getDouble("SkillRigidTime"));
            if (data.isSet("SkillCooltime")) itemParameter.Parameter.put(SkillCooltime, data.getDouble("SkillCooltime"));
            if (data.isSet("ReqLevel")) itemParameter.ReqLevel = data.getInt("ReqLevel");
            if (data.isSet("ModuleSlot")) itemParameter.ModuleSlot = data.getInt("ModuleSlot");
            if (data.isSet("Durable")) {
                itemParameter.Durable = data.getInt("Durable");
                itemParameter.MaxDurable = itemParameter.Durable;
            }
            ItemList.put(fileName, itemParameter);
        }

        File moduleDirectories = new File(DataBasePath, "ModuleData/");
        File[] moduleFile = moduleDirectories.listFiles();
        for (File file : moduleFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            ModuleParameter moduleData = new ModuleParameter();
            moduleData.Display = data.getString("Display");
            moduleData.Lore = data.getStringList("Lore");
            for (StatusParameter param : StatusParameter.values()) {
                if (data.isSet(param.toString())) {
                    moduleData.Parameter.put(param, data.getDouble(param.toString()));
                } else {
                    moduleData.Parameter.put(param, 0d);
                }
            }
            ModuleList.put(fileName, moduleData);
        }

        File skillActiveDirectories = new File(DataBasePath, "SkillDataActive/");
        File[] skillActiveFile = skillActiveDirectories.listFiles();
        for (File file : skillActiveFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            SkillData skillData = new SkillData();
            skillData.Id = fileName;
            skillData.Icon = Material.getMaterial(data.getString("Icon", "END_CRYSTAL"));
            skillData.Display = data.getString("Display");
            List<String> Lore = new ArrayList<>();
            for (String str : data.getStringList("Lore")) {
                Lore.add(colored("&a&l" + str));
            }
            skillData.Lore = Lore;
            skillData.SkillType = SkillType.Active;
            int i = 0;
            while (data.isSet("Parameter-" + i + ".Display")) {
                SkillParameter param = new SkillParameter();
                param.Display = data.getString("Parameter-" + i + ".Display");
                param.Value = data.getDouble("Parameter-" + i + ".Value");
                param.Prefix = data.getString("Parameter-" + i + ".Prefix");
                param.Suffix = data.getString("Parameter-" + i + ".Suffix");
                param.isInt = data.getBoolean("Parameter-" + i + ".isInt");
                skillData.Parameter.add(param);
                i++;
            }
            if (data.isSet("ReqMainHand")) {
                for (String str : data.getStringList("ReqMainHand")) {
                    skillData.ReqMainHand.add(EquipmentCategory.Blade.getEquipmentCategory(str));
                }
            }
            skillData.Mana = data.getInt("Mana");
            skillData.CastTime = data.getInt("CastTime");
            skillData.RigidTime = data.getInt("RigidTime");
            skillData.CoolTime = data.getInt("CoolTime");
            SkillDataList.put(skillData.Id, skillData);
            SkillDataDisplayList.put(skillData.Display, skillData);
        }

        File skillPassiveDirectories = new File(DataBasePath, "SkillDataPassive/");
        File[] skillPassiveFile = skillPassiveDirectories.listFiles();
        for (File file : skillPassiveFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            SkillData skillData = new SkillData();
            skillData.Id = fileName;
            skillData.Icon = Material.getMaterial(data.getString("Icon", "END_CRYSTAL"));
            skillData.Display = data.getString("Display");
            List<String> Lore = new ArrayList<>();
            for (String str : data.getStringList("Lore")) {
                Lore.add(colored("&a&l" + str));
            }
            skillData.Lore = Lore;
            skillData.SkillType = SkillType.Passive;
            int i = 0;
            while (data.isSet("Parameter-" + i + ".Display")) {
                SkillParameter param = new SkillParameter();
                param.Display = data.getString("Parameter-" + i + ".Display");
                param.Value = data.getDouble("Parameter-" + i + ".Value");
                param.Prefix = data.getString("Parameter-" + i + ".Prefix");
                param.Suffix = data.getString("Parameter-" + i + ".Suffix");
                param.isInt = data.getBoolean("Parameter-" + i + ".isInt");
                skillData.Parameter.add(param);
                i++;
            }
            SkillDataList.put(skillData.Id, skillData);
            SkillDataDisplayList.put(skillData.Display, skillData);
        }

        File classDirectories = new File(DataBasePath, "ClassData/");
        File[] classFile = classDirectories.listFiles();
        for (File file : classFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            ClassData classData = new ClassData();
            classData.Display = data.getString("Display");
            classData.Nick = data.getString("Nick");
            classData.Tier = data.getInt("Tier");
            List<SkillData> Skills = new ArrayList<>();
            for (String str : data.getStringList("SkillList")) {
                if (SkillDataList.containsKey(str)) {
                    Skills.add(SkillDataList.get(str));
                }
            }
            classData.SkillList = Skills;
            ClassList.put(fileName, classData);
        }

        File mobDirectories = new File(DataBasePath, "MobData/");
        File[] mobFile = mobDirectories.listFiles();
        for (File file : mobFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            MobData mobData = new MobData();
            mobData.Display = data.getString("Display");
            String entityType = data.getString("Type").toUpperCase().replace(" ", "_");
            if (EntityType.fromName(entityType) != null) {
                mobData.entityType = EntityType.valueOf(entityType);
            } else {
                mobData.entityType = EntityType.SKELETON;
                Log("&cError Non-EntityType: " + fileName);
            }
            mobData.Health = data.getDouble("Health");
            mobData.ATK = data.getDouble("ATK");
            mobData.DEF = data.getDouble("DEF");
            mobData.ACC = data.getDouble("ACC");
            mobData.EVA = data.getDouble("EVA");
            mobData.Exp = data.getDouble("Exp");
            mobData.Movement = data.getDouble("Movement");

            if (data.isSet("Hostile")) {
                mobData.Hostile = data.getBoolean("Hostile");
            }

            List<DropItemData> DropItemTable = new ArrayList<>();
            for (String dropStr : data.getStringList("DropItem")) {
                String[] dropData = dropStr.split(",");
                DropItemData dropItemData = new DropItemData(getItemParameter(dropData[0]));
                for (String str : dropData) {
                    if (str.contains("Amount:")) {
                        str = str.replace("Amount:", "");
                        if (str.contains("-")) {
                            String[] Amount = str.split("-");
                            dropItemData.MinAmount = Integer.parseInt(Amount[0]);
                            dropItemData.MaxAmount = Integer.parseInt(Amount[1]);
                        } else {
                            dropItemData.MinAmount = Integer.parseInt(str);
                            dropItemData.MaxAmount = Integer.parseInt(str);
                        }
                    } else if (str.contains("Level:")) {
                        str = str.replace("Level:", "");
                        if (str.contains("-")) {
                            String[] Level = str.split("-");
                            dropItemData.MinLevel = Integer.parseInt(Level[0]);
                            dropItemData.MaxLevel = Integer.parseInt(Level[1]);
                        } else {
                            dropItemData.MinLevel = Integer.parseInt(str);
                            dropItemData.MaxLevel = Integer.parseInt(str);
                        }
                    } else if (str.contains("Percent")) {
                        dropItemData.Percent = Double.parseDouble(str.replace("Percent:", ""));
                    }
                }
                DropItemTable.add(dropItemData);
            }

            List<DropModuleData> DropModuleTable = new ArrayList<>();
            for (String dropStr : data.getStringList("DropModule")) {
                String[] dropData = dropStr.split(",");
                DropModuleData dropModuleData = new DropModuleData(getModuleParameter(dropData[0]));
                for (String str : dropData) {
                    if (str.contains("Level:")) {
                        str = str.replace("Level:", "");
                        if (str.contains("-")) {
                            String[] Level = str.split("-");
                            dropModuleData.MinLevel = Integer.parseInt(Level[0]);
                            dropModuleData.MaxLevel = Integer.parseInt(Level[1]);
                        } else {
                            dropModuleData.MinLevel = Integer.parseInt(str);
                            dropModuleData.MaxLevel = Integer.parseInt(str);
                        }
                    } else if (str.contains("Percent")) {
                        dropModuleData.Percent = Double.parseDouble(str.replace("Percent:", ""));
                    }
                }
                DropModuleTable.add(dropModuleData);
            }

            mobData.DropItemTable = DropItemTable;
            mobData.DropModuleTable = DropModuleTable;

            MobList.put(fileName, mobData);
        }

        File shopDirectories = new File(DataBasePath, "ShopData/");
        File[] shopFile = shopDirectories.listFiles();
        for (File file : shopFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            ShopData shopData = new ShopData();
            shopData.Display = fileName;
            int slot = 0;
            for (String str : data.getStringList("Data")) {
                ShopSlot shopSlot = new ShopSlot();
                String[] split = str.split(",");
                shopSlot.itemParameter = getItemParameter(split[0]);
                for (String str2 : split) {
                    if (str2.contains("mel")) {
                        shopSlot.Mel = Integer.parseInt(str2.replace("mel", ""));
                    } else if (str2.contains("slot")) {
                        slot = Integer.parseInt(str2.replace("slot", ""));
                    }
                }
                shopData.Data.put(slot, shopSlot);
                slot++;
            }
            ShopList.put(fileName, shopData);
        }
    }

    static PlayerData playerData(Player player) {
        if (player.isOnline()) {
            playerData.putIfAbsent(player, new PlayerData(player));
            return playerData.get(player);
        }
        Log(colored("&c" + player.getName() + "&c, " + player.getUniqueId() + " is Offline or Npc"), true);
        return new PlayerData(null);
    }

    static void removePlayerData(Player player) {
        playerData.remove(player);
    }

    static HashMap<String, ItemParameter> getItemList() {
        return ItemList;
    }

    static HashMap<String, ClassData> getClassList() {
        return ClassList;
    }

    static HashMap<String, SkillData> getSkillList() {
        return SkillDataList;
    }

    static HashMap<String, MobData> getMobList() {
        return MobList;
    }

    static ItemParameter getItemParameter(String str) {
        if (ItemList.containsKey(str)) {
            return ItemList.get(str).clone();
        } else {
            Log("&cNon-ItemParameter: " + str, true);
            return new ItemParameter();
        }
    }

    static ModuleParameter getModuleParameter(String str) {
        if (ModuleList.containsKey(str)) {
            return ModuleList.get(str).clone();
        } else {
            Log("&cNon-ModuleParameter: " + str, true);
            return new ModuleParameter();
        }
    }

    static ClassData getClassData(String str) {
        if (ClassList.containsKey(str)) {
            return ClassList.get(str);
        } else {
            Log("&cNon-ClassData: " + str, true);
            return new ClassData();
        }
    }

    static SkillData getSkillData(String skill) {
        if (SkillDataList.containsKey(skill)) {
            return SkillDataList.get(skill);
        } else if (SkillDataDisplayList.containsKey(skill)) {
            return SkillDataDisplayList.get(skill);
        } else {
            Log("&cNon-SkillData: " + skill, true);
            return new SkillData();
        }
    }

    static MobData getMobData(String str) {
        if (MobList.containsKey(str)) {
            return MobList.get(str);
        } else {
            Log("&cNon-MobData: " + str, true);
            return new MobData();
        }
    }

    static ShopData getShopData(String str) {
        if (ShopList.containsKey(str)) {
            return ShopList.get(str);
        } else {
            Log("&cNon-ShopList: " + str, true);
            return new ShopData();
        }
    }

    static String itemToString(ItemParameterStack item) {
        StringBuilder data;
        if (!item.isEmpty()) {
            ItemParameter itemParameter = item.itemParameter;
            ItemCategory category = itemParameter.Category;
            data = new StringBuilder(unDecoText(itemParameter.Display) + ",Amount:" + item.Amount);
            if (category == ItemCategory.Equipment) {
                data.append(",Plus:").append(itemParameter.Plus).append(",Durable:").append(itemParameter.Durable);
                for (ModuleParameter moduleParameter : itemParameter.getModule()) {
                    if (!moduleToString(moduleParameter).equals("None"))
                    data.append(",Module:").append(moduleToString(moduleParameter));
                }
            }
        } else {
            data = new StringBuilder("None");
        }
        return data.toString();
    }

    static String moduleToString(ModuleParameter module) {
        String data;
        if (!module.isEmpty()) {
            data = unDecoText(module.Display) + ";Level:" + module.Level + ";Quality:" + String.format("%.5f",module.Quality);
        } else {
            data = "None";
        }
        return data;
    }

    static String hotBarToString(HotBarData hotBarData) {
        String data = "None";
        if (hotBarData != null) {
            if (hotBarData.category != HotBarCategory.None) {
                data = hotBarData.Icon + "," + hotBarData.category;
            }
        }
        return data;
    }

    static ItemParameterStack stringToItem(String data) {
        ItemParameterStack parameterStack = new ItemParameterStack();
        if (!data.equals("None")) {
            String[] split = data.split(",");
            if (DataBase.ItemList.containsKey(split[0])) {
                ItemParameter itemParameter = getItemParameter(split[0]);
                for (String str : split) {
                    if (str.contains("Amount:")) {
                        parameterStack.Amount = Integer.parseInt(str.replace("Amount:", ""));
                    }
                    if (str.contains("Durable:")) {
                        itemParameter.Durable = Integer.parseInt(str.replace("Durable:", ""));
                    }
                    if (str.contains("Plus:")) {
                        itemParameter.Plus = Integer.parseInt(str.replace("Plus:", ""));
                    }
                    if (str.contains("Module:")) {
                        itemParameter.addModule(stringToModule(str.replace("Module:", "")));
                    }
                }
                parameterStack.itemParameter = itemParameter;
            } else {
                Log("&cError NotFoundItemData: " + split[0]);
            }
        }
        return parameterStack;
    }

    static ModuleParameter stringToModule(String data) {
        ModuleParameter moduleParameter = new ModuleParameter();
        if (!data.equals("None")) {
            String[] split = data.split(";");
            if (DataBase.ModuleList.containsKey(split[0])) {
                moduleParameter = getModuleParameter(split[0]);
                for (String str : split) {
                    if (str.contains("Level:")) {
                        moduleParameter.Level = Integer.parseInt(str.replace("Level:", ""));
                    } else if (str.contains("Quality:")) {
                        moduleParameter.Quality = Double.parseDouble((str.replace("Quality:", "")));
                    }
                }
            } else {
                Log("&cError NotFoundModuleData: " + split[0]);
            }
        }
        return moduleParameter;
    }

    static HotBarData stringToHotBar(String data) {
        HotBarData hotBarData = new HotBarData();
        if (!data.equals("None")) {
            String[] split = data.split(",");
            hotBarData.Icon = split[0];
            hotBarData.category = HotBarCategory.valueOf(split[1]);
        }
        return hotBarData;
    }
}

enum ViewInventory {
    ItemInventory("アイテムインベントリ"),
    ModuleInventory("モジュールインベントリ"),
    HotBar("ホットバー"),
    ;

    String Display;

    ViewInventory(String Display) {
        this.Display = Display;
    }

    boolean isItem() {
        return this == ItemInventory;
    }

    boolean isModule() {
        return this == ModuleInventory;
    }

    boolean isHotBar() {
        return this == HotBar;
    }
}

class PlayerData {
    private final Plugin plugin;
    private final Player player;
    private boolean able = false;
    ItemInventory ItemInventory;
    HotBar HotBar;
    ModuleInventory ModuleInventory;
    Equipment Equipment;
    Status Status;
    Classes Classes;
    Skill Skill;
    Menu Menu;
    Attribute Attribute;

    String Nick;

    boolean DamageLog = false;
    boolean ExpLog = false;
    boolean PvPMode = false;
    boolean PlayMode = true;
    CastType CastType = swordofmagic7.CastType.Renewed;
    int Mel = 10000;
    int ViewFormat = 0;

    ViewInventory ViewInventory = swordofmagic7.ViewInventory.ItemInventory;

    PlayerData(Player player) {
        plugin = System.plugin;
        this.player = player;
        ItemInventory = new ItemInventory(player, this);
        HotBar = new HotBar(player, this);
        ModuleInventory = new ModuleInventory(player, this);
        Equipment = new Equipment(player, this);
        Status = new Status(player, this);
        Classes = new Classes(player, this);
        Skill = new Skill(player, this, plugin);
        Menu = new Menu(player, this);
        Attribute = new Attribute(player);

        Nick = player.getName();

        able = true;
    }

    void DamageLog(boolean bool) {
        DamageLog = bool;
        String msg = "&cダメージログ&aを";
        if (bool) msg += "&b有効";
        else msg += "&c無効";
        msg += "&aにしました";
        player.sendMessage(colored(msg));
    }

    void ExpLog(boolean bool) {
        ExpLog = bool;
        String msg = "&e経験値ログ&aを";
        if (bool) msg += "&b有効";
        else msg += "&c無効";
        msg += "&aにしました";
        player.sendMessage(colored(msg));
    }

    void PvPMode(boolean bool) {
        PvPMode = bool;
        String msg = "&ePvP&aを";
        if (bool) msg += "&b有効";
        else msg += "&c無効";
        msg += "&aにしました";
        player.sendMessage(colored(msg));
    }

    String ViewFormat() {
        return "%." + ViewFormat + "f";
    }

    void setViewFormat(int ViewFormat) {
        this.ViewFormat = ViewFormat;
        player.sendMessage(colored("&e表記小数桁数&aを&e[" + ViewFormat + "桁]&aに&e設定&aしました"));
        viewUpdate();
    }

    void remove() {
        Status.tickUpdateTask.cancel();
        removePlayerData(player);
    }

    void save() {
        File playerFile = new File(DataBasePath, "PlayerData/" + player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error creating " + playerFile.getName() + "!");
            }
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        data.set("Location.x", player.getLocation().getX());
        data.set("Location.y", player.getLocation().getY());
        data.set("Location.z", player.getLocation().getZ());
        data.set("Location.yaw", player.getLocation().getYaw());
        data.set("Location.pitch", player.getLocation().getPitch());

        data.set("Mel", Mel);
        data.set("Health", Status.Health);
        data.set("Mana", Status.Mana);

        data.set("Setting.DamageLog", DamageLog);
        data.set("Setting.ExpLog", ExpLog);
        data.set("Setting.PvPMode", PvPMode);
        data.set("Setting.ViewFormat", ViewFormat);
        data.set("Setting.PlayMode", PlayMode);

        for (Map.Entry<String, ClassData> classData : getClassList().entrySet()) {
            data.set("ClassData." + classData.getKey() + ".Level", Classes.getLevel(classData.getValue()));
            data.set("ClassData." + classData.getKey() + ".Exp", Classes.getExp(classData.getValue()));
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            data.set("Inventory." + slot.toString(), itemToString(new ItemParameterStack(Equipment.getEquip(slot))));
        }
        List<String> itemList = new ArrayList<>();
        for (ItemParameterStack stack : ItemInventory.getList()) {
            itemList.add(itemToString(stack));
        }
        data.set("Inventory.ItemList", itemList);

        List<String> moduleList = new ArrayList<>();
        for (ModuleParameter module : ModuleInventory.getList()) {
            moduleList.add(moduleToString(module));
        }
        data.set("Inventory.ModuleList", moduleList);

        List<String> hotBarList = new ArrayList<>();
        for (HotBarData hotBarData : HotBar.getHotBar()) {
            hotBarList.add(hotBarToString(hotBarData));
        }
        data.set("Inventory.HotBar", hotBarList);

        try {
            data.save(playerFile);
            player.sendMessage("§eプレイヤデータ§aの§bセーブ§aが完了しました");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void load() {
        File playerFile = new File(DataBasePath, "PlayerData/" + player.getUniqueId() + ".yml");
        if (playerFile.exists()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
            World world = player.getWorld();
            double x = data.getDouble("Location.x", world.getSpawnLocation().getX());
            double y = data.getDouble("Location.y", world.getSpawnLocation().getY());
            double z = data.getDouble("Location.z", world.getSpawnLocation().getZ());
            float yaw = (float) data.getDouble("Location.yaw", world.getSpawnLocation().getYaw());
            float pitch = (float) data.getDouble("Location.pitch", world.getSpawnLocation().getPitch());
            Location loc = new Location(world, x, y, z, yaw, pitch);
            player.teleportAsync(loc);

            Mel = data.getInt("Mel", 10000);
            Status.Health = data.getDouble("Health", 20);
            Status.Mana = data.getDouble("Mana", 100);

            DamageLog = data.getBoolean("Setting.DamageLog", false);
            ExpLog = data.getBoolean("Setting.ExpLog", false);
            PvPMode = data.getBoolean("Setting.PvPMode", false);
            PlayMode = data.getBoolean("Setting.PlayMode", true);

            for (Map.Entry<String, ClassData> classData : getClassList().entrySet()) {
                Classes.setLevel(classData.getValue(), data.getInt("ClassData." + classData.getKey() + ".Level"));
                Classes.setExp(classData.getValue(), data.getInt("ClassData." + classData.getKey() + ".Exp"));
            }

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemParameter param = stringToItem(data.getString("Inventory." + slot.toString(), "None")).itemParameter;
                if (!param.isEmpty()) Equipment.Equip(slot, param);
            }

            List<String> itemList = data.getStringList("Inventory.ItemList");
            ItemInventory.clear();
            for (String itemData : itemList) {
                ItemParameterStack stack = stringToItem(itemData);
                if (!stack.isEmpty()) ItemInventory.addItemParameter(stack.itemParameter, stack.Amount);
            }

            List<String> moduleList = data.getStringList("Inventory.ModuleList");
            ModuleInventory.clear();
            for (String moduleData : moduleList) {
                ModuleParameter module = stringToModule(moduleData);
                if (!module.isEmpty()) ModuleInventory.addModuleParameter(module);
            }

            List<String> hotBarList = data.getStringList("Inventory.HotBar");
            int i = 0;
            HotBarData[] HotBarData = new HotBarData[32];
            for (String hotBarData : hotBarList) {
                HotBarData hotBar = stringToHotBar(hotBarData);
                HotBarData[i] = hotBar;
                i++;
            }
            HotBar.setHotBar(HotBarData);

            viewUpdate();
        }
        Status.StatusUpdate();
        Status.tickUpdate();
    }

    ItemStack UserMenuIcon() {
        List<String> Lore = new ArrayList<>();
        Lore.add(colored("&a&lユーザーメニューを開きます"));
        Lore.add(colored("&a&lシフトクリックでインベントリ表示を"));
        Lore.add(colored("&a&l瞬時に切り替えることが出来ます"));
        Lore.add(decoText("&3&lインベントリ表示"));
        Lore.add(decoLore("&e&lインベントリ表示") + ViewInventory.Display);
        if (ViewInventory.isItem())
            Lore.add(decoLore("&e&lインベントリ容量") + ItemInventory.getList().size() + "/300");
        else if (ViewInventory.isModule())
            Lore.add(decoLore("&e&lインベントリ容量") + ModuleInventory.getList().size() + "/300");
        return new ItemStackData(Material.BOOK, decoText("&e&lユーザーメニュー"), Lore).view();
    }

    void viewUpdate() {
        switch (ViewInventory) {
            case ItemInventory -> ItemInventory.viewInventory();
            case ModuleInventory -> ModuleInventory.viewModule();
            case HotBar -> HotBar.viewTop();
        }
        HotBar.viewBottom();
        Equipment.viewEquip();
        player.getInventory().setItem(26, UserMenuIcon());
        player.getInventory().setItem(17, UpScrollItem);
        player.getInventory().setItem(35, DownScrollItem);
    }

    void setView(ViewInventory ViewInventory) {
        setView(ViewInventory, true);
    }

    void setView(ViewInventory ViewInventory, boolean log) {
        this.ViewInventory = ViewInventory;
        if (log) player.sendMessage(colored("&eインベントリ表示&aを&e[" + ViewInventory.Display + "]&aに切り替えました"));
        viewUpdate();
    }

    private boolean RightClickHold = false;
    private BukkitTask RightClickHoldTask;
    void setRightClickHold() {
        RightClickHold = true;
        if (RightClickHoldTask != null) RightClickHoldTask.cancel();
        RightClickHoldTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            RightClickHold = false;
        }, 6);
    }

    boolean isRightClickHold() {
        return RightClickHold;
    }

    void dead() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendTitle(colored("&4&lYou Are Dead"), "", 20, 60, 20);
            Bukkit.getScheduler().runTaskLater(plugin,() -> {
                player.teleportAsync(player.getWorld().getSpawnLocation());
                player.setGameMode(GameMode.ADVENTURE);
                Status.Health = Status.MaxHealth;
                Status.Mana = Status.MaxMana;
            }, 100);
        });
    }
}