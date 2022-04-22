package swordofmagic7.Effect;

public enum EffectRank {
    Normal("§a§l一般解除"),
    High("§b§l高級解除"),
    Impossible("§c§l解除不可"),
    ;

    public String Display;

    EffectRank(String Display) {
        this.Display = Display;
    }

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
