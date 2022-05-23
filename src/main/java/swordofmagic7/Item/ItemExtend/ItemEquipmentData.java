package swordofmagic7.Item.ItemExtend;

import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemEquipmentData implements Cloneable {
    public EquipmentCategory equipmentCategory;
    public EquipmentSlot EquipmentSlot;
    public int ReqLevel = 0;
    public int Plus = 0;
    public int UpgradeCost = 999;
    public HashMap<StatusParameter, Double> Parameter = new HashMap<>();
    public int RuneSlot = 0;
    public List<RuneParameter> Rune = new ArrayList<>();
    public double RuneMultiply = 1;
    public ItemAccessory itemAccessory = new ItemAccessory();

    public HashMap<StatusParameter, Double> Parameter() {
        return Parameter(PlayerData.MaxLevel);
    }

    public boolean isAccessory() {
        return equipmentCategory == EquipmentCategory.Accessory;
    }

    public HashMap<StatusParameter, Double> Parameter(int limit) {
        HashMap<StatusParameter, Double> Parameter = new HashMap<>();
        double multiply = 1 + (isAccessory() ? Plus/100f : Plus*0.05+(Math.pow(Plus, 1.8)/100));
        for (StatusParameter statusParameter : StatusParameter.values()) {
            double parameter = this.Parameter.get(statusParameter) * multiply;
            for (RuneParameter rune : Rune) {
                parameter += rune.Parameter(statusParameter, limit)*RuneMultiply;
            }
            if (isAccessory()) {
                parameter += itemAccessory.Parameter.getOrDefault(statusParameter, 0d) * multiply;
            }
            Parameter.put(statusParameter, parameter);
        }
        return Parameter;
    }

    public int getRuneSize() {
        return Rune.size();
    }

    public RuneParameter getRune(int i) {
        return Rune.get(i);
    }

    public void addRune(RuneParameter rune) {
        Rune.add(rune);
    }

    public void removeRune(int i) {
        Rune.remove(i);
    }

    @Override
    public ItemEquipmentData clone() {
        try {
            ItemEquipmentData clone = (ItemEquipmentData) super.clone();
            clone.Rune = new ArrayList<>(Rune);
            clone.itemAccessory = itemAccessory.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
