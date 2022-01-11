package swordofmagic7.Life;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;

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

    public int MineLevel = 1;
    public int MineExp = 0;
    public int LumberLevel = 1;
    public int LumberExp = 0;
    public int HarvestLevel = 1;
    public int HarvestExp = 0;
    public int AnglerLevel = 1;
    public int AnglerExp = 0;
    public int CookLevel = 1;
    public int CookExp = 0;
    public int SmithLevel = 1;
    public int SmithExp = 0;

    public static int ReqExp(int Level) {
        double reqExp = 100;
        reqExp *= Math.pow(Level, 1.2);
        return (int) Math.round(reqExp);
    }

    public void addMineExp(int exp) {
        if (MineLevel < MaxLifeLevel) {
            int ReqExp = ReqExp(MineLevel);
            MineExp += exp;
            if (ReqExp <= MineExp) {
                MineExp -= ReqExp;
                MineLevel++;
                BroadCast(playerData.getNick() + "§aさんの§e[採掘レベル]§aが§e[Lv" + MineLevel + "]§aになりました");
                playSound(player, SoundList.LevelUp);
            }
            if (playerData.ExpLog) player.sendMessage("§e採掘経験値§7: §a" + exp);
        }
    }

    public void addLumberExp(int exp) {
        if (LumberLevel < MaxLifeLevel) {
            int ReqExp = ReqExp(LumberLevel);
            LumberExp += exp;
            if (ReqExp <= LumberExp) {
                LumberExp -= ReqExp;
                LumberLevel++;
                BroadCast(playerData.getNick() + "§aさんの§e[伐採レベル]§aが§e[Lv" + LumberLevel + "]§aになりました");
                playSound(player, SoundList.LevelUp);
            }
            if (playerData.ExpLog) player.sendMessage("§e伐採経験値§7: §a" + exp);
        }
    }

    public void addHarvestExp(int exp) {
        if (HarvestLevel < MaxLifeLevel) {
            int ReqExp = ReqExp(HarvestLevel);
            HarvestExp += exp;
            if (ReqExp <= HarvestExp) {
                HarvestExp -= ReqExp;
                HarvestLevel++;
                BroadCast(playerData.getNick() + "§aさんの§e[採集レベル]§aが§e[Lv" + HarvestLevel + "]§aになりました");
                playSound(player, SoundList.LevelUp);
            }
            if (playerData.ExpLog) player.sendMessage("§e採集経験値§7: §a" + exp);
        }
    }

    public void addAnglerExp(int exp) {
        if (AnglerLevel < MaxLifeLevel) {
            int ReqExp = ReqExp(AnglerLevel);
            AnglerExp += exp;
            if (ReqExp <= AnglerExp) {
                AnglerExp -= ReqExp;
                AnglerLevel++;
                BroadCast(playerData.getNick() + "§aさんの§e[釣獲レベル]§aが§e[Lv" + AnglerLevel + "]§aになりました");
                playSound(player, SoundList.LevelUp);
            }
        }
    }

    public void addCookExp(int exp) {
        if (CookLevel < MaxLifeLevel) {
            int ReqExp = ReqExp(CookLevel);
            CookExp += exp;
            if (ReqExp <= CookExp) {
                CookExp -= ReqExp;
                CookLevel++;
                BroadCast(playerData.getNick() + "§aさんの§e[料理レベル]§aが§e[Lv" + CookLevel + "]§aになりました");
                playSound(player, SoundList.LevelUp);
            }
        }
    }

    public void addSmithExp(int exp) {
        if (SmithLevel < MaxLifeLevel) {
            int ReqExp = ReqExp(SmithLevel);
            SmithExp += exp;
            if (ReqExp <= SmithExp) {
                SmithExp -= ReqExp;
                SmithLevel++;
                BroadCast(playerData.getNick() + "§aさんの§e[鍛冶レベル]§aが§e[Lv" + SmithLevel + "]§aになりました");
                playSound(player, SoundList.LevelUp);
            }
        }
    }
}
