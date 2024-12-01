package swordofmagic7.Data;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Function;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Item.ItemUseList.RewardBox;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Life.Angler.AnglerData;
import swordofmagic7.Life.Cook.CookData;
import swordofmagic7.Life.Harvest.HarvestData;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Life.Lumber.LumberData;
import swordofmagic7.Life.Mine.MineData;
import swordofmagic7.Life.Smith.MakeData;
import swordofmagic7.Life.Smith.SmeltData;
import swordofmagic7.Map.MapData;
import swordofmagic7.Map.TeleportGateParameter;
import swordofmagic7.Map.WarpGateParameter;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobSpawnerData;
import swordofmagic7.Npc.NpcData;
import swordofmagic7.Pet.PetData;
import swordofmagic7.Shop.ItemRecipe;
import swordofmagic7.Shop.ShopData;
import swordofmagic7.Skill.SkillClass.Alchemist.AlchemyData;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.SomCore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.decoText;

public final class DataBase {
    public static List<String> IgnoreIPList = new ArrayList<>();
    public static final String DataBasePath = SomCore.plugin.getDataFolder().getPath();
    public static final String format = "%.3f";
    public static final int MaxStackAmount = 100;
    public static final String Som7VIP = "som7.VIP";
    public static final String Som7Premium = "som7.Premium";
    public static final Location SpawnLocation = new Location(Bukkit.getWorld("world"), 1200.5, 100, 0.5, 0, 0);
    public static final ItemStack AirItem = new ItemStack(Material.AIR);
    public static ItemStack FlameItem(int i) {
        return new ItemStackData(Material.IRON_BARS, "§7§l空スロット[" + i + "]", 1).view();
    }
    public static String ServerId = "Initialize";
    public static final int[] AnvilUISlot = new int[3];
    public static final ItemStack ItemFlame = new ItemStackData(Material.IRON_BARS, " ", 1).view();
    public static final ItemStack ShopFlame = new ItemStackData(Material.IRON_BARS, " ", 2).view();
    public static final ItemStack BrownItemFlame = new ItemStackData(Material.IRON_BARS, " ", 2).view();
    public static final ItemStack AnvilUIFlame = new ItemStackData(Material.IRON_BARS, " ", 3).view();
    public static final ItemStack NoneFlame = new ItemStackData(Material.IRON_BARS, " ", 4).view();
    public static final ItemStack TradeFlame = new ItemStackData(Material.IRON_BARS, " ").view();
    public static final ItemStack UpScrollItem = new ItemStackData(Material.ITEM_FRAME, "§e§l上にスクロール").view();
    public static final ItemStack DownScrollItem = new ItemStackData(Material.ITEM_FRAME, "§e§l下にスクロール").view();
    public static final ItemStack NextPageItem = new ItemStackData(Material.ITEM_FRAME, "§e§l次のページ").view();
    public static final ItemStack PreviousPageItem = new ItemStackData(Material.ITEM_FRAME, "§e§l前のページ").view();
    public static final String itemInformation = decoText("§3§lアイテム情報");
    public static final String itemParameter = decoText("§3§lパラメーター");
    public static final String itemRune = decoText("§3§lルーン");
    public static final HashMap<String, ItemParameter> ItemList = new HashMap<>();
    public static final HashMap<String, RuneParameter> RuneList = new HashMap<>();
    public static final HashMap<String, ClassData> ClassList = new HashMap<>();
    public static final HashMap<String, ClassData> ClassListDisplay = new HashMap<>();
    public static final HashMap<Integer, String> ClassDataMap = new HashMap<>();
    public static final HashMap<String, SkillData> SkillDataList = new HashMap<>();
    public static final HashMap<String, SkillData> SkillDataDisplayList = new HashMap<>();
    public static final HashMap<String, MobData> MobList = new HashMap<>();
    public static final HashMap<String, MobSpawnerData> MobSpawnerList = new HashMap<>();
    public static final HashMap<String, ShopData> ShopList = new HashMap<>();
    public static final HashMap<String, ItemRecipe> ItemRecipeList = new HashMap<>();
    public static final HashMap<String, WarpGateParameter> WarpGateList = new HashMap<>();
    public static final HashMap<String, TeleportGateParameter> TeleportGateList = new HashMap<>();
    public static final HashMap<String, MapData> MapList = new HashMap<>();
    public static final HashMap<String, PetData> PetList = new HashMap<>();
    public static final HashMap<Integer, NpcData> NpcList = new HashMap<>();
    public static final HashMap<Integer, String> TeleportGateMenu = new HashMap<>();
    public static final HashMap<String, MineData> MineDataList = new HashMap<>();
    public static final HashMap<String, LumberData> LumberDataList = new HashMap<>();
    public static final HashMap<String, HarvestData> HarvestDataList = new HashMap<>();
    public static final HashMap<String, AnglerData> AnglerDataList = new HashMap<>();
    public static final HashMap<String, CookData> CookDataList = new HashMap<>();
    public static final HashMap<String, SmeltData> SmeltDataList = new HashMap<>();
    public static final HashMap<String, MakeData> MakeDataList = new HashMap<>();
    public static final HashMap<Integer, String> MakeGUIMap = new HashMap<>();
    public static final HashMap<String, TitleData> TitleDataList = new HashMap<>();
    public static final List<TitleData> HiddenTitleDataList = new ArrayList<>();
    public static final HashMap<Integer, String> TitleGUIMap = new HashMap<>();
    public static final HashMap<String, AlchemyData> AlchemyDataList = new HashMap<>();
    public static final HashMap<Integer, String> AlchemyShopMap = new HashMap<>();
    public static final HashMap<String, List<String>> MaterializationMap = new HashMap<>();
    public static final HashMap<String, List<String>> ItemInfoData = new HashMap<>();
    public static final HashMap<String, List<String>> RuneInfoData = new HashMap<>();
    public static final HashMap<String, RewardBox> RewardBoxList = new HashMap<>();

