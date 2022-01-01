package swordofmagic7.Map;

import org.bukkit.entity.Player;

import static swordofmagic7.Data.PlayerData.playerData;

public class MapData {
    public String Display;
    public String Color;
    public int Level = 0;
    public boolean Safe;

    public void enter(Player player) {
        playerData(player).Map = this;
        player.sendTitle(Color + "§l" + Display, Color + "§l推奨Lv" + Level, 20, 40, 20);
    }
}
