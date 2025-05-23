package swordofmagic7.Status;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import swordofmagic7.Attribute.AttributeType;
import swordofmagic7.classes.ClassData;
import swordofmagic7.classes.Classes;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectDataBase;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.ItemExtend.ItemEquipmentData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillParameter;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.Attribute.AttributeType.*;
import static swordofmagic7.Data.DataBase.*;

public class Status {
    private final Player player;
    private final PlayerData playerData;
    private final Classes classes;
    private final Skill skill;
    public Status(Player player, PlayerData playerData, Classes classes, Skill skill) {
        this.player = player;
        this.playerData = playerData;
        this.classes = classes;
        this.skill = skill;
        movementEffect.put(EffectType.Cloaking, getSkillData("Cloaking").ParameterValue(1)/100);
        movementEffect.put(EffectType.Modafinil, getSkillData("Modafinil").ParameterValue(1)/100);
        movementEffect.put(EffectType.Ole, getSkillData("Ole").ParameterValue(2)/100);
    }

    public HashMap<EffectType, Double> movementEffect = new HashMap<>();

    public double MaxHealth;
    public double HealthRegen;
    public double Health;
    public double MaxMana;
    public double ManaRegen;
    public double Mana;
    public double ATK;
    public double DEF;
    public double HLP;
    public double ACC;
    public double EVA;
    public double CriticalRate;
    public double CriticalResist;
    public double SkillCastTime;
    public double SkillRigidTime;
    public double SkillCooltime;
    public double Movement;
    public double Shield;
    public double DamageMultiplyATK;
    public double DamageMultiplyMAT;
    public double DamageResistanceATK;
    public double DamageResistanceMAT;
    public double MagicCircleRange;


    public HashMap<StatusParameter, Double> BaseStatus = new HashMap<>();
    public HashMap<AttributeType, Integer> Attribute = new HashMap<>();
    public HashMap<StatusParameter, Double> EquipStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> BaseMultiplyStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> MultiplyStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> FixedStatus = new HashMap<>();
    public double CriticalMultiply;

    public double BaseStatus(StatusParameter param) {
        return BaseStatus.getOrDefault(param, 0d);
    }

    int Attribute(AttributeType attr) {
        return Attribute.getOrDefault(attr, 0);
    }

    double EquipStatus(StatusParameter param) {
        return EquipStatus.getOrDefault(param, 0d);
    }

    double BaseMultiplyStatus(StatusParameter param) {
        return BaseMultiplyStatus.getOrDefault(param, 0d);
    }

    double MultiplyStatus(StatusParameter param) {
        return MultiplyStatus.getOrDefault(param, 0d);
    }

    double FixedStatus(StatusParameter param) {
        return FixedStatus.getOrDefault(param, 0d);
    }

    private double CombatPower;
    public double getCombatPower() {
        return CombatPower;
    }

