package swordofmagic7.Skill;

public class SkillParameter {
    public String Display;
    public double Value = 0;
    public double Increase = 0;
    public String Prefix = "";
    public String Suffix = "";
    public boolean isInt;

    public String valueView() {
        if (isInt) {
            return String.valueOf((int) Math.round(Value));
        } else {
            return String.valueOf(Value);
        }
    }
}
