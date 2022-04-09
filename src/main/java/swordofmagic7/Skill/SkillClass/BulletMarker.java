package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;

public class BulletMarker {

    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public BulletMarker(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public double multiply() {
        if (playerData.EffectManager.hasEffect(EffectType.DoubleGunStance)) {
            return DataBase.getSkillData("DoubleGunStance").ParameterValue(1)/100+1;
        } else return 1;
    }

    public void RestInPeace(SkillData skillData, SkillProcess skillProcess) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100* multiply();
            int count = (int) skillData.ParameterValue(1);
            int length = 20;
            ParticleData particleData = new ParticleData(Particle.CRIT);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), length, 0.5, skillProcess.Predicate());
            Location loc;
            if (ray.isHitEntity()) {
                Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, value, count);
                loc = ray.HitPosition;
            } else {
                loc = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().multiply(length));
            }
            for (int i = 0; i < count; i++) {
                ParticleManager.LineParticle(particleData, playerHandLocation(player), loc, 0, 8);
                playSound(player, GunAttack);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, "RestInPeace");
    }

    public void FreezeBullet(SkillData skillData, SkillProcess skillProcess) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100* multiply();
            MultiThread.sleepTick(skillData.CastTime);
            ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f, true, 0.1f);
            ParticleData particleData1 = new ParticleData(Particle.CRIT);

            ParticleManager.LineParticle(particleData1, playerHandLocation(player), 20, 0, 10);
            ParticleManager.LineParticle(particleData, playerHandLocation(player), 20, 0, 10);
            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, value, 1);
                EffectManager.addEffect(ray.HitEntity, EffectType.Freeze, 50, player);
            }
            playSound(player, GunAttack);
            skillProcess.SkillRigid(skillData);
        }, "FreezeBullet");
    }
}
