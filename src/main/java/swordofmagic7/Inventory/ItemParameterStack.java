package swordofmagic7.Inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Item.ItemCategory;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Data.DataBase.format;
import static swordofmagic7.Data.DataBase.getItemParameter;
import static swordofmagic7.Function.Log;

public class ItemParameterStack implements Cloneable {
        public ItemParameter itemParameter;
        public int Amount = 0;

        public ItemParameterStack() {
                this.itemParameter = new ItemParameter();
        }

        public ItemParameterStack(ItemParameter itemParameter) {
                this.itemParameter = itemParameter;
        }

        public boolean isEmpty() {
                return itemParameter.Icon == Material.BARRIER;
        }

        @Override
        public String toString() {
                StringBuilder data;
                if (!isEmpty()) {
                        ItemCategory category = itemParameter.Category;
                        data = new StringBuilder(itemParameter.Id + ",Amount:" + Amount);
                        if (category == ItemCategory.Equipment) {
                                data.append(",Plus:").append(itemParameter.itemEquipmentData.Plus);
                                if (itemParameter.itemEquipmentData.equipmentCategory == EquipmentCategory.Accessory) {
                                        for (Map.Entry<StatusParameter, Double> entry : itemParameter.itemEquipmentData.itemAccessory.Parameter.entrySet()) {
                                                data.append(",").append(entry.getKey()).append(":").append(String.format(format, entry.getValue()));
                                        }
                                }
                                for (RuneParameter runeParameter : itemParameter.itemEquipmentData.Rune) {
                                        if (!runeParameter.toString().equals("None")) {
                                                data.append(",Rune:").append(runeParameter);
                                        }
                                }
                        }
                } else {
                        data = new StringBuilder("None");
                }
                return data.toString();
        }

        public static ItemParameterStack fromString(String data) {
                ItemParameterStack parameterStack = new ItemParameterStack();
                if (!data.equals("None")) {
                        String[] split = data.split(",");
                        if (DataBase.ItemList.containsKey(split[0])) {
                                ItemParameter itemParameter = getItemParameter(split[0]);
                                List<RuneParameter> Rune = new ArrayList<>();
                                for (String str : split) {
                                        if (itemParameter.itemEquipmentData.equipmentCategory == EquipmentCategory.Accessory) {
                                                for (StatusParameter param : StatusParameter.values()) {
                                                        if (str.contains(param + ":")) {
                                                                itemParameter.itemEquipmentData.itemAccessory.Parameter.put(param, Double.parseDouble(str.replace(param + ":", "")));
                                                        }
                                                }
                                        }
                                        if (str.contains("Amount:")) {
                                                parameterStack.Amount = Integer.parseInt(str.replace("Amount:", ""));
                                        }
                                        if (str.contains("Plus:")) {
                                                itemParameter.itemEquipmentData.Plus = Integer.parseInt(str.replace("Plus:", ""));
                                        }
                                        if (str.contains("Rune:")) {
                                                Rune.add(RuneParameter.fromString(str.replace("Rune:", "")));
                                        }
                                        itemParameter.itemEquipmentData.Rune = Rune;
                                }
                                parameterStack.itemParameter = itemParameter;
                        } else {
                                Log("§cError NotFoundItemData: " + split[0], true);
                        }
                }
                return parameterStack;
        }

        public ItemStack viewItem(String format) {
                return itemParameter.viewItem(Amount, format);
        }

        @Override
        public ItemParameterStack clone() {
                try {
                        ItemParameterStack clone = (ItemParameterStack) super.clone();
                        // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
                        return clone;
                } catch (CloneNotSupportedException e) {
                        throw new AssertionError();
                }
        }
}
