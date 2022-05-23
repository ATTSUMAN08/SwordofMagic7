package swordofmagic7.Mob.Skill;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Sound.CustomSound.playSound;

public class EnemySkillBase {
    public final EnemySkillManager Manager;
    public double radius = 64;
    public EnemySkillBase(EnemySkillManager manager) {
        this.Manager = manager;
    }

    public void radiusMessage(String message, SoundList sound) {
        for (Player player : PlayerList.getNear(entity().getLocation(), radius)) {
            Function.sendMessage(player, message);
            playSound(player, sound);
        }
    }

    public void setMessageRadius(double radius) {
        this.radius = radius;
    }

    public EnemyData enemyData() {
        return Manager.enemyData;
    }

    public LivingEntity entity() {
        return enemyData().entity;
    }

    public Location location() {
        Location location = entity().getLocation().clone();
        if (target() != null) {
            double x = location.getX();
            double z = location.getZ();
            double x2 = target().getLocation().getX();
            double z2 = target().getLocation().getZ();
            float yaw = (float) (Math.atan2(z2-z, x2-x)*180/Math.PI)-90;
            location.setYaw(yaw);
        }
        return location;
    }

    public LivingEntity target() {
        return enemyData().target;
    }

    public EffectManager effectManager() {
        return enemyData().effectManager;
    }

    public boolean isRunnableAI() {
        return enemyData().isRunnableAI();
    }
}
