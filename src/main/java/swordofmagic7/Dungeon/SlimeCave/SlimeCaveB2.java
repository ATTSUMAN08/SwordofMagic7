package swordofmagic7.Dungeon.SlimeCave;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

public class SlimeCaveB2 {
    private static final Location EventLocation = new Location(world,871.5, -43, -1541.5);
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 600;
    private static final double Radius = 48;
    private static EnemyData enemy;
    private static EnemyData enemy2;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{
            "§c[ロイヤルナイトスライム&ファラス]§aが退治されました"
    };
    public static boolean Start() {
        if (!Start && (enemy == null || enemy.isDead()) && (enemy2 == null || enemy2.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronized(() -> {
                enemy = MobManager.mobSpawn(getMobData("ロイヤルナイトスライム"), 35, EventLocation);
                enemy2 = MobManager.mobSpawn(getMobData("ファラス"), 35, EventLocation);
                ((Horse) enemy2.entity).getInventory().setArmor(ItemStack.of(Material.GOLDEN_HORSE_ARMOR));
                enemy2.entity.addPassenger(enemy.entity);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cロイヤルナイトスライム&ファラス§aを討伐せよ", EnterTextData, SoundList.DUNGEON_TRIGGER);
                    while (Time > 0 && (enemy.isAlive() || enemy2.isAlive()) && !list.isEmpty() && instance.isEnabled()) {
                        list = PlayerList.getNear(EventLocation, Radius);
                        Players.addAll(list);
                        Function.setPlayDungeonQuest(Players, true);
                        Time--;
                        ViewBar.setBossBarOverrideTargetInfo(Players, enemy.entity);
                        ViewBar.setBossBarTimer(Players, "§e残り時間 " + Time + "秒", (float) Time/StartTime);
                        MultiThread.sleepTick(20);
                    }
                    ViewBar.resetBossBarTimer(Players);
                    ViewBar.resetBossBarOverrideTargetInfo(Players);
                    Function.setPlayDungeonQuest(Players, false);
                    if (enemy.isDead() && enemy2.isDead()) {
                        Able = true;
                        getWarpGate("SlimeCaveB2_to_SlimeCaveGate").ActiveAtTime(ElevatorActiveTime);
                        Message(Players, DungeonQuestClear, "", ClearText, SoundList.LEVEL_UP);
                        MultiThread.sleepTick(ElevatorActiveTime);
                    } else {
                        enemy.delete();
                        enemy2.delete();
                        Message(Players, DungeonQuestFailed, "", null, SoundList.DUNGEON_TRIGGER);
                    }
                    Players.clear();
                    Able = false;
                    Start = false;
                }, "SlimeCaveB2DungeonQuest");
            });
        }
        return !Able;
    }
}
