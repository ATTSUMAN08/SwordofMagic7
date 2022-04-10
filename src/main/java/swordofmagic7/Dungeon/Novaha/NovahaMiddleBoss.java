package swordofmagic7.Dungeon.Novaha;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.ViewBar.ViewBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Data.DataBase.getWarpGate;
import static swordofmagic7.Dungeon.Dungeon.*;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.SomCore.plugin;

public class NovahaMiddleBoss {

    private static final Location EventLocation = new Location(world,5396, 115, 2402);
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 300;
    private static final double Radius = 96;
    private static final String sidebarId = "NovahaMiddleBoss";
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{
            "§c[エクスタ]§aが退治されました",
            "§e[本院]§aへの扉が開きました"};
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronized(() -> {
                Enemy = MobManager.mobSpawn(getMobData("エクスタ"), 45, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cエクスタ§aを討伐せよ", EnterTextData, SoundList.DungeonTrigger);
                    while (Time > 0 && Enemy.isAlive() && list.size() > 0 && plugin.isEnabled()) {
                        list = PlayerList.getNear(EventLocation, Radius);
                        Players.addAll(list);
                        Time--;
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("ボス体力") + Enemy.viewHealthString());
                        textData.add(decoLore("残り時間") + Time + "秒");
                        ViewBar.setSideBar(Players, sidebarId, textData);
                        MultiThread.sleepTick(20);
                    }
                    ViewBar.resetSideBar(Players, sidebarId);
                    if (Enemy.isDead()) {
                        Able = true;
                        getWarpGate("Novaha2_to_Novaha3").ActiveAtTime(ElevatorActiveTime);
                        Message(Players, DungeonQuestClear, "", ClearText, SoundList.LevelUp);
                        MultiThread.sleepTick(ElevatorActiveTime);
                    } else {
                        Enemy.delete();
                        Message(Players, DungeonQuestFailed, "", null, SoundList.DungeonTrigger);
                    }
                    Players.clear();
                    Able = false;
                    Start = false;
                }, "NovahaMiddleBoss");
            });
        }
        return !Able;
    }
}
