package swordofmagic7.Mob.Skill;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.CustomSound;
import swordofmagic7.Sound.SoundList;

public class Faras extends EnemySkillBase {

    public Faras(EnemySkillManager manager) {
        super(manager);
    }

    public void rush(int castTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            waitCastTime(castTime);

            CustomSound.playSound(entity().getLocation(), SoundList.EXPLOSION);
            ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_EMITTER);
            particleData2.spawn(entity().getLocation());
            knockEntityTowardsTarget(entity(), target(), 3.0, 1.0);

            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "FarasRush");
    }

    public void rapidRush(int castTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            waitCastTime(castTime);

            CustomSound.playSound(entity().getLocation(), SoundList.EXPLOSION);
            ParticleData particleData = new ParticleData(Particle.EXPLOSION_EMITTER);
            particleData.spawn(entity().getLocation());

            knockEntityTowardsTarget(entity(), target(), 3.0, 1.0);

            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "FarasRapidRush");
    }

    public void muteCry() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);

            radiusMessage("§c黙れ！", SoundList.DUNGEON_TRIGGER);
            for (Player player : PlayerList.getNearNonDead(entity().getLocation(), radius)) {
                EffectManager.addEffect(player, EffectType.Silence, 150, null);
            }

            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "FarasMuteCry");
    }

    public void quickening() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);

            radiusMessage("§c加速！", SoundList.DUNGEON_TRIGGER);
            enemyData().MovementMultiply = 1.5;

            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "FarasQuickening");
    }

    private void waitCastTime(int castTime) {
        ParticleData particleData = new ParticleData(Particle.SMOKE, 0.05f);

        for (int i = 0; i < castTime; i += Manager.period) {
            ParticleManager.CircleParticle(particleData, target().getLocation(), 1, 24);
            MultiThread.sleepTick(Manager.period);
        }
    }


    /**
     * 指定されたエンティティを別のエンティティに向かって飛ばす
     *
     * @param entityToKnock 飛ばされるエンティティ
     * @param targetEntity 目標となるエンティティ
     * @param distance 飛ばす距離（ブロック単位）
     * @param power 飛ばす力の強度（通常0.5～2.0程度）
     */
    private static void knockEntityTowardsTarget(Entity entityToKnock, Entity targetEntity, double distance, double power) {
        // 両方のエンティティの位置を取得
        Vector fromLocation = entityToKnock.getLocation().toVector();
        Vector toLocation = targetEntity.getLocation().toVector();

        // 方向ベクトルを計算（目標 - 現在位置）
        Vector direction = toLocation.subtract(fromLocation);

        // Y軸の調整（少し上向きに飛ばす）
        direction.setY(direction.getY() + 0.3);

        // ベクトルを正規化（長さを1にする）
        direction.normalize();

        // 指定された距離と力を適用
        direction.multiply(distance * power);

        // エンティティのベロシティを設定
        entityToKnock.setVelocity(direction);
    }
}
