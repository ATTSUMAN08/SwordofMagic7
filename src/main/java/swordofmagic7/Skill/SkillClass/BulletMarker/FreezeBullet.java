package swordofmagic7.Skill.SkillClass.BulletMarker;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
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

public class FreezeBullet {

    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f, true, 0.1f);
    public ParticleData particleData1 = new ParticleData(Particle.CRIT);

    public FreezeBullet(SkillData skillData, SkillProcess skillProcess) {
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;

        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            ParticleManager.LineParticle(particleData1, playerHandLocation(player), 20, 0, 10);
            ParticleManager.LineParticle(particleData, playerHandLocation(player), 20, 0, 10);
            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            if (ray.isHitEntity()) {
                Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100*DoubleGunStance.multiply(playerData), 1);
                EffectManager.addEffect(ray.HitEntity, EffectType.Freeze, 50, player);
            }
            playSound(player, GunAttack);
            skillProcess.SkillRigid(skillData);
        }, "FreezeBullet: " + player.getName());
    }
}
