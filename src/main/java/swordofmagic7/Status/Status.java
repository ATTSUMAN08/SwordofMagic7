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
import swordofmagic7.Data.PlayerData;
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

import static swordofmagic7.Attribute.AttributeType.*;
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
        int level = 0;
        for (ClassData classData : classes.classTier) {
            level += classes.getLevel(classData)-1;
        }
        return 1+level*0.05;
    }

    void DamageCauseMultiplyAdd(DamageCause damageCause, double add) {
        DamageCauseMultiply.put(damageCause, DamageCauseMultiply.get(damageCause)+add);
    }

    void DamageCauseResistanceAdd(DamageCause damageCause, double add) {
        DamageCauseResistance.put(damageCause, DamageCauseResistance.get(damageCause)+add);
    }

    void MultiplyStatusAdd(StatusParameter statusParameter, double add) {
        MultiplyStatus.put(statusParameter, MultiplyStatus.get(statusParameter)+add);
    }

    public void StatusUpdate() {
        for (StatusParameter param : StatusParameter.values()) {
            EquipStatus.put(param, 0d);
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
                if (param.Display.equalsIgnoreCase("攻撃力")) {
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

        CriticalMultiply = 1.2;
        CriticalMultiply += Attribute(AttributeType.DEX) * 0.008;
        DamageCauseMultiplyAdd(DamageCause.ATK, Attribute(AttributeType.STR) * 0.005);
        DamageCauseMultiplyAdd(DamageCause.MAT, Attribute(AttributeType.INT) * 0.004);
        DamageCauseResistanceAdd(DamageCause.ATK,Attribute(VIT) * 0.003);
        DamageCauseResistanceAdd(DamageCause.MAT,Attribute(AttributeType.INT) * 0.001);
        DamageCauseResistanceAdd(DamageCause.MAT,Attribute(AttributeType.SPI) * 0.001);
        DamageCauseResistanceAdd(DamageCause.MAT,Attribute(VIT) * 0.001);


        double M = LevelMultiply();
        BaseStatus.put(StatusParameter.MaxHealth, (M*100) * (1+Attribute(VIT)*0.008) * MultiplyStatus.get(StatusParameter.MaxHealth));
        BaseStatus.put(StatusParameter.HealthRegen, (M*2) * (1+Attribute(VIT)*0.002) * MultiplyStatus.get(StatusParameter.HealthRegen));
        BaseStatus.put(StatusParameter.MaxMana, (M*100) * (1+Attribute(SPI)*0.008) * MultiplyStatus.get(StatusParameter.MaxMana));
        BaseStatus.put(StatusParameter.ManaRegen, (M*5) * (1+Attribute(SPI)*0.006) * MultiplyStatus.get(StatusParameter.ManaRegen));
        BaseStatus.put(StatusParameter.ATK, (M*10) * (1+Attribute(STR)*0.005+Attribute.get(INT)*0.005) * MultiplyStatus.get(StatusParameter.ATK));
        BaseStatus.put(StatusParameter.DEF, (M*5) * (1+Attribute(VIT)*0.005) * MultiplyStatus.get(StatusParameter.DEF));
        BaseStatus.put(StatusParameter.HLP, (M*5) * (1+Attribute(SPI)*0.005) * MultiplyStatus.get(StatusParameter.HLP));
        BaseStatus.put(StatusParameter.ACC, (M*10) * (1+Attribute(TEC)*0.008) * MultiplyStatus.get(StatusParameter.ACC));
        BaseStatus.put(StatusParameter.EVA, (M*5) * (1+Attribute(DEX)*0.008) * MultiplyStatus.get(StatusParameter.EVA));
        BaseStatus.put(StatusParameter.CriticalRate, (M*10) * (1+Attribute(TEC)*0.01) * MultiplyStatus.get(StatusParameter.CriticalRate));
        BaseStatus.put(StatusParameter.CriticalResist, (M*3) * (1+Attribute(SPI)*0.02) * MultiplyStatus.get(StatusParameter.CriticalResist));

        MaxHealth = BaseStatus.get(StatusParameter.MaxHealth) + EquipStatus.get(StatusParameter.MaxHealth);
        HealthRegen = BaseStatus.get(StatusParameter.HealthRegen) + EquipStatus.get(StatusParameter.HealthRegen);
        MaxMana = BaseStatus.get(StatusParameter.MaxMana) + EquipStatus.get(StatusParameter.MaxMana);
        ManaRegen = BaseStatus.get(StatusParameter.ManaRegen) + EquipStatus.get(StatusParameter.ManaRegen);
        ATK = BaseStatus.get(StatusParameter.ATK) + EquipStatus.get(StatusParameter.ATK);
        DEF = BaseStatus.get(StatusParameter.DEF) + EquipStatus.get(StatusParameter.DEF);
        HLP = BaseStatus.get(StatusParameter.HLP) + EquipStatus.get(StatusParameter.HLP);
        ACC = BaseStatus.get(StatusParameter.ACC) + EquipStatus.get(StatusParameter.ACC);
        EVA = BaseStatus.get(StatusParameter.EVA) + EquipStatus.get(StatusParameter.EVA);
        CriticalRate = BaseStatus.get(StatusParameter.CriticalRate) + EquipStatus.get(StatusParameter.CriticalRate);
        CriticalResist = BaseStatus.get(StatusParameter.CriticalResist) + EquipStatus.get(StatusParameter.CriticalResist);
        SkillCastTime = EquipStatus.get(StatusParameter.SkillCastTime);
        SkillRigidTime = EquipStatus.get(StatusParameter.SkillRigidTime);
        SkillCooltime = EquipStatus.get(StatusParameter.SkillCooltime);

        player.setPlayerListName("§b§l[" + playerData.Classes.topClass().Nick + "§b§l] §f§l" + playerData.Nick);

        player.setWalkSpeed(0.24f);
    }

    public BukkitTask tickUpdateTask;
    private final List<String> ScoreKey = new ArrayList<>();
    public void tickUpdate() {
        player.setHealthScaled(true);
        player.setGlowing(false);
        if (playerData.PlayMode) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebarObject = board.registerNewObjective("Sidebar", "dummy", decoText("§bSword of Magic Ⅶ"));
        sidebarObject.setDisplaySlot(DisplaySlot.SIDEBAR);
        Team team = board.registerNewTeam(player.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        team.setCanSeeFriendlyInvisibles(true);
        if (tickUpdateTask != null) tickUpdateTask.cancel();
        tickUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) this.cancel();
                if (playerData.PlayMode) {
                    Classes classes = playerData.Classes;
                    ClassData topClass = classes.topClass();
                    int Level = classes.getLevel(topClass);
                    int Exp = classes.getExp(topClass);
                    int Tier = topClass.Tier;
                    int ReqExp = classes.ReqExp(Level, Tier);
                    float ExpPercent = (float) Exp / ReqExp * 100;
                    if (Float.isNaN(ExpPercent)) ExpPercent = 1f;
                    float ExpPercentBar = ExpPercent / 100;
                    if (ExpPercentBar < 0.01) ExpPercentBar = 0.01f;
                    if (ExpPercentBar > 0.99) ExpPercentBar = 0.99f;
                    player.setLevel(Level);
                    player.setExp(ExpPercentBar);
                    player.sendActionBar("§6§l《" + topClass.Color + "§l" + topClass.Display + " §e§lLv" + Level + "§6§l》" +
                            "§c§l《Health: " + (int) Math.round(Health) + "/" + (int) Math.round(MaxHealth) + "》" +
                            "§b§l《Mana: " + (int) Math.round(Mana) + "/" + (int) Math.round(MaxMana) + "》" +
                            "§a§l《Exp: " + String.format("%.3f", ExpPercent) + "%》"
                    );

                    for (String scoreName : ScoreKey) {
                        board.resetScores(scoreName);
                    }
                    ScoreKey.clear();
                    ScoreKey.add(decoLore("メル") + playerData.Mel);
                    if (TagGame.isPlayer(player)) {
                        ScoreKey.add(decoText("鬼ごっこ"));
                        ScoreKey.addAll(List.of(TagGame.info()));
                    }
                    ScoreKey.add(decoText("スキルクールタイム"));
                    for (SkillData skillData : playerData.Classes.getActiveSkillList()) {
                        int cooltime = playerData.Skill.getSkillCoolTime(skillData);
                        if (cooltime > 0) {
                            ScoreKey.add(decoLore(skillData.Display) + String.format("%.1f", cooltime / 20f) + "秒");
                        }
                    }
                    int i = 15;
                    for (String scoreName : ScoreKey) {
                        Score sidebarScore = sidebarObject.getScore(scoreName);
                        sidebarScore.setScore(i);
                        i--;
                        for (Player player : PlayerList.get()) {
                            if (!team.hasEntry(player.getName())) {
                                team.addEntry(player.getName());
                            }
                        }
                        if (i < 1) break;
                    }
                    player.setScoreboard(board);

                    Health += HealthRegen / 20;
                    Mana += ManaRegen / 20;

                    Bukkit.getScheduler().runTask(swordofmagic7.System.plugin, () -> {
                        if (Health > MaxHealth) Health = MaxHealth;
                        if (Mana > MaxMana) Mana = MaxMana;
                        double ManaPercent = Mana / MaxMana;
                        player.setAbsorptionAmount(0);
                        player.setMaxHealth(MaxHealth);
                        player.setHealth(Health);
                        player.setFoodLevel((int) Math.ceil(ManaPercent * 20));
                        player.removePotionEffect(PotionEffectType.JUMP);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 19, 0, false, false));
                    });

                    playerData.HotBar.UpdateHotBar();
                }
            }
        }.runTaskTimerAsynchronously(System.plugin, 0, 10);
        BTTSet(tickUpdateTask, "StatusUpdate:" + player.getName());
    }
}


