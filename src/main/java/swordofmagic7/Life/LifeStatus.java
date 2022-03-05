package swordofmagic7.Life;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Sound.CustomSound.playSound;

public class LifeStatus {
    public final static int MaxLifeLevel = 30;

    private final Player player;
    private final PlayerData playerData;

    public LifeStatus(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    HashMap<LifeType, Integer> Level = new HashMap<>();
    HashMap<LifeType, Integer> Exp = new HashMap<>();

    public static int LifeReqExp(int Level) {
        double reqExp = 100;
        reqExp *= Math.pow(Level, 1.2);
        return (int) Math.round(reqExp);
    }

    public int getLevel(LifeType type) {
        return Level.getOrDefault(type, 1);
    }

    public int getExp(LifeType type) {
        return Exp.getOrDefault(type, 0);
    }

    public void setLevel(LifeType type, int level) {
        Level.put(type, level);
    }

    public void setExp(LifeType type, int exp) {
        Exp.put(type, exp);
    }

    public void addLifeExp(LifeType type, int exp) {
        int level = getLevel(type);
        if (level < MaxLifeLevel) {
            Exp.put(type, getExp(type)+exp);
            int addLevel = 0;
            while (LifeReqExp(level+addLevel) <= getExp(type)) {
                Exp.put(type, getExp(type) - LifeReqExp(level+addLevel));
                addLevel++;
            }
            if (addLevel > 0) addLifeLevel(type, addLevel);
            if (playerData.ExpLog) player.sendMessage("§e" + type.Display + "経験値§7: §a" + exp);
        } else {
            setExp(type, 0);
        }
    }

    public void addLifeLevel(LifeType type, int level) {
        if (getLevel(type) < MaxLifeLevel) {
            Level.put(type, getLevel(type)+level);
            BroadCast(playerData.getNick() + "§aさんの§e[" + type.Display + "レベル]§aが§e[Lv" + getLevel(type) + "]§aになりました");
            playSound(player, SoundList.LevelUp);
        }
    }

    public String viewExpPercent(LifeType type) {
        return String.format("%.1f", (float) getExp(type)/LifeStatus.LifeReqExp(getLevel(type))*100) + "%";
    }
}
