package swordofmagic7.Item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackData {
    Material material;
    int CustomModelData = 0;
    String Display = "";
    List<String> Lore = new ArrayList<>();

    public ItemStackData(Material material) {
        this.material = material;
    }

    public ItemStackData(Material material, String Display) {
        this.material = material;
        this.Display = Display;
    }

    public ItemStackData(Material material, String Display, List<String> Lore) {
        this.material = material;
        this.Display = Display;
        this.Lore = Lore;
    }

    public ItemStackData(Material material, String Display, String Lore) {
        this.material = material;
        this.Display = Display;
        String[] LoreData = Lore.split("\n");
        this.Lore = List.of(LoreData);
    }

    public ItemStack view() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(CustomModelData);
        meta.setDisplayName(Display);
        meta.setLore(Lore);
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}
