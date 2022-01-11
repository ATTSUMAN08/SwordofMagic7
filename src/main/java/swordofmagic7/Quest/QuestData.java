package swordofmagic7.Quest;

import swordofmagic7.Inventory.ItemParameterStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestData {
    public String Id;
    public String Display;
    public List<String> Lore;
    public QuestType type;
    public HashMap<QuestReqContentKey, Integer> ReqContent = new HashMap<>();

    public int RewardExp;
    public List<ItemParameterStack> RewardItemStack = new ArrayList<>();
}
