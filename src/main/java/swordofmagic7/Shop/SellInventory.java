package swordofmagic7.Shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.BasicInventory;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.ShopFlame;
import static swordofmagic7.Function.*;
import static swordofmagic7.Inventory.ItemInventory.ItemStackCheck;
import static swordofmagic7.Shop.Shop.*;

public class SellInventory extends BasicInventory {
    private final List<ItemParameterStack> List = new ArrayList<>();
    private final String itemStack = decoText("§3§lアイテムスタック");


    public SellInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    public List<ItemParameterStack> getList() {
        return List;
    }

    public void clear() {
        List.clear();
    }

    public Inventory viewInventory(int SellAmount) {
        Inventory inv = decoInv(ShopSellDisplay, 6);
        for (int i = 0; i < 45; i++) {
            if (i < List.size()) {
                ItemParameterStack stack = List.get(i);
                ItemStack item = stack.itemParameter.viewItem(stack.Amount, playerData.ViewFormat());
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add(itemStack);
                Lore.add(decoLore("個数") + stack.Amount);
                Lore.add("§8SlotID:" + i);
                meta.setLore(Lore);
                item.setItemMeta(meta);
                inv.setItem(i, item);
            }
        }
        inv.setItem(45, ShopFlame);
        inv.setItem(46,ItemFlame(-100));
        inv.setItem(47,ItemFlame(-10));
        inv.setItem(48,ItemFlame(-1));
        inv.setItem(49,ItemFlameAmount(ShopSellPrefix, SellAmount));
        inv.setItem(50,ItemFlame(1));
        inv.setItem(51,ItemFlame(10));
        inv.setItem(52,ItemFlame(100));
        inv.setItem(53, ShopFlame);
        return inv;
    }

    ItemParameterStack getItemParameterStack(ItemParameter param) {
        for (ItemParameterStack stack : List) {
            if (ItemStackCheck(stack.itemParameter, param)) {
                return stack;
            }
        }
        return null;
    }

    public ItemParameter getItemParameter(int i) {
        if (i < List.size()) {
            return List.get(i).itemParameter.clone();
        }
        return null;
    }

    public void addItemParameter(ItemParameter param, int addAmount) {
        ItemParameterStack stack = getItemParameterStack(param);
        if (stack != null) {
            stack.Amount += addAmount;
        } else {
            ItemParameterStack newStack = new ItemParameterStack();
            newStack.itemParameter = param.clone();
            newStack.Amount = addAmount;
            List.add(newStack);
        }
        if (45 < List.size()) {
            List.remove(0);
        }
    }

    public void removeItemParameter(ItemParameter param, int removeAmount) {
        ItemParameterStack stack = getItemParameterStack(param);
        if (stack != null) {
            stack.Amount -= removeAmount;
            if (stack.Amount <= 0) {
                List.remove(stack);
            }
        }
    }
}
