package swordofmagic7.Mob.Skill;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

public class BigAusSlime {

    private final EnemySkillManager Manager;
    public BigAusSlime(EnemySkillManager manager) {
        this.Manager = manager;
    }

    public void SlimeLaser() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity entity = Manager.enemyData.entity;
            List<LivingEntity> targets = new ArrayList<>(Function.NearEntityByEnemy(entity.getLocation(), 48));
            if (targets.size() > 0) {
                LivingEntity target = targets.get(random.nextInt(targets.size()));
                ParticleData particleData = new ParticleData(Particle.SLIME);
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), target.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, target, DamageCause.ATK, "Laser", 3, 1);
                if (target instanceof Player player) playSound(player, SoundList.Slime);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "Laser");
    }

    public void Crush() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity target = Manager.enemyData.target;
            LivingEntity entity = Manager.enemyData.entity;
            if (target != null && target.getLocation().distance(Manager.enemyData.entity.getLocation()) < 10) {
                ParticleData particleData = new ParticleData(Particle.SLIME);
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), target.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, target, DamageCause.ATK, "Crush", 3, 1);
                if (target instanceof Player player) playSound(player, SoundList.Slime);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "Crush");
    }

    public void Adhesive(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            LivingEntity entity = Manager.enemyData.entity;
            ParticleData particleData = new ParticleData(Particle.SLIME, 0.05f);
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                ParticleManager.CircleParticle(particleData, entity.getLocation(), 10, 24);
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            for (LivingEntity victim : Function.NearEntityByEnemy(Manager.enemyData.entity.getLocation(), 32)) {
                EffectManager.addEffect(victim, EffectType.Adhesive, 600, null);
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), victim.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, victim, DamageCause.ATK, "Adhesive", 2, 1);
                if (victim instanceof Player player) playSound(player, SoundList.Slime);
            }
            Manager.CastSkill(false);
        }, "Adhesive");
    }

    private final Collection<EnemyData> Familiar = new HashSet<>();
    private final int maxMob = 15;
    public void SummonFamiliar() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity entity = Manager.enemyData.entity;
            MobData mobData = DataBase.getMobData("ナイトスライム");
            int i = 0;
            Familiar.removeIf(EnemyData::isDead);
            while (Familiar.size() < maxMob && i < maxMob) {
                MobManager.mobSpawn(mobData, Manager.enemyData.Level, entity.getLocation());
                i++;
                MultiThread.sleepTick(20);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "SummonFamiliar");
    }

    public void InsaneRush(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            LivingEntity entity = Manager.enemyData.entity;
            double length = 20;
            double angle = 130;
            ParticleData particleData = new ParticleData(Particle.SLIME, 0.05f);
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                ParticleManager.FanShapedParticle(particleData, entity.getLocation(), length, angle, 24);
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            for (LivingEntity victim : ParticleManager.FanShapedCollider(Manager.enemyData.entity.getLocation(), Function.NearEntityByEnemy(Manager.enemyData.entity.getLocation(), 32), angle)) {
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), victim.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, victim, DamageCause.ATK, "InsaneRush", 10000, 1);
                if (victim instanceof Player player) playSound(player, SoundList.Explosion);
            }
            Manager.CastSkill(false);
        }, "SummonFamiliar");
    }
}
