package swordofmagic7.Mob.Skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Exta extends EnemySkillBase {

    private final Location[] location = new Location[5];
    public Exta(EnemySkillManager manager) {
        super(manager);
        location[0] = new Location(world,5396, 115, 2402);
        location[1] = new Location(world,5425, 115, 2402);
        location[2] = new Location(world,5368, 115, 2402);
        location[3] = new Location(world,5396, 115, 2429);
        location[4] = new Location(world,5396, 115, 2375);
    }

    public void Launch(int CastTime) {
        MultiThread.TaskRun(() -> {
            if (target() != null) {
                Manager.CastSkillIgnoreAI(true);
                ParticleData particleData = new ParticleData(Particle.REDSTONE, 0.05f);
                ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_LARGE);

                for (int i = 0; i < CastTime; i += Manager.period) {
                    ParticleManager.CircleParticle(particleData, target().getLocation(), 1, 24);
                    MultiThread.sleepTick(Manager.period);
                }

                Damage.makeDamage(entity(), target(), DamageCause.ATK, "Launch", 2, 1);
                Function.setVelocity(target(), new Vector(0, 2, 0));
                particleData2.spawn(target().getLocation());
                playSound(target().getLocation(), SoundList.Explosion);

                MultiThread.sleepTick(10);
                Manager.CastSkillIgnoreAI(false);
            }
        }, "SingleFlameCircle");
    }

    public void Impact(int CastTime) {
        MultiThread.TaskRun(() -> {
            if (target() != null) {
                radiusMessage("§c衝撃波が来ます！避けてください！", SoundList.DungeonTrigger);
                Manager.CastSkill(true);
                effectManager().addEffect(EffectType.Invincible, CastTime+60);
                ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f, Function.VectorUp);
                ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_LARGE);
                for (int i = 0; i < CastTime; i += Manager.period) {
                    ParticleManager.CircleParticle(particleData, entity().getLocation(), 1, 24);
                    MultiThread.sleepTick(Manager.period);
                }

                for (int i = 0; i < 5; i++) {
                    double radius = i*5;
                    double radius2 = (i+1)*5;
                    Location origin = entity().getLocation();
                    ParticleManager.CircleParticle(particleData2, origin, radius, 12);
                    ParticleManager.CircleParticle(particleData2, origin, radius2, 12);
                    Set<LivingEntity> victims = Function.NearEntityByEnemy(origin, radius2);
                    victims.removeAll(Function.NearEntityByEnemy(origin, radius));
                    for (LivingEntity victim : victims) {
                        Vector vector = victim.getLocation().toVector().subtract(origin.toVector()).setY(1);
                        Function.setVelocity(victim, vector);
                        Damage.makeDamage(entity(), victim, DamageCause.ATK, "Impact", 4, 1);
                        EffectManager.addEffect(victim, EffectType.Concussion, 60, null);
                        if (victim instanceof Player player) playSound(player, SoundList.Explosion);
                    }
                    MultiThread.sleepTick(10);
                }

                MultiThread.sleepTick(10);
                Manager.CastSkill(false);
            }
        }, "SingleFlameCircle");
    }

    public void Starting() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            radiusMessage("§c祭壇が起動しようとしています！阻止してください！", SoundList.DungeonTrigger);
            effectManager().addEffect(EffectType.Invincible, 300);
            ParticleData particleData = new ParticleData(Particle.EXPLOSION_LARGE);
            ParticleData particleData2 = new ParticleData(Particle.SPELL_WITCH).setRandomOffset().setRandomOffset(2);
            Set<EnemyData> enemyList = new HashSet<>();
            MobData mobData = DataBase.getMobData("起動結晶");
            MultiThread.TaskRunSynchronized(() -> {
                for (Location location : location) {
                    enemyList.add(MobManager.mobSpawn(mobData, Manager.enemyData.Level, location));
                }
            });

            for (int i = 0; i < 300/5; i++) {
                ParticleManager.RandomVectorParticle(particleData2, entity().getLocation(), 30);
                MultiThread.sleepTick(5);
            }
            enemyList.removeIf(EnemyData::isDead);
            if (enemyList.size() > 0) {
                for (LivingEntity victim : Function.NearEntityByEnemy(location[0], 64)) {
                    Damage.makeDamage(entity(), victim, DamageCause.MAT, "Starting", 1000, 1);
                    particleData.spawn(victim.getEyeLocation());
                }
                for (EnemyData enemyData : enemyList){
                    enemyData.delete();
                }
                radiusMessage("§c祭壇が起動してしまいました...", SoundList.Explosion);
            } else {
                radiusMessage("§c祭壇の起動を防ぎました！", SoundList.Tick);
            }

            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "Starting");
    }

    public void Thought(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            radiusMessage("§c遠くにいる人を見つめています！", SoundList.DungeonTrigger);
            ParticleData particleData = new ParticleData(Particle.SMOKE_NORMAL, 0.05f);
            ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_LARGE);
            Location origin = entity().getLocation();
            LivingEntity target = Function.FarthestLivingEntity(origin, Function.NearEntityByEnemy(origin, 64));
            if (target != null) {
                for (int i = 0; i < CastTime; i += Manager.period) {
                    ParticleManager.CircleParticle(particleData, target.getLocation(), 1, 24);
                    MultiThread.sleepTick(Manager.period);
                }

                EffectManager.addEffect(target, EffectType.Silence, 200, null);
                particleData2.spawn(target.getEyeLocation());
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "Thought");
    }

    public void Acceleration() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c移動速度が早くなっています！気を付けてください！", SoundList.DungeonTrigger);
            enemyData().MovementMultiply = 1.5;
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "Acceleration");
    }

}
