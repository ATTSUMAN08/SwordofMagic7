package swordofmagic7.Classes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Classes {
    public static final int MaxTier = 1;
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<ClassData, Integer> ClassLevel = new HashMap<>();
    private final HashMap<ClassData, Integer> ClassExp = new HashMap<>();
    public ClassData[] classTier = new ClassData[4];
    public Classes(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        for (ClassData classData : DataBase.getClassList().values()) {
            ClassLevel.put(classData, 1);
            ClassExp.put(classData, 0);
        }
        classTier[0] = DataBase.getClassData("Novice");
    }

    public int ReqExp(int Level, int Tier) {
        double reqExp = 100f;
        reqExp *= Math.pow(Level, 1.2);
        reqExp *= Math.pow(Tier+1, 1.2);
        for (int i = 0; i < Math.floor((Level-10)/10f); i++) {
            reqExp *= 2.5;
        }
        return (int) Math.round(reqExp);
    }

    public void setLevel(ClassData classData, int level) {
        ClassLevel.put(classData, level);
    }

    public void addLevel(ClassData classData, int addLevel) {
        ClassLevel.put(classData, getLevel(classData) + addLevel);
        if (classData.Id.equals("Novice")) {
            playerData.Attribute.addPoint(addLevel*5);
        }
    }

    public int getLevel(ClassData classData) {
        if (ClassLevel.getOrDefault(classData, 0) <= 0) {
            ClassLevel.put(classData, 1);
        }
        return ClassLevel.get(classData);
    }

    public void setExp(ClassData classData, int exp) {
        ClassExp.put(classData, exp);
    }

    public void addExp(ClassData classData, int addExp) {
        ClassExp.put(classData, getExp(classData) + addExp);
        if (ReqExp(getLevel(classData), classData.Tier) <= getExp(classData)) {
            int addLevel = 0;
            while (ReqExp(getLevel(classData), classData.Tier) <= getExp(classData)) {
                removeExp(classData, ReqExp(getLevel(classData), classData.Tier));
                addLevel++;
            }
            addLevel(classData, addLevel);
            BroadCast(playerData.getNick() + "§aさんの§e[" + classData.Display.replace("§l", "") + "§e]§aが§eLv" + getLevel(classData) + "§aになりました");
            playSound(player, SoundList.LevelUp);
        }
        if (playerData.ExpLog) player.sendMessage("§e経験値[" + classData.Color + classData.Display + "§e]§7: §a+" + addExp);
    }

    public void removeExp(ClassData classData, int addExp) {
        ClassExp.put(classData, getExp(classData) - addExp);
    }

    public int getExp(ClassData classData) {
        if (!ClassExp.containsKey(classData)) {
            ClassExp.put(classData, 0);
        }
        return ClassExp.get(classData);
    }

    public ClassData topClass() {
        for (int i = classTier.length-1; i > 0; i--) {
            if (classTier[i] != null) return classTier[i];
        }
        return DataBase.getClassData("Novice");
    }

    public List<SkillData> getPassiveSkillList() {
        List<SkillData> list = new ArrayList<>();
        for (ClassData classData : classTier) {
            if (classData != null) {
                for (SkillData skillData : classData.SkillList) {
                    if (skillData.SkillType.isPassive()) {
                        list.add(skillData);
                    }
                }
            }
        }
        return list;
    }

    public List<SkillData> getActiveSkillList() {
        List<SkillData> list = new ArrayList<>();
        for (ClassData classData : classTier) {
            if (classData != null) {
                for (SkillData skillData : classData.SkillList) {
                    if (!skillData.SkillType.isActive()) {
                        list.add(skillData);
                    }
                }
            }
        }
        return list;
    }

    public void ClassChange(ClassData classData) {
        boolean changeAble = true;
        List<String> reqText = new ArrayList<>();
        for (Map.Entry<ClassData, Integer> classes : classData.ReqClass.entrySet()) {
            if (playerData.Classes.getLevel(classes.getKey()) < classes.getValue()) {
                changeAble = false;
                reqText.add("§7・§e§l" + classes.getKey().Display + " Lv" + classes.getValue() + " §c✖");
            } else {
                reqText.add("§7・§e§l" + classes.getKey().Display + " Lv" + classes.getValue() + " §b✔");
            }
        }
        if (changeAble) {
            classTier[classData.Tier] = classData;
            player.sendMessage("§e[クラスT" + classData.Tier + "]§aを" + classData.Color + "[" + classData.Display + "]§aに§b転職§aしました");
            playSound(player, SoundList.LevelUp);
        } else {
            player.sendMessage(decoText("§c転職条件"));
            for (String str : reqText) {
                player.sendMessage(str);
            }
            playSound(player, SoundList.Nope);
        }
    }

    private final ClassData[] ClassSelectCache = new ClassData[9];
    private int SelectTier = 0;
    public void ClassSelectView(int tier) {
        Inventory inv = decoInv("クラスカウンター", 1);
        SelectTier = tier;
        switch (tier) {
            case 0 -> {
                ItemStack tier1 = new ItemStackData(Material.END_CRYSTAL, decoText("クラス一覧 [T1]")).view();
                inv.setItem(0, tier1);
                playSound(player, SoundList.MenuOpen);
            }
            case 1 -> {
                int i = 0;
                for (ClassData classData : DataBase.getClassList().values()) {
                    if (classData.Tier == 1) {
                        ClassSelectCache[i] = classData;
                        inv.setItem(i, classData.view());
                        i++;
                    }
                }
                playSound(player, SoundList.Click);
            }
        }
        player.openInventory(inv);
    }

    public void ClassSelectClick(InventoryView view, int slot) {
        if (equalInv(view, "クラスカウンター")) {
            if (SelectTier > 0 && ClassSelectCache[slot] != null) {
                if (!(playerData.Equipment.isEquip(EquipmentSlot.MainHand)
                || playerData.Equipment.isEquip(EquipmentSlot.OffHand)
                || playerData.Equipment.isEquip(EquipmentSlot.Armor))) {
                    ClassData classData = ClassSelectCache[slot];
                    ClassChange(classData);
                } else {
                    player.sendMessage("§e[装備]§aを外してください");
                    playSound(player, SoundList.Nope);
                }
            } else if (0 <= slot && slot < MaxTier){
                ClassSelectView(slot+1);
            }
        }
    }
}
