package swordofmagic7.Status;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import swordofmagic7.Attribute.AttributeType;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectDataBase;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillParameter;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.Attribute.AttributeType.*;

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
    }

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


    public HashMap<StatusParameter, Double> BaseStatus = new HashMap<>();
    public HashMap<AttributeType, Integer> Attribute = new HashMap<>();
    public HashMap<StatusParameter, Double> EquipStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> BaseMultiplyStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> MultiplyStatus = new HashMap<>();
    public HashMap<StatusParameter, Double> FixedStatus = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseMultiply = new HashMap<>();
    public HashMap<DamageCause, Double> DamageCauseResistance = new HashMap<>();
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

    public double getCombatPower() {
        double combatPower = 0;
        combatPower += MaxHealth/1.2;
        combatPower += HealthRegen*12;
        combatPower += MaxMana/1.2;
        combatPower += ManaRegen*12;
        combatPower += ATK*1.2;
        combatPower += DEF*1.2;
        combatPower += ACC*1.2;
        combatPower += EVA*1.2;
        for (AttributeType attr : AttributeType.values()) {
            combatPower += playerData.Attribute.getAttribute(attr)*7.5;
        }
        return combatPower/2.2;
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

    void DamageCauseMultiplyAdd(DamageCause damageCause, double add) {
        DamageCauseMultiply.put(damageCause, DamageCauseMultiply.get(damageCause)+add);
    }

    void DamageCauseResistanceAdd(DamageCause damageCause, double add) {
        DamageCauseResistance.put(damageCause, DamageCauseResistance.get(damageCause)+add);
    }

    void BaseMultiplyStatusAdd(StatusParameter statusParameter, double add) {
        BaseMultiplyStatus.put(statusParameter, BaseMultiplyStatus.get(statusParameter)+add);
    }

    void MultiplyStatusAdd(StatusParameter statusParameter, double add) {
        MultiplyStatus.put(statusParameter, MultiplyStatus.get(statusParameter)+add);
    }

    public synchronized void StatusUpdate() {
        MultiThread.TaskRun(() -> {
            synchronized (this) {
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
                for (DamageCause cause : DamageCause.values()) {
                    DamageCauseMultiply.put(cause, 1d);
                    DamageCauseResistance.put(cause, 1d);
                }
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    for (StatusParameter param : StatusParameter.values()) {
                        EquipStatus.put(param, EquipStatus(param) + playerData.Equipment.getEquip(slot).itemEquipmentData.Parameter().get(param));
                    }
                }
                for (AttributeType attr: AttributeType.values()) {
                    Attribute.put(attr, playerData.Attribute.getAttribute(attr));
                }
                for (SkillData skillData : playerData.Classes.getPassiveSkillList()) {
                    if (playerData.Skill.CategoryCheck(skillData)) {
                        for (SkillParameter param : skillData.Parameter) {
                            for (StatusParameter statusParam : StatusParameter.values()) {
                                if (param.Display.equalsIgnoreCase("基礎" + statusParam.Display)) {
                                    BaseMultiplyStatusAdd(statusParam, param.Value / 100);
                                } else if (param.Display.equalsIgnoreCase(statusParam.Display)) {
                                    MultiplyStatusAdd(statusParam, param.Value / 100);
                                }
                            }
                            if (param.Display.equalsIgnoreCase("物理与ダメージ")) {
                                DamageCauseMultiplyAdd(DamageCause.ATK, param.Value / 100);
                            } else if (param.Display.equalsIgnoreCase("魔法与ダメージ")) {
                                DamageCauseMultiplyAdd(DamageCause.MAT, param.Value / 100);
                            } else if (param.Display.equalsIgnoreCase("物理被ダメージ耐性")) {
                                DamageCauseResistanceAdd(DamageCause.ATK, param.Value / 100);
                            } else if (param.Display.equalsIgnoreCase("魔法被ダメージ耐性")) {
                                DamageCauseResistanceAdd(DamageCause.MAT, param.Value / 100);
                            }
                        }
                    }
                }

                if (playerData.EffectManager.Effect.size() > 0) {
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
                        for (DamageCause cause : DamageCause.values()) {
                            DamageCauseMultiplyAdd(cause, EffectDataBase.EffectStatus(effectType).DamageCauseMultiply.getOrDefault(cause, 0d) * effectData.stack);
                            DamageCauseResistanceAdd(cause, EffectDataBase.EffectStatus(effectType).DamageCauseResistance.getOrDefault(cause, 0d) * effectData.stack);
                        }
                    }
                    for (StatusParameter param : StatusParameter.values()) {
                        MultiplyStatus.put(param, MultiplyStatus(param) * multiplyStatusRev.get(param));
                        BaseMultiplyStatus.put(param, BaseMultiplyStatus(param) * baseMultiplyStatusRev.get(param));
                    }
                }

                if (playerData.instantBuff.InstantBuffs.size() > 0) {
                    HashMap<StatusParameter, Double> multiply = playerData.instantBuff.getMultiply();
                    HashMap<StatusParameter, Double> fixed = playerData.instantBuff.getFixed();
                    for (StatusParameter param : StatusParameter.values()) {
                        MultiplyStatus.put(param, MultiplyStatus.get(param) + multiply.get(param));
                        FixedStatus.put(param, FixedStatus(param) + fixed.get(param));
                    }
                }

                CriticalMultiply = 1.2;
                CriticalMultiply += Attribute(AttributeType.DEX) * 0.008;
                DamageCauseMultiplyAdd(DamageCause.ATK, Attribute(STR) * 0.005);
                DamageCauseMultiplyAdd(DamageCause.MAT, Attribute(INT) * 0.004);
                DamageCauseResistanceAdd(DamageCause.ATK,Attribute(VIT) * 0.003);
                DamageCauseResistanceAdd(DamageCause.MAT,(Attribute(INT) + Attribute(SPI) + Attribute(VIT)) * 0.001);

                double M = LevelMultiply();
                BaseStatus.put(StatusParameter.MaxHealth, (M*100) * (1+Attribute(VIT)*0.008) * BaseMultiplyStatus(StatusParameter.MaxHealth));
                BaseStatus.put(StatusParameter.HealthRegen, (M*2) * (1+Attribute(VIT)*0.002) * BaseMultiplyStatus(StatusParameter.HealthRegen));
                BaseStatus.put(StatusParameter.MaxMana, (M*100) * (1+Attribute(SPI)*0.008) * BaseMultiplyStatus(StatusParameter.MaxMana));
                BaseStatus.put(StatusParameter.ManaRegen, (M*5) * (1+Attribute(SPI)*0.0005) * BaseMultiplyStatus(StatusParameter.ManaRegen));
                BaseStatus.put(StatusParameter.ATK, (M*10) * (1+Attribute(STR)*0.005+Attribute(INT)*0.005) * BaseMultiplyStatus(StatusParameter.ATK));
                BaseStatus.put(StatusParameter.DEF, (M*5) * (1+Attribute(VIT)*0.005) * BaseMultiplyStatus(StatusParameter.DEF));
                BaseStatus.put(StatusParameter.HLP, (M*5) * (1+Attribute(SPI)*0.005) * BaseMultiplyStatus(StatusParameter.HLP));
                BaseStatus.put(StatusParameter.ACC, (M*10) * (1+Attribute(TEC)*0.008) * BaseMultiplyStatus(StatusParameter.ACC));
                BaseStatus.put(StatusParameter.EVA, (M*5) * (1+Attribute(DEX)*0.008) * BaseMultiplyStatus(StatusParameter.EVA));
                BaseStatus.put(StatusParameter.CriticalRate, (M*10) * (1+Attribute(TEC)*0.01) * BaseMultiplyStatus(StatusParameter.CriticalRate));
                BaseStatus.put(StatusParameter.CriticalResist, (M*3) * (1+Attribute(SPI)*0.02) * BaseMultiplyStatus(StatusParameter.CriticalResist));
                BaseStatus.put(StatusParameter.SkillCastTime, BaseMultiplyStatus(StatusParameter.SkillCastTime));
                BaseStatus.put(StatusParameter.SkillRigidTime, BaseMultiplyStatus(StatusParameter.SkillRigidTime));
                BaseStatus.put(StatusParameter.SkillCooltime, BaseMultiplyStatus(StatusParameter.SkillCooltime));

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

                if (playerData.EffectManager.hasEffect(EffectType.InsufficientFilling)) ATK *= 0.1f;

                String color = "§f";
                if (playerData.PvPMode) color = "§c";
                player.setPlayerListName(playerData.Classes.topClass().Color + "§l" + playerData.Classes.topClass().Display + " " + color + playerData.Nick);

                MultiThread.TaskRunSynchronized(() -> {
                    player.setWalkSpeed(0.24f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0, false, false));
                });
            }
        }, "StatusUpdate: " + player.getName());
    }

    public double finalStatus(StatusParameter param) {
        return (BaseStatus(param) + EquipStatus(param)) * MultiplyStatus(param) + FixedStatus(param);
    }
}


