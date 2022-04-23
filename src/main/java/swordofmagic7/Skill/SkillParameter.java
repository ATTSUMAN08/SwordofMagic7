package swordofmagic7.Skill;

public class SkillParameter implements Cloneable {
    public String Display;
    public double Value = 0;
    public double Increase = 0;
    public String Prefix = "";
    public String Suffix = "";
    public int Format = 0;

    public String valueView() {
        return valueView(1);
    }

    public String valueView(int level) {
        if (Format > -1) {
            return Prefix + String.format("%." + Format + "f", Value + Increase*(level-1)) + Suffix;
        } else {
            return Prefix + Suffix;
        }
    }

    @Override
    public SkillParameter clone() {
        try {
            return (SkillParameter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
