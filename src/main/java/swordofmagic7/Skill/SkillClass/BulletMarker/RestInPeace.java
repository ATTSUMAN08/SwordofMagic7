package swordofmagic7.Skill.SkillClass.BulletMarker;

import org.bukkit.Location;
import org.bukkit.Particle;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.Skill.SkillClass.SkillBase;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;

import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.GunAttack;

public class RestInPeace extends SkillBase {

    public ParticleData particleData = new ParticleData(Particle.CRIT);

    public RestInPeace(SkillData skillData, SkillProcess skillProcess) {
        super(skillData, skillProcess);

        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100*DoubleGunStance.multiply(playerData);
            int count = (int) skillData.ParameterValue(1);
            int length = 20;

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
}
