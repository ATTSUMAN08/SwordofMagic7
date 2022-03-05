package swordofmagic7.Mob.Skill;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.List;
import java.util.Set;

import static swordofmagic7.PlayerList.getNearLivingEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Symmore {
    private final EnemySkillManager Manager;
    public Symmore(EnemySkillManager manager) {
        this.Manager = manager;
    }

    public void PileOut(int CastTime) {
        Manager.CastSkill(true);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.CircleParticle(Manager.particleCasting, Manager.enemyData.target.getLocation(), 1, 72);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(Manager.particleActivate, Manager.enemyData.target.getLocation(), 1, 72);
                    Damage.makeDamage(Manager.enemyData.entity, Manager.enemyData.target, DamageCause.ATK, "PileOut", 3, 1);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Manager.CastSkill(false), 10);
                }
                i += Manager.period;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }

    public void Howl(int CastTime) {
        Manager.CastSkill(true);
        new BukkitRunnable() {
            int i = 0;
            final Set<LivingEntity> list = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
            @Override
            public void run() {
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < list.size()*2) {
                    i++;
                    playSound(Manager.enemyData.entity.getLocation(), SoundList.Howl, 3, 1);
                    ParticleManager.RandomVectorParticle(new ParticleData(Particle.CRIT), Manager.enemyData.entity.getLocation(), 100);
                    final LivingEntity target = (LivingEntity) Function.GetRandom(list);
                    if (target != null) {
                        new BukkitRunnable() {
                            int i = 0;

                            @Override
                            public void run() {
                                if (Manager.enemyData.isDead || Manager.setCancel) {
                                    this.cancel();
                                    Manager.SkillCancel();
                                } else if (i < CastTime) {
                                    ParticleManager.CircleParticle(Manager.particleCasting, target.getLocation(), 3, 36);
                                } else {
                                    this.cancel();
                                    ParticleManager.CircleParticle(Manager.particleActivate, target.getLocation(), 3, 36);
                                    Set<LivingEntity> victims = PlayerList.getNearLivingEntity(target.getLocation(), 3);
                                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.ATK, "Howl", 3, 1, 2);
                                }
                                i += Manager.period;
                            }
                        }.runTaskTimer(plugin, 0, Manager.period);
                    } else {
                        Manager.CastSkill(false);
                    }
                } else {
                    this.cancel();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Manager.CastSkill(false), 40);
                }
            }
        }.runTaskTimer(plugin, 0, 50);
    }

    public void MagicExplosion(int CastTime) {
        Manager.CastSkill(true);
        Manager.enemyData.HitCount = 0;
        Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 310);
        for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 32)) {
            player.sendMessage("§c強力な攻撃§aの準備をしています！");
            player.sendMessage("§c攻撃§aして§c阻止§aしてください！");
            playSound(player, SoundList.DungeonTrigger);
        }
        final Set<LivingEntity> list = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
        ParticleData particleData = new ParticleData(Particle.SPELL_WITCH, 0.5f);
        ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_LARGE);
        final int ReqCount = list.size()*7;
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                Manager.CastSkill(true);
                if (Manager.enemyData.isDead || Manager.setCancel) {
                    this.cancel();
                    Manager.SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.RandomVectorParticle(particleData, Manager.enemyData.entity.getLocation(), 100);
                } else {
                    this.cancel();
                    if (Manager.enemyData.HitCount >= ReqCount) {
                        this.cancel();
                        ParticleManager.RandomVectorParticle(Manager.particleCasting, Manager.enemyData.entity.getLocation(), 100);
                        for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 32)) {
                            player.sendMessage("§c強力な攻撃§aを防ぎました！");
                            playSound(player, SoundList.Tick);
                        }
                    } else {
                        for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 32)) {
                            player.sendMessage("§c強力な攻撃§aを防げませんでした...");
                            playSound(player, SoundList.Tick);
                        }
                        ParticleManager.RandomVectorParticle(particleData, Manager.enemyData.entity.getLocation(), 100);
                        ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 10);
                        for (LivingEntity entity : list) {
                            ParticleManager.RandomVectorParticle(particleData1, entity.getLocation(), 10);
                        }
                        Damage.makeDamage(Manager.enemyData.entity, list, DamageCause.MAT, "MagicExplosion", 10, 1, 1);
                        playSound(Manager.enemyData.entity.getLocation(), SoundList.Explosion, 10, 1);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Manager.CastSkill(false), 100);
                }
                i += Manager.period;
            }
        }.runTaskTimer(plugin, 0, Manager.period);
    }
}
