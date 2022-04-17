package swordofmagic7.Effect;

public class EffectData {
    public EffectType effectType;
    public int time;
    public int stack = 1;
    public Object[] objectData;
    public boolean flags = false;

    public double getDouble(int i) {
        return (double) objectData[i];
    }

    public int getInt(int i) {
        return (int) objectData[i];
    }

    public Object getObject(int i) {
        return objectData[i];
    }

    public void addStack(int addStack) {
        stack = Math.min(stack + addStack, effectType.MaxStack);
    }

    EffectData(EffectType effectType, int time) {
        this.effectType = effectType;
        this.time = time;
    }
}
