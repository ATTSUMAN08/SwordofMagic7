package swordofmagic7.Life;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.DataBase.format;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.SomCore.random;
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

    public static int[] ReqLifeExp;
    public static int LifeReqExp(int Level) {
        if (ReqLifeExp == null) {
            ReqLifeExp = new int[MaxLifeLevel+1];
            for (int level = 0; level < ReqLifeExp.length; level++) {
                double reqExp = 100f;
                reqExp *= Math.pow(level, 1.8);
                reqExp *= Math.ceil(level/5f);
                if (level >= 15) reqExp *= 3;
                ReqLifeExp[level] = (int) Math.round(reqExp);
            }
        }
        if (Level < 0) return 100;
        if (Level > MaxLifeLevel) return Integer.MAX_VALUE;
        return ReqLifeExp[Level];
    }

    public int getMultiplyAmount(LifeType type) {
        double base = (getLevel(type)-1)*0.05;
        int amount = 1;
        while (base >= 1) {
            amount++;
            base--;
        }
        if (random.nextDouble() < base) {
            amount++;
        }
        return amount;
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
        playerData.addPlayerExp(exp);
        if (level < MaxLifeLevel) {
            Exp.put(type, getExp(type)+exp);
            int addLevel = 0;
            while (LifeReqExp(level+addLevel) <= getExp(type)) {
                Exp.put(type, getExp(type) - LifeReqExp(level+addLevel));
                addLevel++;
            }
            if (addLevel > 0) addLifeLevel(type, addLevel);
            if (playerData.ExpLog) player.sendMessage("§e" + type.Display + "経験値§7: §a" + exp + " §7(" + String.format(format, (double) exp/LifeStatus.LifeReqExp(getLevel(type))*100) + "%)");
        } else {
            setExp(type, 0);
        }
    }

    public void addLifeLevel(LifeType type, int level) {
        if (getLevel(type) < MaxLifeLevel) {
            Level.put(type, getLevel(type)+level);
            BroadCast(playerData.getNick() + "§aさんの§e[" + type.Display + "レベル]§aが§e[Lv" + getLevel(type) + "]§aになりました", true);
            playSound(player, SoundList.LevelUp);
        }
    }

    public String viewExpPercent(LifeType type) {
        return String.format("%.1f", (float) getExp(type)/LifeStatus.LifeReqExp(getLevel(type))*100) + "%";
    }
}
