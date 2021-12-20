package swordofmagic7;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.DataBase.getClassData;
import static swordofmagic7.DataBase.getClassList;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Function.colored;

class ClassData {
    String Display;
    String Nick;
    int Tier;
    List<SkillData> SkillList = new ArrayList<>();
}

public class Classes {
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<ClassData, Integer> ClassLevel = new HashMap<>();
    private final HashMap<ClassData, Integer> ClassExp = new HashMap<>();
    Classes(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        for (ClassData classData : getClassList().values()) {
            ClassLevel.put(classData, 1);
            ClassExp.put(classData, 0);
        }
    }

    ClassData classT0 = getClassData("Novice");

    int ReqExp(int Level, int Tier) {
        double reqExp = 100f;
        reqExp *= Math.pow(Level, 1.8);
        reqExp *= Math.pow(Tier+1, 1.8);
        return (int) Math.round(reqExp);
    }

    void setLevel(ClassData classData, int level) {
        ClassLevel.put(classData, level);
    }

    void addLevel(ClassData classData, int addLevel) {
        ClassLevel.put(classData, getLevel(classData) + addLevel);
    }

    int getLevel(ClassData classData) {
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
            BroadCast("&e" + playerData.Nick + "&aさんの&e" + classData.Display + "&aが&eLv" + getLevel(classData) + "&aになりました");
        }
        player.sendMessage(colored("&e経験値&7: &a+" + addExp));
    }

    void removeExp(ClassData classData, int addExp) {
        ClassExp.put(classData, getExp(classData) - addExp);
    }

    int getExp(ClassData classData) {
        return ClassExp.get(classData);
    }

    ClassData topClass() {
        return classT0;
    }
}
