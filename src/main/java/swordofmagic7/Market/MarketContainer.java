package swordofmagic7.Market;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static swordofmagic7.Data.DataBase.DataBasePath;

public class MarketContainer {
    public static HashMap<String, MarketContainer> MarketContainers = new HashMap<>();
    public static MarketContainer getMarket(UUID uuidData) {
        String uuid = uuidData.toString();
        if (!MarketContainers.containsKey(uuid)) {
            MarketContainers.put(uuid, new MarketContainer(uuid));
        } else {
            MarketContainers.get(uuid).load();
        }
        return MarketContainers.get(uuid);
    }
    public static String getOwnerNick(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (!player.isOnline()) {
            File playerFile = new File(DataBasePath, "PlayerData/" + uuid + ".yml");
            FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
            return data.getString("Nick", Bukkit.getOfflinePlayer(uuid).getName());
        } else {
            return PlayerData.playerData((Player) player).Nick;
        }
    }

    public final String uuid;
    public int Mel = 0;
    public List<MarketData> marketData = new ArrayList<>();

    public MarketContainer(String uuid) {
        this.uuid = uuid;
        load();
    }

    public void save() {
        File marketFile = new File(DataBasePath, "Market/" + uuid + ".yml");
        if (marketData.size() > 0 && Mel == 0) {
            if (!marketFile.exists()) {
                try {
                    marketFile.createNewFile();
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error creating " + marketFile.getName() + "!");
                }
            }
            FileConfiguration data = YamlConfiguration.loadConfiguration(marketFile);
            List<String> stringData = new ArrayList<>();
            for (MarketData marketDatum : marketData) {
                stringData.add(marketDatum.toString());
            }
            data.set("Market", stringData);
            data.set("Mel", Mel);
            try {
                data.save(marketFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.deleteIfExists(marketFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MarketContainer load() {
        File marketFile = new File(DataBasePath, "Market/" + uuid + ".yml");
        marketData.clear();
        if (marketFile.exists()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(marketFile);
            List<String> stringData = data.getStringList("Market");
            for (String str : stringData) {
                marketData.add(new MarketData(str));
            }
            Mel = data.getInt("Mel", 0);
            if (marketData.size() == 0 && Mel == 0) {
                try {
                    Files.deleteIfExists(marketFile.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }
}
