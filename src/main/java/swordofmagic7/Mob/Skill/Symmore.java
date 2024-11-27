package swordofmagic7.Mob.Skill;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.Set;

import static swordofmagic7.Sound.CustomSound.playSound;

public class Symmore {
    private final EnemySkillManager Manager;
    public Symmore(EnemySkillManager manager) {
        this.Manager = manager;
    }

    public void PileOut(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.CircleParticle(Manager.particleCasting, Manager.enemyData.target.getLocation(), 1, 72);
                } else {
                    ParticleManager.CircleParticle(Manager.particleActivate, Manager.enemyData.target.getLocation(), 1, 72);
                    Damage.makeDamage(Manager.enemyData.entity, Manager.enemyData.target, DamageCause.ATK, "PileOut", 3, 1);
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "PileOut");
    }

    public void Howl(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            final Set<LivingEntity> list = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < list.size()*2) {
                    i++;
                    playSound(Manager.enemyData.entity.getLocation(), SoundList.Howl, 3, 1);
                    ParticleManager.RandomVectorParticle(new ParticleData(Particle.CRIT), Manager.enemyData.entity.getLocation(), 100);
                    final LivingEntity target = (LivingEntity) Function.GetRandom(list);
                    if (target != null) {
                        MultiThread.TaskRun(() -> {
                            int i2 = 0;
                            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                                if (i2 < CastTime) {
                                    ParticleManager.CircleParticle(Manager.particleCasting, target.getLocation(), 3, 36);
                                } else {
                                    ParticleManager.CircleParticle(Manager.particleActivate, target.getLocation(), 3, 36);
                                    Set<LivingEntity> victims = PlayerList.getNearLivingEntity(target.getLocation(), 3);
                                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.ATK, "Howl", 3, 1, 2);
                                    break;
                                }
                                i2 += Manager.period;
                                MultiThread.sleepTick(Manager.period);
                            }
                        }, "Howl2");
                    }
                }
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "Howl");
    }

    public void MagicExplosion(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            Manager.enemyData.HitCount = 0;
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 310);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 32)) {
                player.sendMessage("§c強力な攻撃§aの準備をしています！");
                player.sendMessage("§c攻撃§aして§c阻止§aしてください！");
                playSound(player, SoundList.DungeonTrigger);
            }
            final Set<LivingEntity> list = PlayerList.getNearLivingEntity(Manager.enemyData.entity.getLocation(), 32);
            ParticleData particleData = new ParticleData(Particle.WITCH, 0.5f);
            ParticleData particleData1 = new ParticleData(Particle.EXPLOSION_EMITTER);
            final int ReqCount = list.size()*7;
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.RandomVectorParticle(particleData, Manager.enemyData.entity.getLocation(), 100);
                } else {
                    if (Manager.enemyData.HitCount >= ReqCount) {
                        ParticleManager.RandomVectorParticle(Manager.particleCasting, Manager.enemyData.entity.getLocation(), 100);
                        for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 32)) {
                            player.sendMessage("§c強力な攻撃§aを防ぎました！");
                            playSound(player, SoundList.Tick);
                        }
                    } else {
                        for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 32)) {
                            player.sendMessage("§c強力な攻撃§aを防げませんでした...");
                            playSound(player, SoundList.Tick);
                        }
                        ParticleManager.RandomVectorParticle(particleData, Manager.enemyData.entity.getLocation(), 100);
                        ParticleManager.RandomVectorParticle(particleData1, Manager.enemyData.entity.getLocation(), 10);
                        for (LivingEntity entity : list) {
                            ParticleManager.RandomVectorParticle(particleData1, entity.getLocation(), 10);
                            Damage.makeDamage(Manager.enemyData.entity, entity, DamageCause.MAT, "MagicExplosion", 100, 1, 0.75, true);
                        }
                        playSound(Manager.enemyData.entity.getLocation(), SoundList.Explosion, 10, 1);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(100);
            Manager.CastSkill(false);
        }, "MagicExplosion");
    }
}
