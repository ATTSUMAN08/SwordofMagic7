package swordofmagic7.Dungeon.AusMine;

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
import static swordofmagic7.Function.*;
import static swordofmagic7.SomCore.plugin;

public class AusMineB2 {

    private static final Location EventLocation = new Location(world,907, 81, 1457);
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 300;
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{
            "§e[エレベーター]§aを動かすための動力結晶が動いていません",
            "§a動力結晶付近にいる§c[サイモア]§aが原因だと思われます",
            "§c[サイモア]§aを退治してください"};
    private static final String[] ClearText = new String[]{
            "§c[サイモア]§aが退治されました",
            "§e[エレベーター]§aが§e[" + ElevatorActiveTime/20 + "秒間]§a稼働します",
            "§a急いで§e[エレベーター]§aを使用してください"};
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronized(() -> {
                Enemy = MobManager.mobSpawn(getMobData("サイモア"), 15, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cサイモア§aを討伐せよ", EnterTextData, SoundList.DungeonTrigger);
                    while (Time > 0 && Enemy.isAlive() && list.size() > 0 && plugin.isEnabled()) {
                        list = PlayerList.getNear(EventLocation, Radius);
                        Players.addAll(list);
                        Time--;
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("ボス体力") + String.format("%.0f", Enemy.Health));
                        textData.add(decoLore("残り時間") + Time + "秒");
                        ViewBar.setSideBar(Players, "AusMineB2", textData);
                        MultiThread.sleepTick(20);
                    }
                    ViewBar.resetSideBar(Players, "AusMineB2");
                    if (Enemy.isDead()) {
                        Able = true;
                        getWarpGate("AusMineB2_to_AusMineB3").ActiveAtTime(ElevatorActiveTime);
                        Message(Players, DungeonQuestClear, "", ClearText, SoundList.LevelUp);
                        MultiThread.sleepTick(ElevatorActiveTime);
                    } else {
                        Enemy.delete();
                        Message(Players, DungeonQuestFailed, "", null, SoundList.DungeonTrigger);
                    }
                    Players.clear();
                    Able = false;
                    Start = false;
                }, "AusMineB2DungeonQuest");
            });
        }
        return !Able;
    }
}
