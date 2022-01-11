package swordofmagic7.Quest;

public class QuestReqContentKey {
    public String mainKey;
    public int[] intKey;
    public double[] doubleKey;

    public String toString(QuestType questType) {
        if (questType.isItem()) {
            return mainKey;
        } else if (questType.isEnemy()) {
            return mainKey + "Lv" + intKey[0];
        }
        return "";
    }
}
