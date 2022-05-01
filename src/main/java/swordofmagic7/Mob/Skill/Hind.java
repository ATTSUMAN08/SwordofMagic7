package swordofmagic7.Mob.Skill;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.Set;

import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.SomCore.random;

public class Hind extends EnemySkillBase {

    Location[] LeaveBehindLocation = new Location[4];
    Location[] HeWasKindnessLocation = new Location[4];
    public Hind(EnemySkillManager manager) {
        super(manager);
        setMessageRadius(96);
        LeaveBehindLocation[0] = new Location(world, 0, 0, 0);
        LeaveBehindLocation[1] = new Location(world, 0, 0, 0);
        LeaveBehindLocation[2] = new Location(world, 0, 0, 0);
        LeaveBehindLocation[3] = new Location(world, 0, 0, 0);

        HeWasKindnessLocation[0] = new Location(world, 0, 0, 0);
        HeWasKindnessLocation[1] = new Location(world, 0, 0, 0);
        HeWasKindnessLocation[2] = new Location(world, 0, 0, 0);
        HeWasKindnessLocation[3] = new Location(world, 0, 0, 0);
    }

    public void TheStartOfHope() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「変な動き...しないで...」", SoundList.DungeonTrigger);
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.05f);
            Location origin = entity().getLocation();

            for (int i = 0; i < 200; i += Manager.period) {
                ParticleManager.CircleParticle(particleData, entity().getLocation(), 1, 24);
                MultiThread.sleepTick(Manager.period);
            }

            Set<Player> safeTarget = PlayerList.getNearNonDead(origin, 14);
            safeTarget.removeAll(PlayerList.getNearNonDead(origin, 7));
            if (target() instanceof Player player) safeTarget.add(player);
            Set<Player> victims = PlayerList.getNearNonDead(origin, radius);
            victims.removeAll(safeTarget);
            for (Player victim : victims) {
                PlayerData.playerData(victim).dead();
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "TheStartOfHope");
    }

    public void LeaveBehind() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「...」", SoundList.DungeonTrigger);

            MultiThread.TaskRunSynchronized(() -> {
                for (Location location : LeaveBehindLocation) {
                    for (int i = 0; i < 10; i++) {
                        MobManager.mobSpawn(DataBase.getMobData("プライズ"), enemyData().Level, location);
                    }
                }
                EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("プライズ"), enemyData().Level, LeaveBehindLocation[random.nextInt(LeaveBehindLocation.length-1)]);
                MultiThread.TaskRun(() -> {
                    MultiThread.sleepTick(400);
                    if (enemyData.isAlive()) {
                        for (Player victim : PlayerList.getNearNonDead(entity().getLocation(), radius)) {
                            PlayerData.playerData(victim).dead();
                        }
                    }
                }, "LeaveBehind");
            });

            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "LeaveBehind");
    }

    public void Understanding() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「...」", SoundList.DungeonTrigger);

            for (LivingEntity victim : Function.NearEntityByEnemy(entity().getLocation(), radius)) {
                EffectManager.addEffect(victim, EffectType.AttackProhibited, 300, null);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "Understanding");
    }

    public void CouldNotHelp() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「誰か...止めて...」", SoundList.DungeonTrigger);

            MultiThread.TaskRunSynchronized(() -> {
                EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("執行装置"), enemyData().Level, new Location(world, 0, 0, 0));
                MultiThread.TaskRun(() -> {
                    MultiThread.sleepTick(400);
                    if (enemyData.isAlive()) {
                        for (Player victim : PlayerList.getNearNonDead(entity().getLocation(), radius)) {
                            PlayerData.playerData(victim).dead();
                        }
                    }
                }, "CouldNotHelp");
            });
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "CouldNotHelp");
    }

    public void JustLooking() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「なんで...助けないの...」", SoundList.DungeonTrigger);

            MultiThread.TaskRunSynchronized(() -> {
                Location loc = new Location(world, 0, 0, 0);
                for (Player victim : PlayerList.getNearNonDead(entity().getLocation(), radius)) {
                    victim.teleportAsync(loc);
                }
            });
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "JustLooking");
    }

    public void JustHopeButNot() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「あの人が...帰ってきますように...」", SoundList.DungeonTrigger);

            Set<Player> players = PlayerList.getNearNonDead(entity().getLocation(), radius);
            for (Player victim : players) {
                EffectManager.addEffect(victim, EffectType.DoNotStop, 400, null);
            }

            HashMap<Player, Location> map = new HashMap<>();
            for (int i = 0; i < 20; i++) {
                players.removeIf(player -> player.getGameMode() != GameMode.SURVIVAL);
                for (Player victim : players) {
                    if (map.containsKey(victim) && map.get(victim).distance(victim.getLocation()) < 2) {
                        Damage.makeDamage(entity(), victim, DamageCause.ATK, "JustHopeButNot", 10, 1, 0.5);
                    }
                    map.put(victim, victim.getLocation().clone());
                }
                MultiThread.sleepTick(20);
            }
            Manager.CastSkillIgnoreAI(false);
        }, "JustHopeButNot");
    }

    public void HeWasKindness() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「あの人は...優しかった...」", SoundList.DungeonTrigger);

            MultiThread.TaskRunSynchronized(() -> {
                EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("回収"), enemyData().Level, HeWasKindnessLocation[random.nextInt(HeWasKindnessLocation.length-1)]);
                MultiThread.TaskRun(() -> {
                    for (int i = 0; i < 30; i++) {
                        if (enemyData.isDead()) {
                            EffectManager.addEffect(target(), EffectType.CanBeSedated, 21, null);
                            if (entity().getLocation().distance(target().getLocation()) < 2) {
                                radiusMessage("§c「なんでこんな場所にいたんでしょう」", SoundList.Tick);
                                return;
                            }
                        }
                        MultiThread.sleepTick(20);
                    }
                    for (Player victim : PlayerList.getNearNonDead(entity().getLocation(), radius)) {
                        PlayerData.playerData(victim).dead();
                    }
                }, "HeWasKindness");
            });
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "HeWasKindness");
    }
}
