package swordofmagic7.Life.Cook;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Shop.ItemRecipe;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.decoLore;

public class CookData {
    public List<CookItemData> CookItemData;
    public ItemParameter viewItem;
    public int viewAmount;
    public int ReqLevel;
    public int Exp;
    public ItemRecipe itemRecipe;

    public CookData(List<CookItemData> list, ItemParameter viewItem, int viewAmount, int reqLevel, int exp, ItemRecipe itemRecipe) {
        CookItemData = list;
        ReqLevel = reqLevel;
        Exp = exp;
        this.viewItem = viewItem;
        this.viewAmount = viewAmount;
        this.itemRecipe = itemRecipe;
    }

    public ItemStack view(int Level, String format) {
        ItemStack item = viewItem.viewItem(viewAmount, format);
        ItemMeta meta = item.getItemMeta();
        List<String> Lore = new ArrayList<>();
        if (meta.getLore() != null) {
            Lore.addAll(meta.getLore());
        }
        Lore.add(Function.decoText("§3§l料理情報"));
        Lore.add(Function.decoLore("必要料理レベル") + ReqLevel);
        Lore.add(Function.decoLore("料理経験値") + Exp);
        for (ItemParameterStack stack : itemRecipe.ReqStack) {
            Lore.add(decoLore(stack.itemParameter.Id) + stack.Amount + "個");
        }
        Lore.add(Function.decoText("§3§l料理完成品リスト"));
        double viewPercent = 1;
        for (CookItemData cookItemData : CookItemData) {
            double loopPercent = (viewPercent * cookItemData.getPercent(ReqLevel, Level));
            Lore.add(decoLore(cookItemData.itemParameter.Display + "§a§lx" + cookItemData.Amount) + String.format("%.0f", loopPercent*100) + "%");
            viewPercent -= loopPercent;
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }
}
