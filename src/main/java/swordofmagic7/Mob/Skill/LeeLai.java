package swordofmagic7.Mob.Skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Sound.CustomSound.playSound;

public class LeeLai {
    private final EnemySkillManager Manager;
    private final List<String> Glory = new ArrayList<>();
    private final List<String> Reflection = new ArrayList<>();
    private final List<String> Seiko = new ArrayList<>();
    public LeeLai(EnemySkillManager manager) {
        this.Manager = manager;

        Glory.add("§c栄光デバフを周囲に付与しようとしています！");
        Reflection.add("§c15秒間ダメージの10%が反射されます！");
        Seiko.add("§cリーライの被ダメージが1/2倍になります！");
    }

    private void radiusMessage(List<String> message) {
        for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 48)) {
            Function.sendMessage(player, message);
            playSound(player, SoundList.DungeonTrigger);
        }
    }

    public void Glitter(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            double distance = 0;
            Location location = Manager.enemyData.entity.getLocation();
            Set<Player> playerSet = PlayerList.getNear(location, 48);
            Player target = null;
            for (Player player : playerSet) {
                double distance2 = player.getLocation().distance(location);
                if (distance2 > distance) {
                    target = player;
                    distance = distance2;
                }
            }
            if (target != null) {
                try {
                    int i = 0;
                    while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                        if (i < CastTime) {
                            ParticleManager.CircleParticle(Manager.particleCasting, target.getLocation(), 1, 72);
                        } else {
                            ParticleManager.CircleParticle(Manager.particleActivate, target.getLocation(), 1, 72);
                            Damage.makeDamage(Manager.enemyData.entity, target, DamageCause.ATK, "Glitter", 3, 1);
                            break;
                        }
                        i += Manager.period;
                        MultiThread.sleepTick(Manager.period);
                    }
                    MultiThread.sleepTick(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Manager.CastSkill(false);
        }, "Glitter");
    }

    public void Flash(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            try {
                Location location = Manager.enemyData.entity.getLocation();
                location.add(location.getDirection().clone().setY(0).normalize().multiply(5));
                ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0);
                int i = 0;
                Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
                while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                    if (i < CastTime) {
                        ParticleManager.CircleParticle(Manager.particleCasting, location, 5, 72);
                    } else {
                        for (Player player : PlayerList.getNear(location, 5)) {
                            ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 0, 10);
                            playerData(player).EffectManager.addEffect(EffectType.Stun, 60);
                        }
                        break;
                    }
                    i += Manager.period;
                    MultiThread.sleepTick(Manager.period);
                }
                MultiThread.sleepTick(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Manager.CastSkill(false);
        }, "Flash");
    }

    public void Glory(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            try {
                radiusMessage(Glory);
                Location location = Manager.enemyData.entity.getLocation();
                ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.3f, Function.VectorUp);
                int i = 0;
                Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
                while (Manager.enemyData.isAlive() && !Manager.setCancel) {
                    if (i < CastTime) {
                        ParticleManager.CircleParticle(Manager.particleCasting, location, 12, 72);
                    } else {
                        for (Player player : PlayerList.getNear(location, 12)) {
                            PlayerData playerData = playerData(player);
                            ParticleManager.LineParticle(particleData, Manager.enemyData.entity.getEyeLocation(), player.getEyeLocation(), 0, 10);
                            playerData.EffectManager.addEffect(EffectType.Glory, 600);
                            playerData.changeHealth(playerData.Status.Health/2);
                        }
                        break;
                    }
                    i += Manager.period;
                    MultiThread.sleepTick(Manager.period);
                }
                MultiThread.sleepTick(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Manager.CastSkill(false);
        }, "Glory");
    }

    public void Reflection() {
        MultiThread.TaskRun(() -> {
            radiusMessage(Reflection);
            Manager.enemyData.effectManager.addEffect(EffectType.Reflection, 300);
            ParticleData particleData = new ParticleData(Particle.CRIT_MAGIC, 0, true, 2.5f);
            while (Manager.enemyData.isAlive() && !Manager.setCancel && Manager.enemyData.effectManager.hasEffect(EffectType.Reflection)) {
                ParticleManager.spawnParticle(particleData, Manager.enemyData.entity.getLocation());
                MultiThread.sleepTick(Manager.period);
            }
        }, "Reflection");
    }

    public void Seiko() {
        radiusMessage(Seiko);
        Manager.enemyData.effectManager.addEffect(EffectType.Seiko, Integer.MAX_VALUE);
    }
}
