package swordofmagic7.Effect;

public class EffectData {
    public EffectType effectType;
    public int time;
    public int stack = 1;
    public double[] doubleData;

    public double getDouble(int i) {
        return doubleData[i];
    }

    public int getInt(int i) {
        return Math.toIntExact(Math.round(doubleData[i]));
    }

    EffectData(EffectType effectType, int time) {
        this.time = time;
    }
}
