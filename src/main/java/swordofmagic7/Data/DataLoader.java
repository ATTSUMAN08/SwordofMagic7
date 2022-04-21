package swordofmagic7.Data;

import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Horse;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Dungeon.DefenseBattle;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemCategory;
import swordofmagic7.Item.ItemExtend.ItemPotionType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemUseList.RewardBoxData;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Life.Angler.AnglerData;
import swordofmagic7.Life.Angler.AnglerItemData;
import swordofmagic7.Life.Cook.CookData;
import swordofmagic7.Life.Cook.CookItemData;
import swordofmagic7.Life.Harvest.HarvestData;
import swordofmagic7.Life.Harvest.HarvestItemData;
import swordofmagic7.Life.Lumber.LumberData;
import swordofmagic7.Life.Lumber.LumberItemData;
import swordofmagic7.Life.Mine.MineData;
import swordofmagic7.Life.Mine.MineItemData;
import swordofmagic7.Life.Smith.MakeData;
import swordofmagic7.Life.Smith.MakeItemData;
import swordofmagic7.Life.Smith.SmeltData;
import swordofmagic7.Map.MapData;
import swordofmagic7.Menu.TitleMenu;
import swordofmagic7.Mob.*;
import swordofmagic7.Npc.NpcData;
import swordofmagic7.Pet.PetData;
import swordofmagic7.Shop.ItemRecipe;
import swordofmagic7.Shop.ShopData;
import swordofmagic7.Shop.ShopSlot;
import swordofmagic7.Skill.SkillClass.Alchemist.AlchemyData;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillParameter;
import swordofmagic7.Skill.SkillType;
import swordofmagic7.Status.StatusParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.decoText;

public class DataLoader {

    public static void AllLoad() {
        ItemDataLoad();
        RuneDataLoad();
        PetDataLoad();
        RecipeDataLoad();
        MapDataLoad();
        LifeDataLoad();
        SkillDataLoad();
        ClassDataLoad();
        MobDataLoad();
        ShopDataLoad();
        TitleDataLoad();
        NpcDataLoad();
        MobSpawnerDataLoad();
        ItemInfoDataLoad();
        DefenseBattleMobListLoad();
        RewardBoxListLoad();
        Log("§aDataLoader -> AllLoad");
    }

