package swordofmagic7.Dungeon.Ashark;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.ViewBar.ViewBar;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Data.DataBase.getWarpGate;
import static swordofmagic7.Dungeon.Dungeon.*;
import static swordofmagic7.SomCore.plugin;

public class AsharkB2 {
    private static final Location EventLocation = new Location(world,6009.5, 72, 1763.5);
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 600;
    private static final double Radius = 64;
    private static final int ElevatorActiveTime = Dungeon.ElevatorActiveTime*2;
    private static final String mobName = "ナイアス";
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{
            "§c[" + mobName + "]§aが退治されました",
            "§e[B3]§aへの扉が開きました"};
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronized(() -> {
                Enemy = MobManager.mobSpawn(getMobData(mobName), 55, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNearNonDead(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNearNonDead(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§c" + mobName + "§aを討伐せよ", EnterTextData, SoundList.DungeonTrigger);
                    while (Time > 0 && Enemy.isAlive() && !list.isEmpty() && plugin.isEnabled()) {
                        list = PlayerList.getNearNonDead(EventLocation, Radius);
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
                        getWarpGate("AsharkB2_to_AsharkB3").ActiveAtTime(ElevatorActiveTime);
                        Message(Players, DungeonQuestClear, "", ClearText, SoundList.LevelUp);
                        MultiThread.sleepTick(ElevatorActiveTime);
                    } else {
                        Enemy.delete();
                        Message(Players, DungeonQuestFailed, "", null, SoundList.DungeonTrigger);
                    }
                    Players.clear();
                    Able = false;
                    Start = false;
                }, mobName);
            });
        }
        return !Able;
    }
}
