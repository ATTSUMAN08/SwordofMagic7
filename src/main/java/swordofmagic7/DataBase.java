package swordofmagic7;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.w3c.dom.Attr;
import org.w3c.dom.Entity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Classes.MaxTier;
import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.StatusParameter.*;

public final class DataBase {
    Plugin plugin;
    static final String DataBasePath = "M:\\Minecraft\\Server\\SwordofMagic7\\DataBase\\";
    static final String format = "%.3f";
    static final Location SpawnLocation = new Location(Bukkit.getWorld("world"), 1200.5, 100, 0.5, 0, 0);
    static final ItemStack AirItem = new ItemStack(Material.AIR);
    static final ItemStack FlameItem = new ItemStackData(Material.GRAY_STAINED_GLASS_PANE, "§7§l空スロット").view();
    static final ItemStack ShopFlame = new ItemStackData(Material.BROWN_STAINED_GLASS_PANE, " ").view();
    static final ItemStack UpScrollItem = UpScrollItem();
    static final ItemStack DownScrollItem = DownScrollItem();
    static final String itemInformation = decoText("§3§lアイテム情報");
    static final String itemParameter = decoText("§3§lパラメーター");
    static final String itemRune = decoText("§3§lルーン");
    static final HashMap<Player, PlayerData> playerData = new HashMap<>();
    private static final HashMap<String, ItemParameter> ItemList = new HashMap<>();
    private static final HashMap<String, RuneParameter> RuneList = new HashMap<>();
    private static final HashMap<String, ClassData> ClassList = new HashMap<>();
    private static final HashMap<String, SkillData> SkillDataList = new HashMap<>();
    private static final HashMap<String, SkillData> SkillDataDisplayList = new HashMap<>();
    private static final HashMap<String, MobData> MobList = new HashMap<>();
    static final HashMap<String, MobSpawnerData> MobSpawnerList = new HashMap<>();
    static final HashMap<String, ShopData> ShopList = new HashMap<>();
    static final HashMap<String, WarpGateParameter> WarpGateList = new HashMap<>();
    static final HashMap<String, MapData> MapList = new HashMap<>();
    static final HashMap<String, PetData> PetList = new HashMap<>();

    static ItemStack ItemStackPlayerHead(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack UpScrollItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner("MHF_ArrowUp");
        meta.setDisplayName("§e§l上にスクロール");
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack DownScrollItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner("MHF_ArrowDown");
        meta.setDisplayName("§e§l下にスクロール");
        item.setItemMeta(meta);
        return item;
    }

    public DataBase(Plugin plugin) {
        this.plugin = plugin;
        DataLoad();
    }

    private static List<File> dumpFile(File file){
        List<File> list = new ArrayList<>();
        File[] files = file.listFiles();
        for (File tmpFile : files) {
            if(tmpFile.isDirectory()){
                list.addAll(dumpFile(tmpFile));
            }else{
                list.add(tmpFile);
            }
        }
        return list;
    }

    static void DataLoad() {
        File itemDirectories = new File(DataBasePath, "ItemData");
        List<File> itemFiles = dumpFile(itemDirectories);
        //Log(String.valueOf(itemFiles));
        for (File file : itemFiles) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            ItemParameter itemParameter = new ItemParameter();
            itemParameter.Id = fileName;
            itemParameter.Display = data.getString("Display");
            itemParameter.Lore = data.getStringList("Lore");
            itemParameter.Sell = data.getInt("Sell");
            itemParameter.Category = ItemCategory.Item.getItemCategory(data.getString("Category"));
            if (itemParameter.Category == ItemCategory.PetEgg) {
                itemParameter.PetId = data.getString("PetId");
                itemParameter.PetMaxLevel = data.getInt("PetMaxLevel");
                itemParameter.PetLevel = data.getInt("PetLevel");
            }
            itemParameter.EquipmentCategory = EquipmentCategory.Blade.getEquipmentCategory(data.getString("EquipmentCategory"));
            if (data.isSet("Material")) {
                itemParameter.Icon = Material.getMaterial(data.getString("Material", "BARRIER"));
            } else if (itemParameter.Category == ItemCategory.Equipment) {
                itemParameter.Icon = itemParameter.EquipmentCategory.material;
            }
            itemParameter.EquipmentSlot = EquipmentSlot.MainHand.getEquipmentSlot(data.getString("EquipmentSlot"));
            for (StatusParameter param : StatusParameter.values()) {
                if (data.isSet(param.toString())) {
                    itemParameter.Parameter.put(param, data.getDouble(param.toString()));
                }
            }
            if (data.isSet("ReqLevel")) itemParameter.ReqLevel = data.getInt("ReqLevel");
            if (data.isSet("RuneSlot")) itemParameter.RuneSlot = data.getInt("RuneSlot");
            if (data.isSet("Durable")) {
                itemParameter.Durable = data.getInt("Durable");
                itemParameter.MaxDurable = itemParameter.Durable;
            }
            ItemList.put(itemParameter.Id, itemParameter);
        }

