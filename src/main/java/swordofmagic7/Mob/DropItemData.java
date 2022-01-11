package swordofmagic7.Mob;

import swordofmagic7.Item.ItemParameter;

public class DropItemData {
    public ItemParameter itemParameter;
    public int MaxAmount = 1;
    public int MinAmount = 1;
    public double Percent = 0;
    public int MaxLevel = 0;
    public int MinLevel = 0;

    public DropItemData(ItemParameter itemParameter) {
        this.itemParameter = itemParameter;
    }

    public DropItemData(ItemParameter itemParameter, double Percent) {
        this.itemParameter = itemParameter;
        this.Percent = Percent;
    }
}
