package swordofmagic7.Mob;

import swordofmagic7.Item.RuneParameter;

public class DropRuneData {
    public RuneParameter runeParameter;
    public double Percent = 0;
    public int MaxLevel = 0;
    public int MinLevel = 0;

    public DropRuneData(RuneParameter runeParameter) {
        this.runeParameter = runeParameter;
    }
}
