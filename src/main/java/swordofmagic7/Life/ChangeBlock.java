package swordofmagic7.Life;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;

public class ChangeBlock {
    public HashMap<Location, Material> changeBlock = new HashMap<>();

    public boolean checkLocation(Location location) {
        return changeBlock.containsKey(location);
    }

    public void put(Location location, Material material) {
        changeBlock.put(location, material);
    }

    public Material get(Location location) {
        return changeBlock.get(location);
    }

    public void remove(Location location) {
        changeBlock.remove(location);
    }
}
