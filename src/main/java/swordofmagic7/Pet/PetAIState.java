package swordofmagic7.Pet;

enum PetAIState {
    Attack("攻撃"),
    Follow("追従"),
    Support("サポート"),
    ;

    public String Display;

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
