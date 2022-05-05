package swordofmagic7.Skill.SkillClass.Alchemist;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Shop.ItemRecipe;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.decoLore;

public class AlchemyData {
    public ItemParameter itemParameter;
    public int Amount;
    public int ReqLevel;
    public int Exp;
    public ItemRecipe itemRecipe;

    public AlchemyData(ItemParameter item, int amount, int reqLevel, int exp, ItemRecipe itemRecipe) {
        itemParameter = item;
        Amount = amount;
        ReqLevel = reqLevel;
        Exp = exp;
        this.itemRecipe = itemRecipe;
    }

    public ItemStack view(String format) {
        ItemStack item = itemParameter.viewItem(Amount, format);
        ItemMeta meta = item.getItemMeta();
        List<String> Lore = new ArrayList<String>(meta.getLore());
        Lore.add(Function.decoText("§3§lアルケミー情報"));
        Lore.add(Function.decoLore("必要レベル") + ReqLevel);
        Lore.add(Function.decoLore("経験値") + Exp);
        for (ItemParameterStack stack : itemRecipe.ReqStack) {
            Lore.add(decoLore(stack.itemParameter.Id) + stack.Amount + "個");
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }
}
