package swordofmagic7.Status;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import swordofmagic7.Attribute.AttributeType;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.PlayerList;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillParameter;
import swordofmagic7.System;
import swordofmagic7.TagGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Attribute.AttributeType.*;
import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.System.BTTSet;

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

    double LevelMultiply() {
        int level = playerData.Level;
        for (ClassData classData : classes.classSlot) {
            level += classes.getClassLevel(classData)-1;
        }
        return 1+level*0.05;
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

    public void StatusUpdate() {
        for (StatusParameter param : StatusParameter.values()) {
            EquipStatus.put(param, 0d);
            BaseMultiplyStatus.put(param, 1d);
            MultiplyStatus.put(param, 1d);
        }
        for (DamageCause cause : DamageCause.values()) {
            DamageCauseMultiply.put(cause, 1d);
            DamageCauseResistance.put(cause, 1d);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (StatusParameter param : StatusParameter.values()) {
                EquipStatus.put(param, EquipStatus.get(param) + playerData.Equipment.getEquip(slot).itemEquipmentData.Parameter().get(param));
            }
        }
        for (AttributeType attr: AttributeType.values()) {
            Attribute.put(attr, playerData.Attribute.getAttribute(attr));
        }
        for (SkillData skillData : playerData.Classes.getPassiveSkillList()) {
            for (SkillParameter param : skillData.Parameter) {
                if (param.Display.equalsIgnoreCase("基礎攻撃力")) {
                    BaseMultiplyStatusAdd(StatusParameter.ATK, param.Value/100);
                } else if (param.Display.equalsIgnoreCase("基礎防御力")) {
                    BaseMultiplyStatusAdd(StatusParameter.DEF, param.Value / 100);
                } else if (param.Display.equalsIgnoreCase("基礎治癒力")) {
                    BaseMultiplyStatusAdd(StatusParameter.HLP, param.Value/100);
                } else if (param.Display.equalsIgnoreCase("攻撃力")) {
                    MultiplyStatusAdd(StatusParameter.ATK, param.Value/100);
                } else if (param.Display.equalsIgnoreCase("防御力")) {
                    MultiplyStatusAdd(StatusParameter.DEF, param.Value/100);
                } else if (param.Display.equalsIgnoreCase("治癒力")) {
                    MultiplyStatusAdd(StatusParameter.HLP, param.Value/100);
                } else if (param.Display.equalsIgnoreCase("物理与ダメージ")) {
                    DamageCauseMultiplyAdd(DamageCause.ATK, param.Value/100);
                } else if (param.Display.equalsIgnoreCase("魔法与ダメージ")) {
                    DamageCauseMultiplyAdd(DamageCause.MAT, param.Value/100);
                }
            }
        }
        if (playerData.EffectManager.hasEffect(EffectType.PainBarrier)) {
            double add = DataBase.getSkillData("PainBarrier").ParameterValue(1)/100;
            MultiplyStatusAdd(StatusParameter.DEF, add);
        }
        if (playerData.EffectManager.hasEffect(EffectType.Aiming)) {
            double add = DataBase.getSkillData("Aiming").ParameterValue(1)/100;
            DamageCauseMultiplyAdd(DamageCause.ATK, add);
            DamageCauseMultiplyAdd(DamageCause.MAT, add);
        }
        if (playerData.EffectManager.hasEffect(EffectType.HolyAttack)) {
            double add = DataBase.getSkillData("HolyAttack").ParameterValue(2)/100;
            DamageCauseMultiplyAdd(DamageCause.ATK, add);
        }
        if (playerData.EffectManager.hasEffect(EffectType.HolyDefense)) {
            double add = DataBase.getSkillData("HolyDefense").ParameterValue(2)/100;
            MultiplyStatusAdd(StatusParameter.DEF, add);
        }

        if (playerData.EffectManager.hasEffect(EffectType.Monstrance)) {
            double rev = DataBase.getSkillData("Monstrance").ParameterValue(1)/100;
            StatusParameter param = StatusParameter.EVA;
            MultiplyStatusAdd(param, -(MultiplyStatus(param)*rev));
        }

        CriticalMultiply = 1.2;
        CriticalMultiply += Attribute(AttributeType.DEX) * 0.008;
        DamageCauseMultiplyAdd(DamageCause.ATK, Attribute(STR) * 0.005);
        DamageCauseMultiplyAdd(DamageCause.MAT, Attribute(INT) * 0.004);
        DamageCauseResistanceAdd(DamageCause.ATK,Attribute(VIT) * 0.003);
        DamageCauseResistanceAdd(DamageCause.MAT,(Attribute(INT) + Attribute(SPI) + Attribute(VIT)) * 0.001);

        double M = LevelMultiply();
        BaseStatus.put(StatusParameter.MaxHealth, (M*100) * (1+Attribute(VIT)*0.008));
        BaseStatus.put(StatusParameter.HealthRegen, (M*2) * (1+Attribute(VIT)*0.002) * BaseMultiplyStatus(StatusParameter.HealthRegen));
        BaseStatus.put(StatusParameter.MaxMana, (M*100) * (1+Attribute(SPI)*0.008) * BaseMultiplyStatus(StatusParameter.MaxMana));
        BaseStatus.put(StatusParameter.ManaRegen, (M*5) * (1+Attribute(SPI)*0.006) * BaseMultiplyStatus(StatusParameter.ManaRegen));
        BaseStatus.put(StatusParameter.ATK, (M*10) * (1+Attribute(STR)*0.005+Attribute(INT)*0.005) * BaseMultiplyStatus(StatusParameter.ATK));
        BaseStatus.put(StatusParameter.DEF, (M*5) * (1+Attribute(VIT)*0.005) * BaseMultiplyStatus(StatusParameter.DEF));
        BaseStatus.put(StatusParameter.HLP, (M*5) * (1+Attribute(SPI)*0.005) * BaseMultiplyStatus(StatusParameter.HLP));
        BaseStatus.put(StatusParameter.ACC, (M*10) * (1+Attribute(TEC)*0.008) * BaseMultiplyStatus(StatusParameter.ACC));
        BaseStatus.put(StatusParameter.EVA, (M*5) * (1+Attribute(DEX)*0.008) * BaseMultiplyStatus(StatusParameter.EVA));
        BaseStatus.put(StatusParameter.CriticalRate, (M*10) * (1+Attribute(TEC)*0.01) * BaseMultiplyStatus(StatusParameter.CriticalRate));
        BaseStatus.put(StatusParameter.CriticalResist, (M*3) * (1+Attribute(SPI)*0.02) * BaseMultiplyStatus(StatusParameter.CriticalResist));

        MaxHealth = (BaseStatus(StatusParameter.MaxHealth) + EquipStatus(StatusParameter.MaxHealth)) * MultiplyStatus(StatusParameter.MaxHealth);
        HealthRegen = (BaseStatus(StatusParameter.HealthRegen) + EquipStatus(StatusParameter.HealthRegen)) * MultiplyStatus(StatusParameter.HealthRegen);
        MaxMana = (BaseStatus(StatusParameter.MaxMana) + EquipStatus(StatusParameter.MaxMana)) * MultiplyStatus(StatusParameter.MaxMana);
        ManaRegen = (BaseStatus(StatusParameter.ManaRegen) + EquipStatus(StatusParameter.ManaRegen)) * MultiplyStatus(StatusParameter.ManaRegen);
        ATK = (BaseStatus(StatusParameter.ATK) + EquipStatus(StatusParameter.ATK)) * MultiplyStatus(StatusParameter.ATK);
        DEF = (BaseStatus(StatusParameter.DEF) + EquipStatus(StatusParameter.DEF)) * MultiplyStatus(StatusParameter.DEF);
        HLP = (BaseStatus(StatusParameter.HLP) + EquipStatus(StatusParameter.HLP)) * MultiplyStatus(StatusParameter.HLP);
        ACC = (BaseStatus(StatusParameter.ACC) + EquipStatus(StatusParameter.ACC)) * MultiplyStatus(StatusParameter.ACC);
        EVA = (BaseStatus(StatusParameter.EVA) + EquipStatus(StatusParameter.EVA)) * MultiplyStatus(StatusParameter.EVA);
        CriticalRate = (BaseStatus(StatusParameter.CriticalRate) + EquipStatus(StatusParameter.CriticalRate)) * MultiplyStatus(StatusParameter.CriticalResist);
        CriticalResist = (BaseStatus(StatusParameter.CriticalResist) + EquipStatus(StatusParameter.CriticalResist)) * MultiplyStatus(StatusParameter.CriticalResist);
        SkillCastTime = (EquipStatus(StatusParameter.SkillCastTime)) * MultiplyStatus(StatusParameter.SkillCastTime);
        SkillRigidTime = (EquipStatus(StatusParameter.SkillRigidTime)) * MultiplyStatus(StatusParameter.SkillRigidTime);
        SkillCooltime = (EquipStatus(StatusParameter.SkillCooltime)) * MultiplyStatus(StatusParameter.SkillCooltime);

        String color = "§f";
        if (playerData.PvPMode) color = "§c";
        player.setPlayerListName(playerData.Classes.lastClass().Color + "§l" + playerData.Classes.lastClass().Display + " " + color + "§l" + playerData.Nick);

        player.setWalkSpeed(0.24f);
    }
}