        File runeDirectories = new File(DataBasePath, "RuneData/");
        File[] runeFile = runeDirectories.listFiles();
        for (File file : runeFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            RuneParameter runeData = new RuneParameter();
            runeData.Id = fileName;
            runeData.Display = data.getString("Display");
            runeData.Lore = data.getStringList("Lore");
            for (StatusParameter param : StatusParameter.values()) {
                if (data.isSet(param.toString())) {
                    runeData.Parameter.put(param, data.getDouble(param.toString()));
                } else {
                    runeData.Parameter.put(param, 0d);
                }
            }
            RuneList.put(runeData.Id, runeData);
        }

        File petDirectories = new File(DataBasePath, "PetData/");
        File[] petFile = petDirectories.listFiles();
        for (File file : petFile) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            PetData petData = new PetData();
            petData.Id = fileName;
            petData.Display = data.getString("Display");
            petData.Lore = data.getStringList("Lore");
            petData.entityType = EntityType.fromName(data.getString("Type").toUpperCase());
            if (data.isSet("Disguise.Type")) {
                petData.disguise = new MobDisguise(DisguiseType.valueOf(data.getString("Disguise.Type").toUpperCase()));
                if (petData.disguise.getType() == DisguiseType.SLIME) {
                    SlimeWatcher slimeWatcher = new SlimeWatcher(petData.disguise);
                    slimeWatcher.setSize(data.getInt("Disguise.Size"));
                    petData.disguise.setWatcher(slimeWatcher);
                }
            }
            petData.Icon = Material.getMaterial(data.getString("Icon"));
            petData.MaxStamina = data.getDouble("MaxStamina");
            petData.MaxHealth = data.getDouble("MaxHealth");
            petData.HealthRegen = data.getDouble("HealthRegen");
            petData.MaxMana = data.getDouble("MaxMana");
            petData.ManaRegen = data.getDouble("ManaRegen");
            petData.ATK = data.getDouble("ATK");
            petData.DEF = data.getDouble("DEF");
            petData.ACC = data.getDouble("ACC");
            petData.EVA = data.getDouble("EVA");
            petData.CriticalRate = data.getDouble("CriticalRate");
            petData.CriticalResist = data.getDouble("CriticalResist");
            PetList.put(fileName, petData);
        }

        File mapDirectories  = new File(DataBasePath, "MapData/");
        File[] mapFile = mapDirectories.listFiles();
        for (File file : mapFile) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            String fileName = file.getName().replace(".yml", "");
            MapData mapData = new MapData();
            mapData.Display = data.getString("Display");
            mapData.Level = data.getInt("Level");
            mapData.Safe = data.getBoolean("Safe");
            MapList.put(fileName, mapData);
        }

        File warpDirectories  = new File(DataBasePath, "WarpGateData/");
        File[] warpFile = warpDirectories.listFiles();
        for (File file : warpFile) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            String fileName = file.getName().replace(".yml", "");
            World world = Bukkit.getWorld(data.getString("Location.w", "world"));
            double x = data.getDouble("Location.x");
            double y = data.getDouble("Location.y");
            double z = data.getDouble("Location.z");
            float yaw = (float) data.getDouble("Location.yaw");
            float pitch = (float) data.getDouble("Location.pitch");
            Location loc = new Location(world, x,y,z,yaw,pitch);
            WarpGateParameter warp = new WarpGateParameter();
            warp.Location = loc;
            warp.Target = data.getString("Target");
            warp.NextMap = MapList.get(data.getString("NextMap"));
            WarpGateList.put(fileName, warp);
        }

        File skillActiveDirectories = new File(DataBasePath, "SkillData/Active/");
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
                Lore.add("§a§l" + str);
            }
            skillData.Lore = Lore;
            skillData.SkillType = SkillType.Active;
            int i = 0;
            while (data.isSet("Parameter-" + i + ".Display")) {
                SkillParameter param = new SkillParameter();
                param.Display = data.getString("Parameter-" + i + ".Display");
                param.Value = data.getDouble("Parameter-" + i + ".Value");
                param.Increase = data.getDouble("Parameter-" + i + ".Increase");
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

        File skillPassiveDirectories = new File(DataBasePath, "SkillData/Passive/");
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
                Lore.add("§a§l" + str);
            }
            skillData.Lore = Lore;
            skillData.SkillType = SkillType.Passive;
            int i = 0;
            while (data.isSet("Parameter-" + i + ".Display")) {
                SkillParameter param = new SkillParameter();
                param.Display = data.getString("Parameter-" + i + ".Display");
                param.Value = data.getDouble("Parameter-" + i + ".Value");
                param.Increase = data.getDouble("Parameter-" + i + ".Increase");
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
            classData.Id = fileName;
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
            String entityType = data.getString("Type").toUpperCase();
            if (EntityType.fromName(entityType) != null) {
                mobData.entityType = EntityType.valueOf(entityType);
            } else {
                mobData.entityType = EntityType.SKELETON;
                Log("§cError Non-EntityType: " + fileName);
            }
            if (data.isSet("Disguise.Type")) {
                mobData.disguise = new MobDisguise(DisguiseType.valueOf(data.getString("Disguise.Type").toUpperCase()));
                if (mobData.disguise.getType() == DisguiseType.SLIME) {
                    SlimeWatcher slimeWatcher = new SlimeWatcher(mobData.disguise);
                    slimeWatcher.setSize(data.getInt("Disguise.Size"));
                    mobData.disguise.setWatcher(slimeWatcher);
                }
            }
            mobData.Health = data.getDouble("Health");
            mobData.ATK = data.getDouble("ATK");
            mobData.DEF = data.getDouble("DEF");
            mobData.ACC = data.getDouble("ACC");
            mobData.EVA = data.getDouble("EVA");
            mobData.CriticalRate = data.getDouble("CriticalRate");
            mobData.CriticalResist = data.getDouble("CriticalResist");
            mobData.Exp = data.getDouble("Exp");
            mobData.Mov = data.getDouble("Mov");
            mobData.Reach = data.getDouble("Reach");
            if (data.isSet("Skill")) {
                List<MobSkillData> SkillList = new ArrayList<>();
                for (String str : data.getStringList("Skill")) {
                    String[] split = str.split(",");
                    MobSkillData mobSkillData = new MobSkillData();
                    mobSkillData.Skill = split[0];
                    mobSkillData.Percent = Double.parseDouble(split[1]);
                    SkillList.add(mobSkillData);
                }
                mobData.SkillList = SkillList;
            }
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

            List<DropRuneData> DropRuneTable = new ArrayList<>();
            for (String dropStr : data.getStringList("DropRune")) {
                String[] dropData = dropStr.split(",");
                DropRuneData dropRuneData = new DropRuneData(getRuneParameter(dropData[0]));
                for (String str : dropData) {
                    if (str.contains("Level:")) {
                        str = str.replace("Level:", "");
                        if (str.contains("-")) {
                            String[] Level = str.split("-");
                            dropRuneData.MinLevel = Integer.parseInt(Level[0]);
                            dropRuneData.MaxLevel = Integer.parseInt(Level[1]);
                        } else {
                            dropRuneData.MinLevel = Integer.parseInt(str);
                            dropRuneData.MaxLevel = Integer.parseInt(str);
                        }
                    } else if (str.contains("Percent")) {
                        dropRuneData.Percent = Double.parseDouble(str.replace("Percent:", ""));
                    }
                }
                DropRuneTable.add(dropRuneData);
            }

            mobData.DropItemTable = DropItemTable;
            mobData.DropRuneTable = DropRuneTable;

            MobList.put(fileName, mobData);
        }

        File mobSpawnerDirectories = new File(DataBasePath, "MobSpawner/");
        File[] mobSpawner = mobSpawnerDirectories.listFiles();
        for (File file : mobSpawner) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            MobSpawnerData mobSpawnerData = new MobSpawnerData();
            mobSpawnerData.mobData = getMobData(data.getString("MobData"));
            mobSpawnerData.Level = data.getInt("Level");
            mobSpawnerData.Radius = data.getInt("Radius");
            mobSpawnerData.RadiusY = data.getInt("RadiusY");
            mobSpawnerData.MaxMob = data.getInt("MaxMob");
            mobSpawnerData.PerSpawn = data.getInt("PerSpawn");
            double x = data.getDouble("Location.x");
            double y = data.getDouble("Location.y");
            double z = data.getDouble("Location.z");
            mobSpawnerData.location = new Location(Bukkit.getWorld(data.getString("Location.w", "world")), x, y, z);
            mobSpawnerData.start();
            MobSpawnerList.put(fileName, mobSpawnerData);
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
        Log("§c" + player.getName() + "§c, " + player.getUniqueId() + " is Offline or Npc", true);
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

    static HashMap<String, PetData> getPetList() {
        return PetList;
    }

    static ItemParameter getItemParameter(String str) {
        if (ItemList.containsKey(str)) {
            return ItemList.get(str).clone();
        } else {
            Log("§cNon-ItemParameter: " + str, true);
            return new ItemParameter();
        }
    }

    static RuneParameter getRuneParameter(String str) {
        if (RuneList.containsKey(str)) {
            return RuneList.get(str).clone();
        } else {
            Log("§cNon-RuneParameter: " + str, true);
            return new RuneParameter();
        }
    }

    static ClassData getClassData(String str) {
        if (ClassList.containsKey(str)) {
            return ClassList.get(str);
        } else {
            Log("§cNon-ClassData: " + str, true);
            return new ClassData();
        }
    }

    static SkillData getSkillData(String skill) {
        if (SkillDataList.containsKey(skill)) {
            return SkillDataList.get(skill);
        } else if (SkillDataDisplayList.containsKey(skill)) {
            return SkillDataDisplayList.get(skill);
        } else {
            Log("§cNon-SkillData: " + skill, true);
            return new SkillData();
        }
    }

    static MobData getMobData(String str) {
        if (MobList.containsKey(str)) {
            return MobList.get(str);
        } else {
            Log("§cNon-MobData: " + str, true);
            return new MobData();
        }
    }

    static MapData getMapData(String str) {
        if (MapList.containsKey(str)) {
            return MapList.get(str);
        } else {
            Log("§cNon-MapData: " + str, true);
            return new MapData();
        }
    }

    static PetData getPetData(String str) {
        if (PetList.containsKey(str)) {
            return PetList.get(str);
        } else {
            Log("§cNon-PetData: " + str, true);
            return new PetData();
        }
    }

    static ShopData getShopData(String str) {
        if (ShopList.containsKey(str)) {
            return ShopList.get(str);
        } else {
            Log("§cNon-ShopList: " + str, true);
            return new ShopData();
        }
    }

    static String itemToString(ItemParameterStack item) {
        StringBuilder data;
        if (!item.isEmpty()) {
            ItemParameter itemParameter = item.itemParameter;
            ItemCategory category = itemParameter.Category;
            data = new StringBuilder(itemParameter.Id + ",Amount:" + item.Amount);
            if (category == ItemCategory.Equipment) {
                data.append(",Plus:").append(itemParameter.Plus).append(",Durable:").append(itemParameter.Durable);
                for (RuneParameter runeParameter : itemParameter.getRune()) {
                    if (!runeToString(runeParameter).equals("None"))
                    data.append(",Rune:").append(runeToString(runeParameter));
                }
            }
        } else {
            data = new StringBuilder("None");
        }
        return data.toString();
    }

    static String runeToString(RuneParameter rune) {
        String data;
        if (!rune.isEmpty()) {
            data = rune.Id + ";Level:" + rune.Level + ";Quality:" + String.format("%.5f",rune.Quality);
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
                    if (str.contains("Rune:")) {
                        itemParameter.addRune(stringToRune(str.replace("Rune:", "")));
                    }
                }
                parameterStack.itemParameter = itemParameter;
            } else {
                Log("§cError NotFoundItemData: " + split[0]);
            }
        }
        return parameterStack;
    }

    static RuneParameter stringToRune(String data) {
        RuneParameter runeParameter = new RuneParameter();
        if (!data.equals("None")) {
            String[] split = data.split(";");
            if (DataBase.RuneList.containsKey(split[0])) {
                runeParameter = getRuneParameter(split[0]);
                for (String str : split) {
                    if (str.contains("Level:")) {
                        runeParameter.Level = Integer.parseInt(str.replace("Level:", ""));
                    } else if (str.contains("Quality:")) {
                        runeParameter.Quality = Double.parseDouble((str.replace("Quality:", "")));
                    }
                }
            } else {
                Log("§cError NotFoundRuneData: " + split[0]);
            }
        }
        return runeParameter;
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
    RuneInventory("ルーンインベントリ"),
    PetInventory("ペットケージ"),
    HotBar("ホットバー"),
    ;

    String Display;

    ViewInventory(String Display) {
        this.Display = Display;
    }

    boolean isItem() {
        return this == ItemInventory;
    }

    boolean isRune() {
        return this == RuneInventory;
    }

    boolean isPet() {
        return this == PetInventory;
    }

    boolean isHotBar() {
        return this == HotBar;
    }
}

