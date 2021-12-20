package swordofmagic7;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;

class ShopSlot {
    ItemParameter itemParameter;
    int Mel;
}

class ShopData implements Cloneable {
    String Display;
    int MaxPage = 1;
    HashMap<Integer, ShopSlot> Data = new HashMap<>();

    Inventory view(int page, String format) {
        Inventory inv = decoInv(Display, 6);
        int slot = 0;
        for (int i = (page-1)*45; i < page*45; i++) {
            if (Data.containsKey(i)) {
                ShopSlot data = Data.get(i);
                ItemStack item = data.itemParameter.viewItem(1, format);
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add(decoText("&3&l販売情報"));
                Lore.add(decoLore("メル") + data.Mel);
                meta.setLore(Lore);
                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
            slot++;
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, ShopFlame);
        }
        if (page > 1) inv.setItem(45, UpScrollItem);
        if (page < MaxPage) inv.setItem(53, DownScrollItem);
        return inv;
    }

    @Override
    public ShopData clone() {
        try {
            ShopData clone = (ShopData) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
