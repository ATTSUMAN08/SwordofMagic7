package swordofmagic7.Skill;

import org.bukkit.Location;

import java.util.*;

public class MagicCircleData {
    public static HashMap<UUID, MagicCircleData> MagicCircleList = new HashMap<>();
    public static Set<MagicCircleData> getMagicCircleList() {
        for (Map.Entry<UUID, MagicCircleData> data : new HashMap<>(MagicCircleList).entrySet()) {
            if (data.getValue().isCancel()) {
                MagicCircleList.remove(data.getKey());
            }
        }
        return new HashSet<>(MagicCircleList.values());
    }

    public MagicCircleData(Location location, Thread thread, SkillData skillData) {
        this.location = location;
        this.thread = thread;
        this.skillData = skillData;
        uuid = UUID.randomUUID();
        MagicCircleList.put(uuid, this);
    }

    public void cancel() {
        thread.interrupt();
        MagicCircleList.remove(uuid);
    }

    public boolean isCancel() {
        return thread.isInterrupted();
    }

    public UUID uuid;
    public Location location;
    public Thread thread;
    public SkillData skillData;
}
