package swordofmagic7.Data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Function;
import swordofmagic7.Item.ItemStackData;

import java.util.ArrayList;
import java.util.List;

public class TitleData {
    public final String Id;
    public final String[] Display;
    public final List<String> Lore;
    public final int[] waitTick;
    public final int flame;

    TitleData(String Id, List<String> Data, List<String> Lore) {
        this.Id = Id;
        this.Lore = Lore;
        flame = Data.size();
        Display = new String[flame];
        waitTick = new int[flame];
        int i = 0;
        for (String data : Data) {
            String[] split = data.split(",");
            Display[i] = split[0];
            waitTick[i] = Integer.parseInt(split[1]);
            i++;
        }
    }

    public ItemStack view(boolean has) {
        Material material;
        if (has) material = Material.PAPER;
        else material = Material.MAP;
        List<String> lore = new ArrayList<>();
        for (String str : Lore) {
            lore.add("§a§l" + str);
        }
        lore.add(Function.decoText("プレビュー"));
        lore.addAll(List.of(Display));
        return new ItemStackData(material, Function.decoText(Id), lore).view();
    }
}
