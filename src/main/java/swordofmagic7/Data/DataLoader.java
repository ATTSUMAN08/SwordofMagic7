package swordofmagic7.Data;

import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.classes.ClassData;
import swordofmagic7.Dungeon.DefenseBattle;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemCategory;
import swordofmagic7.Item.ItemExtend.ItemPotionType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemUseList.RewardBox;
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
import net.somrpg.swordofmagic7.SomCore;
import swordofmagic7.Status.StatusParameter;

import java.io.File;
import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.decoText;

public class DataLoader {

    public static void AllLoad() {
        long start = System.currentTimeMillis();
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
        RuneInfoDataLoad();
        DefenseBattleMobListLoad();
        RewardBoxListLoad();
        SomCore.instance.getLogger().info("[DataLoader] 全データ読み込みが完了しました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void ItemDataLoad() {
        long start = System.currentTimeMillis();
        File itemDirectories = new File(DataBasePath, "ItemData");
        Function.createFolder(itemDirectories);
        for (File file : dumpFile(itemDirectories)) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                ItemParameter itemParameter = new ItemParameter();
                itemParameter.File = file;
                itemParameter.Id = fileName;
                itemParameter.Display = data.getString("Display");
                itemParameter.Lore = data.getStringList("Lore");
                itemParameter.Sell = data.getInt("Sell");
                itemParameter.Category = ItemCategory.getItemCategory(data.getString("Category", "None"));
                itemParameter.CustomModelData = data.getInt("Model", 0);
                itemParameter.isHide = data.getBoolean("isHide", false);
                itemParameter.isLoreHide = data.getBoolean("isLoreHide", false);
                itemParameter.isNonTrade = data.getBoolean("isNonTrade", false);
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
                    itemParameter.itemEquipmentData.equipmentCategory = EquipmentCategory.getEquipmentCategory(data.getString("EquipmentCategory"));
                    itemParameter.Icon = itemParameter.itemEquipmentData.equipmentCategory.material;
                    itemParameter.itemEquipmentData.ReqLevel = data.getInt("ReqLevel");
                    itemParameter.itemEquipmentData.RuneSlot = data.getInt("RuneSlot");
                    itemParameter.itemEquipmentData.UpgradeCost = data.getInt("UpgradeCost");
                    itemParameter.itemEquipmentData.EquipmentSlot = EquipmentSlot.MainHand.getEquipmentSlot(data.getString("EquipmentSlot"));
                    itemParameter.itemEquipmentData.RuneMultiply = data.getDouble("RuneMultiply", 1);
                    double statusMultiply = data.getDouble("StatusMultiply", 1);
                    if (itemParameter.itemEquipmentData.isAccessory()) itemParameter.itemEquipmentData.itemAccessory.maxSlot = data.getInt("MaxSlot", 1);
                    for (StatusParameter param : StatusParameter.values()) {
                        if (data.isSet(param.toString())) {
                            if (itemParameter.itemEquipmentData.isAccessory()) {
                                itemParameter.itemEquipmentData.itemAccessory.Base.put(param, data.getDouble(param.toString()) * statusMultiply);
                                itemParameter.itemEquipmentData.itemAccessory.Range.put(param, data.getDouble(param + "-Range", 1) * statusMultiply);
                            } else itemParameter.itemEquipmentData.Parameter.put(param, data.getDouble(param.toString()) * statusMultiply);
                        }
                    }
                }
                ItemList.put(itemParameter.Id, itemParameter);
            } catch (Exception e) {
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] ItemDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void RuneDataLoad() {
        long start = System.currentTimeMillis();
        File runeDirectories = new File(DataBasePath, "RuneData/");
        Function.createFolder(runeDirectories);
        for (File file : dumpFile(runeDirectories)) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                RuneParameter runeData = new RuneParameter();
                runeData.Id = fileName;
                runeData.Icon = Material.getMaterial(data.getString("Icon", "BARRIER"));
                runeData.Display = data.getString("Display");
                runeData.Lore = data.getStringList("Lore");
                runeData.isSpecial = data.getBoolean("isSpecial", false);
                runeData.isHide = data.getBoolean("isHide", false);
                runeData.isLoreHide = data.getBoolean("isLoreHide", false);
                runeData.isNonTrade = data.getBoolean("isNonTrade", false);
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] RuneDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void PetDataLoad() {
        long start = System.currentTimeMillis();
        File petDirectories = new File(DataBasePath, "PetData/");
        Function.createFolder(petDirectories);
        for (File file : dumpFile(petDirectories)) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                PetData petData = new PetData();
                petData.Id = fileName;
                petData.Display = data.getString("Display");
                petData.Lore = data.getStringList("Lore");
                petData.isNonTrade = data.getBoolean("isNonTrade", false);
                petData.entityType = EntityType.valueOf(data.getString("Type", "ZOMBIE").toUpperCase());
                if (data.isSet("Disguise.Type")) {
                    DisguiseType disguiseType = DisguiseType.valueOf(data.getString("Disguise.Type", "SKELETON").toUpperCase());
                    if (disguiseType == DisguiseType.PLAYER) {
                        petData.disguise = new PlayerDisguise(data.getString("Disguise.Player", "MomiNeko"));
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] PetDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void RecipeDataLoad() {
        long start = System.currentTimeMillis();
        File recipeDirectories = new File(DataBasePath, "Recipe/");
        Function.createFolder(recipeDirectories);
        for (File file : dumpFile(recipeDirectories)) {
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] RecipeDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void MapDataLoad() {
        long start = System.currentTimeMillis();
        File mapDirectories = new File(DataBasePath, "MapData/");
        Function.createFolder(mapDirectories);
        for (File file : dumpFile(mapDirectories)) {
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
                mapData.isRaid = data.getBoolean("isRaid", false);
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] MapDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void LifeDataLoad() {
        long start = System.currentTimeMillis();
        File lifeMineDirectories = new File(DataBasePath, "Life/Mine");
        Function.createFolder(lifeMineDirectories);
        for (File file : dumpFile(lifeMineDirectories)) {
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
                loadError(file, e.getMessage());
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
                loadError(file, e.getMessage());
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
                loadError(file, e.getMessage());
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
                loadError(file, e.getMessage());
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
                loadError(file, e.getMessage());
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
                loadError(file, e.getMessage());
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
                loadError(file, e.getMessage());
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] LifeDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }
    public static int MaxMakeSlot = 0;

    public static void SkillDataLoad() {
        long start = System.currentTimeMillis();
        File skillDirectories = new File(DataBasePath, "SkillData/");
        Function.createFolder(skillDirectories);
        for (File file : dumpFile(skillDirectories)) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                SkillData skillData = new SkillData();
                skillData.Id = fileName;
                skillData.Icon = Material.getMaterial(data.getString("Icon", "END_CRYSTAL"));
                skillData.Display = data.getString("Display", "名前が未設定のスキル");
                List<String> Lore = new ArrayList<>();
                for (String str : data.getStringList("Lore")) {
                    Lore.add("§a§l" + str);
                }
                skillData.Lore = Lore;
                skillData.SkillType = SkillType.valueOf(data.getString("SkillType", "ACTIVE").toUpperCase(Locale.ROOT));
                skillData.ReqLevel = data.getInt("ReqLevel", 1);
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
                for (String str : data.getStringList("ReqMainHand")) {
                    skillData.ReqMainHand.add(EquipmentCategory.getEquipmentCategory(str));
                }
                for (String str : data.getStringList("ReqOffHand")) {
                    skillData.ReqOffHand.add(EquipmentCategory.getEquipmentCategory(str));
                }
                if (skillData.SkillType.isActive()) {
                    skillData.Stack = data.getInt("Stack", 1);
                    skillData.Mana = data.getInt("Mana");
                    skillData.CastTime = data.getInt("CastTime");
                    skillData.RigidTime = data.getInt("RigidTime");
                    skillData.CoolTime = data.getInt("CoolTime");
                }
                SkillDataList.put(skillData.Id, skillData);
                SkillDataDisplayList.put(skillData.Display, skillData);
            } catch (Exception e) {
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] SkillDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void moveToTop(List<File> list, List<String> targetNames) {
        List<File> priorityFiles = new ArrayList<>();

        // 優先するファイルを targetNames の順番通りにリストへ追加
        for (String targetName : targetNames) {
            Iterator<File> iterator = list.iterator();
            while (iterator.hasNext()) {
                File file = iterator.next();
                if (file.getName().equals(targetName)) {
                    priorityFiles.add(file);
                    iterator.remove(); // 元のリストから削除
                    break; // 同じファイル名が複数ある場合、最初のものだけ取得
                }
            }
        }

        // 優先ファイルを先頭に追加（targetNames の順番通り）
        list.addAll(0, priorityFiles);
    }

    public static void ClassDataLoad() {
        long start = System.currentTimeMillis();
        File classDirectories = new File(DataBasePath, "ClassData/");
        Function.createFolder(classDirectories);
        List<File> classList = dumpFile(classDirectories);
        moveToTop(classList, List.of("Novice.yml", "Swordman.yml", "Mage.yml", "Gunner.yml", "Tamer.yml", "Cleric.yml"));
        for (File file : classList) {
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
                loadError(file, e.getMessage());
            }

            // ReqClass
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] ClassDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void MobDataLoad() {
        long start = System.currentTimeMillis();
        File mobDirectories = new File(DataBasePath, "EnemyData/");
        Function.createFolder(mobDirectories);
        for (File file : dumpFile(mobDirectories)) {
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
                mobData.ColliderSizeY = data.getDouble("ColliderSizeY", 0);
                mobData.Glowing = data.getBoolean("Glowing", false);
                mobData.isHide = data.getBoolean("isHide", false);
                String entityType = data.getString("Type", "ZOMBIE").toUpperCase();
                mobData.entityType = EntityType.valueOf(entityType);
                mobData.Icon = Material.getMaterial(data.getString("Icon", mobData.entityType + "_SPAWN_EGG"));
                if (mobData.Icon == null) mobData.Icon = Material.PAPER;
                if (data.isSet("Disguise.Type")) {
                    DisguiseType disguiseType = DisguiseType.valueOf(data.getString("Disguise.Type", "SKELETON").toUpperCase());
                    if (disguiseType == DisguiseType.PLAYER) {
                        mobData.disguise = new PlayerDisguise(data.getString("Disguise.Player", "MomiNeko"));
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
                mobData.NonTame = data.getBoolean("NonTame", false);
                mobData.NonDespawn = data.getBoolean("NonDespawn", false);
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
                    for (String str : data.getStringList("HPStop")) {
                        String[] split = str.split(",");
                        double hpStop = Double.parseDouble(split[0]);
                        mobData.HPStopPercent.add(hpStop);
                        mobData.HPStop.put(hpStop, new ArrayList<>());
                        for (int i = 1; i < split.length; i++) {
                            mobData.HPStop.get(hpStop).add(split[i]);
                        }

                    }
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] EnemyDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void ShopDataLoad() {
        long start = System.currentTimeMillis();
        File shopDirectories = new File(DataBasePath, "ShopData/");
        Function.createFolder(shopDirectories);
        for (File file : dumpFile(shopDirectories)) {
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] ShopDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static int MaxTitleSlot = 0;

    public static void TitleDataLoad() {
        long start = System.currentTimeMillis();

        TitleGUIMap.clear();
        File titleDirectories = new File(DataBasePath, "TitleData/");
        Function.createFolder(titleDirectories);
        for (File file : dumpFile(titleDirectories)) {
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] TitleDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void NpcDataLoad() {
        long start = System.currentTimeMillis();
        File npcDirectories = new File(DataBasePath, "Npc");
        Function.createFolder(npcDirectories);
        for (File file : dumpFile(npcDirectories)) {
            String fileName = file.getName().replace(".yml", "");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            NpcData npcData = new NpcData();
            npcData.Message = data.getStringList("Message");
            NpcList.put(Integer.valueOf(fileName), npcData);
        }
        SomCore.instance.getLogger().info("[DataLoader] NpcDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void MobSpawnerDataLoad() {
        long start = System.currentTimeMillis();
        File spawnerDirectories = new File(DataBasePath, "Spawner");
        Function.createFolder(spawnerDirectories);
        for (File file : dumpFile(spawnerDirectories)) {
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
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] MobSpawnerDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void ItemInfoDataLoad() {
        long start = System.currentTimeMillis();
        for (ItemParameter itemData : ItemList.values()) {
            List<String> tempList = new ArrayList<>();
            if (itemData.isLoreHide) tempList.add("§c§lこの情報へのアクセス権限がありません");
            else {
                ItemStack tempItem = itemData.viewItem(1, "%.0f");
                if (tempItem != null && tempItem.hasItemMeta() && tempItem.getItemMeta().hasLore()) {
                    tempList.addAll(tempItem.getItemMeta().getLore());
                }
            }
            tempList.add(decoText("§3§l入手方法"));
            ItemInfoData.put(itemData.Id, tempList);
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
        SomCore.instance.getLogger().info("[DataLoader] ItemInfoDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void RuneInfoDataLoad() {
        long start = System.currentTimeMillis();
        for (RuneParameter runeData : RuneList.values()) {
            try {
                RuneInfoData.put(runeData.Id, new ArrayList<>(runeData.viewRune("%.0f", runeData.isLoreHide).getLore()));
                RuneInfoData.get(runeData.Id).add(decoText("§3§l入手方法"));
            } catch (Exception e) {
                e.printStackTrace();
                Log("§cRuneInfoDataLoadError -> " + runeData.Id);
            }
        }
        for (MobData mobData : MobList.values()) {
            for (DropRuneData dropData : mobData.DropRuneTable) {
                RuneInfoData.get(dropData.runeParameter.Id).add("§7・§e§l" + mobData.Display + " §b§l-> §e§l" + dropData.Percent*100 + "%");
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] RuneInfoDataを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void DefenseBattleMobListLoad() {
        long start = System.currentTimeMillis();
        File file = new File(DataBasePath, "DefenseBattle.yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        DefenseBattle.MobList.clear();
        for (String name : data.getStringList("MobList")) {
            DefenseBattle.MobList.add(DataBase.getMobData(name));
        }
        SomCore.instance.getLogger().info("[DataLoader] DefenseBattleMobListを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void RewardBoxListLoad() {
        long start = System.currentTimeMillis();
        File rewardBoxDirectories = new File(DataBasePath, "RewardBox/");
        Function.createFolder(rewardBoxDirectories);
        for (File file : dumpFile(rewardBoxDirectories)) {
            try {
                String fileName = file.getName().replace(".yml", "");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                RewardBox rewardBox = new RewardBox();
                rewardBox.isPartition = data.getBoolean("isPartition", false);
                HashMap<String, Set<RewardBoxData>> group = new HashMap<>();
                for (String str : data.getStringList("RewardBox")) {
                    String[] split = str.split(",");
                    RewardBoxData rewardBoxData = new RewardBoxData();
                    rewardBoxData.id = split[0];
                    String groupId = null;
                    for (String meta : split) {
                        if (meta.contains("Amount:")) rewardBoxData.amount = Integer.parseInt(meta.replace("Amount:", ""));
                        if (meta.contains("Percent:")) rewardBoxData.percent = Double.parseDouble(meta.replace("Percent:", ""));
                        if (meta.contains("Level:")) rewardBoxData.Level = Integer.parseInt(meta.replace("Level:", ""));
                        if (meta.contains("MaxLevel:")) rewardBoxData.MaxLevel = Integer.parseInt(meta.replace("MaxLevel:", ""));
                        if (meta.contains("GrowthRate:")) rewardBoxData.GrowthRate = Double.parseDouble(meta.replace("GrowthRate:", ""));
                        if (meta.contains("Group:")) groupId = meta.replace("Group:", "");
                    }
                    if (groupId != null) {
                        if (!group.containsKey(groupId)) group.put(groupId, new HashSet<>());
                        group.get(groupId).add(rewardBoxData);
                    } else {
                        rewardBox.List.add(rewardBoxData);
                    }
                }
                for (Map.Entry<String, Set<RewardBoxData>> entry : group.entrySet()) {
                    String groupPath = "Group." + entry.getKey();
                    double percent;
                    if (data.isSet(groupPath)) {
                        percent = data.getDouble(groupPath);
                    } else {
                        Log("§c[" + file.getName() + "]のGroup[" + entry.getKey() + "]は設定されていません");
                        percent = 0;
                    }
                    int spiltIndex = entry.getValue().size();
                    for (RewardBoxData rewardBoxData : entry.getValue()) {
                        rewardBoxData.percent = percent/spiltIndex;
                        rewardBox.List.add(rewardBoxData);
                    }
                }
                RewardBoxList.put(fileName, rewardBox);
            } catch (Exception e) {
                loadError(file, e.getMessage());
            }
        }
        SomCore.instance.getLogger().info("[DataLoader] RewardBoxListを読み込みました (" + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void disguiseLoader(Disguise disguise, FileConfiguration data) {
        switch (disguise.getType()) {
            case SLIME, MAGMA_CUBE -> {
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
                watcher.setType(data.isSet("Disguise.RabbitType") ? Rabbit.Type.valueOf(data.getString("Disguise.RabbitType")) : Rabbit.Type.BROWN);
                disguise.setWatcher(watcher);
            }
            case FOX -> {
                FoxWatcher watcher = new FoxWatcher(disguise);
                watcher.setType(data.isSet("Disguise.FoxType") ? Fox.Type.valueOf(data.getString("Disguise.FoxType")) : Fox.Type.RED);
                disguise.setWatcher(watcher);
            }
            case CAT -> {
                CatWatcher watcher = new CatWatcher(disguise);
                watcher.setType(data.isSet("Disguise.CatType") ? Registry.CAT_VARIANT.getOrThrow(NamespacedKey.fromString(data.getString("Disguise.CatType", "RED").toLowerCase(Locale.ROOT))) : Cat.Type.RED);
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
