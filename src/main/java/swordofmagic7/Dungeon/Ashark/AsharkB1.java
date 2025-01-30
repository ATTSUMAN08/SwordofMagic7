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
import static swordofmagic7.Function.sendMessage;
import static net.somrpg.swordofmagic7.SomCore.instance;
import static net.somrpg.swordofmagic7.SomCore.random;

public class AsharkB1 {
    private static final int x = 4;
    private static final boolean[] Able = new boolean[x];
    private static final boolean[] Start = new boolean[x];
    private static final int[] Time = new int[x];
    private static final int StartTime = 120;
    private static final int[] Count = new int[x];
    private static final int StartCount = 30;
    private static final int ElevatorActiveTime = 12000;
    private static final int[] Level = new int[]{54,54,55,55};
    private static final String[] MobList = new String[]{"ノム","プローズ","ジェイル"};
    private static final Set<EnemyData> EnemyList = new HashSet<>();
    private static Set<Player> Players = new HashSet<>();
    private static final double Radius = 32;

    public static boolean Check(Player player) {
        int i = 1;
        boolean _return = true;
        for (boolean bool : Able) {
            if (!bool) {
                sendMessage(player, "§e[" + i + "番]§aが起動していません");
                _return = false;
            }
            i++;
        }
        return _return;
    }

    public static boolean Start(int i) {
        if (!Start[i]) {
            Start[i] = true;
            MultiThread.TaskRun(() -> {
                Location location = getWarpGate("AsharkB1_Trigger" + i).getLocation();
                Time[i] = StartTime;
                Count[i] = StartCount;
                Players = PlayerList.getNearNonDead(location, Radius);
                Set<Player> list = PlayerList.getNearNonDead(location, Radius);
                Message(Players, DungeonQuestTrigger, "§cエネミー§aを§c" + Count[i] + "体§a討伐せよ", null, SoundList.DUNGEON_TRIGGER);
                while (Time[i] > 0 && !list.isEmpty() && instance.isEnabled()) {
                    list = PlayerList.getNearNonDead(location, Radius);
                    Players.addAll(list);
                    Function.setPlayDungeonQuest(Players, true);
                    for (EnemyData enemyData : new HashSet<>(EnemyList)) {
                        if (enemyData.isDead()) {
                            EnemyList.remove(enemyData);
                            Count[i]--;
                        }
                    }
                    if (Count[i] > 0) {
                        Time[i]--;
                        if (EnemyList.size() < 10) {
                            Location loc = location.clone().add(random.nextDouble() * 20, 0, random.nextDouble() * 20);
                            MultiThread.TaskRunSynchronized(() -> EnemyList.add(MobManager.mobSpawn(getMobData(MobList[random.nextInt(MobList.length-1)]), Level[i], loc)));
                        }
                        ViewBar.setBossBarOther(Players, "§c残存敵数 " + Count[i] + "体", (float) Count[i]/StartCount);
                        ViewBar.setBossBarTimer(Players, "§e残り時間 " + Time[i] + "秒", (float) Time[i]/StartTime);
                    } else {
                        Able[i] = true;
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
                if (Able[i]) {
                    getWarpGate("AsharkB1_Trigger" + i).ActiveAtTime(ElevatorActiveTime);
                    Message(Players, DungeonQuestClear, "", null, SoundList.LEVEL_UP);
                    MultiThread.sleepTick(ElevatorActiveTime);
                } else {
                    Message(Players, DungeonQuestFailed, "", null, SoundList.DUNGEON_TRIGGER);
                }
                Players.clear();
                Able[i] = false;
                Start[i] = false;
            }, "AsharkB1_Trigger" + i);
        }
        return !Able[i];
    }
}
