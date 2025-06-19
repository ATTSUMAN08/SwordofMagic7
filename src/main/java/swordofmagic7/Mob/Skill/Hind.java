package swordofmagic7.Mob.Skill;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
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
import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Dungeon.Dungeon.world;
import static net.somrpg.swordofmagic7.SomCore.random;

public class Hind extends EnemySkillBase {

    Location pivot = new Location(world, -408, 54, 3985);
    Location[] locations = new Location[4];
    public Hind(EnemySkillManager manager) {
        super(manager);
        setMessageRadius(150);
        locations[0] = new Location(world, -330, 51, 3908);
        locations[1] = new Location(world, -481, 51, 3908);
        locations[2] = new Location(world, -486, 51, 4060);
        locations[3] = new Location(world, -321, 51, 4060);

        MultiThread.TaskRun(() -> {
            int i = 0;
            while (enemyData().isRunnableAI()) {
                Burning();
                if (i > 3) {
                    Arson();
                    i = 0;
                }
                i++;
                MultiThread.sleepTick(40);
            }
        }, "Hind");
    }

    public void Burning() {
        MultiThread.TaskRun(() -> {
            double radius = 8;
            double angle = 100;
            ParticleManager.FanShapedParticle(Manager.particleCasting, location(), radius, angle, 10);
            Damage.makeDamage(entity(), ParticleManager.FanShapedCollider(location(), Function.NearEntityByEnemy(entity().getLocation(), radius), angle), DamageCause.ATK, "Burning", 1.4, 1, 1);
        }, "Burning");
    }

    public void Arson() {
        MultiThread.TaskRun(() -> {
            double radius = 10;
            double angle = 90;
            Location right = location().clone();
            Location left = location().clone();
            right.setYaw(right.getYaw()+90);
            left.setYaw(left.getYaw()-90);
            Set<LivingEntity> targets = Function.NearEntityByEnemy(location(), radius);
            Set<LivingEntity> victims = new HashSet<>();
            victims.addAll(ParticleManager.FanShapedCollider(right, targets, angle));
            victims.addAll(ParticleManager.FanShapedCollider(left, targets, angle));
            ParticleManager.FanShapedParticle(Manager.particleCasting, right, radius, angle, 10);
            ParticleManager.FanShapedParticle(Manager.particleCasting, left, radius, angle, 10);
            Damage.makeDamage(entity(), victims, DamageCause.ATK, "Arson", 3, 1, 1);
        }, "Arson");
    }

    public void RangeBurning() {
        MultiThread.TaskRun(() -> {
            double radius = 15;
            ParticleManager.CircleParticle(Manager.particleCasting, location(), radius, 24);
            Damage.makeDamage(entity(), Function.NearEntityByEnemy(location(), radius), DamageCause.ATK, "RangeBurning", 1.4, 1, 1);
        }, "RangeBurning");
    }


    public void TheStartOfHope() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「変な動きしないで」", SoundList.DUNGEON_TRIGGER);
            ParticleData particleData = new ParticleData(Particle.FIREWORK, 0.05f);
            double max = 18;
            double min = 7;

            for (int i = 0; i < 50; i += Manager.period) {
                ParticleManager.CircleParticle(Manager.particleCasting, entity().getLocation(), max, 24);
                ParticleManager.CircleParticle(Manager.particleCasting, entity().getLocation(), min, 24);
                MultiThread.sleepTick(Manager.period);
            }

