package swordofmagic7.Damage;

import swordofmagic7.Status.StatusParameter;

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
