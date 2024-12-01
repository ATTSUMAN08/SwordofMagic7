package swordofmagic7.Item;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ItemStackData {
    Material material;
    int customModelData;
    String displayName = "";
    List<String> lore = new ArrayList<>();
    ItemStack item;

    public ItemStackData(@NotNull Material material) {
        this.material = material;

        this.item = new ItemStack(material);
    }

    public ItemStackData(@NotNull Material material, String displayName) {
        this.material = material;
        this.displayName = displayName;

        this.item = new ItemStack(material);
    }

    public ItemStackData(@NotNull Material material, String displayName, int customModelData) {
        this.material = material;
        this.displayName = displayName;
        this.customModelData = customModelData;

        this.item = new ItemStack(material);
    }

    public ItemStackData(@NotNull Material material, String displayName, List<String> lore) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;

        this.item = new ItemStack(material);
    }


    public ItemStackData(@NotNull Material material, String displayName, String lore) {
        this.material = material;
        this.displayName = displayName;
        String[] loreData = lore.split("\n");
        this.lore = List.of(loreData);

        this.item = new ItemStack(material);
    }

    public ItemStackData(@NotNull Material material, String displayName, String Lore, int customModelData) {
        this.material = material;
        this.displayName = displayName;
        String[] loreData = Lore.split("\n");
        this.lore = List.of(loreData);
        this.customModelData = customModelData;

        this.item = new ItemStack(material);
    }

    public ItemStackData(@NotNull Material material, String displayName, List<String> lore, int customModelData) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.customModelData = customModelData;

        this.item = new ItemStack(material);
    }

    public ItemStackData setHeadData(String playerName) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack view() {
        ItemMeta meta = item.getItemMeta();
        if (customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }
        meta.displayName(Component.text(displayName));
        meta.lore(lore.stream().map(Component::text).toList());
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}
