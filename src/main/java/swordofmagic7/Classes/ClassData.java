package swordofmagic7.Classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Skill.SkillData;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.decoText;

public class ClassData {
    public String Id;
    public String Color;
    public Material Icon;
    public List<String> Lore;
    public String Display;
    public String Nick;
    public int Tier;
    public List<SkillData> SkillList = new ArrayList<>();

    public ItemStack view() {
        List<String> lore = new ArrayList<>();
        for (String str : Lore) {
            lore.add("§a§l" + str);
        }
        lore.add(decoText("§3§lスキル一覧"));
        for (SkillData skill : SkillList) {
            lore.add("§7・§e§l" + skill.Display);
        }
        return new ItemStackData(Icon, decoText(Display), lore).view();
    }
}
