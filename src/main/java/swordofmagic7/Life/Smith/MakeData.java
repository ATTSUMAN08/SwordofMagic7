package swordofmagic7.Life.Smith;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Shop.ItemRecipe;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;

public class MakeData {
    public String Display;
    public Material Icon;
    public List<MakeItemData> makeList = new ArrayList<>();
    public ItemRecipe itemRecipe;
    public int ReqLevel;
    public int Exp;

    public ItemStack view(String format) {
        ItemStack item = new ItemStack(Icon, 1);
        ItemMeta meta = item.getItemMeta();
        List<String> Lore = new ArrayList<>();
        meta.setDisplayName(decoText(Display));
        Lore.add("§a§l素材を消費して武器を作成します");
        Lore.add("§a§l完成品は以下の中からランダムに選ばれます");
        Lore.add(decoText("§3§lアイテムリスト"));
        for (MakeItemData data : makeList) {
            Lore.add(decoLore(data.itemParameter.Display) + String.format(format, data.Percent*100) + "%");
        }
        Lore.add(Function.decoText("§3§l制作情報"));
        Lore.add(Function.decoLore("必要鍛冶レベル") + ReqLevel);
        Lore.add(Function.decoLore("鍛冶経験値") + Exp);
        for (ItemParameterStack stack : itemRecipe.ReqStack) {
            Lore.add(decoLore(stack.itemParameter.Id) + stack.Amount + "個");
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }
}
