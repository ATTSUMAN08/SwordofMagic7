package swordofmagic7.Quest;

import java.util.HashMap;
import java.util.Map;

public class QuestProcess {
    public HashMap<QuestReqContentKey, Integer> ProcessContent = new HashMap<>();

    QuestProcess(QuestData questData) {
        for (Map.Entry<QuestReqContentKey, Integer> content : questData.ReqContent.entrySet()) {
            ProcessContent.put(content.getKey(), 0);
        }
    }
}
