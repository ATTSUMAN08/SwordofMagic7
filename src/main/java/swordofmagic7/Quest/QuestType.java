package swordofmagic7.Quest;

public enum QuestType {
    Item("アイテム収集"),
    Enemy("エネミー討伐"),
    ;

    public final String Display;

    QuestType(String Display) {
        this.Display = Display;
    }

    public boolean isItem() {
        return this == Item;
    }

    public boolean isEnemy() {
        return this == Enemy;
    }
}
