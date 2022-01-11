package swordofmagic7.Skill;

public class SkillParameter {
    public String Display;
    public double Value = 0;
    public double Increase = 0;
    public String Prefix = "";
    public String Suffix = "";
    public String Format;

    public String valueView() {
        return String.format(Format, Value);
    }
}
