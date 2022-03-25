package swordofmagic7.Skill;

public class SkillParameter {
    public String Display;
    public double Value = 0;
    public double Increase = 0;
    public String Prefix = "";
    public String Suffix = "";
    public int Format = 0;

    public String valueView() {
        if (Format > -1) {
            return Prefix + String.format("%." + Format + "f", Value) + Suffix;
        } else {
            return Prefix + Suffix;
        }
    }
}