enum StrafeType {
    DoubleJump("ダブルジャンプ"),
    AirDash("空中ダッシュ"),
    All("すべての条件"),
    ;

    String Display;

    StrafeType(String Display) {
        this.Display = Display;
    }

    boolean isAirDash() {
        return this == AirDash || this == All;
    }

    boolean isDoubleJump() {
        return this == DoubleJump || this == All;
    }

    static StrafeType fromString(String str) {
        if (str != null) for (StrafeType strafeType : StrafeType.values()) {
            if (strafeType.toString().equalsIgnoreCase(str)) {
                return strafeType;
            }
        }
        return DoubleJump;
    }
}

enum DropLogType {
    None("非表示"),
    All("すべて表示"),
    Item("アイテムのみ"),
    Rune("ルーンのみ"),
    ;

    String Display;

    DropLogType(String Display) {
        this.Display = Display;
    }

    boolean isItem() {
        return this == Item || this == All;
    }

    boolean isRune() {
        return this == Rune || this == All;
    }

    static DropLogType fromString(String str) {
        if (str != null) for (DropLogType dropLogType : DropLogType.values()) {
            if (dropLogType.toString().equalsIgnoreCase(str)) {
                return dropLogType;
            }
        }
        return None;
    }
}

enum DamageLogType {
    None("非表示"),
    DamageOnly("ダメージのみ"),
    Detail("詳細情報"),
    All("すべて表示"),
    ;

