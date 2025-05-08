package swordofmagic7.Dungeon.AusMine;

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
import static net.somrpg.swordofmagic7.SomCore.random;

public class AusMineB1 {
    private static boolean Able = false;
    private static boolean Start = false;
    private static int Time;
    private static final int StartTime = 300;
    private static int Count;
    private static final int StartCount = 30;
    private static final Location EventLocation = new Location(world, 2451, 46, -71);
    private static final Set<EnemyData> EnemyList = new HashSet<>();
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{
            "§e[エレベーター]§aを動かそうとしたら§c[ゴブリン]§aが襲ってきました",
            "§aこのままでは§e[エレベーター]§aを動かせません",
            "§c[ゴブリン]§aを退治してください"};
    private static final String[] ClearText = new String[]{
            "§c[コブリン]§aが退治されました",
            "§e[エレベーター]§aが§e[" + ElevatorActiveTime/20 + "秒間]§a稼働します",
            "§a急いで§e[エレベーター]§aを使用してください"};
    public static boolean Start() {
        if (!Start) {
            Start = true;
            MultiThread.TaskRun(() -> {
                Time = StartTime;
                Count = StartCount;
                Players = PlayerList.getNear(EventLocation, Radius);
                Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                Message(Players, DungeonQuestTrigger, "§cゴブリン§aを§c" + Count + "体§a討伐せよ", EnterTextData, SoundList.DUNGEON_TRIGGER);
                while (Time > 0 && !list.isEmpty() && instance.isEnabled()) {
                    list = PlayerList.getNear(EventLocation, Radius);
                    Players.addAll(list);
                    Function.setPlayDungeonQuest(Players, true);
                    for (EnemyData enemyData : new HashSet<>(EnemyList)) {
                        if (enemyData.isDead()) {
                            EnemyList.remove(enemyData);
                            Count--;
                        }
                    }
                    if (Count > 0) {
                        Time--;
                        if (EnemyList.size() < 5) {
                            Location loc = EventLocation.clone().add(random.nextDouble() * 20, 0, random.nextDouble() * 20);
                            MultiThread.TaskRunSynchronized(() -> EnemyList.add(MobManager.mobSpawn(getMobData("ゴブリン"), 15, loc)));
                        }
                        ViewBar.setBossBarOther(Players, "§c残存敵数 " + Count + "体", (float) Count/StartCount);
                        ViewBar.setBossBarTimer(Players, "§e残り時間 " + Time + "秒", (float) Time/StartTime);
                    } else {
                        Able = true;
                        break;
                    }
                    MultiThread.sleepTick(20);
                }
                for (EnemyData enemyData : EnemyList) {
                    enemyData.delete();
                }
                ViewBar.resetBossBarTimer(Players);
                ViewBar.resetBossBarOther(Players);
                Function.setPlayDungeonQuest(Players, false);
                for (EnemyData enemyData : EnemyList) {
                    enemyData.delete();
                }
                EnemyList.clear();
                if (Able) {
                    getWarpGate("AusMineB1_to_AusMineB2").ActiveAtTime(ElevatorActiveTime);
                    Message(Players, DungeonQuestClear, "", ClearText, SoundList.LEVEL_UP);
                    MultiThread.sleepTick(ElevatorActiveTime);
                } else {
                    Message(Players, DungeonQuestFailed, "", null, SoundList.DUNGEON_TRIGGER);
                }
                Players.clear();
                Able = false;
                Start = false;
            }, "AusMineB1DungeonQuest");
        }
        return !Able;
    }
}
