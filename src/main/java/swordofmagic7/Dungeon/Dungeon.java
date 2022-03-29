package swordofmagic7.Dungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import swordofmagic7.Dungeon.Tarnet.TarnetEnter;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.createTouchHologram;

public class Dungeon {
    public static final double Radius = 64;
    public static final String DungeonQuestTrigger = "§c§l《ダンジョンクエスト発生》";
    public static final String DungeonQuestClear = "§b§l《ダンジョンクエスト達成》";
    public static final String DungeonQuestFailed = "§e§l《ダンジョンクエスト失敗》";
    public static final int ElevatorActiveTime = 600;
    public static final World world = Bukkit.getWorld("world");

    public static void Message(Set<Player> players, String title, String subtitle, String[] textData, SoundList sound) {
        for (Player player : players) {
            if (player.isOnline()) {
                player.sendTitle(title, subtitle, 30, 50, 30);
                if (textData != null) for (String text : textData) {
                    player.sendMessage(text);
                }
                playSound(player, sound);
            }
        }
    }

    public static void Trigger(String trigger) {

    }

    public static void Initialize() {
        TarnetEnter.reset();
        createTouchHologram("§e§lアルターターネット", new Location(world, 3109, 132, 749.5), (Player player) -> {
            TarnetEnter.start();
        });
    }

    public static void BossBarAdd(BossBar bossBar, Set<Player> Players) {
        for (Player player : Players) {
            if (player.isOnline() && !bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    public static void BossBarRemove(BossBar bossBar) {
        bossBar.setVisible(false);
        bossBar.removeAll();
    }
}
