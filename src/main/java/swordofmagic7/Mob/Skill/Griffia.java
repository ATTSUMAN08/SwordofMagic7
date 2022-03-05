package swordofmagic7.Mob.Skill;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Dungeon.AusMine;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.List;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Griffia {
    private final EnemySkillManager Manager;
    public Griffia(EnemySkillManager manager) {
        this.Manager = manager;
    }

    public void SingleFlameCircle(int CastTime) {
        final Set<Player> list = PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 32);
        Player target = (Player) Function.GetRandom(list);
        if (target != null) {
            Manager.CastSkillIgnoreAI(true);
            ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f);
            new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {
                    if (Manager.enemyData.isDead || Manager.setCancel) {
                        this.cancel();
                        Manager.SkillCancel();
                    } else if (i < CastTime) {
                        Location loc = target.getEyeLocation().clone().add(0, 2, 0);
                        ParticleManager.RandomVectorParticle(particleData, loc, 30);
                    } else {
                        this.cancel();
                        Location loc = target.getEyeLocation().clone().add(0, 2, 0);
                        ParticleManager.LineParticle(particleData, loc, target.getLocation(), 0.5, 10);
                        Damage.makeDamage(Manager.enemyData.entity, target, DamageCause.MAT, "SingleFlameCircle", 1.5, 1);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> Manager.CastSkillIgnoreAI(false), 10);
                    }
                    i += Manager.period;
                }
            }.runTaskTimer(plugin, 0, Manager.period);
        }
    }

    public void AreaFlameCircle(int CastTime) {
        Manager.CastSkillIgnoreAI(true);
        ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 5, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 5, 30);
                    final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "AreaFlameCircle", 1.2, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Manager.CastSkillIgnoreAI(false), 10);
                }
                i += Manager.period;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }

    public void FlamePile(int CastTime) {
        Manager.CastSkill(true);
        ParticleData particleData = new ParticleData(Particle.FLAME, 0.1f);
        ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
        final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData, player.getEyeLocation().clone().add(0,3,0), 10);
                    }
                    ParticleManager.RandomVectorParticle(particleData, Manager.enemyData.entity.getLocation(), 30);
                } else {
                    this.cancel();
                    ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 30);
                    final Set<LivingEntity> victims2 = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 2);
                    victims.removeAll(victims2);
                    for (LivingEntity player : victims) {
                        ParticleManager.LineParticle(particleData, player.getEyeLocation().clone().add(0,3,0), player.getLocation(), 1, 30);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "FlamePile", 1.2, 1, 2);
                    Damage.makeDamage(Manager.enemyData.entity, victims2, DamageCause.MAT, "FlamePile", 2, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Manager.CastSkill(false), 10);
                }
                i += Manager.period;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }

    public void Call(int CastTime) {
        Manager.CastSkillIgnoreAI(true);
        ParticleData particleData = new ParticleData(Particle.SMOKE_NORMAL, 0.1f);
        ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
        final Set<Player> victims = PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64);
        LivingEntity target = null;
        double distance = 0;
        for (Player player : victims) {
            double distance2 = Manager.enemyData.entity.getLocation().distance(player.getLocation());
            if (distance2 > distance) {
                distance = distance2;
                target = player;
            }
        }
        LivingEntity finalTarget = target;
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, finalTarget.getLocation(), 5, 50);
                } else {
                    this.cancel();

                    final Set<Player> victims = PlayerList.getNearNonDead(finalTarget.getLocation(), 5);
                    for (Player player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 30);
                        playerData(player).EffectManager.addEffect(EffectType.Stun, 200);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Manager.CastSkillIgnoreAI(false), 10);
                }
                i += Manager.period;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }

    public void Loyalty(int CastTime) {
        Manager.CastSkill(true);
        ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f);
        ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 7, 15);
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), 25, 45);
                } else {
                    this.cancel();
                    ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 10);
                    final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 25);
                    victims.removeAll(PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 7));
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 10);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "Loyalty", 2.5, true, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        AusMine.AusMineB4SkillTime = -1;
                        Manager.CastSkill(false);
                    }, 10);
                }
                i += Manager.period;
                AusMine.AusMineB4SkillTime = (float) i/CastTime;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }

    public void Fluctuation(int CastTime) {
        Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
        final Location[] Candle = new Location[4];
        Candle[0] = new Location(Manager.enemyData.entity.getWorld(), 649.5, 119, 2031.5);
        Candle[1] = new Location(Manager.enemyData.entity.getWorld(), 652.5, 119, 1923.5);
        Candle[2] = new Location(Manager.enemyData.entity.getWorld(), 760.5, 119, 1927.5);
        Candle[3] = new Location(Manager.enemyData.entity.getWorld(), 756.5, 119, 2035.5);
        final Location TargetCandle = Candle[Manager.random.nextInt(Candle.length)];
        Manager.CastSkill(true);
        ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.5f, Function.VectorUp);
        ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
        for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
            player.sendMessage("§c強力な攻撃§aの準備をしています！");
            player.sendMessage("§c有効なロウソク§aの所へ§c避難§aしてください！");
            playSound(player, SoundList.DungeonTrigger);
        }
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, TargetCandle, 12, 15);
                } else {
                    this.cancel();
                    ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 30);
                    final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 64);
                    for (Player player : PlayerList.getNearNonDead(TargetCandle, 12)) {
                        player.sendMessage("§c強力な攻撃§aを§e回避§aしました！");
                        playSound(player, SoundList.Tick);
                        victims.remove(player);
                    }
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 10);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "Fluctuation", 100, true, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        AusMine.AusMineB4SkillTime = -1;
                        Manager.CastSkill(false);
                    }, 10);
                }
                i += Manager.period;
                AusMine.AusMineB4SkillTime = (float) i/CastTime;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }

    public void FixedStar(int CastTime) {
        Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
        Manager.CastSkill(true);
        ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f, Function.VectorUp);
        ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
        final Set<LivingEntity> victims = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 64);
        final Location SafeLocation = Manager.enemyData.entity.getLocation();
        double randomInx = Manager.random.nextDouble()*Math.PI*2;
        double radius = 25;
        SafeLocation.add(Math.cos(randomInx)*radius, 0, Math.sin(randomInx)*radius);
        for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
            player.sendMessage("§cとても強力な攻撃§aの準備をしています！");
            player.sendMessage("§c安全エリア§aへ§c避難§aしてください！");
            playSound(player, SoundList.DungeonTrigger);
        }
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, SafeLocation, 5, 15);
                } else {
                    this.cancel();
                    for (Player player : PlayerList.getNearNonDead(SafeLocation, 5)) {
                        player.sendMessage("§cとても強力な攻撃§aを§e回避§aしました！");
                        playSound(player, SoundList.Tick);
                        victims.remove(player);
                    }
                    for (LivingEntity player : victims) {
                        ParticleManager.RandomVectorParticle(particleData1, player.getLocation(), 10);
                    }
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "FixedStar", 300, true, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        AusMine.AusMineB4SkillTime = -1;
                        Manager.CastSkill(false);
                    }, 10);
                }
                i += Manager.period;
                AusMine.AusMineB4SkillTime = (float) i/CastTime;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }
}
