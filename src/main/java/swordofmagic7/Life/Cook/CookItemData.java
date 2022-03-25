package swordofmagic7.Life.Cook;

import swordofmagic7.Item.ItemParameter;

public class CookItemData {
    public ItemParameter itemParameter;
    public int Amount;
    public double Percent;

    public CookItemData(ItemParameter itemParameter, int Amount, double Percent) {
        this.itemParameter = itemParameter;
        this.Amount = Amount;
        this.Percent = Percent;
    }

    public double getPercent(int ReqLevel, int Level) {
        int level = Level - ReqLevel;
        return Math.min(Math.max(Percent * (1+level/50f), 0), 1);
    }
}
