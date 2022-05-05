package swordofmagic7.Effect;

import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Skill.SkillParameter;
import swordofmagic7.Status.StatusParameter;

import java.util.HashMap;

public class EffectDataBase {
    public static HashMap<EffectType, EffectDataBase> EffectStatus = new HashMap<>();
    public static EffectDataBase EffectStatus(EffectType effectType) {
        if (!EffectStatus.containsKey(effectType)) {
            EffectStatus.put(effectType, new EffectDataBase(effectType));
        }
        return EffectStatus.get(effectType);
    }

    public HashMap<StatusParameter, Double> BaseMultiplyStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> MultiplyStatus = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseMultiply = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseResistance = new HashMap<>();

    void DamageCauseMultiplyAdd(DamageCause damageCause, double add) {
        DamageCauseMultiply.put(damageCause, DamageCauseMultiply.getOrDefault(damageCause, 0d)+add);
    }

    void DamageCauseResistanceAdd(DamageCause damageCause, double add) {
        DamageCauseResistance.put(damageCause, DamageCauseResistance.getOrDefault(damageCause, 0d)+add);
    }

    void BaseMultiplyStatusAdd(StatusParameter statusParameter, double add) {
        BaseMultiplyStatus.put(statusParameter, BaseMultiplyStatus.getOrDefault(statusParameter, 0d)+add);
    }

    void MultiplyStatusAdd(StatusParameter statusParameter, double add) {
        MultiplyStatus.put(statusParameter, MultiplyStatus.getOrDefault(statusParameter, 0d)+add);
    }

    public EffectDataBase(EffectType effectType) {
        String skillText = effectType.toString();
        if (DataBase.getSkillList().containsKey(skillText)) {
            for (SkillParameter param : DataBase.getSkillData(skillText).Parameter) {
                for (StatusParameter statusParam : StatusParameter.values()) {
                    if (param.Display.equalsIgnoreCase("基礎" + statusParam.Display)) {
                        BaseMultiplyStatusAdd(statusParam, param.Value / 100);
                    } else if (param.Display.equalsIgnoreCase(statusParam.Display)) {
                        MultiplyStatusAdd(statusParam, param.Value / 100);
                    }
                }
                if (param.Display.equalsIgnoreCase("物理与ダメージ") || param.Display.equalsIgnoreCase("与ダメージ")) {
                    DamageCauseMultiplyAdd(DamageCause.ATK, param.Value / 100);
                } if (param.Display.equalsIgnoreCase("魔法与ダメージ") || param.Display.equalsIgnoreCase("与ダメージ")) {
                    DamageCauseMultiplyAdd(DamageCause.MAT, param.Value / 100);
                } if (param.Display.equalsIgnoreCase("物理被ダメージ耐性") || param.Display.equalsIgnoreCase("被ダメージ耐性")) {
                    DamageCauseResistanceAdd(DamageCause.ATK, param.Value / 100);
                } if (param.Display.equalsIgnoreCase("魔法被ダメージ耐性") || param.Display.equalsIgnoreCase("被ダメージ耐性")) {
                    DamageCauseResistanceAdd(DamageCause.MAT, param.Value / 100);
                }
            }
        }
        switch (effectType) {
            case CrossGuardCounter -> DamageCauseMultiplyAdd(DamageCause.ATK, DataBase.getSkillData("CrossGuard").ParameterValue(2) / 100);
            case InsufficientFilling -> MultiplyStatusAdd(StatusParameter.ATK, -0.9);
            case Adhesive -> MultiplyStatusAdd(StatusParameter.ATK, -0.5);
            case ImmuneDepression -> {
                for (DamageCause cause : DamageCause.values()) DamageCauseResistanceAdd(cause, DataBase.getRuneParameter("免疫低下のルーン").AdditionParameterValue(1)/100);
            }
            case HitAndGuard -> MultiplyStatusAdd(StatusParameter.DEF, DataBase.getRuneParameter("ヒットアンドガードのルーン").AdditionParameterValue(1)/100);
            case MagicBarrier -> DamageCauseResistanceAdd(DamageCause.MAT, DataBase.getRuneParameter("魔法障壁のルーン").AdditionParameterValue(1)/100);
            case IceThorns -> DamageCauseMultiplyAdd(DamageCause.MAT, DataBase.getRuneParameter("氷の棘のルーン").AdditionParameterValue(0)/100);
        }
    }
}
