package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static swordofmagic7.Function.*;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.SkillProcess.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Mage {

    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;


    public Mage(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void Teleportation(SkillData skillData) {
        skill.setCastReady(false);
        playerData.EffectManager.addEffect(EffectType.Invincible, (int) (skillData.ParameterValue(1)*20));
        playerData.EffectManager.addEffect(EffectType.Teleportation, (int) (skillData.ParameterValue(2)*20));
        final Location loc = RayTrace.rayLocation(player.getEyeLocation(),32, 0.1, false, skillProcess.PredicateE()).HitPosition;
        loc.setPitch(90);
        final ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK);
        final Location origin = RayTrace.rayLocationBlock(loc, 32, false).HitPosition;
        origin.add(origin.toVector().subtract(player.getEyeLocation().toVector()).normalize());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleData, origin, 1, 10);
                    ParticleManager.CircleParticle(particleData, origin, 1, 10);
                } else {
                    this.cancel();
                    ParticleManager.CylinderParticle(particleData, origin, 1, 2, 30, 10);
                    ParticleManager.CylinderParticle(particleData, origin, 1, 2, 30, 10);
                    origin.setDirection(player.getLocation().getDirection());
                    player.teleportAsync(origin.add(0, 0.2, 0));
                    playSound(player, SoundList.Warp);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void MagicMissile(SkillData skillData) {
        skill.setCastReady(false);
        ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.05f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.PredicateE());
                    if (ray.isHitEntity()) {
                        Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, 5);
                    }
                    new BukkitRunnable() {
                        int i = -2;
                        @Override
                        public void run() {
                            if (i <= 2) {
                                Location lineLocation = player.getEyeLocation().clone().add(VectorUp).add(getRightDirection(player.getEyeLocation().clone()).multiply(i));
                                ParticleManager.LineParticle(particleData, lineLocation, ray.HitPosition, 0, 10);
                                playSound(player, SoundList.RodAttack);
                                i++;
                            } else {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0, 2);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void Infall(SkillData skillData, double radius) {
        skill.setCastReady(false);
        final Location origin = player.getLocation().clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
                    Set<LivingEntity> victims = new HashSet<>(origin.getNearbyLivingEntities(radius, skillProcess.Predicate()));
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    new BukkitRunnable() {
                        int i = 0;
                        @Override
                        public void run() {
                            for (LivingEntity victim : victims) {
                                Location top = victim.getLocation().clone().add(0, 8, 0);
                                ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), victim.getLocation(), top, 0.1, 5);
                                ParticleManager.LineParticle(new ParticleData(Particle.FIREWORKS_SPARK, 0.5f, VectorDown), victim.getLocation(), top, 0.1, 5);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.runTaskAsynchronously(plugin);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}
