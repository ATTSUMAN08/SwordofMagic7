package swordofmagic7.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static swordofmagic7.Data.PlayerData.playerData;

public class MapData {
    public String Id;
    public String Display;
    public String Color;
    public int Level = 0;
    public boolean Safe;
    public HashMap<Material, String> GatheringData = new HashMap<>();

    public void enter(Player player) {
        playerData(player).Map = this;
        player.sendTitle(Color + "§l" + Display, Color + "§l推奨Lv" + Level, 20, 40, 20);
    }
}
