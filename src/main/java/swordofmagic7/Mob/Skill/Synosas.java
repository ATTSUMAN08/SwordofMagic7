package swordofmagic7.Mob.Skill;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Dungeon.Tarnet.TarnetB3;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.List;
import java.util.Set;

import static swordofmagic7.Skill.SkillProcess.RectangleCollider;
import static swordofmagic7.Skill.SkillProcess.particleActivate;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Synosas {

    private final EnemySkillManager Manager;
    public Synosas(EnemySkillManager manager) {
        this.Manager = manager;
    }

    private void radiusMessage(List<String> message) {
        for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 48)) {
            Function.sendMessage(player, message);
            playSound(player, SoundList.DungeonTrigger);
        }
    }

    public void RangeAttack(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            double radius = 10;
            int i = 0;
            ParticleData particleData = new ParticleData(Particle.CRIT_MAGIC);
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.CircleParticle(Manager.particleCasting, Manager.enemyData.entity.getLocation(), radius, 72);
                } else {
                    ParticleManager.CircleParticle(Manager.particleCasting, Manager.enemyData.entity.getLocation(), radius, 72);
                    for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 5)) {
                        ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 0.75, 10);
                        Damage.makeDamage(Manager.enemyData.entity, player, DamageCause.MAT, "RangeAttack", 3, 1);
                        MultiThread.sleepTick(1);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            Manager.CastSkill(false);
        }, "RangeAttack");
    }

    public void VerticalAttack(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            int length = 15;
            int width = 8;
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.RectangleParticle(Manager.particleCasting, Manager.enemyData.entity.getLocation(), length, width, 3);
                } else {
                    ParticleManager.RectangleParticle(particleActivate, Manager.enemyData.entity.getLocation(), length, width, 3);
                    Set<LivingEntity> victims = RectangleCollider(Manager.enemyData.entity.getLocation(), length, width, (LivingEntity entity) -> entity instanceof Player, false);
                    Damage.makeDamage(Manager.enemyData.entity, victims, DamageCause.MAT, "VerticalAttack", 3, 1, 1);
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            Manager.CastSkill(false);
        }, "VerticalAttack");
    }

    public void Fear(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            int i = 0;
            ParticleData particleData = new ParticleData(Particle.DRIP_WATER);
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i > CastTime) {
                    for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
                        ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 1, 3);
                        PlayerData.playerData(player).EffectManager.addEffect(EffectType.Silence, 200);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(80);
            Manager.CastSkill(false);
        }, "Fear");
    }

    public void Forced(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            int i = 0;
            ParticleData particleData = new ParticleData(Particle.EXPLOSION_NORMAL);
            ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_LARGE);
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i > CastTime) {
                    for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
                        ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 1, 3);
                        ParticleManager.spawnParticle(particleData2, player.getEyeLocation());
                        playSound(player, SoundList.Explosion);
                        PlayerData playerData = PlayerData.playerData(player);
                        playerData.changeHealth(-playerData.Status.MaxHealth/2);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            Manager.CastSkill(false);
        }, "Forced");
    }

    public void Despair(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            int radius = 15;
            int i = 0;
            ParticleData particleData = new ParticleData(Particle.SPELL_WITCH);
            ParticleData particleData2 = new ParticleData(Particle.REDSTONE);
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i < CastTime) {
                    ParticleManager.CircleParticle(particleData, Manager.enemyData.entity.getLocation(), radius, 72);
                } else {
                    ParticleManager.CircleParticle(particleData2, Manager.enemyData.entity.getLocation(), radius, 72);
                    for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), radius)) {
                        ParticleManager.LineParticle(particleData2, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 0.75, 10);
                        PlayerData playerData = PlayerData.playerData(player);
                        playerData.EffectManager.addEffect(EffectType.Stun, 200);
                        playerData.EffectManager.addEffect(EffectType.Blind, 200);
                        MultiThread.sleepTick(1);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            Manager.CastSkill(false);
        }, "Disappointment");
    }

    public void Effect(int CastTime) {
        Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 100);
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            TarnetB3.radiusMessage("§a危険です！§e[過充填区域]§aから離れてください！");
            int i = 0;
            ParticleData particleData = new ParticleData(Particle.EXPLOSION_HUGE);
            TarnetB3.useParticle = TarnetB3.particleData2;
            TarnetB3.useRadius = 20;
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i > CastTime) {
                    for (Player player : TarnetB3.Players2) {
                        ParticleManager.spawnParticle(particleData, player.getEyeLocation());
                        PlayerData playerData = PlayerData.playerData(player);
                        playerData.dead();
                        MultiThread.sleepTick(1);
                    }
                    playSound(TarnetB3.OverLocation[TarnetB3.selectOver], SoundList.Explosion);
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            TarnetB3.useParticle = TarnetB3.particleData;
            TarnetB3.useRadius = 15;
            Manager.CastSkill(false);
        }, "Disappointment");
    }

    public void Quiet(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 120);
            Manager.CastSkill(true);
            int i = 0;
            ParticleData particleData = new ParticleData(Particle.DRIP_LAVA);
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i > CastTime) {
                    for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
                        ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 1, 3);
                        PlayerData.playerData(player).EffectManager.addEffect(EffectType.Stun, 80);
                        PlayerData.playerData(player).EffectManager.addEffect(EffectType.Silence, 80);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            MultiThread.sleepTick(80);
            Manager.CastSkill(false);
        }, "Quiet");
    }

    public void Distrust(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            int i = 0;
            ParticleData particleData = new ParticleData(Particle.DRIP_LAVA);
            while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                if (i > CastTime) {
                    for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), 64)) {
                        ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 1, 3);
                        PlayerData.playerData(player).EffectManager.addEffect(EffectType.Glory, 300*20);
                    }
                    break;
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }
            Manager.CastSkill(false);
        }, "Distrust");
    }
}