    String Display;

    DamageLogType(String Display) {
        this.Display = Display;
    }

    boolean isDamageOnly() {
        return this == DamageOnly || this == Detail || this == All;
    }

    boolean isDetail() {
        return this == Detail || this == All;
    }

    boolean isAll() {
        return this == All;
    }

    static DamageLogType fromString(String str) {
        for (DamageLogType damageLogType : DamageLogType.values()) {
            if (damageLogType.toString().equalsIgnoreCase(str)) {
                return damageLogType;
            }
        }
        return None;
    }
}

class PlayerData {
    private final Plugin plugin;
    private final Player player;
    private boolean able = false;
    ItemInventory ItemInventory;
    HotBar HotBar;
    RuneInventory RuneInventory;
    PetInventory PetInventory;
    Equipment Equipment;
    Status Status;
    Classes Classes;
    Skill Skill;
    Menu Menu;
    Attribute Attribute;
    EffectManager Effect;

    String Nick;

    DamageLogType DamageLog = DamageLogType.None;
    boolean ExpLog = false;
    DropLogType DropLog = DropLogType.None;
    boolean PvPMode = false;
    boolean PlayMode = true;
    StrafeType StrafeMode = StrafeType.DoubleJump;
    CastType CastMode = CastType.Renewed;
    int Mel = 10000;
    int ViewFormat = 0;
    int Strafe = 2;
    MapData Map = MapList.get("Alden");
    boolean WallKicked = false;
    BukkitTask WallKickedTask;
    List<PetParameter> PetSummon = new ArrayList<>();
    PetParameter PetSelect;

