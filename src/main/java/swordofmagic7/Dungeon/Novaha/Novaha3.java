package swordofmagic7.Dungeon.Novaha;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.ViewBar.ViewBar;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Data.DataBase.getWarpGate;
import static swordofmagic7.Dungeon.Dungeon.*;
import static net.somrpg.swordofmagic7.SomCore.instance;

public class Novaha3 {
    private static boolean Able = false;
    private static boolean Start = false;
    private static int Time;
    public static int Count;
    private static final int StartTime = 600;
    public static final int StartCount = 150;
    private static final double Radius = 164;
    private static final int ElevatorActiveTime = Dungeon.ElevatorActiveTime*4;
    private static final Location EventLocation = new Location(world,5349, 188, 3179);
    private static Set<Player> Players = new HashSet<>();
    private static final String SideBarID = "Novaha3";
    public static boolean Start() {
        if (!Start) {
            Start = true;
            MultiThread.TaskRunLater(() -> {
                Time = StartTime;
                Count = StartCount;
                Players = PlayerList.getNear(EventLocation, Radius);
                Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                Message(Players, DungeonQuestTrigger, "§cエネミ§aを§c" + Count + "体§a討伐せよ", new String[]{"§c祭壇を起動するために生贄を捧げてください"}, SoundList.DungeonTrigger);
                while (Time > 0 && !list.isEmpty() && instance.isEnabled()) {
                    list = PlayerList.getNear(EventLocation, Radius);
                    Players.addAll(list);
                    Function.setPlayDungeonQuest(Players, true);
                    if (Count > 0) {
                        Time--;
                        ViewBar.setBossBarOther(Players, "§c残存敵数 " + Count + "体", (float) Count/StartCount);
                        ViewBar.setBossBarTimer(Players, "§e残り時間 " + Time + "秒", (float) Time/StartTime);
                    } else {
                        Able = true;
                        break;
                    }
                    MultiThread.sleepTick(20);
                }
                ViewBar.resetBossBarTimer(Players);
                ViewBar.resetBossBarOther(Players);
                Function.setPlayDungeonQuest(Players, false);
                if (Able) {
                    getWarpGate("Novaha3_to_Novaha4").ActiveAtTime(ElevatorActiveTime);
                    Message(Players, DungeonQuestClear, "", new String[]{"§c祭壇への道が開かれました"}, SoundList.LevelUp);
                    MultiThread.sleepTick(ElevatorActiveTime);
                } else {
                    Message(Players, DungeonQuestFailed, "", null, SoundList.DungeonTrigger);
                }
                Players.clear();
                Able = false;
                Start = false;
            }, 20, SideBarID);
        }
        return !Able;
    }
}
