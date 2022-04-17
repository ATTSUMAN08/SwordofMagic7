package swordofmagic7.Mob.Skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Dungeon.AusMine.AusMineB4;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.SomCore.random;

public class Griffia {
    private final EnemySkillManager Manager;
    final Location[] Candle = new Location[4];
    public Griffia(EnemySkillManager manager) {
        this.Manager = manager;
        Candle[0] = new Location(world, 649.5, 123, 2031.5);
        Candle[1] = new Location(world, 652.5, 123, 1923.5);
        Candle[2] = new Location(world, 760.5, 123, 1927.5);
        Candle[3] = new Location(world, 756.5, 123, 2035.5);
    }

    public void SingleFlameCircle(int CastTime) {
        MultiThread.TaskRun(() -> {
            final Set<Player> list = PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 32);
            Player target = (Player) Function.GetRandom(list);
            if (target != null) {
                Manager.CastSkillIgnoreAI(true);
                ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f);
                int i = 0;
                while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                    Location loc = target.getEyeLocation().clone().add(0, 2, 0);
                    if (i < CastTime) {
                        ParticleManager.RandomVectorParticle(particleData, loc, 30);
                    } else {
                        ParticleManager.LineParticle(particleData, loc, target.getLocation(), 0.5, 10);
                        Damage.makeDamage(Manager.enemyData.entity, target, DamageCause.MAT, "SingleFlameCircle", 1.5, 1);
                        break;
                    }
                    i += Manager.period;
                    MultiThread.sleepTick(Manager.period);
                }
                MultiThread.sleepTick(10);
                Manager.CastSkillIgnoreAI(false);
            }
        }, "SingleFlameCircle");
    }

    public void AreaFlameCircle(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f);
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 5, 30);
                } else {
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 5, 30);
                    final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "AreaFlameCircle", 1.2, 1, 2);
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "AreaFlameCircle");
    }

    public void FlamePile(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.FLAME, 0.1f);
            ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
            final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData, player.getEyeLocation().clone().add(0,3,0), 10);
                    }
                    ParticleManager.RandomVectorParticle(particleData, Manager.enemyData.entity.getLocation(), 30);
                } else {
                    ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 30);
                    final Set<LivingEntity> victims2 = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 2);
                    victims.removeAll(victims2);
                    for (LivingEntity player : victims) {
                        ParticleManager.LineParticle(particleData, player.getEyeLocation().clone().add(0,3,0), player.getLocation(), 1, 30);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "FlamePile", 1.2, 1, 2);
                    Damage.makeDamage(Manager.enemyData.entity, victims2, DamageCause.MAT, "FlamePile", 2, 1, 2);
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "FlamePile");
    }

    public void Call(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            ParticleData particleData = new ParticleData(Particle.SMOKE_NORMAL, 0.1f);
            ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
            Set<Player> victims = PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64);
            LivingEntity target = null;
            double distance = 0;
            for (Player player : victims) {
                double distance2 = Manager.enemyData.entity.getLocation().distance(player.getLocation());
                if (distance2 > distance) {
                    distance = distance2;
                    target = player;
                }
            }
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && target != null) {
                if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, target.getLocation(), 5, 50);
                } else {
                    victims = PlayerList.getNearNonDead(target.getLocation(), 5);
                    for (Player player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 30);
                        playerData(player).EffectManager.addEffect(EffectType.Stun, 200);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "Call");
    }

    public void Loyalty(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f);
            ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 7, 15);
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 25, 45);
                } else {
                    ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 10);
                    final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 25);
                    victims.removeAll(PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 7));
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 10);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "Loyalty", 2.5, 1, 0.2, true, 2);
                    break;
                }
                i += Manager.period;
                AusMineB4.SkillTime = (float) i/CastTime;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            AusMineB4.SkillTime = -1;
            Manager.CastSkill(false);
        }, "Loyalty");
    }

    public void Fluctuation(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
            final Location TargetCandle = Candle[random.nextInt(Candle.length)];
            ParticleData particleData = new ParticleData(Particle.END_ROD);
            ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
                player.sendMessage("§c強力な攻撃§aの準備をしています！");
                player.sendMessage("§c有効なロウソク§aの所へ§c避難§aしてください！");
                playSound(player, SoundList.DungeonTrigger);
            }
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), TargetCandle, 2, 0.5);
                } else {
                    ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 30);
                    final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 64);
                    for (Player player : PlayerList.getNearNonDead(TargetCandle, 14)) {
                        player.sendMessage("§c強力な攻撃§aを§e回避§aしました！");
                        playSound(player, SoundList.Tick);
                        victims.remove(player);
                    }
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 10);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "Fluctuation", 100, 1, 0.2, true, 2);
                    break;
                }
                i += Manager.period;
                AusMineB4.SkillTime = (float) i/CastTime;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            AusMineB4.SkillTime = -1;
            Manager.CastSkill(false);
        }, "Fluctuation");
    }

    public void FixedStar(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f, Function.VectorUp);
            ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
            final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 64);
            final Location SafeLocation = Manager.enemyData.entity.getLocation();
            double randomInx = random.nextDouble()*Math.PI*2;
            double radius = 25;
            SafeLocation.add(Math.cos(randomInx)*radius, 0, Math.sin(randomInx)*radius);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
                player.sendMessage("§cとても強力な攻撃§aの準備をしています！");
                player.sendMessage("§c安全エリア§aへ§c避難§aしてください！");
                playSound(player, SoundList.DungeonTrigger);
            }
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, SafeLocation, 5, 15);
                } else {
                    for (Player player : PlayerList.getNearNonDead(SafeLocation, 5)) {
                        player.sendMessage("§cとても強力な攻撃§aを§e回避§aしました！");
                        playSound(player, SoundList.Tick);
                        victims.remove(player);
                    }
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 10);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "FixedStar", 300, 1, 0.2, true, 2);
                    break;
                }
                i += Manager.period;
                AusMineB4.SkillTime = (float) i/CastTime;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            AusMineB4.SkillTime = -1;
            Manager.CastSkill(false);
        }, "FixedStar");
    }
}
