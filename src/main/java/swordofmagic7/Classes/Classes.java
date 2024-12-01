package swordofmagic7.Classes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Sound.SoundList;

import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Classes {
    public static final int MaxSlot = 4;
    public static final int MaxLevel = 25;
    public static final int[] SlotReqLevel = {1, 10, 30, 50};
    public static final ClassData defaultClass = getClassData("Novice");
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
        classSlot[0] = defaultClass;
    }

    public static int[] ReqExp;
    public static int ReqExp(int Level) {
        if (ReqExp == null) {
            ReqExp = new int[PlayerData.MaxLevel+1];
            for (int level = 0; level < ReqExp.length; level++) {
                double reqExp = 100f;
                reqExp *= Math.pow(level, 1.8);
                reqExp *= Math.ceil(level/10f);
                if (level >= 30) reqExp *= 2;
                if (level >= 50) reqExp *= 4;
                if (level >= 60) reqExp *= 4;
                if (level >= 65) reqExp *= 2;
                ReqExp[level] = (int) Math.round(reqExp);
            }
        }
        if (Level < 0) return 100;
        if (Level > PlayerData.MaxLevel) return Integer.MAX_VALUE;
        return ReqExp[Level];
    }

    public ClassData lastClass() {
        ClassData lastClass = defaultClass;
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

    public synchronized void addClassExp(ClassData classData, int addExp) {
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
            BroadCast(playerData.getNick() + "§aさんの§e[" + classData.Display.replace("§l", "") + "§e]§aが§eLv" + getClassLevel(classData) + "§aになりました", true);
            playSound(player, SoundList.LevelUp);
        }
        if (playerData.ExpLog) player.sendMessage("§e経験値[" + classData.Color + classData.Display + "§e]§7: §a+" + addExp + " §7(" + String.format(format, (double) addExp/Classes.ReqExp(getClassLevel(classData))*100) + "%)");
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
                reqText.add("§7・" + classes.getKey().getDisplay(true) + " Lv" + classes.getValue() + " §c✖");
            } else {
                reqText.add("§7・" + classes.getKey().getDisplay(true) + " Lv" + classes.getValue() + " §b✔");
            }
        }
        if (changeAble) {
            classSlot[slot] = classData;
            player.sendMessage("§e[クラススロット" + (slot+1) + "]§aを" + classData.getDisplay(true, true) + "§aに§b転職§aしました");
            playerData.EffectManager.clearEffect();
            int petSummoned = playerData.PetSummon.size();
            for (int i = 0; i < petSummoned; i++) {
                playerData.PetSummon.getFirst().cage();
            }
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
                List<String> lore = new ArrayList<>();
                lore.add(decoLore("必要レベル") + SlotReqLevel[i]);
                if (classSlot[i] != null) {
                    lore.add(decoLore("使用状況") + classSlot[i].getDisplay());
                } else {
                    lore.add(decoLore("使用状況") + "§7§l未使用");
                }
                inv.setItem(i, new ItemStackData(Material.END_CRYSTAL, decoText("クラススロット[" + (i+1) + "]"), lore).view());
            }
        } else {
            for (Map.Entry<Integer, String> data : ClassDataMap.entrySet()) {
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
                        player.sendMessage("§aすでに[" + classData.Color + classData.Display + "]§aは使用されています");
                        playSound(player, SoundList.Nope);
                        return;
                    }
                }
                ClassData classData = ClassSelectCache[slot];
                ClassChange(classData, SelectSlot);
            }
        }
    }

    public ClassData topClass() {
        return classSlot[0];
    }
}
