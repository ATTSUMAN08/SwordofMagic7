package swordofmagic7.Inventory;

import org.bukkit.Material;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Item.ItemCategory;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.getItemParameter;
import static swordofmagic7.Function.Log;

public class ItemParameterStack {
        public ItemParameter itemParameter;
        public ItemParameterStack() {
        this.itemParameter = new ItemParameter();
        }

        public ItemParameterStack(ItemParameter itemParameter) {
        this.itemParameter = itemParameter;
        }
        public int Amount = 0;

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
                                data.append(",Plus:").append(itemParameter.itemEquipmentData.Plus).append(",Durable:").append(itemParameter.itemEquipmentData.Durable);
                                for (RuneParameter runeParameter : itemParameter.itemEquipmentData.Rune) {
                                        if (runeParameter.toString().equals("None"))
                                                data.append(",Rune:").append(runeParameter);
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
                                        if (str.contains("Amount:")) {
                                                parameterStack.Amount = Integer.parseInt(str.replace("Amount:", ""));
                                        }
                                        if (str.contains("Durable:")) {
                                                itemParameter.itemEquipmentData.Durable = Integer.parseInt(str.replace("Durable:", ""));
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
}
