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
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.System.plugin;
import static swordofmagic7.System.random;

public class AusMineB1 {
    private static boolean Able = false;
    private static boolean Start = false;
    private static int Time;
    private static int Count;
    private static final Location EventLocation = new Location(world,1145, 141, 1293);
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
                Time = 180;
                Count = 15;
                Players = PlayerList.getNear(EventLocation, Radius);
                Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                Message(Players, DungeonQuestTrigger, "§cゴブリン§aを§c" + Count + "体§a討伐せよ", EnterTextData, SoundList.DungeonTrigger);
                while (Time > 0 && list.size() > 0 && plugin.isEnabled()) {
                    list = PlayerList.getNear(EventLocation, Radius);
                    Players.addAll(list);
                    for (EnemyData enemyData : new HashSet<>(EnemyList)) {
                        if (enemyData.isDead()) {
                            EnemyList.remove(enemyData);
                            Count--;
                        }
                    }
                    if (Count > 0) {
                        Time--;
                        if (EnemyList.size() < 3) {
                            Location loc = EventLocation.clone().add(random.nextDouble() * 20, 0, random.nextDouble() * 20);
                            MultiThread.TaskRunSynchronized(() -> EnemyList.add(MobManager.mobSpawn(getMobData("ゴブリン"), 15, loc)));
                        }
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("残存敵数") + Count);
                        textData.add(decoLore("残り時間") + Time + "秒");
                        ViewBar.setSideBar(Players,"AusMineB1", textData);
                    } else {
                        Able = true;
                        break;
                    }
                    MultiThread.sleepTick(20);
                }
                for (EnemyData enemyData : EnemyList) {
                    enemyData.delete();
                }
                ViewBar.resetSideBar(Players, "AusMineB1");
                for (EnemyData enemyData : EnemyList) {
                    enemyData.delete();
                }
                EnemyList.clear();
                if (Able) {
                    getWarpGate("AusMineB1_to_AusMineB2").ActiveAtTime(ElevatorActiveTime);
                    Message(Players, DungeonQuestClear, "", ClearText, SoundList.LevelUp);
                    MultiThread.sleepTick(ElevatorActiveTime);
                } else {
                    Message(Players, DungeonQuestFailed, "", null, SoundList.DungeonTrigger);
                }
                Players.clear();
                Able = false;
                Start = false;
            }, "AusMineB1DungeonQuest");
        }
        return !Able;
    }
}
