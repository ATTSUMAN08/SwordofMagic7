package swordofmagic7.Effect;

public class EffectData {
    public EffectType effectType;
    public int time;
    public int stack = 1;
    public double[] doubleData;

    EffectData(EffectType effectType, int time) {
        this.time = time;
    }
}
