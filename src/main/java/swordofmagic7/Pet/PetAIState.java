package swordofmagic7.Pet;

public enum PetAIState {
    Attack("攻撃"),
    Follow("追従"),
    Support("サポート"),
    ;

    public final String Display;

    PetAIState(String Display) {
        this.Display = Display;
    }

    public boolean isAttack() {
        return this == Attack;
    }

    public boolean isFollow() {
        return this == Follow;
    }
}