            for (int i = 0; i < 200; i += Manager.period) {
                ParticleManager.CircleParticle(particleData, entity().getLocation(), max, 24);
                ParticleManager.CircleParticle(particleData, entity().getLocation(), min, 24);
                Set<Player> safeTarget = PlayerList.getNearNonDead(entity().getLocation(), max);
                safeTarget.removeAll(PlayerList.getNearNonDead(entity().getLocation(), min));
                if (target() instanceof Player player) safeTarget.add(player);
                Set<Player> victims = PlayerList.getNearNonDead(entity().getLocation(), radius);
                victims.removeAll(safeTarget);
                for (Player victim : victims) {
                    PlayerData.playerData(victim).dead();
                }
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "TheStartOfHope");
    }

    public void LeaveBehind() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「どこにいるの」", SoundList.DUNGEON_TRIGGER);

            MultiThread.TaskRunSynchronized(() -> {
                Set<EnemyData> enemyList = new HashSet<>();
                for (Location location : locations) {
                    for (int i = 0; i < 10; i++) {
                        enemyList.add(MobManager.mobSpawn(DataBase.getMobData("プライズ"), enemyData().Level, location));
                    }
                }
                EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("ナイスパ"), enemyData().Level, locations[random.nextInt(locations.length)]);
                enemyList.add(enemyData);
                MultiThread.TaskRun(() -> {
                    MultiThread.sleepTick(800);
                    if (enemyData.isAlive()) {
                        for (Player victim : PlayerList.getNearNonDead(pivot, radius)) {
                            PlayerData.playerData(victim).dead();
                        }
                    }
                    for (EnemyData enemy : enemyList) {
                        enemy.delete();
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
            radiusMessage("§c「...」", SoundList.DUNGEON_TRIGGER);

            for (LivingEntity victim : Function.NearEntityByEnemy(entity().getLocation(), radius)) {
                EffectManager.addEffect(victim, EffectType.AttackProhibited, 800, null);
                EffectManager.addEffect(victim, EffectType.Silence, 800, null);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "Understanding");
    }

    public void CouldNotHelp() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「誰か止めて」", SoundList.DUNGEON_TRIGGER);

            MultiThread.TaskRunSynchronized(() -> {
                EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("執行装置"), enemyData().Level, new Location(world, -239.5, 69, 3961));
                MultiThread.TaskRun(() -> {
                    MultiThread.sleepTick(600);
                    if (enemyData.isAlive()) {
                        for (Player victim : PlayerList.getNearNonDead(pivot, radius)) {
                            PlayerData.playerData(victim).dead();
                        }
                    }
                    enemyData.delete();
                }, "CouldNotHelp");
            });
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "CouldNotHelp");
    }

    public void JustLooking() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「なんで助けないの」", SoundList.DUNGEON_TRIGGER);

            MultiThread.TaskRunSynchronized(() -> {
                Location loc = new Location(world, -239.5, 69, 3958.5, -180, 0);
                for (Player victim : PlayerList.getNearNonDead(pivot, radius)) {
                    if (target() != victim) victim.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS);
                }
            });
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "JustLooking");
    }

    public void JustHopeButNot() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「あの人が帰ってきますように」", SoundList.DUNGEON_TRIGGER);

            Set<Player> players = PlayerList.getNearNonDead(pivot, radius);
            for (Player victim : players) {
                EffectManager.addEffect(victim, EffectType.DoNotStop, 400, null);
            }

            HashMap<Player, Location> map = new HashMap<>();
            for (int i = 0; i < 20; i++) {
                players.removeIf(player -> player.getGameMode() != GameMode.SURVIVAL);
                for (Player victim : players) {
                    if (map.containsKey(victim) && map.get(victim).distance(victim.getLocation()) < 2) {
                        Damage.makeDamage(entity(), victim, DamageCause.ATK, "JustHopeButNot", 100, 1, 1, true);
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
            radiusMessage("§c「あの人は優しかった」", SoundList.DUNGEON_TRIGGER);

            MultiThread.TaskRunSynchronized(() -> {
                EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("回収"), enemyData().Level, locations[random.nextInt(locations.length)]);
                MultiThread.TaskRun(() -> {
                    for (int i = 0; i < 30; i++) {
                        if (enemyData.isDead()) {
                            EffectManager.addEffect(target(), EffectType.CanBeSedated, 21, null);
                            LivingEntity target = target();
                            if (target != null && entity().getLocation().distance(target.getLocation()) < 4) {
                                radiusMessage("§c「なんでこんな場所にいたんでしょう」", SoundList.TICK);
                                return;
                            }
                        }
                        MultiThread.sleepTick(20);
                    }
                    for (Player victim : PlayerList.getNearNonDead(pivot, radius)) {
                        PlayerData.playerData(victim).dead();
                    }
                    enemyData.delete();
                }, "HeWasKindness");
            });
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "HeWasKindness");
    }

    EnemyData nias;
    public void SummonNias() {
        radiusMessage("§c「ナイアスが召喚されました」", SoundList.DUNGEON_TRIGGER);
        MultiThread.TaskRunSynchronized(() -> {
            if (nias != null) nias.delete();
            nias = MobManager.mobSpawn(DataBase.getMobData("ナイアス2"), enemyData().Level, entity().getLocation());
        });
    }

    public void SummonNiasCheck() {
        if (nias != null && nias.isAlive()) {
            radiusMessage("§c「まだ希望を捨てきれていない」", SoundList.DUNGEON_TRIGGER);
            for (Player victim : PlayerList.getNearNonDead(pivot, radius)) {
                PlayerData.playerData(victim).dead();
            }
            nias.delete();
        }
    }
}
