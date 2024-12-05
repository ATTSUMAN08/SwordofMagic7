package swordofmagic7.Life;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ChangeBlock {
    public final Map<Location, Material> changedBlocks = new HashMap<>();

    public boolean checkLocation(Location location) {
        return changedBlocks.containsKey(location);
    }

    public void put(Location location, Material material) {
        changedBlocks.put(location, material);
    }

    public Material get(Location location) {
        return changedBlocks.get(location);
    }

    public void remove(Location location) {
        changedBlocks.remove(location);
    }
}
