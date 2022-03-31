package swordofmagic7.Data;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Mob.MobSpawnerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Status.StatusParameter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static swordofmagic7.Data.DataBase.DataBasePath;

public class Editor {
    public static void itemDataEditCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender || sender.hasPermission("som7.developer")) {
            try {
                if (args.length >= 7 && args[0].equalsIgnoreCase("create")) {
                    String series = args[1];
                    for (EquipmentCategory suffix : EquipmentCategory.values()) {
                        if (suffix != EquipmentCategory.Baton) {
                            String loopId = series + suffix.Display2;
                            ItemParameter itemData = DataBase.getItemParameter("テンプレート" + suffix.Display2);
                            File dir = new File(DataBasePath, "ItemData/Equipment/" + series + "シリーズ");
                            File file = new File(DataBasePath, "ItemData/Equipment/" + series + "シリーズ/" + loopId + ".yml");
                            dir.mkdirs();
                            file.createNewFile();
                            FileConfiguration data = YamlConfiguration.loadConfiguration(file);

                            List<String> lore = new ArrayList<>();
                            lore.add("準備中...");

                            itemData.Display = loopId;
                            itemData.Lore = lore;
                            itemData.Sell = Integer.parseInt(args[2]);
                            itemData.itemEquipmentData.ReqLevel = Integer.parseInt(args[3]);
                            itemData.itemEquipmentData.RuneSlot = Integer.parseInt(args[4]);
                            itemData.itemEquipmentData.UpgradeCost = Integer.parseInt(args[5]);

                            data.set("Display", itemData.Display);
                            data.set("Lore", itemData.Lore);
                            data.set("Category", itemData.Category.toString());
                            if (args.length >= 8) data.set("Materialization", args[7]);
                            data.set("EquipmentCategory", itemData.itemEquipmentData.EquipmentCategory.toString());
                            data.set("EquipmentSlot", itemData.itemEquipmentData.EquipmentSlot.toString());
                            data.set("Sell", itemData.Sell);
                            for (StatusParameter param : StatusParameter.values()) {
                                if (itemData.itemEquipmentData.Parameter().getOrDefault(param, 0d) > 0) {
                                    data.set(param.toString(), itemData.itemEquipmentData.Parameter().get(param));
                                }
                            }
                            data.set("ReqLevel", itemData.itemEquipmentData.ReqLevel);
                            data.set("Durable", itemData.itemEquipmentData.Durable);
                            data.set("RuneSlot", itemData.itemEquipmentData.RuneSlot);
                            data.set("UpgradeCost", itemData.itemEquipmentData.UpgradeCost);
                            data.set("StatusMultiply", Double.parseDouble(args[6]));
                            data.save(file);
                            sender.sendMessage(file.getName() + "を作成しました");
                        }
                    }
                    return;
                } else if (args.length == 3) {
                    String itemId = args[0];
                    ItemDataPaths dataPath;
                    try {
                        dataPath = ItemDataPaths.valueOf(args[1]);
                    } catch (Exception e) {
                        sender.sendMessage(Arrays.toString(ItemDataPaths.values()));
                        return;
                    }
                    Set<ItemParameter> itemList = new HashSet<>();
                    if (itemId.contains("@シリーズ")) {
                        itemId = itemId.replace("@シリーズ", "");
                        for (EquipmentCategory suffix : EquipmentCategory.values()) {
                            String loopId = itemId + suffix.Display2;
                            if (DataBase.ItemList.containsKey(loopId)) {
                                itemList.add(DataBase.getItemParameter(loopId));
                            } else {
                                sender.sendMessage(loopId + "は存在しません");
                            }
                        }
                    } else if (DataBase.ItemList.containsKey(itemId)) {
                        itemList.add(DataBase.getItemParameter(itemId));
                    } else {
                        sender.sendMessage(itemId + "は存在しません");
                    }
                    if (itemList.size() > 0) {
                        for (ItemParameter item : itemList) {
                            File file = item.File;
                            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                            switch (dataPath) {
                                case Durable, ReqLevel, RuneSlot, UpgradeCost, Sell -> {
                                    int value = Integer.parseInt(args[2]);
                                    data.set(String.valueOf(dataPath), value);
                                }
                                case StatusMultiply -> {
                                    double value = Double.parseDouble(args[2]);
                                    data.set(String.valueOf(dataPath), value);
                                }
                                default -> sender.sendMessage("Missing DataPath" + " -> " + dataPath);
                            }
                            try {
                                data.save(file);
                                sender.sendMessage(file.getName() + ": " + dataPath + " -> " + args[2]);
                                MultiThread.TaskRunLater(() -> {
                                    DataLoader.ItemDataLoad();
                                    DataLoader.ShopDataLoad();
                                }, 5, "ItemDataEditLoad");
                            } catch (IOException e) {
                                sender.sendMessage(file.getName() + ": save error");
                            }
                        }
                    }
                    return;
                }
            } catch (Exception e) {
                sender.sendMessage("format error");
                sender.sendMessage(e.getMessage());
            }
            sender.sendMessage("/itemDataEditCommand <itemId> <Path> <Value>");
            sender.sendMessage("/itemDataEditCommand create <SeriesName> <Sell> <ReqLevel> <RuneSlot> <UpgradeCost> <StatusMultiply>");
        }
    }

    public static void mobSpawnerDataEditCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender || sender.hasPermission("som7.developer")) {
            if (args.length >= 2) {
                String spawnerId = args[0];
                if (DataBase.MobSpawnerList.containsKey(spawnerId)) {
                    MobSpawnerData mobSpawner = DataBase.MobSpawnerList.get(spawnerId);
                    MobSpawnerDataPaths dataPath;
                    try {
                        dataPath = MobSpawnerDataPaths.valueOf(args[1]);
                    } catch (Exception e) {
                        sender.sendMessage(Arrays.toString(MobSpawnerDataPaths.values()));
                        return;
                    }
                    try {
                        File file = mobSpawner.file;
                        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                        if (dataPath == MobSpawnerDataPaths.Location) {
                            if (sender instanceof Player player) {
                                Location location = player.getLocation();
                                data.set("Location.w", location.getWorld().getName());
                                data.set("Location.x", location.getBlockX());
                                data.set("Location.y", location.getBlockY());
                                data.set("Location.z", location.getBlockZ());
                                player.sendMessage(file.getName() + " Edit " + dataPath + " to " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                            } else {
                                sender.sendMessage("プレイヤーのみ実行できます");
                            }
                        } else {
                            int value = Integer.parseInt(args[2]);
                            data.set(dataPath.toString(), value);
                            sender.sendMessage(file.getName() + " Edit " + dataPath + " to " + value);
                        }
                        data.save(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    sender.sendMessage("存在しないSpawnerIDです");
                }
            } else {
                sender.sendMessage("/mobSpawnerDataEditCommand <spawnerId> <Path> <Value>");
            }
        }
    }

    public static void mobSpawnerDataCreateCommand(Player player, String[] args) {
        if (player.hasPermission("som7.developer")) {
            if (args.length >= 7) {
                String name = args[0];
                int level = Integer.parseInt(args[1]);
                int index = Integer.parseInt(args[2]);
                int radius = Integer.parseInt(args[3]);
                int radiusY = Integer.parseInt(args[4]);
                int maxMob = Integer.parseInt(args[5]);
                int perSpawn = Integer.parseInt(args[6]);
                File file = new File(DataBasePath, "Spawner/" + name + "/" + name + "Lv" + level + "_" + index + ".yml");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                        Location loc = player.getLocation();
                        data.set("MobData", name);
                        data.set("Level", level);
                        data.set("Radius", radius);
                        data.set("RadiusY", radiusY);
                        data.set("MaxMob", maxMob);
                        data.set("PerSpawn", perSpawn);
                        data.set("Location.world", loc.getWorld().getName());
                        data.set("Location.x", loc.getBlockX());
                        data.set("Location.y", loc.getBlockY());
                        data.set("Location.z", loc.getBlockZ());
                        data.save(file);
                        player.sendMessage(file.getName() + "を作成しました");
                    } catch (Exception e) {
                        player.sendMessage("File作成中にエラーが発生しました");
                    }
                } else {
                    player.sendMessage("すでに存在しているSpawnerIDです");
                }
            } else {
                player.sendMessage("/mobSpawnerDataCreate <mobName> <level> <index> <radius> <radiusY> <maxMob> <perSpawn>");
            }
        }
    }
}

enum ItemDataPaths {
    Sell,
    ReqLevel,
    Durable,
    RuneSlot,
    UpgradeCost,
    StatusMultiply,
}

enum MobSpawnerDataPaths {
    Radius,
    RadiusY,
    MaxMob,
    PerSpawn,
    Location,
}
