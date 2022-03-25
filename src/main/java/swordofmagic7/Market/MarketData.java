package swordofmagic7.Market;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static swordofmagic7.Data.DataBase.MaxStackAmount;

public class MarketData {
    public static final String MarketDataSplit = "@Market@";
    ItemParameterStack itemParameterStack;
    UUID uuid;
    int Mel;
    UUID Owner;
    long timeStamp;

    public MarketData(ItemParameterStack itemParameterStack, int Mel, UUID Owner, long timeStamp) {
        this.itemParameterStack = itemParameterStack;
        this.Mel = Mel;
        this.Owner = Owner;
        this.timeStamp = timeStamp;
        uuid = UUID.randomUUID();
    }

    public MarketData(String data) {
        String[] split = data.split(MarketDataSplit);
        itemParameterStack = ItemParameterStack.fromString(split[0]);
        Mel = Integer.parseInt(split[1]);
        Owner = UUID.fromString(split[2]);
        timeStamp = Long.parseLong(split[3]);
        uuid = UUID.fromString(split[4]);
    }

    public String toString() {
        return itemParameterStack.toString() + MarketDataSplit
                + Mel + MarketDataSplit
                + Owner + MarketDataSplit
                + timeStamp + MarketDataSplit
                + uuid;
    }

    public ItemStack view(String format) {
        ItemStack item = itemParameterStack.itemParameter.viewItem(itemParameterStack.Amount, format);
        ItemMeta meta = item.getItemMeta();
        List<String> Lore = new ArrayList<>(meta.getLore());
        Lore.add(Function.decoText("§3§l出品情報"));
        Lore.add(Function.decoLore("出品者") + MarketContainer.getOwnerNick(Owner));
        Lore.add(Function.decoLore("出品価格") + Mel + "メル/個");
        Lore.add(Function.decoLore("出品数") + itemParameterStack.Amount + "個");
        Lore.add(Function.decoLore("経過") + (int) Math.floor((System.currentTimeMillis() - timeStamp)/60000f) + "分");
        meta.setLore(Lore);
        item.setItemMeta(meta);
        item.setAmount(Math.min(MaxStackAmount, itemParameterStack.Amount));
        return item;
    }
}