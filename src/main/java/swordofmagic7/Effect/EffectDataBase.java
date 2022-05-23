package swordofmagic7.Effect;

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
                if (param.Display.equals("与ダメージ")) {
                    MultiplyStatusAdd(StatusParameter.DamageMultiplyATK, param.Value / 100);
                    MultiplyStatusAdd(StatusParameter.DamageMultiplyMAT, param.Value / 100);
                } else if (param.Display.equals("被ダメージ耐性")) {
                    MultiplyStatusAdd(StatusParameter.DamageResistanceATK, param.Value / 100);
                    MultiplyStatusAdd(StatusParameter.DamageResistanceMAT, param.Value / 100);
                }
            }
        }
        switch (effectType) {
            case CrossGuardCounter -> MultiplyStatusAdd(StatusParameter.DamageMultiplyATK, DataBase.getSkillData("CrossGuard").ParameterValue(2) / 100);
            case InsufficientFilling -> MultiplyStatusAdd(StatusParameter.ATK, -0.9);
            case Adhesive -> MultiplyStatusAdd(StatusParameter.ATK, -0.5);
            case ImmuneDepression -> {
                double value = DataBase.getRuneParameter("免疫低下のルーン").AdditionParameterValue(1)/100;
                MultiplyStatusAdd(StatusParameter.DamageResistanceATK, value);
                MultiplyStatusAdd(StatusParameter.DamageResistanceMAT, value);
            }
            case HitAndGuard -> MultiplyStatusAdd(StatusParameter.DEF, DataBase.getRuneParameter("ヒットアンドガードのルーン").AdditionParameterValue(1)/100);
            case MagicBarrier -> MultiplyStatusAdd(StatusParameter.DamageResistanceMAT, DataBase.getRuneParameter("魔法障壁のルーン").AdditionParameterValue(1)/100);
            case IceThorns -> MultiplyStatusAdd(StatusParameter.DamageMultiplyMAT, DataBase.getRuneParameter("氷の棘のルーン").AdditionParameterValue(0)/100);
            case LuxuryLiquor -> MultiplyStatusAdd(StatusParameter.DamageMultiplyMAT, DataBase.getSkillData("ブルタリティ").ParameterValue(0)/100);
        }
    }
}