    ViewInventory ViewInventory = swordofmagic7.ViewInventory.ItemInventory;

    PlayerData(Player player) {
        plugin = System.plugin;
        this.player = player;
        ItemInventory = new ItemInventory(player, this);
        HotBar = new HotBar(player, this);
        RuneInventory = new RuneInventory(player, this);
        PetInventory = new PetInventory(player, this);
        Equipment = new Equipment(player, this);
        Classes = new Classes(player, this);
        Skill = new Skill(player, this, plugin);
        Status = new Status(player, this, Classes, Skill);
        Menu = new Menu(player, this);
        Attribute = new Attribute(player, this);
        Effect = new EffectManager(player, this);

        Nick = player.getName();

        able = true;
    }

    void DamageLog() {
        switch (DamageLog) {
            case None -> DamageLog(DamageLogType.DamageOnly);
            case DamageOnly -> DamageLog(DamageLogType.Detail);
            case Detail -> DamageLog(DamageLogType.All);
            case All -> DamageLog(DamageLogType.None);
        }
    }
    void DamageLog(DamageLogType bool) {
        DamageLog = bool;
        String msg = "§c[ダメージログ]§aを";
        msg += "§b[" + DamageLog.Display + "]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
    }

    void ExpLog() {
        ExpLog(!ExpLog);
    }
    void ExpLog(boolean bool) {
        ExpLog = bool;
        String msg = "§e[経験値ログ]§aを";
        if (bool) msg += "§b[有効]";
        else msg += "§c[無効]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
    }

