package swordofmagic7.Dungeon.Tarnet;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
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
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.SomCore.plugin;

public class TarnetB3 {
    private static final Location EventLocation = new Location(world,3372.5, 80, 1497);
    public static final Location[] OverLocation = new Location[4];
    public static int selectOver = 0;
    private static int selectOverTimer = 0;
    private static boolean Able = false;
    private static boolean Start = false;
    public static int Time;
    public static int StartTime = 500;
    private static EnemyData Enemy;
    public static Set<Player> Players = new HashSet<>();
    public static Set<Player> Players2 = new HashSet<>();
    private static final double Radius = Dungeon.Radius*2;
    private static final String sidebarId = "TarnetB3";
    public static float SkillTime = -1;
    public static final ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f, VectorUp);
    public static final ParticleData particleData2 = new ParticleData(Particle.LAVA, 0.1f, VectorUp);
    public static ParticleData useParticle = particleData;
    public static double useRadius = 15;
    public static boolean Danger = false;
    private static final String[] EnterTextData = new String[]{};
    private static final String[] ClearText = new String[]{
            "§cシノサス§aを討伐しました！",
    };

    public static void radiusMessage(String message) {
        for (Player player : Players) {
            Function.sendMessage(player, message);
            playSound(player, SoundList.DungeonTrigger);
        }
    }

    public static boolean Start() {
        if (!Start && (Enemy == null || Enemy.isDead())) {
            Start = true;
            OverLocation[0] = new Location(world,3407, 81, 1457);
            OverLocation[1] = new Location(world,3337, 81, 1457);
            OverLocation[2] = new Location(world,3337, 81, 1537);
            OverLocation[3] = new Location(world,3407, 81, 1537);
            MultiThread.TaskRunSynchronized(() -> {
                Enemy = MobManager.mobSpawn(getMobData("シノサス"), 40, EventLocation);
                MultiThread.TaskRun(() -> {
                    Time = StartTime;
                    Players = PlayerList.getNear(EventLocation, Radius);
                    Set<Player> list = PlayerList.getNear(EventLocation, Radius);
                    Message(Players, DungeonQuestTrigger, "§cシノサス§aを討伐せよ", EnterTextData, SoundList.DungeonTrigger);
                    while (Time > 0 && Enemy.isAlive() && list.size() > 0 && plugin.isEnabled()) {
                        list = PlayerList.getNear(EventLocation, Radius);
                        Players.addAll(list);
                        Time--;
                        List<String> textData = new ArrayList<>();
                        textData.add(decoText("§c§lダンジョンクエスト"));
                        textData.add(decoLore("ボス体力") + String.format("%.0f", Enemy.Health) + " (" + String.format("%.0f", Enemy.Health / Enemy.MaxHealth *100) + "%)");
                        textData.add(decoLore("残り時間") + Time + "秒");
                        ViewBar.setSideBar(Players, sidebarId, textData);
                        Set<Player> deBuff = new HashSet<>(list);
                        Players2 = new HashSet<>(list);
                        deBuff.removeIf(player -> player.getLocation().distance(OverLocation[selectOver]) < useRadius);
                        Players2.removeAll(deBuff);
                        for (Player player : deBuff) {
                            PlayerData.playerData(player).EffectManager.addEffect(EffectType.InsufficientFilling, 25);
                        }
                        for (int i = 0; i < 4; i++) {
                            ParticleManager.CircleParticle(useParticle, OverLocation[selectOver], useRadius, 72);
                            MultiThread.sleepTick(5);
                        }
                        selectOverTimer++;
                        if (selectOverTimer >= 60) {
                            selectOver = selectOver >= 3 ? 0 : selectOver+1;
                            selectOverTimer = 0;
                            radiusMessage("§e[過充填区域]§aが切り替わりました");
                        }
                    }
                    ViewBar.resetSideBar(Players, sidebarId);
                    if (Enemy.isDead()) {
                        MessageTeleport(Players, DungeonQuestClear, ClearText, SoundList.LevelUp, getWarpGate("TarnetB1_to_Nefritas").Location);
                    } else {
                        Enemy.delete();
                        Message(Players, DungeonQuestFailed, "", null, SoundList.DungeonTrigger);
                    }

                    Players.clear();
                    Able = false;
                    Start = false;
                }, sidebarId);
            });
        }
        return false;
    }
}
