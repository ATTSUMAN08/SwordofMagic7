package swordofmagic7.viewBar.SideBarToDo;

public enum SideBarToDoType {
    ItemAmount,
    LifeInfo,
    ClassInfo,
    SkillCoolTime,
    ;

    public boolean isItemAmount() {
        return this == ItemAmount;
    }

    public boolean isLifeInfo() {
        return this == LifeInfo;
    }

    public boolean isClassInfo() {
        return this == ClassInfo;
    }
}
