package swordofmagic7.Life;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;

public class ChangeBlock {
    public HashMap<Location, BlockData> changeBlock = new HashMap<>();

    public boolean checkLocation(Location location) {
        return changeBlock.containsKey(location);
    }

    public void put(Location location, BlockData blockData) {
        changeBlock.put(location, blockData);
    }

    public BlockData get(Location location) {
        return changeBlock.get(location);
    }

    public void remove(Location location) {
        changeBlock.remove(location);
    }
}
