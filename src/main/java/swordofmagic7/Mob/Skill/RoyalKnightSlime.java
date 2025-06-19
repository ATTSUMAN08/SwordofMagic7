package swordofmagic7.Mob.Skill;

import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Sound.SoundList;

public class RoyalKnightSlime extends EnemySkillBase {

    public RoyalKnightSlime(EnemySkillManager manager) {
        super(manager);
    }

    public void cleave() {
        Manager.PullUpper(8, 90, 40, 1.5);
    }

    public void nova() {
        radiusMessage("§cNova", SoundList.DUNGEON_TRIGGER);
        Manager.shapeDamage(5.0, 80, 2.0);
    }

    public void resolve() {
        radiusMessage("§cダメージ強化", SoundList.DUNGEON_TRIGGER);
        enemyData().DamageMultiplyATK = 1.5;
    }

    public void rapidCleave() {
        Manager.PullUpper(8, 90, 15, 1.5);
    }

}