    public void setCombatPower() {
        double combatPower = 0;
        combatPower += (BaseStatus(StatusParameter.MaxHealth) + EquipStatus(StatusParameter.MaxHealth))/3;
        combatPower += (BaseStatus(StatusParameter.HealthRegen) + EquipStatus(StatusParameter.HealthRegen))*3;
        combatPower += (BaseStatus(StatusParameter.MaxMana) + EquipStatus(StatusParameter.MaxMana))/3;
        combatPower += (BaseStatus(StatusParameter.ManaRegen) + EquipStatus(StatusParameter.ManaRegen))*3;
        combatPower += (BaseStatus(StatusParameter.ATK) + EquipStatus(StatusParameter.ATK));
        combatPower += (BaseStatus(StatusParameter.DEF) + EquipStatus(StatusParameter.DEF)/2);
        combatPower += (BaseStatus(StatusParameter.HLP) + EquipStatus(StatusParameter.HLP))*0.75;
        combatPower += (BaseStatus(StatusParameter.ACC) + EquipStatus(StatusParameter.ACC))*3;
        combatPower += (BaseStatus(StatusParameter.EVA) + EquipStatus(StatusParameter.EVA))*3;
        combatPower += (BaseStatus(StatusParameter.CriticalRate) + EquipStatus(StatusParameter.CriticalRate))*0.75;
        combatPower += (BaseStatus(StatusParameter.CriticalResist) + EquipStatus(StatusParameter.CriticalResist));
        combatPower += (BaseStatus(StatusParameter.SkillCooltime) + EquipStatus(StatusParameter.SkillCooltime));
        combatPower += (BaseStatus(StatusParameter.SkillCastTime) + EquipStatus(StatusParameter.SkillCastTime));
        combatPower += (BaseStatus(StatusParameter.SkillRigidTime) + EquipStatus(StatusParameter.SkillRigidTime));
        combatPower += (BaseStatus(StatusParameter.DamageMultiplyATK) + EquipStatus(StatusParameter.DamageMultiplyATK));
        combatPower += (BaseStatus(StatusParameter.DamageMultiplyMAT) + EquipStatus(StatusParameter.DamageMultiplyATK));
        combatPower += (BaseStatus(StatusParameter.DamageResistanceATK) + EquipStatus(StatusParameter.DamageResistanceATK));
        combatPower += (BaseStatus(StatusParameter.DamageResistanceMAT) + EquipStatus(StatusParameter.DamageResistanceMAT));
        combatPower += (BaseStatus(StatusParameter.MagicCircleRange) + EquipStatus(StatusParameter.MagicCircleRange));
        for (AttributeType attr : AttributeType.values()) {
            combatPower += playerData.Attribute.getAttribute(attr)*7.5;
        }
        CombatPower = combatPower/7.5;
    }

    public int totalLevel() {
        int level = playerData.Level;
        for (ClassData classData : classes.classSlot) {
            if (classData != null) {
                level += classes.getClassLevel(classData) - 1;
            }
        }
        return level;
    }

    double LevelMultiply() {
        int level = totalLevel();
        return Math.pow(1.01, level) + level *0.05;
    }

    void BaseMultiplyStatusAdd(StatusParameter statusParameter, double add) {
        BaseMultiplyStatus.put(statusParameter, BaseMultiplyStatus.get(statusParameter)+add);
    }

    void MultiplyStatusAdd(StatusParameter statusParameter, double add) {
        MultiplyStatus.put(statusParameter, MultiplyStatus.get(statusParameter)+add);
    }

