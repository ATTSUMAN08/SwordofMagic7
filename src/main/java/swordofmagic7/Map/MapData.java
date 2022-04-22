package swordofmagic7.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;

import java.util.HashMap;

import static swordofmagic7.Data.PlayerData.playerData;

public class MapData implements Cloneable {
    public String Id;
    public String Display;
    public String Color;
    public int Level = 0;
    public boolean Safe;
    public double ReqCombatPower = 0;
    public boolean isRaid = false;
    public HashMap<String, String> GatheringData = new HashMap<>();

    public boolean isGathering(Material material) {
        return GatheringData.containsKey(material.toString());
    }

    public void enter(Player player) {
        PlayerData playerData = playerData(player);
        playerData.Map = this;
        playerData.EffectManager.clearEffect();
        player.sendTitle(Color + "§l" + Display, Color + "§l推奨Lv" + Level, 20, 40, 20);
    }

    @Override
    public MapData clone() {
        try {
            MapData clone = (MapData) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
