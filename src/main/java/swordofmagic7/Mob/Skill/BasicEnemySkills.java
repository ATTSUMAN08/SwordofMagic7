package swordofmagic7.Mob.Skill;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static net.somrpg.swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

public class BasicEnemySkills extends EnemySkillBase {

    public BasicEnemySkills(EnemySkillManager manager) {
        super(manager);
    }

    public void SkillLaser() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity entity = Manager.enemyData.entity;
            List<LivingEntity> targets = new ArrayList<>(Function.NearEntityByEnemy(entity.getLocation(), 48));
            if (targets.size() > 0) {
                LivingEntity target = targets.get(random.nextInt(targets.size()));
                ParticleData particleData = new ParticleData(Particle.LANDING_LAVA);
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), target.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, target, DamageCause.ATK, "SkillLaser", 1, 1);
                if (target instanceof Player player) playSound(player, SoundList.ROCK);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "SkillLaser");
    }
}
