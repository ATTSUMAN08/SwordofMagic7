package swordofmagic7.Dungeon.Novaha;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.ViewBar.ViewBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static swordofmagic7.Data.DataBase.getWarpGate;
import static swordofmagic7.Dungeon.Dungeon.*;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.SomCore.plugin;

public class Novaha3 {
    private static boolean Able = false;
    private static boolean Start = false;
    private static int Time;
    public static int Count;
    private static final double Radius = 128;
    private static final int ElevatorActiveTime = Dungeon.ElevatorActiveTime*4;
    private static final Location EventLocation = new Location(world,5349, 188, 3179);
    private static Set<Player> Players = new HashSet<>();
    private static final String SideBarID = "Novaha3";
    public static boolean Start() {
        if (!Start) {
            Start = true;
            MultiThread.TaskRunLater(() -> {
                Time = 600;
                Count = 200;
                Players = PlayerList.getNear(EventLocation, Radius);
                Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                Message(Players, DungeonQuestTrigger, "§cエネミ§aを§c" + Count + "体§a討伐せよ", new String[]{"§c祭壇を起動するために生贄を捧げてください"}, SoundList.DungeonTrigger);
                while (Time > 0 && list.size() > 0 && plugin.isEnabled()) {
                    list = PlayerList.getNear(EventLocation, Radius);
                    Players.addAll(list);
                    if (Count > 0) {
                        Time--;
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("残存敵数") + Math.max(Count, 0));
                        textData.add(decoLore("残り時間") + Time + "秒");
                        ViewBar.setSideBar(Players,SideBarID, textData);
                    } else {
                        Able = true;
                        break;
                    }
                    MultiThread.sleepTick(20);
                }
                ViewBar.resetSideBar(Players, SideBarID);
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
