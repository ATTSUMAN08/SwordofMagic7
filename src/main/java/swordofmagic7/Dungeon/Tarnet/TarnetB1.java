package swordofmagic7.Dungeon.Tarnet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.viewBar.ViewBar;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Data.DataBase.getWarpGate;
import static swordofmagic7.Dungeon.Dungeon.*;
import static net.somrpg.swordofmagic7.SomCore.instance;

public class TarnetB1 {

    private static final Location EventLocation = new Location(world,2968, 1, 1853);
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 600;
    private static final double Radius = 48;
    private static final String sidebarId = "TarnetB1";
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{
            "§c[リーライ]§aが退治されました",
            "§e[下層]§aへの扉が開きました"};
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronized(() -> {
                Enemy = MobManager.mobSpawn(getMobData("リーライ"), 30, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cリーライ§aを討伐せよ", EnterTextData, SoundList.DUNGEON_TRIGGER);
                    while (Time > 0 && Enemy.isAlive() && !list.isEmpty() && instance.isEnabled()) {
                        list = PlayerList.getNear(EventLocation, Radius);
                        Players.addAll(list);
                        Function.setPlayDungeonQuest(Players, true);
                        Time--;
                        ViewBar.setBossBarOverrideTargetInfo(Players, Enemy.entity);
                        ViewBar.setBossBarTimer(Players, "§e残り時間 " + Time + "秒", (float) Time/StartTime);
                        MultiThread.sleepTick(20);
                    }
                    ViewBar.resetBossBarTimer(Players);
                    ViewBar.resetBossBarOverrideTargetInfo(Players);
                    Function.setPlayDungeonQuest(Players, false);
                    if (Enemy.isDead()) {
                        Able = true;
                        getWarpGate("TarnetB1BOSS_to_TarnetB2").ActiveAtTime(ElevatorActiveTime);
                        Message(Players, DungeonQuestClear, "", ClearText, SoundList.LEVEL_UP);
                        MultiThread.sleepTick(ElevatorActiveTime);
                    } else {
                        Enemy.delete();
                        Message(Players, DungeonQuestFailed, "", null, SoundList.DUNGEON_TRIGGER);
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
