package swordofmagic7.Shop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Inventory.ItemParameterStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Shop.Shop.ItemFlame;

public class ShopData implements Cloneable {
    public String Display;
    public int MaxPage = 1;
    public HashMap<Integer, ShopSlot> Data = new HashMap<>();

    public Inventory view(int page, String format) {
        Inventory inv = decoInv("§l" + Display, 6);
        int slot = 0;
        for (int i = (page-1)*45; i < page*45; i++) {
            if (Data.containsKey(i)) {
                ShopSlot data = Data.get(i);
                ItemStack item = data.itemParameter.viewItem(1, format);
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add(decoText("§3§l販売情報"));
                Lore.add(decoLore("メル") + data.Mel);
                if (data.itemRecipe != null) {
                    for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                        Lore.add(decoLore(stack.itemParameter.Id) + stack.Amount + "個");
                    }
                }
                meta.setLore(Lore);
                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
            slot++;
        }
        inv.setItem(45, ShopFlame);
        inv.setItem(46,ItemFlame(-100));
        inv.setItem(47,ItemFlame(-10));
        inv.setItem(48,ItemFlame(-1));
        inv.setItem(50,ItemFlame(1));
        inv.setItem(51,ItemFlame(10));
        inv.setItem(52,ItemFlame(100));
        inv.setItem(53, ShopFlame);
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

