package swordofmagic7;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.getClassData;
import static swordofmagic7.DataBase.getClassList;
import static swordofmagic7.Function.*;

class ClassData {
    String Id;
    Material Icon;
    List<String> Lore;
    String Display;
    String Nick;
    int Tier;
    List<SkillData> SkillList = new ArrayList<>();

    ItemStack view() {
        List<String> lore = new ArrayList<>();
        for (String str : Lore) {
            lore.add("§a§l" + str);
        }
        lore.add(decoText("§3§lスキル一覧"));
        for (SkillData skill: SkillList) {
            lore.add("§7・§e§l" + skill.Display);
        }
        return new ItemStackData(Icon, decoText(Display), lore).view();
    }
}

public class Classes {
    static final int MaxTier = 3;
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<ClassData, Integer> ClassLevel = new HashMap<>();
    private final HashMap<ClassData, Integer> ClassExp = new HashMap<>();
    ClassData[] classTier = new ClassData[4];
    Classes(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        for (ClassData classData : getClassList().values()) {
            ClassLevel.put(classData, 1);
            ClassExp.put(classData, 0);
        }
        classTier[0] = getClassData("Novice");
    }

    int ReqExp(int Level, int Tier) {
        double reqExp = 100f;
        reqExp *= Math.pow(Level, 1.2);
        reqExp *= Math.pow(Tier+1, 1.2);
        if (Level >= 20) {
            reqExp *= 2.5;
        }
        return (int) Math.round(reqExp);
    }

    void setLevel(ClassData classData, int level) {
        ClassLevel.put(classData, level);
    }

    void addLevel(ClassData classData, int addLevel) {
        ClassLevel.put(classData, getLevel(classData) + addLevel);
        playerData.Attribute.addPoint(addLevel);
    }

    int getLevel(ClassData classData) {
        ClassLevel.putIfAbsent(classData, 1);
        return ClassLevel.get(classData);
    }

    void setExp(ClassData classData, int exp) {
        ClassExp.put(classData, exp);
    }

    void addExp(ClassData classData, int addExp) {
        ClassExp.put(classData, getExp(classData) + addExp);
        if (ReqExp(getLevel(classData), classData.Tier) <= getExp(classData)) {
            int addLevel = 0;
            while (ReqExp(getLevel(classData), classData.Tier) <= getExp(classData)) {
                removeExp(classData, ReqExp(getLevel(classData), classData.Tier));
                addLevel++;
            }
            addLevel(classData, addLevel);
            BroadCast("§e" + playerData.Nick + "§aさんの§e[" + classData.Display.replace("§l", "") + "§e]§aが§eLv" + getLevel(classData) + "§aになりました");
            playSound(player, SoundList.LevelUp);
        }
        if (playerData.ExpLog) player.sendMessage("§e経験値§7: §a+" + addExp);
    }

    void removeExp(ClassData classData, int addExp) {
        ClassExp.put(classData, getExp(classData) - addExp);
    }

    int getExp(ClassData classData) {
        return ClassExp.get(classData);
    }

    ClassData topClass() {
        for (int i = classTier.length-1; i > 0; i--) {
            if (classTier[i] != null) return classTier[i];
        }
        return getClassData("Novice");
    }
}
