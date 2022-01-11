package swordofmagic7.Item.ItemExtend;

import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemEquipmentData implements Cloneable {
    public EquipmentCategory EquipmentCategory;
    public EquipmentSlot EquipmentSlot;
    public int Durable = 0;
    public int MaxDurable = 0;
    public int ReqLevel = 0;
    public int Plus = 0;
    public int UpgradeCost = 999;
    public HashMap<StatusParameter, Double> Parameter = new HashMap<>();
    public int RuneSlot = 0;
    public List<RuneParameter> Rune = new ArrayList<>();

    public HashMap<StatusParameter, Double> Parameter() {
        HashMap<StatusParameter, Double> Parameter = new HashMap<>();
        for (StatusParameter statusParameter : StatusParameter.values()) {
            double parameter = this.Parameter.get(statusParameter) * (1+Plus*0.05);
            for (RuneParameter rune : Rune) {
                parameter += rune.Parameter(statusParameter);
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
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