    void DropLog() {
        switch (DropLog) {
            case None -> DropLog(DropLogType.All);
            case All -> DropLog(DropLogType.Item);
            case Item -> DropLog(DropLogType.Rune);
            case Rune -> DropLog(DropLogType.None);
        }
    }
    void DropLog(DropLogType bool) {
        DropLog = bool;
        String msg = "§e[ドロップログ]§aを";
        msg += "§b[" + DropLog.Display + "]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
    }

    void PvPMode() {
        PvPMode(!PvPMode);
    }
    void PvPMode(boolean bool) {
        PvPMode = bool;
        String msg = "§ePvP§aを";
        if (bool) msg += "§b有効";
        else msg += "§c無効";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
    }

    void StrafeMode() {
        switch (StrafeMode) {
            case DoubleJump -> StrafeMode(StrafeType.AirDash);
            case AirDash -> StrafeMode(StrafeType.All);
            case All -> StrafeMode(StrafeType.DoubleJump);
        }
    }

    void StrafeMode(StrafeType mode) {
        StrafeMode = mode;
        String msg = "§e[ストレイフ条件]§aを";
        msg += "§b[" + StrafeMode.Display + "]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
    }

    void CastMode() {
        switch (CastMode) {
            case Renewed -> CastMode(CastType.Legacy);
            case Legacy -> CastMode(CastType.Hold);
            case Hold -> CastMode(CastType.Renewed);
        }
    }
    void CastMode(CastType bool) {
        CastMode = bool;
        String msg = "§e[キャストモード]§aを";
        msg += "§b[" + CastMode.Display + "]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
    }

