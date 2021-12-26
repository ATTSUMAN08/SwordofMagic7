package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.AttributeType.*;
import static swordofmagic7.DataBase.getClassList;
import static swordofmagic7.DataBase.getSkillData;
import static swordofmagic7.Function.*;

public class Status {
    private final Player player;
    private final PlayerData playerData;
    private final Classes classes;
    private final Skill skill;
    Status(Player player, PlayerData playerData, Classes classes, Skill skill) {
        this.player = player;
        this.playerData = playerData;
        this.classes = classes;
        this.skill = skill;
    }

    double MaxHealth;
    double HealthRegen;
    double Health;
    double MaxMana;
    double ManaRegen;
    double Mana;
    double ATK;
    double DEF;
    double ACC;
    double EVA;
    double CriticalRate;
    double CriticalResist;
    double SkillCastTime;
    double SkillRigidTime;
    double SkillCooltime;

    HashMap<StatusParameter, Double> BaseStatus = new HashMap<>();
    HashMap<AttributeType, Integer> Attribute = new HashMap<>();
    HashMap<StatusParameter, Double> EquipStatus = new HashMap<>();
    HashMap<StatusParameter, Double> MultiplyStatus = new HashMap<>();

    double BaseStatus(StatusParameter param) {
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

    double getCombatPower() {
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

    void StatusUpdate() {
        for (StatusParameter param : StatusParameter.values()) {
            EquipStatus.put(param, 0d);
            MultiplyStatus.put(param, 1d);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (StatusParameter param : StatusParameter.values()) {
                EquipStatus.put(param, EquipStatus.get(param) + playerData.Equipment.getEquip(slot).Parameter().get(param));
            }
        }
        for (AttributeType attr: AttributeType.values()) {
            Attribute.put(attr, playerData.Attribute.getAttribute(attr));
        }
        String skillName;
        skillName = "BasicTraining";
        if (skill.hasSkill(skillName)) {
            MultiplyStatus.put(StatusParameter.ATK, MultiplyStatus.get(StatusParameter.ATK)+getSkillData(skillName).Parameter.get(0).Value/100);
            MultiplyStatus.put(StatusParameter.DEF, MultiplyStatus.get(StatusParameter.DEF)+getSkillData(skillName).Parameter.get(1).Value/100);
        }
        double M = LevelMultiply();
        BaseStatus.put(StatusParameter.MaxHealth, (M*100) * (1+Attribute(VIT)*0.008) * MultiplyStatus.get(StatusParameter.MaxHealth));
        BaseStatus.put(StatusParameter.HealthRegen, (M*2) * (1+Attribute(VIT)*0.002) * MultiplyStatus.get(StatusParameter.HealthRegen));
        BaseStatus.put(StatusParameter.MaxMana, (M*100) * (1+Attribute(SPI)*0.008) * MultiplyStatus.get(StatusParameter.MaxMana));
        BaseStatus.put(StatusParameter.ManaRegen, (M*5) * (1+Attribute(SPI)*0.006) * MultiplyStatus.get(StatusParameter.ManaRegen));
        BaseStatus.put(StatusParameter.ATK, (M*10) * (1+Attribute(STR)*0.005+Attribute.get(INT)*0.005) * MultiplyStatus.get(StatusParameter.ATK));
        BaseStatus.put(StatusParameter.DEF, (M*3) * (1+Attribute(VIT)*0.005+Attribute.get(SPI)*0.002) * MultiplyStatus.get(StatusParameter.DEF));
        BaseStatus.put(StatusParameter.ACC, (M*10) * (1+Attribute(TEC)*0.008) * MultiplyStatus.get(StatusParameter.ACC));
        BaseStatus.put(StatusParameter.EVA, (M*3) * (1+Attribute(DEX)*0.008) * MultiplyStatus.get(StatusParameter.EVA));
        BaseStatus.put(StatusParameter.CriticalRate, (M*10) * (1+Attribute(TEC)*0.01) * MultiplyStatus.get(StatusParameter.CriticalRate));
        BaseStatus.put(StatusParameter.CriticalResist, (M*3) * (1+Attribute(SPI)*0.02) * MultiplyStatus.get(StatusParameter.CriticalResist));

        MaxHealth = BaseStatus.get(StatusParameter.MaxHealth) + EquipStatus.get(StatusParameter.MaxHealth);
        HealthRegen = BaseStatus.get(StatusParameter.HealthRegen) * MultiplyStatus.get(StatusParameter.HealthRegen) + EquipStatus.get(StatusParameter.HealthRegen);
        MaxMana = BaseStatus.get(StatusParameter.MaxMana) * MultiplyStatus.get(StatusParameter.MaxMana) + EquipStatus.get(StatusParameter.MaxMana);
        ManaRegen = BaseStatus.get(StatusParameter.ManaRegen) * MultiplyStatus.get(StatusParameter.ManaRegen) + EquipStatus.get(StatusParameter.ManaRegen);
        ATK = BaseStatus.get(StatusParameter.ATK) * MultiplyStatus.get(StatusParameter.ATK) + EquipStatus.get(StatusParameter.ATK);
        DEF = BaseStatus.get(StatusParameter.DEF) * MultiplyStatus.get(StatusParameter.DEF) + EquipStatus.get(StatusParameter.DEF);
        ACC = BaseStatus.get(StatusParameter.ACC) * MultiplyStatus.get(StatusParameter.ACC) + EquipStatus.get(StatusParameter.ACC);
        EVA = BaseStatus.get(StatusParameter.EVA) * MultiplyStatus.get(StatusParameter.ATK) + EquipStatus.get(StatusParameter.EVA);
        CriticalRate = BaseStatus.get(StatusParameter.CriticalRate) * MultiplyStatus.get(StatusParameter.CriticalRate) + EquipStatus.get(StatusParameter.CriticalRate);
        CriticalResist = BaseStatus.get(StatusParameter.CriticalResist) * MultiplyStatus.get(StatusParameter.CriticalResist) + EquipStatus.get(StatusParameter.CriticalResist);
        SkillCastTime = EquipStatus.get(StatusParameter.SkillCastTime);
        SkillRigidTime = EquipStatus.get(StatusParameter.SkillRigidTime);
        SkillCooltime = EquipStatus.get(StatusParameter.SkillCooltime);

        player.setPlayerListName("§b§l[" + playerData.Classes.topClass().Nick + "§b§l] §f§l" + playerData.Nick);

        player.setWalkSpeed(0.24f);
    }

    BukkitTask tickUpdateTask;
    private final List<String> ScoreKey = new ArrayList<>();
    void tickUpdate() {
        player.setHealthScaled(true);
        player.setGlowing(false);
        player.setFlying(false);
        player.setAllowFlight(false);
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
                    player.sendActionBar("§6§l《" + topClass.Display + " Lv" + Level + "§6§l》" +
                            "§c§l《Health: " + (int) Math.round(Health) + "/" + (int) Math.round(MaxHealth) + "》" +
                            "§b§l《Mana: " + (int) Math.round(Mana) + "/" + (int) Math.round(MaxMana) + "》" +
                            "§a§l《Exp: " + String.format("%.3f", ExpPercent) + "%》"
                    );

                    for (String scoreName : ScoreKey) {
                        board.resetScores(scoreName);
                    }
                    ScoreKey.clear();
                    ScoreKey.add(decoLore("メル") + playerData.Mel);
                    if (System.tagGame.isPlayer(player)) {
                        ScoreKey.add(decoText("鬼ごっこ"));
                        ScoreKey.addAll(List.of(System.tagGame.info()));
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

                    if (Health > MaxHealth) Health = MaxHealth;
                    if (Mana > MaxMana) Mana = MaxMana;

                    double ManaPercent = Mana / MaxMana;

                    Bukkit.getScheduler().runTask(System.plugin, () -> {
                        player.setAbsorptionAmount(0);
                        player.setMaxHealth(MaxHealth);
                        player.setHealth(Health);
                        player.setFoodLevel((int) Math.ceil(ManaPercent * 20));
                        player.removePotionEffect(PotionEffectType.JUMP);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 19, 0, false, false));
                    });
                }
            }
        }.runTaskTimerAsynchronously(System.plugin, 0, 10);
    }
}

enum StatusParameter {
    MaxHealth("最大体力"),
    HealthRegen("体力回復"),
    MaxMana("最大マナ"),
    ManaRegen("マナ回復"),
    ATK("攻撃力"),
    DEF("防御力"),
    ACC("命中"),
    EVA("回避"),
    CriticalRate("クリティカル発生"),
    CriticalResist("クリティカル耐性"),
    SkillCooltime("スキル再使用時間"),
    SkillCastTime("スキル詠唱時間"),
    SkillRigidTime("スキル硬直時間"),
    ;

    String Display;
    String DecoDisplay;

    StatusParameter(String Display) {
        this.Display = Display;
        this.DecoDisplay = decoLore(Display);
    }
}
