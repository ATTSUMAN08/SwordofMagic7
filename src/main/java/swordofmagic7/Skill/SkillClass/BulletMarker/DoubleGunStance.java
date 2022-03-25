package swordofmagic7.Skill.SkillClass.BulletMarker;

import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;

public class DoubleGunStance {

    public static double multiply(PlayerData playerData) {
        if (playerData.EffectManager.hasEffect(EffectType.DoubleGunStance)) {
            return DataBase.getSkillData("DoubleGunStance").ParameterValue(1)/100+1;
        } else return 1;
    }

}