    String ViewFormat() {
        return "%." + ViewFormat + "f";
    }

    void setViewFormat(int ViewFormat) {
        this.ViewFormat = ViewFormat;
        player.sendMessage("§e表記小数桁数§aを§e[" + ViewFormat + "桁]§aに§e設定§aしました");
        playSound(player, SoundList.Click);
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

        boolean rollback = false;
        for (Map.Entry<String, ClassData> classData : getClassList().entrySet()) {
            if (Classes.getLevel(classData.getValue()) == data.getInt("ClassData." + classData.getKey() + ".Level", 0)) {
                if (Classes.getExp(classData.getValue()) < data.getInt("ClassData." + classData.getKey() + ".Exp", 0)) {
                    rollback = true;
                }
            } else if (Classes.getLevel(classData.getValue()) < data.getInt("ClassData." + classData.getKey() + ".Level", 0)) {
                rollback = true;
            }
        }

        if (rollback) {
            player.sendMessage("§eロールバック§aを検知したため§bセーブ§aを中断しました");
            Log("§cロールバック検知: §f" + player.getName() + ", " + player.getUniqueId());
            return;
        }

        data.set("Location.x", player.getLocation().getX());
        data.set("Location.y", player.getLocation().getY());
        data.set("Location.z", player.getLocation().getZ());
        data.set("Location.yaw", player.getLocation().getYaw());
        data.set("Location.pitch", player.getLocation().getPitch());

        data.set("Mel", Mel);
        data.set("Health", Status.Health);
        data.set("Mana", Status.Mana);

        data.set("Setting.DamageLog", DamageLog.toString());
        data.set("Setting.ExpLog", ExpLog);
        data.set("Setting.DropLog", DropLog.toString());
        data.set("Setting.PvPMode", PvPMode);
        data.set("Setting.CastMode", CastMode.toString());
        data.set("Setting.StrafeMode", StrafeMode.toString());
        data.set("Setting.ViewFormat", ViewFormat);
        data.set("Setting.PlayMode", PlayMode);

        for (Map.Entry<String, ClassData> classData : getClassList().entrySet()) {
            data.set("ClassData." + classData.getKey() + ".Level", Classes.getLevel(classData.getValue()));
            data.set("ClassData." + classData.getKey() + ".Exp", Classes.getExp(classData.getValue()));
        }

        for (int i = 0; i <= MaxTier; i++) {
            if (Classes.classTier[i] != null) {
                data.set("Class.Tier" + i, Classes.classTier[i].Id);
            } else {
                data.set("Class.Tier" + i, "None");
            }
        }

        data.set("Attribute.Point", Attribute.getAttributePoint());
        for (AttributeType attr : AttributeType.values()) {
            data.set("Attribute." + attr.toString(), Attribute.getAttribute(attr));
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            data.set("Inventory." + slot.toString(), itemToString(new ItemParameterStack(Equipment.getEquip(slot))));
        }
        List<String> itemList = new ArrayList<>();
        for (ItemParameterStack stack : ItemInventory.getList()) {
            itemList.add(itemToString(stack));
        }
        data.set("Inventory.ItemList", itemList);

        List<String> runeList = new ArrayList<>();
        for (RuneParameter rune : RuneInventory.getList()) {
            runeList.add(runeToString(rune));
        }
        data.set("Inventory.RuneList", runeList);

        List<String> petList = new ArrayList<>();
        for (PetParameter pet : PetInventory.getList()) {
            petList.add(pet.toString());
        }
        data.set("Inventory.PetList", petList);

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
            double x = data.getDouble("Location.x", SpawnLocation.getX());
            double y = data.getDouble("Location.y", SpawnLocation.getY());
            double z = data.getDouble("Location.z", SpawnLocation.getZ());
            float yaw = (float) data.getDouble("Location.yaw", SpawnLocation.getYaw());
            float pitch = (float) data.getDouble("Location.pitch", SpawnLocation.getPitch());
            Location loc = new Location(world, x, y, z, yaw, pitch);
            player.teleportAsync(loc);

            Mel = data.getInt("Mel", 10000);
            Status.Health = data.getDouble("Health", 20);
            Status.Mana = data.getDouble("Mana", 100);

            DamageLog = DamageLogType.fromString(data.getString("Setting.DamageLog"));
            ExpLog = data.getBoolean("Setting.ExpLog", false);
            DropLog = DropLogType.fromString(data.getString("Setting.DropLog"));
            CastMode = CastType.valueOf(data.getString("Setting.CastMode", "Renewed"));
            StrafeMode = StrafeType.fromString(data.getString("Setting.StrafeMode"));
            PvPMode = data.getBoolean("Setting.PvPMode", false);
            PlayMode = data.getBoolean("Setting.PlayMode", true);

            for (Map.Entry<String, ClassData> classData : getClassList().entrySet()) {
                Classes.setLevel(classData.getValue(), data.getInt("ClassData." + classData.getKey() + ".Level"));
                Classes.setExp(classData.getValue(), data.getInt("ClassData." + classData.getKey() + ".Exp"));
            }

            for (int i = 0; i <= MaxTier; i++) {
                String id = data.getString("Class.Tier" + i, "None");
                if (!id.equalsIgnoreCase("None"))
                Classes.classTier[i] = getClassData(id);
            }

            Attribute.setPoint(data.getInt("Attribute.Point"));
            for (AttributeType attr : AttributeType.values()) {
                Attribute.setAttribute(attr, data.getInt("Attribute." + attr.toString()));
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

            List<String> runeList = data.getStringList("Inventory.RuneList");
            RuneInventory.clear();
            for (String runeData : runeList) {
                RuneParameter rune = stringToRune(runeData);
                if (!rune.isEmpty()) RuneInventory.addRuneParameter(rune);
            }

            List<String> petList = data.getStringList("Inventory.PetList");
            PetInventory.clear();
            for (String petData : petList) {
                PetParameter pet = new PetParameter(player, this, petData);
                PetInventory.addPetParameter(pet);
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

            if (PlayMode) viewUpdate();

            Status.StatusUpdate();
            Status.tickUpdate();
        } else {
            Status.StatusUpdate();
            Status.tickUpdate();
            player.teleportAsync(SpawnLocation);
            Status.Health = Status.MaxHealth;
            Status.Mana = Status.MaxMana;
        }
    }

    ItemStack UserMenuIcon() {
        List<String> Lore = new ArrayList<>();
        Lore.add("§a§lユーザーメニューを開きます");
        Lore.add("§a§lシフトクリックでインベントリ表示を");
        Lore.add("§a§l瞬時に切り替えることが出来ます");
        Lore.add(decoText("§3§lインベントリ表示"));
        Lore.add(decoLore("§e§lインベントリ表示") + ViewInventory.Display);
        if (ViewInventory.isItem())
            Lore.add(decoLore("§e§lインベントリ容量") + ItemInventory.getList().size() + "/300");
        else if (ViewInventory.isRune())
            Lore.add(decoLore("§e§lインベントリ容量") + RuneInventory.getList().size() + "/300");
        else if (ViewInventory.isPet())
            Lore.add(decoLore("§e§lペットケージ容量") + PetInventory.getList().size() + "/100");
        return new ItemStackData(Material.BOOK, decoText("§e§lユーザーメニュー"), Lore).view();
    }

    void viewUpdate() {
        switch (ViewInventory) {
            case ItemInventory -> ItemInventory.viewInventory();
            case RuneInventory -> RuneInventory.viewRune();
            case PetInventory -> PetInventory.viewPet();
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
        if (log) player.sendMessage("§eインベントリ表示§aを§e[" + ViewInventory.Display + "]§aに切り替えました");
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
            player.sendTitle("§4§lYou Are Dead", "", 20, 60, 20);
            Bukkit.getScheduler().runTaskLater(plugin,() -> {
                player.teleportAsync(player.getWorld().getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                Status.Health = Status.MaxHealth;
                Status.Mana = Status.MaxMana;
            }, 100);
        });
    }
}