package swordofmagic7.Dungeon.Ashark;

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

public class AsharkB4 {
    private static final Location EventLocation = new Location(world, -403.5, 48, 3915.5);
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 3600;
    private static final double Radius = 196;
    private static final String mobName = "ハインド";
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{
            "§c[" + mobName + "]§aが退治されました"};
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronizedLater(() -> {
                Enemy = MobManager.mobSpawn(getMobData(mobName), 65, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNearNonDead(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNearNonDead(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§c" + mobName + "§aを討伐せよ", EnterTextData, SoundList.DUNGEON_TRIGGER);
                    while (Time > 0 && Enemy.isAlive() && !list.isEmpty() && instance.isEnabled()) {
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
                        MessageTeleport(list, DungeonQuestClear, ClearText, SoundList.LEVEL_UP, getWarpGate("AsharkB1_to_Gitis").getLocation());
                    } else {
                        Enemy.delete();
                        MessageTeleport(list, DungeonQuestFailed, null, SoundList.DUNGEON_TRIGGER, getWarpGate("AsharkB3_to_AsharkB4").getLocation());
                    }
                    Players.clear();
                    Able = false;
                    Start = false;
                }, mobName);
            }, 5);
        }
        return false;
    }
}
