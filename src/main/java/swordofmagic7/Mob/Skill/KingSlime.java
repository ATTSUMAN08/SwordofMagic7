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
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

public class KingSlime {

    private final EnemySkillManager Manager;
    public KingSlime(EnemySkillManager manager) {
        this.Manager = manager;
    }

    private void radiusMessage(String message) {
        for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 48)) {
            Function.sendMessage(player, message);
            playSound(player, SoundList.DungeonTrigger);
        }
    }

    public void SlimeLaser() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity entity = Manager.enemyData.entity;
            List<LivingEntity> targets = new ArrayList<>(Function.NearEntityByEnemy(entity.getLocation(), 48));
            if (targets.size() > 0) {
                LivingEntity target = targets.get(random.nextInt(targets.size()));
                ParticleData particleData = new ParticleData(Particle.ITEM_SLIME);
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), target.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, target, DamageCause.ATK, "SlimeLaser", 1.5, 1);
                if (target instanceof Player player) playSound(player, SoundList.Slime);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "SlimeLaser");
    }

    public void Crush(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity target = Manager.enemyData.target;
            LivingEntity entity = Manager.enemyData.entity;
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
            double radius = 12;

            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                ParticleManager.CircleParticle(Manager.particleCasting, entity.getLocation(), radius, 48);
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            if (target != null && target.getLocation().distance(entity.getLocation()) < radius) {
                ParticleData particleData = new ParticleData(Particle.ITEM_SLIME);
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), target.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, target, DamageCause.ATK, "Crush", 4, 1);
                Function.setVelocity(target, entity.getLocation().getDirection().setY(1));
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
            ParticleData particleData = new ParticleData(Particle.SWEEP_ATTACK, 0.05f);
            double radius = 15;
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);

            radiusMessage("§cキングスライムが何かを飛ばそうとしています！");

            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                ParticleManager.CircleParticle(Manager.particleCasting, entity.getLocation(), radius, 48);
                ParticleManager.CircleParticle(particleData, entity.getLocation(), radius, 24);
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            for (LivingEntity victim : Function.NearEntityByEnemy(Manager.enemyData.entity.getLocation(), radius)) {
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
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 50);

            radiusMessage("§cキングスライムがナイトスライムを召喚し始めました！");

            MobData mobData = DataBase.getMobData("ナイトスライム");
            int i = 0;
            Familiar.removeIf(EnemyData::isDead);
            while (Familiar.size() < maxMob && i < maxMob) {
                MultiThread.TaskRunSynchronized(() -> MobManager.mobSpawn(mobData, Manager.enemyData.Level, entity.getLocation()));
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
            ParticleData particleData = new ParticleData(Particle.ITEM_SLIME, 0.05f);
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);

            radiusMessage("§cキングスライムが強力な攻撃を使用としてます！避けてください！");

            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                ParticleManager.FanShapedParticle(particleData, entity.getLocation(), length, angle, 24);
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            for (LivingEntity victim : ParticleManager.FanShapedCollider(Manager.enemyData.entity.getLocation(), Function.NearEntityByEnemy(Manager.enemyData.entity.getLocation(), length), angle)) {
                ParticleManager.LineParticle(particleData, entity.getEyeLocation(), victim.getEyeLocation(), 1, 10);
                Damage.makeDamage(entity, victim, DamageCause.ATK, "InsaneRush", 10000, 1, 0.5);
                if (victim instanceof Player player) playSound(player, SoundList.Explosion);
            }
            Manager.CastSkill(false);
        }, "InsaneRush");
    }
}
