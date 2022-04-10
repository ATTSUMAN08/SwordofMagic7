package swordofmagic7.Dungeon.AusMine;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Dungeon.Dungeon;
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

public class AusMineB4 {
    private static final Location EventLocation = new Location(world,704, 119, 1979);
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 500;
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final double Radius = Dungeon.Radius*2;
    public static float SkillTime = -1;
    private static final String[] ClearText = new String[]{
            "§cグリフィア§aを討伐しました！",
            };
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronized(() -> {
                Enemy = MobManager.mobSpawn(getMobData("グリフィア"), 25, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cグリフィア§aを討伐せよ", null, SoundList.DungeonTrigger);
                    while (Time > 0 && Enemy.isAlive() && list.size() > 0 && plugin.isEnabled()) {
                        list = PlayerList.getNear(EventLocation, Radius);
                        Players.addAll(list);
                        Time--;
                        for (int i = 0; i < 10; i++) {
                            List<String> textData = new ArrayList<>();
                            textData.add(decoText("§c§lダンジョンクエスト"));
                            textData.add(decoLore("ボス体力") + Enemy.viewHealthString());
                            if (SkillTime > -1)
                                textData.add(decoLore("スキル詠唱") + String.format("%.0f", SkillTime * 100) + "%");
                            textData.add(decoLore("残り時間") + Time + "秒");
                            ViewBar.setSideBar(Players, "AusMineB4", textData);
                            MultiThread.sleepTick(2);
                        }
                    }
                    ViewBar.resetSideBar(Players, "AusMineB4");
                    if (Enemy.isDead()) {
                        MessageTeleport(list, DungeonQuestClear, ClearText, SoundList.LevelUp, getWarpGate("AusForest_to_AusMineB1").getLocation());
                    } else {
                        Enemy.delete();
                        MessageTeleport(list, DungeonQuestFailed, null, SoundList.DungeonTrigger, getWarpGate("AusMineB4_to_AusMineB4Boss").getLocation());
                    }
                    Players.clear();
                    Start = false;
                }, "AusMineB4DungeonQuest");
            });
        }
        return false;
    }
}
