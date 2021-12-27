package swordofmagic7.Inventory;

import org.bukkit.Material;
import swordofmagic7.Item.ItemParameter;

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
}
