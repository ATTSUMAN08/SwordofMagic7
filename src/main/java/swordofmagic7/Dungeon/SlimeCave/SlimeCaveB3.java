package swordofmagic7.Dungeon.SlimeCave;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.viewBar.ViewBar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.somrpg.swordofmagic7.SomCore.instance;
import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Data.DataBase.getWarpGate;
import static swordofmagic7.Dungeon.Dungeon.*;

public class SlimeCaveB3 {
    private static final Location EventLocation = new Location(world,705.5, -27, -1460.5);
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 600;
    private static final double Radius = 48;
    private static EnemyData Enemy;
    private static Set<Player> Players = new HashSet<>();
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{
            "§c[クイーンスライム]§aが退治されました"
    };
    private static final Map<Player, Integer> stickyTimer = new HashMap<>();
    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            MultiThread.TaskRunSynchronized(() -> {
                Enemy = MobManager.mobSpawn(getMobData("クイーンスライム"), 40, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cクイーンスライム§aを討伐せよ", EnterTextData, SoundList.DUNGEON_TRIGGER);
                    while (Time > 0 && Enemy.isAlive() && !list.isEmpty() && instance.isEnabled()) {
                        list = PlayerList.getNear(EventLocation, Radius);
                        Players.addAll(list);
                        Function.setPlayDungeonQuest(Players, true);
                        Time--;
                        ViewBar.setBossBarOverrideTargetInfo(Players, Enemy.entity);
                        ViewBar.setBossBarTimer(Players, "§e残り時間 " + Time + "秒", (float) Time/StartTime);
                        for (Player player : Players) {
                            PlayerData playerData = PlayerData.playerData(player);
                            if (player.getLocation().getBlock().getType() == Material.WATER && !playerData.EffectManager.hasEffect(EffectType.Sticky)) {
                                stickyTimer.put(player, stickyTimer.getOrDefault(player, 0) + 1);
                            } else {
                                stickyTimer.remove(player);
                            }

                            if (stickyTimer.getOrDefault(player, 0) == 3) {
                                player.sendMessage("§c注意！溶解状態になりました");
                            }

                            if (stickyTimer.getOrDefault(player, 0) >= 3) {
                                playerData.EffectManager.addEffect(EffectType.Dissolution, 1200);
                            }
                        }
                        MultiThread.sleepTick(20);
                    }
                    ViewBar.resetBossBarTimer(Players);
                    ViewBar.resetBossBarOverrideTargetInfo(Players);
                    Function.setPlayDungeonQuest(Players, false);
                    if (Enemy.isDead()) {
                        Able = true;
                        MessageTeleport(list, DungeonQuestClear, ClearText, SoundList.LEVEL_UP, getWarpGate("AusForest_to_SlimeCaveB1").getLocation());
                    } else {
                        Enemy.delete();
                        Message(Players, DungeonQuestFailed, "", null, SoundList.DUNGEON_TRIGGER);
                    }
                    Players.clear();
                    Able = false;
                    Start = false;
                }, "SlimeCaveB3DungeonQuest");
            });
        }
        return !Able;
    }
}