    public static ItemStack ItemStackPlayerHead(OfflinePlayer player) {
        return ItemStackPlayerHead(player, null, null);
    }

    public static ItemStack ItemStackPlayerHead(OfflinePlayer player, String Display) {
        return ItemStackPlayerHead(player, Display, null);
    }

    public static ItemStack ItemStackPlayerHead(OfflinePlayer player, String Display, List<String> Lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        if (Display != null) meta.displayName(Component.text(Display));
        if (Lore != null) meta.lore(Lore.stream().map(Component::text).toList());
        item.setItemMeta(meta);
        return item;
    }

    public static List<File> dumpFile(File file) {
        List<File> list = new ArrayList<>();
        if (!file.exists()) {
            SomCore.plugin.getLogger().warning("存在しないファイルが参照されました: " + file.getPath());
            Function.createFolder(file);
            return list;
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File tmpFile : files) {
                if (!tmpFile.getName().equals(".sync")) {
                    if (tmpFile.isDirectory()) {
                        list.addAll(dumpFile(tmpFile));
                    } else {
                        list.add(tmpFile);
                    }
                }
            }
        } else {
            SomCore.plugin.getLogger().warning("ファイルが存在しません: " + file.getPath());
        }
        return list;
    }

    public static File searchFile(File dir, String search) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File tmpFile : files) {
                if (tmpFile.getName().equals(search)) {
                    return tmpFile;
                }
                if (tmpFile.isDirectory()) {
                    File file = searchFile(tmpFile, search);
                    if (file != null) return file;
                }
            }
        } else {
            SomCore.plugin.getLogger().warning("ファイルが存在しません: " + dir.getPath());
        }
        return null;
    }

    static void loadError(File file) {
        Log(file.getPath() + " のロード中にエラーが発生しました");
    }

    static void loadError(File file, String str) {
        Log(file.getPath() + " のロード中にエラーが発生しました, " + str);
    }

    public static void DataLoad() {
        LifeType.Initialize();
        AnvilUISlot[0] = 1;
        AnvilUISlot[1] = 4;
        AnvilUISlot[2] = 7;

        DataLoader.AllLoad();

        File warpDirectories = new File(DataBasePath, "WarpGateData/");
        List<File> warpFile = dumpFile(warpDirectories);
        for (File file : warpFile) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                World world = Bukkit.getWorld(data.getString("Location.w", "world"));
                double x = data.getDouble("Location.x");
                double y = data.getDouble("Location.y");
                double z = data.getDouble("Location.z");
                float yaw = (float) data.getDouble("Location.yaw");
                float pitch = (float) data.getDouble("Location.pitch");
                Location loc = new Location(world, x, y, z, yaw, pitch);
                WarpGateParameter warp = new WarpGateParameter();
                warp.Id = fileName;
                warp.setLocation(loc.clone());
                if (data.isSet("Target")) {
                    warp.Target = data.getString("Target");
                } else if (data.isSet("TargetLocation")) {
                    double xT = data.getDouble("TargetLocation.x");
                    double yT = data.getDouble("TargetLocation.y");
                    double zT = data.getDouble("TargetLocation.z");
                    float yawT = (float) data.getDouble("TargetLocation.yaw");
                    float pitchT = (float) data.getDouble("TargetLocation.pitch");
                    warp.TargetLocation = new Location(world, xT, yT, zT, yawT, pitchT);
                }
                warp.Trigger = data.getString("Trigger");
                warp.isTrigger = data.getBoolean("isTrigger", false);
                if (warp.isTrigger) {
                    warp.Display = data.getString("Display");
                    warp.Lore = data.getString("Lore");
                } else {
                    warp.NextMap = MapList.get(data.getString("NextMap"));
                    warp.Display = warp.NextMap.Color + "§l《" + warp.NextMap.Display + "》";
                    warp.Lore = warp.NextMap.Color + "§c必要戦闘力 " + String.format("%.0f", warp.NextMap.ReqCombatPower);
                }
                warp.start();
                if (data.getBoolean("Default", true)) {
                    warp.Active();
                } else {
                    warp.Disable();
                }
                WarpGateList.put(fileName, warp);
            } catch (Exception e) {
                loadError(file);
            }
        }

        File teleportDirectories = new File(DataBasePath, "TeleportGateData/");
        List<File> teleportFile = dumpFile(teleportDirectories);
        for (File file : teleportFile) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String fileName = file.getName().replace(".yml", "");
                if (!fileName.equalsIgnoreCase("GUI")) {
                    World world = Bukkit.getWorld(data.getString("Location.w", "world"));
                    double x = data.getDouble("Location.x");
                    double y = data.getDouble("Location.y");
                    double z = data.getDouble("Location.z");
                    float yaw = (float) data.getDouble("Location.yaw");
                    float pitch = (float) data.getDouble("Location.pitch");
                    Location loc = new Location(world, x, y, z, yaw, pitch);
                    TeleportGateParameter teleport = new TeleportGateParameter();
                    teleport.Id = fileName;
                    teleport.Display = data.getString("Display");
                    teleport.Icon = Material.getMaterial(data.getString("Icon", "BARRIER"));
                    teleport.Title = data.getString("Title");
                    teleport.Subtitle = data.getString("Subtitle");
                    teleport.Location = loc;
                    teleport.DefaultActive = data.getBoolean("DefaultActive");
                    teleport.Map = getMapData(data.getString("Map"));
                    teleport.Mel = data.getInt("Mel", 0);
                    TeleportGateList.put(fileName, teleport);
                    teleport.start();
                } else {
                    for (int i = 0; i < 54; i++) {
                        if (data.isSet("TeleportGateMenu." + i)) {
                            TeleportGateMenu.put(i, data.getString("TeleportGateMenu." + i));
                        }
                    }
                }
            } catch (Exception e) {
                loadError(file);
            }
        }

        for (MobSpawnerData spawnerData : MobSpawnerList.values()) {
            spawnerData.start();
        }

        Damage.OutrageResetTime = DataBase.getSkillData("Outrage").ParameterValueInt(0)*20;
        Damage.FrenzyResetTime = DataBase.getSkillData("Frenzy").ParameterValueInt(0)*20;
    }

    public static HashMap<String, ItemParameter> getItemList() {
        return ItemList;
    }

    public static HashMap<String, RuneParameter> getRuneList() {
        return RuneList;
    }

    public static HashMap<String, ClassData> getClassList() {
        return ClassList;
    }

    public static HashMap<String, SkillData> getSkillList() {
        return SkillDataList;
    }

    public static HashMap<String, MobData> getMobList() {
        return MobList;
    }

    public static HashMap<String, PetData> getPetList() {
        return PetList;
    }

    public static HashMap<String, ItemRecipe> getItemRecipeList() {
        return ItemRecipeList;
    }

    public static NpcData getNpcData(int id) {
        if (NpcList.containsKey(id)) {
            return NpcList.get(id);
        } else {
            Log("§cNon-NpcData: " + id, true);
            return new NpcData();
        }
    }

    public static ItemParameter getItemParameter(String str) {
        if (ItemList.containsKey(str)) {
            return ItemList.get(str).clone();
        } else {
            Log("§cNon-ItemParameter: " + str, true);
            return new ItemParameter();
        }
    }

    public static RuneParameter getRuneParameter(String str) {
        if (RuneList.containsKey(str)) {
            return RuneList.get(str).clone();
        } else {
            Log("§cNon-RuneParameter: " + str, true);
            return new RuneParameter();
        }
    }

    public static ClassData getClassData(String className) {
        if (ClassList.containsKey(className)) {
            return ClassList.get(className);
        } else if (ClassListDisplay.containsKey(className)) {
            return ClassListDisplay.get(className);
        } else {
            throw new NullPointerException("クラスのデータが存在しません: " + className);
        }
    }

    public static SkillData getSkillData(String skillName) {
        if (SkillDataList.containsKey(skillName)) {
            return SkillDataList.get(skillName);
        } else if (SkillDataDisplayList.containsKey(skillName)) {
            return SkillDataDisplayList.get(skillName);
        } else {
            throw new NullPointerException("スキルのデータが存在しません: " + skillName);
        }
    }

    public static MobData getMobData(String str) {
        if (MobList.containsKey(str)) {
            return MobList.get(str);
        } else {
            Log("§cNon-MobData: " + str, true);
            return new MobData();
        }
    }

    public static ItemRecipe getItemRecipe(String str) {
        if (ItemRecipeList.containsKey(str)) {
            return ItemRecipeList.get(str);
        } else {
            Log("§cNon-ItemRecipe: " + str, true);
            return new ItemRecipe();
        }
    }

    public static MapData getMapData(String str) {
        if (MapList.containsKey(str)) {
            return MapList.get(str);
        } else {
            Log("§cNon-MapData: " + str, true);
            return new MapData();
        }
    }

    public static PetData getPetData(String str) {
        if (PetList.containsKey(str)) {
            return PetList.get(str);
        } else {
            Log("§cNon-PetData: " + str, true);
            return new PetData();
        }
    }

    public static ShopData getShopData(String str) {
        if (ShopList.containsKey(str)) {
            return ShopList.get(str);
        } else {
            Log("§cNon-ShopList: " + str, true);
            return new ShopData();
        }
    }

    public static WarpGateParameter getWarpGate(String str) {
        if (WarpGateList.containsKey(str)) {
            return WarpGateList.get(str);
        } else {
            Log("§cNon-WarpGate: " + str, true);
            return new WarpGateParameter();
        }
    }
}
