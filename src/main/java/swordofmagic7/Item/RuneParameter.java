package swordofmagic7.Item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Skill.SkillParameter;
import swordofmagic7.Status.StatusParameter;
import swordofmagic7.TextView.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.DataBase.getRuneParameter;
import static swordofmagic7.Function.*;

public class RuneParameter implements Cloneable {
    public String Id;
    public Material Icon;
    public String Display = "ルーン";
    public List<String> Lore = new ArrayList<>();
    public double Quality = 0.5;
    public int Level = 0;
    public boolean isSpecial = false;
    public boolean isHide = false;
    public boolean isLoreHide = false;
    public HashMap<StatusParameter, Double> Parameter = new HashMap<>();
    public List<SkillParameter> AdditionParameter = new ArrayList<>();

    public RuneParameter() {
        for (StatusParameter param : StatusParameter.values()) {
            Parameter.put(param, 0d);
        }
    }

    public boolean isEmpty() {
        return Level == 0;
    }


    public double Parameter(StatusParameter param) {
        return Parameter(param, Level);
    }

    public double Parameter(StatusParameter param, int limit) {
        return Parameter.get(param) * (1+0.0125*Math.pow(Math.min(Level, limit), 1.5) * (Quality*2+1));
    }

    public double AdditionParameterValue(int i) {
        if (AdditionParameter.size() > i) {
            return AdditionParameter.get(i).Value + (AdditionParameter.get(i).Increase*(Level-1));
        } else {
            return -1;
        }
    }

    public int AdditionParameterValueInt(int i) {
        return Math.toIntExact(Math.round(AdditionParameterValue(i)));
    }

    public ItemStack viewRune(String format) {
        return viewRune(format, false);
    }

    public ItemStack viewRune(String format, boolean isLoreHide) {
        List<String> Lore = new ArrayList<>();
        if (isLoreHide) Lore.add("§c§lこの情報へのアクセス権限がありません");
        else Lore.addAll(loreText(this.Lore));
        Lore.add(decoText("§3§lパラメーター"));
        Lore.add(decoLore("§e§lレベル") + Level);
        Lore.add(decoLore("§e§l品質") + String.format(format, Quality*100) + "%");
        for (StatusParameter param : StatusParameter.values()) {
            if (isZero(Parameter.get(param))) Lore.add(param.DecoDisplay +  String.format(format, Parameter(param)));
        }
        for (SkillParameter param : AdditionParameter) {
            Lore.add(decoLore(param.Display) + param.valueView(Level));
        }
        ItemStack item = new ItemStackData(Icon, decoText(Display), Lore).view();
        item.setAmount(Math.min(DataBase.MaxStackAmount, Level));
        return item;
    }

    public TextView getTextView(String format) {
        ItemStack rune = viewRune(format, isLoreHide);
        StringBuilder hoverText = new StringBuilder(rune.getItemMeta().getDisplayName());
        for (String str : rune.getLore()) {
            hoverText.append("\n").append(str);
        }
        return new TextView().addText("§e[" + Display + "§e]").addHover(hoverText.toString()).reset();
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

    @Override
    public String toString() {
        String data;
        if (!isEmpty()) {
            data = Id + ";Level:" + Level + ";Quality:" + String.format("%.5f", Quality);
        } else {
            data = "None";
        }
        return data;
    }

    public static RuneParameter fromString(String data) {
        RuneParameter runeParameter = new RuneParameter();
        if (!data.equals("None")) {
            String[] split = data.split(";");
            if (DataBase.RuneList.containsKey(split[0])) {
                runeParameter = getRuneParameter(split[0]);
                for (String str : split) {
                    if (str.contains("Level:")) {
                        runeParameter.Level = Integer.parseInt(str.replace("Level:", ""));
                    } else if (str.contains("Quality:")) {
                        runeParameter.Quality = Double.parseDouble((str.replace("Quality:", "")));
                    }
                }
            } else {
                Log("§cError NotFoundRuneData: " + split[0]);
            }
        }
        return runeParameter;
    }
}
