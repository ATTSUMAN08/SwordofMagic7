package swordofmagic7.InstantBuff;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Status.StatusParameter;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.Function.playerWhileCheck;

public class InstantBuff {
    private final Player player;
    private final PlayerData playerData;
    public HashMap<String, InstantBuffData> InstantBuffs = new HashMap<>();
    public void instantBuff(String key, InstantBuffData data) {
        InstantBuffs.put(key, data);
    }

    public HashMap<StatusParameter, Double> getFixed() {
        HashMap<StatusParameter, Double> value = new HashMap<>();
        for (InstantBuffData data : InstantBuffs.values()) {
            for (StatusParameter param : StatusParameter.values()) {
                value.put(param, value.getOrDefault(param, 0d) + data.Fixed.getOrDefault(param, 0d));
            }
        }
        return value;
    }

    public HashMap<StatusParameter, Double> getMultiply() {
        HashMap<StatusParameter, Double> value = new HashMap<>();
        for (StatusParameter param : StatusParameter.values()) {
            double deMultiply = 1;
            for (InstantBuffData data : InstantBuffs.values()) {
                double multiply = data.Multiply.getOrDefault(param, 0d);
                if (multiply >= 0) {
                    value.put(param, value.getOrDefault(param, 0d) + multiply);
                } else {
                    deMultiply *= multiply;
                }
            }
            value.put(param, value.getOrDefault(param, 0d) * deMultiply);
        }
        return value;
    }

    public InstantBuff(PlayerData playerData) {
        player = playerData.player;
        this.playerData = playerData;
        MultiThread.TaskRun(() -> {
            while (playerWhileCheck(playerData)) {
                if (!InstantBuffs.isEmpty()) {
                    for (Map.Entry<String, InstantBuffData> data : InstantBuffs.entrySet()) {
                        data.getValue().time--;
                    }
                    InstantBuffs.entrySet().removeIf(entry -> entry.getValue().time < 1);
                }
                MultiThread.sleepTick(20);
            }
        }, "InstantBuff");
    }

}
