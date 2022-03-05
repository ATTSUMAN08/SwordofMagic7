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
import swordofmagic7.Life.LifeStatus;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Sound.SoundList;

import java.util.*;

import static swordofmagic7.Data.DataBase.getClassData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Classes {
    public static final int MaxSlot = 4;
    public static final int MaxLevel = 15;
    public static final int[] SlotReqLevel = {1, 10, 30, 50};
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<ClassData, Integer> ClassLevel = new HashMap<>();
    private final HashMap<ClassData, Integer> ClassExp = new HashMap<>();
    public ClassData[] classSlot = new ClassData[MaxSlot];
    public Classes(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        for (ClassData classData : DataBase.getClassList().values()) {
            ClassLevel.put(classData, 1);
            ClassExp.put(classData, 0);
        }
        classSlot[0] = getClassData("Novice");
    }

    public static int ReqExp(int Level) {
        double reqExp = 100f;
        reqExp *= Math.pow(Level, 1.5);
        if (Level >= 30) reqExp *= 3;
        if (Level >= 50) reqExp *= 3;
        return (int) Math.round(reqExp);
    }

    public ClassData lastClass() {
        ClassData lastClass = getClassData("Novice");
        for (ClassData classData : classSlot) {
            if (classData != null){
                lastClass = classData;
            }
        }
        return lastClass;
    }

    public void setClassLevel(ClassData classData, int level) {
        ClassLevel.put(classData, level);
    }

    public void addClassLevel(ClassData classData, int addLevel) {
        ClassLevel.put(classData, getClassLevel(classData) + addLevel);
        if (getClassLevel(classData) >= MaxLevel) {
            setClassExp(classData, 0);
        }
    }

    public int getClassLevel(ClassData classData) {
        if (ClassLevel.getOrDefault(classData, 0) <= 0) {
            ClassLevel.put(classData, 1);
        }
        return ClassLevel.get(classData);
    }

    public void setClassExp(ClassData classData, int exp) {
        ClassExp.put(classData, exp);
    }

    public void addClassExp(ClassData classData, int addExp) {
        if (getClassLevel(classData) >= MaxLevel) {
            ClassExp.put(classData, 0);
            addExp = 0;
        } else {
            ClassExp.put(classData, getClassExp(classData) + addExp);
        }
        int Level = getClassLevel(classData);
        if (ReqExp(Level) <= getClassExp(classData)) {
            int addLevel = 0;
            while (ReqExp(Level+addLevel) <= getClassExp(classData)) {
                removeExp(classData, ReqExp(Level+addLevel));
                addLevel++;
            }
            addClassLevel(classData, addLevel);
            BroadCast(playerData.getNick() + "§aさんの§e[" + classData.Display.replace("§l", "") + "§e]§aが§eLv" + getClassLevel(classData) + "§aになりました");
            playSound(player, SoundList.LevelUp);
        }
        if (playerData.ExpLog) player.sendMessage("§e経験値[" + classData.Color + classData.Display + "§e]§7: §a+" + addExp);
    }

    public void removeExp(ClassData classData, int addExp) {
        ClassExp.put(classData, getClassExp(classData) - addExp);
    }

    public int getClassExp(ClassData classData) {
        if (!ClassExp.containsKey(classData)) {
            ClassExp.put(classData, 0);
        }
        return ClassExp.get(classData);
    }

    public String viewExpPercent(ClassData classData) {
        return String.format("%.3f", (float) getClassExp(classData)/ ReqExp(getClassLevel(classData))*100) + "%";
    }

    public Set<SkillData> getPassiveSkillList() {
        Set<SkillData> list = new HashSet<>();
        for (ClassData classData : classSlot) {
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

    public Set<SkillData> getActiveSkillList() {
        Set<SkillData> list = new HashSet<>();
        for (ClassData classData : classSlot) {
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

    public void ClassChange(ClassData classData, int slot) {
        boolean changeAble = true;
        List<String> reqText = new ArrayList<>();
        for (Map.Entry<ClassData, Integer> classes : classData.ReqClass.entrySet()) {
            if (playerData.Classes.getClassLevel(classes.getKey()) < classes.getValue()) {
                changeAble = false;
                reqText.add("§7・§e§l" + classes.getKey().Display + " Lv" + classes.getValue() + " §c✖");
            } else {
                reqText.add("§7・§e§l" + classes.getKey().Display + " Lv" + classes.getValue() + " §b✔");
            }
        }
        if (changeAble) {
            classSlot[slot] = classData;
            player.sendMessage("§e[クラススロット" + (slot+1) + "]§aを" + classData.Color + "[" + classData.Display + "]§aに§b転職§aしました");
            playSound(player, SoundList.LevelUp);
        } else {
            player.sendMessage(decoText("§c転職条件"));
            for (String str : reqText) {
                player.sendMessage(str);
            }
            playSound(player, SoundList.Nope);
        }
    }

    private final ClassData[] ClassSelectCache = new ClassData[54];
    private int SelectSlot = -1;
    public void ClassSelectView(boolean isSlotMenu) {
        int size = 6;
        if (isSlotMenu) size = 1;
        Inventory inv = decoInv("クラスカウンター", size);
        if (isSlotMenu) {
            SelectSlot = -1;
            for (int i = 0; i < SlotReqLevel.length; i++) {
                inv.setItem(i, new ItemStackData(Material.END_CRYSTAL, decoLore("クラススロット[" + (i+1) + "]"), decoLore("必要レベル") + SlotReqLevel[i]).view());
            }
        } else {
            HashMap<Integer, String> ClassTable = new HashMap<>();
            ClassTable.put(0, "Novice");
            ClassTable.put(9, "Swordman");
            ClassTable.put(18, "Mage");
            ClassTable.put(27, "Gunner");
            ClassTable.put(36, "Cleric");
            ClassTable.put(37, "Priest");
            ClassTable.put(45, "Tamer");
            for (Map.Entry<Integer, String> data : ClassTable.entrySet()) {
                ClassData classData = getClassData(data.getValue());
                ClassSelectCache[data.getKey()] = classData;
                inv.setItem(data.getKey(), classData.view());
            }
        }
        playSound(player, SoundList.Click);
        player.openInventory(inv);
    }

    public void ClassSelectClick(InventoryView view, int slot) {
        if (equalInv(view, "クラスカウンター")) {
            if (SelectSlot == -1 && slot < classSlot.length) {
                if (SlotReqLevel[slot] <= playerData.Level) {
                    SelectSlot = slot;
                    ClassSelectView(false);
                } else {
                    player.sendMessage("§aレベルが足りません");
                    playSound(player, SoundList.Nope);
                }
            } else if (ClassSelectCache[slot] != null) {
                for (ClassData classData : classSlot) {
                    if (classData == ClassSelectCache[slot]) {
                        player.sendMessage("§aすでに" + classData.Color + classData.Display + "]§aは使用されています");
                        playSound(player, SoundList.Nope);
                        return;
                    }
                }
                if (!(playerData.Equipment.isEquip(EquipmentSlot.MainHand)
                        || playerData.Equipment.isEquip(EquipmentSlot.OffHand)
                        || playerData.Equipment.isEquip(EquipmentSlot.Armor))) {
                    ClassData classData = ClassSelectCache[slot];
                    ClassChange(classData, SelectSlot);
                } else {
                    player.sendMessage("§e[装備]§aを外してください");
                    playSound(player, SoundList.Nope);
                }
            }
        }
    }
}
