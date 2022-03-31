package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
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

import static swordofmagic7.Function.*;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.particleActivate;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;

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
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            playerData.EffectManager.addEffect(EffectType.Invincible, skillData.ParameterValueInt(1) * 20);
            playerData.EffectManager.addEffect(EffectType.Teleportation, skillData.ParameterValueInt(2) * 20);
            Location origin;
            Ray ray = RayTrace.rayLocationBlock(player.getEyeLocation(), 32, false);
            if (ray.isHitBlock()) {
                Location loc = ray.HitPosition;
                loc.setPitch(90);
                origin = RayTrace.rayLocationBlock(loc, 32, false).HitPosition;;
            } else {
                origin = ray.HitPosition;
            }
            origin.add(player.getEyeLocation().getDirection().multiply(-1));
            origin.add(0, 0.2, 0);
            final ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK);


            while (skill.SkillCastProgress < 1) {
                ParticleManager.CircleParticle(particleData, player.getLocation(), 1, 10);
                ParticleManager.CircleParticle(particleData, origin, 1, 10);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.CircleParticle(particleData, player.getLocation(), 1, 10);
            ParticleManager.CircleParticle(particleData, origin, 1, 10);
            origin.setDirection(player.getLocation().getDirection());
            MultiThread.TaskRunSynchronized(() -> player.teleportAsync(origin.add(0, 0.2, 0)));
            playSound(player, SoundList.Warp);
            skillProcess.SkillRigid(skillData);
        }, "Teleportation: " + player.getName());
    }

    public void MagicMissile(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.05f);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value / 100, 5);
            }

            skillProcess.SkillRigid(skillData);

            for (int i = -2; i < 2; i++) {
                Location lineLocation = player.getEyeLocation().clone().add(VectorUp).add(getRightDirection(player.getEyeLocation().clone()).multiply(i));
                ParticleManager.LineParticle(particleData, lineLocation, ray.HitPosition, 0, 10);
                playSound(player, SoundList.RodAttack);
                MultiThread.sleepTick(2);
            }
        }, "MagicMissile: " + player.getName());
    }

    public void Infall(SkillData skillData, double radius) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            final Location origin = player.getLocation().clone();
            ParticleData particleData = new ParticleData(Particle.CRIT_MAGIC);
            ParticleData particleData1 = new ParticleData(Particle.FIREWORKS_SPARK, 0.5f, VectorDown);

            while (skill.SkillCastProgress < 1) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
            Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
            skillProcess.SkillRigid(skillData);
            for (LivingEntity victim : victims) {
                Location top = victim.getLocation().clone().add(0, 8, 0);
                ParticleManager.LineParticle(particleData, victim.getLocation(), top, 0.1, 5);
                ParticleManager.LineParticle(particleData1, victim.getLocation(), top, 0.1, 5);
                Damage.makeDamage(player, victim, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1);
                MultiThread.sleepTick(2);
            }
        }, "Infall: " + player.getName());
    }
}
