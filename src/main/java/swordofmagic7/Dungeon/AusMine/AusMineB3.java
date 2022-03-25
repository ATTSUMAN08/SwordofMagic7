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
import static swordofmagic7.System.random;

public class AusMineB3 {
    private static boolean Able = false;
    private static boolean Start = false;
    private static int Time;
    private static int Health;
    private static final Location EventLocation = new Location(world,945, 121, 1709);
    private static final Set<EnemyData> EnemyList = new HashSet<>();
    private static Set<Player> Players = new HashSet<>();
    private static final String[] MobList = {"ロウスパイダー", "スケール", "レースト", "ベース"};
    private static final int SpawnRadius = 21;
    private static final String[] EnterTextData = new String[]{
            "§e[エレベーター]§aを動かすための動力結晶が襲われています",
            "§a動力結晶付近を守ってください"};
    private static final String[] ClearText = new String[]{
            "§e[動力結晶]§aの防衛に§a成功§aしました",
            "§e[エレベーター]§aが§e[" + ElevatorActiveTime/20 + "秒間]§a稼働します",
            "§a急いで§e[エレベーター]§aを使用してください"};
    public static boolean Start() {
        if (!Start) {
            Start = true;
            MultiThread.TaskRun(() -> {
                Time = 90;
                Health = 7000;
                Players = PlayerList.getNear(EventLocation, Radius);
                Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                Message(Players, DungeonQuestTrigger, "§e動力結晶§aを防衛せよ", EnterTextData, SoundList.DungeonTrigger);
                int spawnWait = 0;
                while (Time > 0 && Health > 0 && list.size() > 0) {
                    list = PlayerList.getNear(EventLocation, Radius);
                    Players.addAll(list);
                    spawnWait++;
                    if (spawnWait > 3 || EnemyList.size() < 15) {
                        spawnWait = 0;
                        Location spawnLoc = EventLocation.clone();
                        double randomLoc = 2 * Math.PI * random.nextDouble();
                        spawnLoc.add(Math.cos(randomLoc) * SpawnRadius, 0, Math.sin(randomLoc) * SpawnRadius);
                        int MobSelect = random.nextInt(MobList.length);
                        MultiThread.TaskRunSynchronized(() -> {
                            EnemyData enemyData = MobManager.mobSpawn(getMobData(MobList[MobSelect]), 20, spawnLoc);
                            enemyData.overrideTargetLocation = EventLocation;
                            EnemyList.add(enemyData);
                        });
                    }
                    for (EnemyData enemyData : new HashSet<>(EnemyList)) {
                        if (enemyData.entity.getLocation().distance(EventLocation) < 3 && enemyData.isAlive()) {
                            Health -= enemyData.ATK/5;
                        }
                        if (enemyData.isDead()) EnemyList.remove(enemyData);
                    }
                    Time--;
                    List<String> textData = new ArrayList<>();
                    textData.add(decoText("§c§lダンジョンクエスト"));
                    textData.add(decoLore("動力結晶耐久") + Health);
                    textData.add(decoLore("残り時間") + Time + "秒");
                    ViewBar.setSideBar(Players,"AusMineB3", textData);
                    MultiThread.sleepTick(20);
                }
                for (EnemyData enemyData : EnemyList) {
                    enemyData.delete();
                }
                ViewBar.resetSideBar(Players, "AusMineB3");
                EnemyList.clear();
                if (Health > 0 && list.size() > 0) {
                    Able = true;
                    getWarpGate("AusMineB3_to_AusMineB4").ActiveAtTime(ElevatorActiveTime);
                    Message(Players, DungeonQuestClear, "", ClearText, SoundList.LevelUp);
                    MultiThread.sleepTick(ElevatorActiveTime);
                } else {
                    Message(Players, DungeonQuestFailed, "", null, SoundList.DungeonTrigger);
                }
                Players.clear();
                Able = false;
                Start = false;
            }, "AusMineB3DungeonQuest");
        }
        return !Able;
    }
}