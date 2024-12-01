package swordofmagic7.Mob.Skill;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Particle.ParticleManager.angle;
import static swordofmagic7.SomCore.random;

public class Nias extends EnemySkillBase {
    private static final double radius = 96;

    private final Location[] locations = new Location[12];
    public Nias(EnemySkillManager manager) {
        super(manager);
        locations[0] = new Location(world, 5977.5, 72, 1781.5);
        locations[1] = new Location(world, 5976.5, 72, 1773.5);
        locations[2] = new Location(world, 5975.5, 72, 1765.5);
        locations[3] = new Location(world, 5975.5, 72, 1757.5);
        locations[4] = new Location(world, 5976.5, 72, 1749.5);
        locations[5] = new Location(world, 5977.5, 72, 1741.5);
        locations[6] = new Location(world, 6038.5, 72, 1740.5);
        locations[7] = new Location(world, 6039.5, 72, 1748.5);
        locations[8] = new Location(world, 6040.5, 72, 1756.5);
        locations[9] = new Location(world, 6041.5, 72, 1764.5);
        locations[10] = new Location(world, 6040.5, 72, 1772.5);
        locations[11] = new Location(world, 6039.5, 72, 1780.5);
        MultiThread.TaskRun(() -> {
            while (Manager.enemyData.isRunnableAI()) {
                enemyList.removeIf(EnemyData::isDead);
                if (enemyList.isEmpty()) {
                    Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 25);
                }
                MultiThread.sleepTick(20);
            }
        }, "Nias");
    }

    private final Set<EnemyData> enemyList = new HashSet<>();
    public void Displeased() {
        MultiThread.TaskRun(() -> {
            if (!enemyList.isEmpty()) {
                enemyList.removeIf(EnemyData::isDead);
                for (EnemyData enemyData : enemyList) {
                    enemyData.delete();
                }
                enemyList.clear();
            }
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「ここにいる全員があなたを標的にします」", SoundList.DungeonTrigger);

            MultiThread.TaskRunSynchronized(() -> {
                for (Location loc : locations) {
                    enemyList.add(MobManager.mobSpawn(DataBase.getMobData("プライズ"), Manager.enemyData.Level, loc));
                }
            });

            Manager.CastSkillIgnoreAI(false);
        }, "Displeased");
    }

    public void Regret() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity entity = Manager.enemyData.entity;
            radiusMessage("§c「あなたは過去を振り返ったことがありますか？」", SoundList.DungeonTrigger);

            int time = 200;
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, time);
            HashMap<Player, Location> save = new HashMap<>();
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                save.put(player, player.getLocation().clone());
                EffectManager.addEffect(player, EffectType.Regret, time, null);
            }
            MultiThread.sleepTick(time);
            for (Map.Entry<Player, Location> data : save.entrySet()) {
                Player player = data.getKey();
                Location location = data.getValue();
                if (Math.abs(angle(location.getDirection()) % 360 - angle(player.getLocation().getDirection()) % 360) < 170) {
                    Damage.makeDamage(entity, player, DamageCause.ATK, "Regret", 100, 1, 0.5, true);
                    sendMessage(player, "§c「たまには過去を振り返ってみるべきです」", SoundList.Nope);
                } else sendMessage(player, "§c「過去を振り返ってみてどうでしたか？」", SoundList.Tick);
            }

            Manager.CastSkillIgnoreAI(false);
        }, "Regret");
    }

    public void SmallHope() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity entity = Manager.enemyData.entity;

            MultiThread.TaskRunSynchronized(() -> {
                Location origin = locations[random.nextInt(locations.length-1)];
                Manager.enemyData.overrideTargetLocation = origin;
                EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("ハインダ"), Manager.enemyData.Level, origin);
                MultiThread.TaskRun(() -> {
                    while (enemyData.isAlive() && Manager.enemyData.isRunnableAI()) {
                        if (enemyData.entity.getLocation().distance(entity.getLocation()) < 3) {
                            for (Player player : PlayerList.getNearNonDead(origin, radius)) {
                                PlayerData.playerData(player).dead();
                            }
                            break;
                        }
                        MultiThread.sleepTick(5);
                    }
                    Manager.enemyData.overrideTargetLocation = null;
                }, "SmallHope");
            });

            Manager.CastSkillIgnoreAI(false);
        }, "SmallHope");
    }

    public void Execution() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);

            radiusMessage("§c「嫌だ...」", SoundList.DungeonTrigger);
            int time = 200;
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, time);
            Set<Player> players = new HashSet<>();
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                EffectManager.addEffect(player, EffectType.Regret, time, null);
                players.add(player);
            }
            Vector vector = new Vector(0, -1, 0.7);
            for (int i = 0; i < time; i++) {
                for (Player player : players) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        player.setVelocity(vector);
                        if (player.isSneaking()) Damage.makeDamage(entity(), player, DamageCause.ATK, "Execution2", 1, 1, 1, true, true);
                    }
                }
                MultiThread.sleepTick(1);
            }

            Manager.CastSkill(false);
        }, "Execution");
    }
}
