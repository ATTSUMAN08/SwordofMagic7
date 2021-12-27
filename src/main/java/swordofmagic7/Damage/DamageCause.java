package swordofmagic7.Damage;

public enum DamageCause {
    ATK,
    MAT,
    ;

    boolean isATK() {
        return this == ATK;
    }

    boolean isMAT() {
        return this == MAT;
    }
}
