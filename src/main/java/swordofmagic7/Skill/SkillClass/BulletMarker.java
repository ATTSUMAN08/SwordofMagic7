package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GUN_ATTACK;

public class BulletMarker extends BaseSkillClass {

    public BulletMarker(SkillProcess skillProcess) {
        super(skillProcess);
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
                playSound(player, GUN_ATTACK);
                MultiThread.sleepTick(1);
            }
            skillProcess.SkillRigid(skillData);
        }, "RestInPeace");
    }

    public void FreezeBullet(SkillData skillData, SkillProcess skillProcess) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100* multiply();
            int time = skillData.ParameterValueInt(1)*20;
            MultiThread.sleepTick(skillData.CastTime);
            ParticleData particleData = new ParticleData(Particle.FIREWORK, 0.1f, true, 0.1f);
            ParticleData particleData1 = new ParticleData(Particle.CRIT);

            ParticleManager.LineParticle(particleData1, playerHandLocation(player), 20, 0, 10);
            ParticleManager.LineParticle(particleData, playerHandLocation(player), 20, 0, 10);
            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                int count = 1;
                if (playerData.Equipment.isEquipRune("凍傷のルーン")) count++;
                else EffectManager.addEffect(ray.HitEntity, EffectType.Freeze, time, player);
                Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, value, count);
            }
            playSound(player, GUN_ATTACK);
            skillProcess.SkillRigid(skillData);
        }, "FreezeBullet");
    }
}