    public synchronized void StatusUpdate() {
        HashMap<StatusParameter, Double> baseMultiplyStatusRev = new HashMap<>();
        HashMap<StatusParameter, Double> multiplyStatusRev = new HashMap<>();
        for (StatusParameter param : StatusParameter.values()) {
            EquipStatus.put(param, 0d);
            BaseMultiplyStatus.put(param, 1d);
            MultiplyStatus.put(param, 1d);
            FixedStatus.put(param, 0d);
            multiplyStatusRev.put(param, 1d);
            baseMultiplyStatusRev.put(param, 1d);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemEquipmentData itemEquipmentData = playerData.Equipment.getEquip(slot).itemEquipmentData;
            if (itemEquipmentData.equipmentCategory != EquipmentCategory.Baton) for (StatusParameter param : StatusParameter.values()) {
                EquipStatus.put(param, EquipStatus(param) + itemEquipmentData.Parameter(playerData.Level).get(param));
            }
        }
        for (AttributeType attr : AttributeType.values()) {
            Attribute.put(attr, playerData.Attribute.getAttribute(attr));
        }
        for (SkillData skillData : playerData.Classes.getPassiveSkillList()) {
            if (playerData.Skill.CategoryCheck(skillData)) {
                double multiply = 1;
                if (skillData.Id.equals("BasicDamageDown") && playerData.Equipment.isMainHandEquip(EquipmentCategory.Mace)) multiply = -1;
                for (SkillParameter param : skillData.Parameter) {
                    for (StatusParameter statusParam : StatusParameter.values()) {
                        if (param.Display.equalsIgnoreCase("基礎" + statusParam.Display)) {
                            BaseMultiplyStatusAdd(statusParam, param.Value / 100 * multiply);
                        } else if (param.Display.equalsIgnoreCase(statusParam.Display)) {
                            MultiplyStatusAdd(statusParam, param.Value / 100 * multiply);
                        }
                    }
                }
            }
        }

        if (!playerData.EffectManager.Effect.isEmpty()) {
            for (Map.Entry<EffectType, EffectData> data : playerData.EffectManager.Effect.entrySet()) {
                EffectType effectType = data.getKey();
                EffectData effectData = data.getValue();
                for (StatusParameter param : StatusParameter.values()) {
                    double multiplyStatusAdd = EffectDataBase.EffectStatus(effectType).MultiplyStatus.getOrDefault(param, 0d) * effectData.stack;
                    double baseMultiplyStatusAdd = EffectDataBase.EffectStatus(effectType).BaseMultiplyStatus.getOrDefault(param, 0d) * effectData.stack;
                    if (multiplyStatusAdd >= 0) MultiplyStatusAdd(param, multiplyStatusAdd);
                    else {
                        multiplyStatusRev.put(param, multiplyStatusRev.get(param) * Math.pow(1 + multiplyStatusAdd, effectData.stack));
                    }
                    if (baseMultiplyStatusAdd >= 0) BaseMultiplyStatusAdd(param, baseMultiplyStatusAdd);
                    else {
                        baseMultiplyStatusRev.put(param, baseMultiplyStatusRev.get(param) * Math.pow(1 + baseMultiplyStatusAdd, effectData.stack));
                    }
                }
            }
            for (StatusParameter param : StatusParameter.values()) {
                MultiplyStatus.put(param, MultiplyStatus(param) * multiplyStatusRev.get(param));
                BaseMultiplyStatus.put(param, BaseMultiplyStatus(param) * baseMultiplyStatusRev.get(param));
            }
        }

        if (!playerData.instantBuff.InstantBuffs.isEmpty()) {
            HashMap<StatusParameter, Double> multiply = playerData.instantBuff.getMultiply();
            HashMap<StatusParameter, Double> fixed = playerData.instantBuff.getFixed();
            for (StatusParameter param : StatusParameter.values()) {
                MultiplyStatus.put(param, MultiplyStatus.get(param) + multiply.get(param));
                FixedStatus.put(param, FixedStatus(param) + fixed.get(param));
            }
        }

        CriticalMultiply = 1.3;
        CriticalMultiply += Attribute(AttributeType.DEX) * 0.008;

        double M = LevelMultiply();
        BaseStatus.put(StatusParameter.MaxHealth, (M * 100) * (1 + Attribute(VIT) * 0.008) * BaseMultiplyStatus(StatusParameter.MaxHealth));
        BaseStatus.put(StatusParameter.HealthRegen, (M * 2) * BaseMultiplyStatus(StatusParameter.HealthRegen));
        BaseStatus.put(StatusParameter.MaxMana, (M * 100) * (1 + Attribute(SPI) * 0.008) * BaseMultiplyStatus(StatusParameter.MaxMana));
        BaseStatus.put(StatusParameter.ManaRegen, (M * 5) * (1 + Attribute(SPI) * 0.0005) * BaseMultiplyStatus(StatusParameter.ManaRegen));
        BaseStatus.put(StatusParameter.ATK, (M * 10) * (1 + Attribute(STR) * 0.025 + Attribute(INT) * 0.025) * BaseMultiplyStatus(StatusParameter.ATK));
        BaseStatus.put(StatusParameter.DEF, (M * 5) * (1 + Attribute(VIT) * 0.025) * BaseMultiplyStatus(StatusParameter.DEF));
        BaseStatus.put(StatusParameter.HLP, (M * 5) * (1 + Attribute(SPI) * 0.025) * BaseMultiplyStatus(StatusParameter.HLP));
        BaseStatus.put(StatusParameter.ACC, (M * 10) * (1 + Attribute(TEC) * 0.042) * BaseMultiplyStatus(StatusParameter.ACC));
        BaseStatus.put(StatusParameter.EVA, (M * 5) * (1 + Attribute(DEX) * 0.042) * BaseMultiplyStatus(StatusParameter.EVA));
        BaseStatus.put(StatusParameter.CriticalRate, (M * 10) * (1 + Attribute(TEC) * 0.042) * BaseMultiplyStatus(StatusParameter.CriticalRate));
        BaseStatus.put(StatusParameter.CriticalResist, (M * 3) * (1 + Attribute(VIT) * 0.038) * BaseMultiplyStatus(StatusParameter.CriticalResist));
        BaseStatus.put(StatusParameter.SkillCastTime, BaseMultiplyStatus(StatusParameter.SkillCastTime));
        BaseStatus.put(StatusParameter.SkillRigidTime, BaseMultiplyStatus(StatusParameter.SkillRigidTime));
        BaseStatus.put(StatusParameter.SkillCooltime, BaseMultiplyStatus(StatusParameter.SkillCooltime));
        BaseStatus.put(StatusParameter.DamageMultiplyATK, 1d);
        BaseStatus.put(StatusParameter.DamageMultiplyMAT, 1d);
        BaseStatus.put(StatusParameter.DamageResistanceATK, 1d);
        BaseStatus.put(StatusParameter.DamageResistanceMAT, 1d);
        MultiplyStatusAdd(StatusParameter.DamageMultiplyATK, Attribute(STR) * 0.005);
        MultiplyStatusAdd(StatusParameter.DamageMultiplyMAT, Attribute(INT) * 0.004);
        MultiplyStatusAdd(StatusParameter.DamageResistanceATK, Attribute(VIT) * 0.003);
        MultiplyStatusAdd(StatusParameter.DamageResistanceMAT, (Attribute(INT) + Attribute(SPI) + Attribute(VIT)) * 0.001);

        MaxHealth = finalStatus(StatusParameter.MaxHealth);
        HealthRegen = finalStatus(StatusParameter.HealthRegen);
        MaxMana = finalStatus(StatusParameter.MaxMana);
        ManaRegen = finalStatus(StatusParameter.ManaRegen);
        ATK = finalStatus(StatusParameter.ATK);
        DEF = finalStatus(StatusParameter.DEF);
        HLP = finalStatus(StatusParameter.HLP);
        ACC = finalStatus(StatusParameter.ACC);
        EVA = finalStatus(StatusParameter.EVA);
        CriticalRate = finalStatus(StatusParameter.CriticalRate);
        CriticalResist = finalStatus(StatusParameter.CriticalResist);
        SkillCastTime = finalStatus(StatusParameter.SkillCastTime);
        SkillRigidTime = finalStatus(StatusParameter.SkillRigidTime);
        SkillCooltime = finalStatus(StatusParameter.SkillCooltime);
        DamageMultiplyATK = finalStatus(StatusParameter.DamageMultiplyATK);
        DamageMultiplyMAT = finalStatus(StatusParameter.DamageMultiplyMAT);
        DamageResistanceATK = finalStatus(StatusParameter.DamageResistanceATK);
        DamageResistanceMAT = finalStatus(StatusParameter.DamageResistanceMAT);
        MagicCircleRange = finalStatus(StatusParameter.MagicCircleRange);
        Movement = 0.24f;
        for (Map.Entry<EffectType, Double> entry : movementEffect.entrySet()) {
            if (playerData.EffectManager.hasEffect(entry.getKey())) {
                Movement += entry.getValue();
            }
        }

        setCombatPower();
        MultiThread.TaskRunSynchronized(() -> {
            player.setWalkSpeed((float) Math.min(Movement, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 0, false, false, false));
        });
        playerData.updateBlockPlayer();
    }

    public double finalStatus(StatusParameter param) {
        return (BaseStatus(param) + EquipStatus(param)) * MultiplyStatus(param) + FixedStatus(param);
    }
}


