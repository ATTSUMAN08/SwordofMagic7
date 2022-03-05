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
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.Particle.ParticleManager.ShapedParticle;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.SkillProcess.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;
import static swordofmagic7.System.plugin;

public class Novice {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;


    public Novice(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void Slash(SkillData skillData, double radius, double angle) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime < skillData.CastTime) {
                    ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                } else {
                    this.cancel();
                    ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
                    Set<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    ShapedParticle(new ParticleData(Particle.SWEEP_ATTACK), player.getLocation(), radius, angle, angle/2, 1, true);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void Vertical(SkillData skillData, double length, double width) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime < skillData.CastTime) {
                    ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                } else {
                    this.cancel();
                    ParticleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
                    Set<LivingEntity> victims = RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void Rain(SkillData skillData, double radius) {
        skill.setCastReady(false);
        final Location loc = RayTrace.rayLocation(player.getEyeLocation(),32, 0.1, false, skillProcess.PredicateE()).HitPosition;
        loc.setPitch(90);
        final Location origin = RayTrace.rayLocationBlock(loc, 32, false).HitPosition;
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
                        final Location top = origin.clone().add(0, 8, 0);
                        @Override
                        public void run() {
                            for (LivingEntity victim : victims) {
                                ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), victim.getLocation(), top, 0.1, 10);
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

    public void TriggerShot(SkillData skillData, int count) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), 20, 0, 10);
                    Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.PredicateE());
                    if (ray.isHitEntity()) Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, count);
                    playSound(player, GunAttack, count, 2);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void Smite(SkillData skillData, double radius) {
        skill.setCastReady(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                final Location origin = player.getLocation().clone().add(player.getLocation().getDirection().setY(0).normalize().multiply(radius));
                if (skillProcess.SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
                    Set<LivingEntity> victims = new HashSet<>(origin.getNearbyLivingEntities(radius, skillProcess.Predicate()));
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public void FireBall(SkillData skillData) {
        skill.setCastReady(false);
        ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skillProcess.SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.PredicateE());
                    ParticleManager.LineParticle(particleData, playerHandLocation(player), ray.HitPosition, 0, 10);
                    ParticleManager.RandomVectorParticle(particleData, ray.HitPosition, 100);
                    Set<LivingEntity> victims = new HashSet<>(ray.HitPosition.getNearbyLivingEntities(skillData.Parameter.get(1).Value, skillProcess.Predicate()));
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    playSound(player, GunAttack);
                    skillProcess.SkillRigid(skillData);
                }
                skillProcess.SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}
