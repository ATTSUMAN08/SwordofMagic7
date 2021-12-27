package swordofmagic7.Item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Function.*;

public class RuneParameter implements Cloneable {
    public String Id;
    public String Display = "ルーン";
    public List<String> Lore = new ArrayList<>();
    public double Quality = 0.5;
    public int Level = 0;
    public HashMap<StatusParameter, Double> Parameter = new HashMap<>();

    public RuneParameter() {
        for (StatusParameter param : StatusParameter.values()) {
            Parameter.put(param, 0d);
        }
    }

    public boolean isEmpty() {
        return Level == 0;
    }

    public double Parameter(StatusParameter param) {
        return (Parameter.get(param)/2 + (Quality * Parameter.get(param))) * (Math.pow(Level, 1.4) / Level);
    }

    public ItemStack viewRune(String format) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(Display));
        List<String> Lore = loreText(this.Lore);
        Lore.add(decoText("§3§lパラメーター"));
        Lore.add(decoLore("§e§lレベル") + Level);
        Lore.add(decoLore("§e§l品質") + String.format(format, Quality*100) + "%");
        for (StatusParameter param : StatusParameter.values()) {
            if (isZero(Parameter.get(param))) Lore.add(param.DecoDisplay +  String.format(format, Parameter(param)));
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public RuneParameter clone() {
        try {
            RuneParameter clone = (RuneParameter) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
