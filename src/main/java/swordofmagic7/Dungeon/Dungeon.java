package swordofmagic7.Dungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.Random;

import static swordofmagic7.Sound.CustomSound.playSound;

public class Dungeon {
    public static final double Radius = 64;
    public static final Random random = new Random();
    public static final String DungeonQuestTrigger = "§c§l《ダンジョンクエスト発生》";
    public static final String DungeonQuestClear = "§b§l《ダンジョンクエスト達成》";
    public static final String DungeonQuestFailed = "§e§l《ダンジョンクエスト失敗》";
    public static final int ElevatorActiveTime = 600;
    public static final World world = Bukkit.getWorld("world");

    public static void triggerTitle(Player player, String title, String subtitle, SoundList sound) {
        player.sendTitle(title, subtitle, 30, 50, 30);
        playSound(player, sound);
    }

    public static void elevatorActive(Location location, String text) {
        for (Player player : PlayerList.getNear(location, Radius)) {
            player.sendMessage("§c[" + text + "]§aが退治されました");
            player.sendMessage("§e[エレベーター]§aが§e[" + ElevatorActiveTime/20 + "秒間]§a稼働します");
            player.sendMessage("§a急いで§e[エレベーター]§aを使用してください");
            triggerTitle(player, DungeonQuestClear, "§e[エレベーター]§aに向かってください", SoundList.LevelUp);
        }
    }
}
