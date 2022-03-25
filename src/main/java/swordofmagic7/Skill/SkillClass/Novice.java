package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.Particle.ParticleManager.ShapedParticle;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;

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
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            while (skill.SkillCastProgress < 1) {
                ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
            Set<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 1);
            ShapedParticle(new ParticleData(Particle.SWEEP_ATTACK), player.getLocation(), radius, angle, angle/2, 1, true);
            skillProcess.SkillRigid(skillData);
        }, "Slash: " + player.getName());
    }

    public void Vertical(SkillData skillData, double length, double width) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            while (skill.SkillCastProgress < 1) {
                ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
            Set<LivingEntity> victims = RectangleCollider(player.getLocation(), length, width, skillProcess.Predicate(), false);
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 1);
            skillProcess.SkillRigid(skillData);
        }, "Vertical: " + player.getName());
    }

    public void Rain(SkillData skillData, double radius) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            final Location loc = RayTrace.rayLocationBlock(player.getEyeLocation(), 32, false).HitPosition;
            loc.setPitch(90);
            final Location origin = RayTrace.rayLocationBlock(loc, 32, false).HitPosition;

            while (skill.SkillCastProgress < 1) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
            Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
            final Location top = origin.clone().add(0, 8, 0);
            skillProcess.SkillRigid(skillData);
            for (LivingEntity victim : victims) {
                ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), victim.getLocation(), top, 0.1, 10);
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1);
                MultiThread.sleepTick(2);
            }
        }, "Rain: " + player.getName());
    }

    public void TriggerShot(SkillData skillData, int count) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(new ParticleData(Particle.CRIT), playerHandLocation(player), 20, 0, 10);
            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.ParameterValue(0)/100, count);
            playSound(player, GunAttack, count, 2);
            skillProcess.SkillRigid(skillData);
        }, "TriggerShot: " + player.getName());
    }

    public void Smite(SkillData skillData, double radius) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            Location origin = null;

            while (skill.SkillCastProgress < 1) {
                origin = player.getLocation().clone().add(player.getLocation().getDirection().setY(0).normalize().multiply(radius));
                ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
            Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
            Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 1);
            skillProcess.SkillRigid(skillData);
        }, "Smite: " + player.getName());
    }

    public void FireBall(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            ParticleManager.LineParticle(particleData, playerHandLocation(player), ray.HitPosition, 0, 10);
            ParticleManager.RandomVectorParticle(particleData, ray.HitPosition, 100);
            Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(ray.HitPosition, skillData.Parameter.get(1).Value, skillProcess.Predicate()));
            Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 1);
            playSound(player, SoundList.Fire);
            skillProcess.SkillRigid(skillData);
        }, "FireBall: " + player.getName());
    }
}
