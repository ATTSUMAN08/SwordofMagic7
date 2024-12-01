package swordofmagic7.Dungeon.Novaha;

import org.bukkit.Location;
import org.bukkit.entity.Player;
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

public class Novaha4 {
    private static final Location EventLocation = new Location(world, 5371.5, 174, 3902.5);
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 1200;
    private static final double Radius = 96;
    private static final String EventID = "Novaha4";
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{"§c[ヴァノセト]§aが退治されました"};
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronizedLater(() -> {
                Enemy = MobManager.mobSpawn(getMobData("ヴァノセト"), 55, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNearNonDead(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cヴァノセト§aを討伐せよ", null, SoundList.DungeonTrigger);
                    while (Time > 0 && Enemy.isAlive() && !list.isEmpty() && plugin.isEnabled()) {
                        list = PlayerList.getNearNonDead(EventLocation, Radius);
                        Players.addAll(list);
                        Function.setPlayDungeonQuest(Players, true);
                        Time--;
                        ViewBar.setBossBarOverrideTargetInfo(Players, Enemy.entity);
                        if (Enemy.skillManager.vanoset.Altar != null) ViewBar.setBossBarOtherTargetInfo(Players, Enemy.skillManager.vanoset.Altar.entity);
                        ViewBar.setBossBarTimer(Players, "§e残り時間 " + Time + "秒", (float) Time / StartTime);
                        MultiThread.sleepTick(20);
                    }
                    ViewBar.resetBossBarTimer(Players);
                    ViewBar.resetBossBarOtherTargetInfo(Players);
                    ViewBar.resetBossBarOverrideTargetInfo(Players);
                    Function.setPlayDungeonQuest(Players, false);
                    if (Enemy.isDead()) {
                        MessageTeleport(list, DungeonQuestClear, ClearText, SoundList.LevelUp, getWarpGate("Novaha1_to_Vieta").getLocation());
                    } else {
                        Enemy.delete();
                        MessageTeleport(list, DungeonQuestFailed, null, SoundList.DungeonTrigger, getWarpGate("Novaha3_to_Novaha4").getLocation());
                    }
                    Enemy.skillManager.vanoset.Altar.delete();
                    Players.clear();
                    Start = false;
                }, EventID);
            }, 20);
        }
        return false;
    }
}