    public static void ItemDataLoad() {
        File itemDirectories = new File(DataBasePath, "ItemData");
        List<File> itemFiles = dumpFile(itemDirectories);
        for (File file : itemFiles) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                ItemParameter itemParameter = new ItemParameter();
                itemParameter.File = file;
                itemParameter.Id = fileName;
                itemParameter.Display = data.getString("Display");
                itemParameter.Lore = data.getStringList("Lore");
                itemParameter.Sell = data.getInt("Sell");
                itemParameter.Category = ItemCategory.Item.getItemCategory(data.getString("Category"));
                itemParameter.CustomModelData = data.getInt("Model", 0);
                if (data.isSet("Materialization")) {
                    itemParameter.Materialization = data.getString("Materialization");
                    if (!MaterializationMap.containsKey(itemParameter.Materialization)) MaterializationMap.put(itemParameter.Materialization, new ArrayList<>());
                    if (itemParameter.Category.isEquipment()) MaterializationMap.get(itemParameter.Materialization).add(itemParameter.Id);
                }
                if (data.isSet("Color.R")) {
                    itemParameter.color = Color.fromRGB(data.getInt("Color.R"), data.getInt("Color.G"), data.getInt("Color.B"));
                }
                if (itemParameter.Category.isPetEgg()) {
                    itemParameter.itemPetEgg.PetId = data.getString("PetId");
                    itemParameter.itemPetEgg.PetMaxLevel = data.getInt("PetMaxLevel");
                    itemParameter.itemPetEgg.PetLevel = data.getInt("PetLevel");
                } else if (itemParameter.Category.isPotion()) {
                    itemParameter.itemPotion.PotionType = ItemPotionType.valueOf(data.getString("Potion.Type"));
                    itemParameter.itemPotion.CoolTime = data.getInt("Potion.CoolTime");
                    for (int i = 0; i < 4; i++) {
                        itemParameter.itemPotion.Value[i] = data.getDouble("Potion.Value." + i);
                    }
                } else if (itemParameter.Category.isCook()) {
                    itemParameter.itemCook.isBuff = data.getBoolean("Cook.Buff", false);
                    itemParameter.itemCook.BuffTime = data.getInt("Cook.BuffTime", 0);
                    itemParameter.itemCook.CoolTime = data.getInt("Cook.CoolTime", 0);
                    itemParameter.itemCook.Health = data.getDouble("Cook.Health", 0);
                    itemParameter.itemCook.Mana = data.getDouble("Cook.Mana", 0);
                    for (StatusParameter param : StatusParameter.values()) {
                        String fixed = "Cook.Fixed." + param;
                        String multiply = "Cook.Multiply." + param;
                        if (data.isSet(fixed)) {
                            itemParameter.itemCook.Fixed.put(param, data.getDouble(fixed));
                        }
                        if (data.isSet(multiply)) {
                            itemParameter.itemCook.Multiply.put(param, data.getDouble(multiply));
                        }
                    }
                } else if (itemParameter.Category.isPetFood()) {
                    itemParameter.itemPetFood.Stamina = data.getInt("PetFood.Stamina");
                }
                if (data.isSet("Material")) {
                    itemParameter.Icon = Material.getMaterial(data.getString("Material", "BARRIER"));
                    if (itemParameter.Icon == Material.PLAYER_HEAD) {
                        itemParameter.IconData = data.getString("PlayerHead");
                    }
                } else if (itemParameter.Category == ItemCategory.Equipment) {
                    itemParameter.itemEquipmentData.EquipmentCategory = EquipmentCategory.getEquipmentCategory(data.getString("EquipmentCategory"));
                    itemParameter.Icon = itemParameter.itemEquipmentData.EquipmentCategory.material;
                    itemParameter.itemEquipmentData.ReqLevel = data.getInt("ReqLevel");
                    itemParameter.itemEquipmentData.RuneSlot = data.getInt("RuneSlot");
                    itemParameter.itemEquipmentData.UpgradeCost = data.getInt("UpgradeCost");
                    itemParameter.itemEquipmentData.Durable = data.getInt("Durable");
                    itemParameter.itemEquipmentData.MaxDurable = itemParameter.itemEquipmentData.Durable;
                    itemParameter.itemEquipmentData.EquipmentSlot = EquipmentSlot.MainHand.getEquipmentSlot(data.getString("EquipmentSlot"));
                    itemParameter.itemEquipmentData.RuneMultiply = data.getDouble("RuneMultiply", 1);
                    double statusMultiply = data.getDouble("StatusMultiply", 1);
                    for (StatusParameter param : StatusParameter.values()) {
                        if (data.isSet(param.toString())) {
                            itemParameter.itemEquipmentData.Parameter.put(param, data.getDouble(param.toString()) * statusMultiply);
                        }
                    }
                }
                ItemList.put(itemParameter.Id, itemParameter);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void RuneDataLoad() {
        File runeDirectories = new File(DataBasePath, "RuneData/");
        List<File> runeFile = dumpFile(runeDirectories);
        for (File file : runeFile) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                RuneParameter runeData = new RuneParameter();
                runeData.Id = fileName;
                runeData.Icon = Material.getMaterial(data.getString("Icon"));
                runeData.Display = data.getString("Display");
                runeData.Lore = data.getStringList("Lore");
                runeData.isSpecial = data.getBoolean("isSpecial", false);
                for (StatusParameter param : StatusParameter.values()) {
                    if (data.isSet(param.toString())) {
                        runeData.Parameter.put(param, data.getDouble(param.toString()));
                    } else {
                        runeData.Parameter.put(param, 0d);
                    }
                }
                int i = 0;
                while (data.isSet("Parameter-" + i + ".Display")) {
                    SkillParameter param = new SkillParameter();
                    param.Display = data.getString("Parameter-" + i + ".Display");
                    param.Value = data.getDouble("Parameter-" + i + ".Value");
                    param.Increase = data.getDouble("Parameter-" + i + ".Increase");
                    param.Prefix = data.getString("Parameter-" + i + ".Prefix");
                    param.Suffix = data.getString("Parameter-" + i + ".Suffix");
                    param.Format = data.getInt("Parameter-" + i + ".Format");
                    runeData.AdditionParameter.add(param);
                    i++;
                }
                RuneList.put(runeData.Id, runeData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void PetDataLoad() {
        File petDirectories = new File(DataBasePath, "PetData/");
        List<File> petFile = dumpFile(petDirectories);
        for (File file : petFile) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                PetData petData = new PetData();
                petData.Id = fileName;
                petData.Display = data.getString("Display");
                petData.Lore = data.getStringList("Lore");
                petData.entityType = EntityType.fromName(data.getString("Type").toUpperCase());
                if (data.isSet("Disguise.Type")) {
                    DisguiseType disguiseType = DisguiseType.valueOf(data.getString("Disguise.Type").toUpperCase());
                    if (disguiseType == DisguiseType.PLAYER) {
                        petData.disguise = new PlayerDisguise(data.getString("Disguise.Player"));
                    } else {
                        petData.disguise = new MobDisguise(disguiseType);
                    }
                    disguiseLoader(petData.disguise, data);
                }
                petData.Icon = Material.getMaterial(data.getString("Icon", "BARRIER"));
                petData.MaxStamina = data.getDouble("MaxStamina");
                petData.MaxHealth = data.getDouble("MaxHealth");
                petData.HealthRegen = data.getDouble("HealthRegen");
                petData.MaxMana = data.getDouble("MaxMana");
                petData.ManaRegen = data.getDouble("ManaRegen");
                petData.ATK = data.getDouble("ATK");
                petData.DEF = data.getDouble("DEF");
                petData.HLP = data.getDouble("HLP");
                petData.ACC = data.getDouble("ACC");
                petData.EVA = data.getDouble("EVA");
                petData.CriticalRate = data.getDouble("CriticalRate");
                petData.CriticalResist = data.getDouble("CriticalResist");
                petData.BossPet = data.getBoolean("BossPet", false);
                PetList.put(fileName, petData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void RecipeDataLoad() {
        for (File file : dumpFile(new File(DataBasePath, "Recipe/"))) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                ItemRecipe itemRecipe = new ItemRecipe();
                for (String str : data.getStringList("ReqStack")) {
                    String[] split = str.split(",");
                    ItemParameter itemParameter = getItemParameter(split[0]);
                    ItemParameterStack stack = new ItemParameterStack(itemParameter);
                    for (String str2 : split) {
                        if (str2.contains("Amount:")) {
                            stack.Amount = Integer.parseInt(str2.replace("Amount:", ""));
                        }
                    }
                    itemRecipe.ReqStack.add(stack);
                }
                ItemRecipeList.put(fileName, itemRecipe);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void MapDataLoad() {
        File mapDirectories = new File(DataBasePath, "MapData/");
        List<File> mapFile = dumpFile(mapDirectories);
        for (File file : mapFile) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                MapData mapData = new MapData();
                mapData.Id = fileName;
                mapData.Display = data.getString("Display");
                mapData.Color = data.getString("Color");
                mapData.Level = data.getInt("Level");
                mapData.Safe = data.getBoolean("Safe");
                mapData.ReqCombatPower = data.getDouble("ReqCombatPower", mapData.Level*10);
                if (data.isSet("Life.Mine")) {
                    for (String str : data.getStringList("Life.Mine")) {
                        String[] split = str.split(",");
                        mapData.GatheringData.put(split[0], split[1]);
                    }
                }
                if (data.isSet("Life.Lumber")) {
                    for (String str : data.getStringList("Life.Lumber")) {
                        String[] split = str.split(",");
                        mapData.GatheringData.put(split[0], split[1]);
                    }
                }
                if (data.isSet("Life.Harvest")) {
                    for (String str : data.getStringList("Life.Harvest")) {
                        String[] split = str.split(",");
                        mapData.GatheringData.put(split[0], split[1]);
                    }
                }
                if (data.isSet("Life.Angler")) {
                    int i = 0;
                    for (String str : data.getStringList("Life.Angler")) {
                        mapData.GatheringData.put("Fishing-" + i, str);
                        i++;
                    }
                }
                MapList.put(fileName, mapData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void LifeDataLoad() {
        File lifeMineDirectories = new File(DataBasePath, "Life/Mine");
        List<File> lifeMineFile = dumpFile(lifeMineDirectories);
        for (File file : lifeMineFile) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                int CoolTime = data.getInt("CoolTime");
                int Exp = data.getInt("Exp");
                int ReqLevel = data.getInt("ReqLevel");
                MineData lifeData = new MineData(CoolTime, Exp, ReqLevel);
                for (String str : data.getStringList("Item")) {
                    String[] split = str.split(",");
                    MineItemData itemData = new MineItemData();
                    itemData.itemParameter = getItemParameter(split[0]);
                    itemData.Percent = Double.parseDouble(split[1]);
                    lifeData.itemData.add(itemData);
                }
                MineDataList.put(fileName, lifeData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }

        File lifeLumberDirectories = new File(DataBasePath, "Life/Lumber");
        List<File> lifeLumberFile = dumpFile(lifeLumberDirectories);
        for (File file : lifeLumberFile) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                int CoolTime = data.getInt("CoolTime");
                int Exp = data.getInt("Exp");
                int ReqLevel = data.getInt("ReqLevel");
                LumberData lifeData = new LumberData(CoolTime, Exp, ReqLevel);
                for (String str : data.getStringList("Item")) {
                    String[] split = str.split(",");
                    LumberItemData itemData = new LumberItemData();
                    itemData.itemParameter = getItemParameter(split[0]);
                    itemData.Percent = Double.parseDouble(split[1]);
                    lifeData.itemData.add(itemData);
                }
                LumberDataList.put(fileName, lifeData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }

        File lifeHarvestDirectories = new File(DataBasePath, "Life/Harvest");
        List<File> lifeHarvestFile = dumpFile(lifeHarvestDirectories);
        for (File file : lifeHarvestFile) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                int CoolTime = data.getInt("CoolTime");
                int Exp = data.getInt("Exp");
                int ReqLevel = data.getInt("ReqLevel");
                HarvestData lifeData = new HarvestData(CoolTime, Exp, ReqLevel);
                for (String str : data.getStringList("Item")) {
                    String[] split = str.split(",");
                    HarvestItemData itemData = new HarvestItemData();
                    itemData.itemParameter = getItemParameter(split[0]);
                    itemData.Percent = Double.parseDouble(split[1]);
                    lifeData.itemData.add(itemData);
                }
                HarvestDataList.put(fileName, lifeData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }

        File lifeAnglerDirectories = new File(DataBasePath, "Life/Angler");
        for (File file : dumpFile(lifeAnglerDirectories)) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                int Exp = data.getInt("Exp");
                int ReqLevel = data.getInt("ReqLevel");
                AnglerData lifeData = new AnglerData(Exp, ReqLevel);
                for (String str : data.getStringList("Item")) {
                    String[] split = str.split(",");
                    AnglerItemData itemData = new AnglerItemData();
                    itemData.itemParameter = getItemParameter(split[0]);
                    itemData.Percent = Double.parseDouble(split[1]);
                    itemData.expMultiply = Double.parseDouble(split[2]);
                    lifeData.itemData.add(itemData);
                }
                AnglerDataList.put(fileName, lifeData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }

        for (File file : dumpFile(new File(DataBasePath, "Life/Cook"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                List<CookItemData> cookItem = new ArrayList<>();
                List<String> list = data.getStringList("Item");
                for (String str : list) {
                    String[] split = str.split(",");
                    cookItem.add(new CookItemData(getItemParameter(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[2])));
                }
                ItemParameter viewItem = getItemParameter(data.getString("ViewItem"));
                int viewAmount = data.getInt("ViewAmount");
                int ReqLevel = data.getInt("ReqLevel");
                int Exp = data.getInt("Exp");
                ItemRecipe recipe = getItemRecipe(data.getString("Recipe"));
                CookData lifeData = new CookData(cookItem, viewItem, viewAmount, ReqLevel, Exp, recipe);
                CookDataList.put(fileName, lifeData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }

        for (File file : dumpFile(new File(DataBasePath, "Life/Alchemy"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                if (!fileName.equals("GUI")) {
                    ItemParameter itemParameter = getItemParameter(data.getString("Item"));
                    ItemRecipe itemRecipe = getItemRecipe(data.getString("Recipe"));
                    int Amount = data.getInt("Amount");
                    int Exp = data.getInt("Exp");
                    int ReqLevel = data.getInt("ReqLevel");
                    AlchemyData alchemyData = new AlchemyData(itemParameter, Amount, ReqLevel, Exp, itemRecipe);
                    AlchemyDataList.put(fileName, alchemyData);
                } else {
                    AlchemyShopMap.clear();
                    int slot = 0;
                    for (String str : data.getStringList("GUI")) {
                        String[] split  = str.split(",Slot:");
                        slot = split.length == 2 ? Integer.parseInt(split[1]) : slot+1;
                        AlchemyShopMap.put(slot, split[0]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }

        for (File file : dumpFile(new File(DataBasePath, "Life/Smelt"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                ItemParameter item = getItemParameter(data.getString("Item"));
                int Amount = data.getInt("Amount");
                int ReqLevel = data.getInt("ReqLevel");
                int Exp = data.getInt("Exp");
                ItemRecipe recipe = getItemRecipe(data.getString("Recipe"));
                SmeltData lifeData = new SmeltData(item, Amount, ReqLevel, Exp, recipe);
                SmeltDataList.put(fileName, lifeData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }

        for (File file : dumpFile(new File(DataBasePath, "Life/Make"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                if (!fileName.equals("GUI")) {
                    MakeData makeData = new MakeData();
                    List<MakeItemData> list = new ArrayList<>();
                    for (String str : data.getStringList("Item")) {
                        MakeItemData makeItemData = new MakeItemData();
                        String[] split = str.split(",");
                        makeItemData.itemParameter = getItemParameter(split[0]);
                        makeItemData.Amount = Integer.parseInt(split[1]);
                        makeItemData.Percent = Double.parseDouble(split[2]);
                        list.add(makeItemData);
                    }
                    makeData.Display = data.getString("Display");
                    makeData.Icon = Material.getMaterial(data.getString("Icon", "BARRIER"));
                    makeData.makeList = list;
                    makeData.ReqLevel = data.getInt("ReqLevel");
                    makeData.Exp = data.getInt("Exp");
                    makeData.itemRecipe = getItemRecipe(data.getString("Recipe"));
                    MakeDataList.put(fileName, makeData);
                } else {
                    int slot = 0;
                    for (String str : data.getStringList("MakeGUI")) {
                        String[] split = str.split(",");
                        if (split.length != 1) {
                            int i = Integer.parseInt(split[1]);
                            slot = i == -1 ? (int) Math.ceil(slot/9f)*9 : i;
                        }
                        MakeGUIMap.put(slot, split[0]);
                        slot++;
                        if (MaxMakeSlot < slot) MaxMakeSlot = slot;
                        if (TitleMenu.nonSlotVertical(slot)) slot++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }
    public static int MaxMakeSlot = 0;

    public static void SkillDataLoad() {
        File skillDirectories = new File(DataBasePath, "SkillData/");
        List<File> skillFile = dumpFile(skillDirectories);
        for (File file : skillFile) {
            try {
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
                skillData.SkillType = SkillType.valueOf(data.getString("SkillType"));
                skillData.ReqLevel = data.getInt("ReqLevel");
                int i = 0;
                while (data.isSet("Parameter-" + i + ".Display")) {
                    SkillParameter param = new SkillParameter();
                    param.Display = data.getString("Parameter-" + i + ".Display");
                    param.Value = data.getDouble("Parameter-" + i + ".Value");
                    param.Increase = data.getDouble("Parameter-" + i + ".Increase");
                    param.Prefix = data.getString("Parameter-" + i + ".Prefix");
                    param.Suffix = data.getString("Parameter-" + i + ".Suffix");
                    param.Format = data.getInt("Parameter-" + i + ".Format");
                    skillData.Parameter.add(param);
                    i++;
                }
                if (skillData.SkillType.isActive()) {
                    for (String str : data.getStringList("ReqMainHand")) {
                        skillData.ReqMainHand.add(EquipmentCategory.getEquipmentCategory(str));
                    }
                    for (String str : data.getStringList("ReqOffHand")) {
                        skillData.ReqOffHand.add(EquipmentCategory.getEquipmentCategory(str));
                    }
                    skillData.Stack = data.getInt("Stack", 1);
                    skillData.Mana = data.getInt("Mana");
                    skillData.CastTime = data.getInt("CastTime");
                    skillData.RigidTime = data.getInt("RigidTime");
                    skillData.CoolTime = data.getInt("CoolTime");
                }
                SkillDataList.put(skillData.Id, skillData);
                SkillDataDisplayList.put(skillData.Display, skillData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void ClassDataLoad() {
        File classDirectories = new File(DataBasePath, "ClassData/");
        List<File> classFile = dumpFile(classDirectories);
        for (File file : classFile) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                if (!fileName.equals("GUI")) {
                    ClassData classData = new ClassData();
                    classData.Id = fileName;
                    classData.Color = data.getString("Color");
                    classData.Icon = Material.getMaterial(data.getString("Icon", "BARRIER"));
                    classData.Display = data.getString("Display");
                    classData.Lore = data.getStringList("Lore");
                    classData.Nick = data.getString("Nick");
                    if (data.isSet("ProductionClass")) classData.ProductionClass = data.getBoolean("ProductionClass");
                    List<SkillData> Skills = new ArrayList<>();
                    for (String str : data.getStringList("SkillList")) {
                        if (SkillDataList.containsKey(str)) {
                            Skills.add(SkillDataList.get(str));
                        }
                    }
                    classData.SkillList = Skills;
                    ClassList.put(fileName, classData);
                    ClassListDisplay.put(classData.Display, classData);
                } else {
                    ClassDataMap.clear();
                    for (String str : data.getStringList("ClassGUI")) {
                        String[] split = str.split(",");
                        ClassDataMap.put(Integer.parseInt(split[1]), split[0]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
        for (File file : classFile) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                if (data.isSet("ReqClass")) {
                    for (String str : data.getStringList("ReqClass")) {
                        String[] split = str.split(":");
                        ClassList.get(fileName).ReqClass.put(getClassData(split[0]), Integer.valueOf(split[1]));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void MobDataLoad() {
        File mobDirectories = new File(DataBasePath, "EnemyData/");
        List<File> mobFile = dumpFile(mobDirectories);
        for (File file : mobFile) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                MobData mobData = new MobData();
                mobData.Id = fileName;
                mobData.Display = data.getString("Display");
                mobData.Lore = data.getStringList("Lore");
                mobData.Invisible = data.getBoolean("Invisible", false);
                mobData.NoAI = data.getBoolean("NoAI", false);
                mobData.ColliderSize = data.getDouble("ColliderSize", 0);
                mobData.Glowing = data.getBoolean("Glowing", false);
                String entityType = data.getString("Type").toUpperCase();
                if (EntityType.fromName(entityType) != null) {
                    mobData.entityType = EntityType.valueOf(entityType);
                } else {
                    mobData.entityType = EntityType.SKELETON;
                    Log("§cError Non-EntityType: " + fileName);
                }
                mobData.Icon = Material.getMaterial(data.getString("Icon", mobData.entityType + "_SPAWN_EGG"));
                if (mobData.Icon == null) mobData.Icon = Material.PAPER;
                if (data.isSet("Disguise.Type")) {
                    DisguiseType disguiseType = DisguiseType.valueOf(data.getString("Disguise.Type").toUpperCase());
                    if (disguiseType == DisguiseType.PLAYER) {
                        mobData.disguise = new PlayerDisguise(data.getString("Disguise.Player"));
                    } else {
                        mobData.disguise = new MobDisguise(disguiseType);
                    }
                    disguiseLoader(mobData.disguise, data);
                }
                mobData.Health = data.getDouble("Health", 100);
                mobData.ATK = data.getDouble("ATK", 10);
                mobData.DEF = data.getDouble("DEF", 10);
                mobData.ACC = data.getDouble("ACC", 2);
                mobData.EVA = data.getDouble("EVA", 2);
                mobData.CriticalRate = data.getDouble("CriticalRate", 10);
                mobData.CriticalResist = data.getDouble("CriticalResist", 10);
                mobData.Exp = data.getDouble("Exp", 10);
                mobData.Mov = data.getDouble("Mov", 1.2);
                mobData.Reach = data.getDouble("Reach", 1.5);
                mobData.Search = data.getDouble("Search", 32);
                mobData.Hostile = data.getBoolean("Hostile", false);
                mobData.Size = data.getInt("Size", 0);
                if (data.isSet("EnemyType")) mobData.enemyType = EnemyType.valueOf(data.getString("EnemyType"));
                mobData.DamageRanking = data.getBoolean("DamageRanking", mobData.enemyType.isBoss());
                if (data.isSet("Skill")) {
                    List<MobSkillData> SkillList = new ArrayList<>();
                    for (String str : data.getStringList("Skill")) {
                        try {
                            String[] split = str.split(",");
                            MobSkillData mobSkillData = new MobSkillData();
                            mobSkillData.Skill = split[0];
                            for (String skillData : split) {
                                if (skillData.contains("Percent:")) {
                                    mobSkillData.Percent = Double.parseDouble(skillData.replace("Percent:", ""));
                                } else if (skillData.contains("CoolTime:")) {
                                    mobSkillData.CoolTime = Integer.parseInt(skillData.replace("CoolTime:", ""));
                                } else if (skillData.contains("MaxHealth:")) {
                                    mobSkillData.maxHealth = Double.parseDouble(skillData.replace("MaxHealth:", ""));
                                } else if (skillData.contains("MinHealth:")) {
                                    mobSkillData.minHealth = Double.parseDouble(skillData.replace("MinHealth:", ""));
                                } else if (skillData.contains("Available:")) {
                                    mobSkillData.Available = Integer.parseInt(skillData.replace("Available:", ""));
                                } else if (skillData.contains("Interrupt:")) {
                                    mobSkillData.Interrupt = Boolean.parseBoolean((skillData.replace("Interrupt:", "")));
                                }
                            }
                            SkillList.add(mobSkillData);
                        } catch (Exception ignored) {
                            Log("MobData Format Error -> " + fileName);
                        }
                    }
                    mobData.SkillList = SkillList;
                }
                if (data.isSet("HPStop")) {
                    mobData.HPStop = data.getDoubleList("HPStop");
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
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void ShopDataLoad() {
        File shopDirectories = new File(DataBasePath, "ShopData/");
        List<File> shopFile = dumpFile(shopDirectories);
        for (File file : shopFile) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                ShopData shopData = new ShopData();
                shopData.Display = fileName;
                shopData.Page = data.getInt("Page", 1);
                int slot = 0;
                for (String str : data.getStringList("Data")) {
                    ShopSlot shopSlot = new ShopSlot();
                    String[] split = str.split(",");
                    shopSlot.itemParameter = getItemParameter(split[0]);
                    for (String str2 : split) {
                        if (str2.contains("Mel:")) {
                            shopSlot.Mel = Integer.parseInt(str2.replace("Mel:", ""));
                        } else if (str2.contains("Amount:")) {
                            shopSlot.Amount = Integer.parseInt(str2.replace("Amount:", ""));
                        } else if (str2.contains("Slot:")) {
                            String str3 = str2.replace("Slot:", "");
                            if (str3.contains("+")) {
                                slot += Integer.parseInt(str3.replace("+", ""));
                            } else {
                                int index = Integer.parseInt(str3);
                                if (index == -1) {
                                    slot = (int) (Math.ceil(slot / 9f) * 9);
                                }
                                else if (index == -2) {
                                    slot = (int) (Math.ceil(slot / 45f) * 45);
                                }
                                else slot = index;
                            }
                        } else if (str2.contains("Recipe:")) {
                            shopSlot.itemRecipe = getItemRecipe(str2.replace("Recipe:", ""));
                        }
                    }
                    shopData.Data.put(slot, shopSlot);
                    slot++;
                }
                ShopList.put(fileName, shopData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static int MaxTitleSlot = 0;
    public static void TitleDataLoad() {
        TitleGUIMap.clear();
        for (File file : dumpFile(new File(DataBasePath, "TitleData/"))) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                if (!fileName.equals("GUI")) {
                    Material icon = Material.getMaterial(data.getString("Icon", "BARRIER"));
                    int amount = data.getInt("Amount", 1);
                    TitleData titleData = new TitleData(fileName, icon, amount, data.getStringList("Display"), data.getStringList("Lore"));
                    titleData.attributePoint = data.getInt("AttributePoint", 1);
                    titleData.isHidden = data.getBoolean("Hidden", false);
                    if (titleData.isHidden) HiddenTitleDataList.add(titleData);
                    TitleDataList.put(fileName, titleData);
                } else {
                    int slot = 0;
                    for (String str : data.getStringList("TitleGUI")) {
                        String[] split = str.split(",");
                        if (split.length != 1) {
                            int i = Integer.parseInt(split[1]);
                            slot = i == -1 ? (int) Math.ceil(slot/9f)*9 : i;
                        }
                        TitleGUIMap.put(slot, split[0]);
                        slot++;
                        if (MaxTitleSlot < slot) MaxTitleSlot = slot;
                        if (TitleMenu.nonSlotVertical(slot)) slot++;
                    }
                }
            } catch (Exception e) {
                loadError(file);
            }
        }
    }

    public static void NpcDataLoad() {
        File npcDirectories = new File(DataBasePath, "Npc");
        List<File> npcFiles = dumpFile(npcDirectories);
        for (File file : npcFiles) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            NpcData npcData = new NpcData();
            npcData.Message = data.getStringList("Message");
            NpcList.put(Integer.valueOf(fileName), npcData);
        }
    }

    public static void MobSpawnerDataLoad() {
        for (File file : dumpFile(new File(DataBasePath, "Spawner/"))) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                MobSpawnerData mobSpawnerData = new MobSpawnerData();
                mobSpawnerData.Id = fileName;
                mobSpawnerData.mobData = getMobData(data.getString("MobData"));
                mobSpawnerData.Level = data.getInt("Level");
                mobSpawnerData.Radius = data.getInt("Radius");
                mobSpawnerData.RadiusY = data.getInt("RadiusY");
                mobSpawnerData.MaxMob = data.getInt("MaxMob");
                mobSpawnerData.PerSpawn = data.getInt("PerSpawn");
                mobSpawnerData.file = file;
                mobSpawnerData.DeathTrigger = data.getString("DeathTrigger", null);
                double x = data.getDouble("Location.x");
                double y = data.getDouble("Location.y");
                double z = data.getDouble("Location.z");
                mobSpawnerData.location = new Location(Bukkit.getWorld(data.getString("Location.w", "world")), x, y, z);
                MobSpawnerList.put(fileName, mobSpawnerData);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void ItemInfoDataLoad() {
        for (ItemParameter itemData : ItemList.values()) {
            ItemInfoData.put(itemData.Id, new ArrayList<>());
            ItemInfoData.get(itemData.Id).addAll(itemData.viewItem(1, "%.0f").getLore());
            ItemInfoData.get(itemData.Id).add(decoText("§3§l入手方法"));
        }
        for (MobData mobData : MobList.values()) {
            for (DropItemData dropData : mobData.DropItemTable) {
                ItemInfoData.get(dropData.itemParameter.Id).add("§7・§e§l" + mobData.Display + " §b§l-> §e§l" + dropData.Percent*100 + "%");
            }
        }
        for (ShopData shopData : ShopList.values()) {
            for (ShopSlot shopSlot : shopData.Data.values()) {
                ItemInfoData.get(shopSlot.itemParameter.Id).add("§7・§e§l" + shopData.Display + " §b§l-> §e§l購入§a§lor§e§l制作");
            }
        }
        for (CookData cookData : CookDataList.values()) {
            for (CookItemData cookItemData : cookData.CookItemData) {
                ItemInfoData.get(cookItemData.itemParameter.Id).add("§7・§e§l料理場");
            }
        }
        for (SmeltData smeltData : SmeltDataList.values()) {
            ItemInfoData.get(smeltData.itemParameter.Id).add("§7・§e§l鍛冶場 §b§l-> §e§l精錬");
        }
        for (MakeData makeData : MakeDataList.values()) {
            for (MakeItemData makeItemData : makeData.makeList) {
                ItemInfoData.get(makeItemData.itemParameter.Id).add("§7・§e§l鍛冶場 §b§l-> §e§l装備制作 §b§l-> §e§l" + makeData.Display);
            }
        }
        for (ItemParameter itemData : ItemList.values()) {
            ItemInfoData.get(itemData.Id).add(decoText("§3§l使用用途"));
            if (itemData.Materialization != null) ItemInfoData.get(itemData.Id).add("§7・§e§l素材化装備" + itemData.Materialization);
        }
        for (Map.Entry<String, ItemRecipe> recipe : ItemRecipeList.entrySet()) {
            for (ItemParameterStack stack : recipe.getValue().ReqStack) {
                ItemInfoData.get(stack.itemParameter.Id).add("§7・§e§l" + recipe.getKey());
            }
        }
    }

    public static void DefenseBattleMobListLoad() {
        File file = new File(DataBasePath, "DefenseBattle.yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        DefenseBattle.MobList.clear();
        for (String name : data.getStringList("MobList")) {
            DefenseBattle.MobList.add(DataBase.getMobData(name));
        }
    }

    public static void RewardBoxListLoad() {
        for (File file : dumpFile(new File(DataBasePath, "RewardBox/"))) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                List<RewardBoxData> list = new ArrayList<>();
                for (String str : data.getStringList("RewardBox")) {
                    String[] split = str.split(",");
                    RewardBoxData rewardBoxData = new RewardBoxData();
                    rewardBoxData.id = split[0];
                    rewardBoxData.amount = Integer.parseInt(split[1]);
                    rewardBoxData.percent = Double.parseDouble(split[2]);
                    list.add(rewardBoxData);
                }
                RewardBoxList.put(fileName, list);
            } catch (Exception e) {
                e.printStackTrace();
                loadError(file);
            }
        }
    }

    public static void disguiseLoader(Disguise disguise, FileConfiguration data) {
        switch (disguise.getType()) {
            case SLIME -> {
                SlimeWatcher watcher = new SlimeWatcher(disguise);
                watcher.setSize(data.getInt("Disguise.Size", 1));
                disguise.setWatcher(watcher);
            }
            case PHANTOM -> {
                PhantomWatcher watcher = new PhantomWatcher(disguise);
                watcher.setSize(data.getInt("Disguise.Size", 1));
                disguise.setWatcher(watcher);
            }
            case HORSE -> {
                HorseWatcher watcher = new HorseWatcher(disguise);
                watcher.setStyle(data.isSet("Disguise.HorseStyle") ? Horse.Style.valueOf(data.getString("Disguise.HorseStyle")) : Horse.Style.NONE);
                disguise.setWatcher(watcher);
            }
            case RABBIT -> {
                RabbitWatcher watcher = new RabbitWatcher(disguise);
                watcher.setType(data.isSet("Disguise.RabbitType") ? RabbitType.valueOf(data.getString("Disguise.RabbitType")) : RabbitType.BROWN);
                disguise.setWatcher(watcher);
            }
            case FOX -> {
                FoxWatcher watcher = new FoxWatcher(disguise);
                watcher.setType(data.isSet("Disguise.FoxType") ? Fox.Type.valueOf(data.getString("Disguise.FoxType")) : Fox.Type.RED);
                disguise.setWatcher(watcher);
            }
            case CAT -> {
                CatWatcher watcher = new CatWatcher(disguise);
                watcher.setType(data.isSet("Disguise.CatType") ? Cat.Type.valueOf(data.getString("Disguise.CatType")) : Cat.Type.RED);
                disguise.setWatcher(watcher);
            }
            case PLAYER -> {
                PlayerWatcher watcher = new PlayerWatcher(disguise);
                watcher.setSkin(data.getString("Disguise.Player", "MomiNeko"));
                disguise.setWatcher(watcher);
            }
        }
    }
}
