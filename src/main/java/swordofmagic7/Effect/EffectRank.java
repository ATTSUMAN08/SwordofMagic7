package swordofmagic7.Effect;

public enum EffectRank {
    Normal,
    High,
    Impossible,
    ;

    public boolean isNormal() {
        return this == Normal;
    }

    public boolean isHigh() {
        return this == High;
    }

    public boolean isImpossible() {
        return this == Impossible;
    }

}
