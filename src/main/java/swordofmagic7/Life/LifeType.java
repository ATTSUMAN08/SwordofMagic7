package swordofmagic7.Life;

import java.util.HashMap;

public enum LifeType {
    Mine("採掘"),
    Lumber("伐採"),
    Harvest("採集"),
    Angler("釣獲"),
    Cook("料理"),
    Smith("鍛冶"),
    ;

    public final String Display;
    public static final HashMap<String, LifeType> getData = new HashMap<>();

    LifeType(String Display) {
        this.Display = Display;
    }

    public static void Initialize() {
        for (LifeType type : LifeType.values()) {
            getData.put(type.Display, type);
            getData.put(String.valueOf(type), type);
        }
    }

    public static LifeType getData(String str) {
        return getData.get(str);
    }
}
