package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Function.*;

public class Status {
    private final Player player;
    private final PlayerData playerData;
    Status(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    double MaxHealth = 100;
    double Health = 100;
    double HealthRegen = 0.1;
    double MaxMana = 100;
    double Mana = 100;
    double ManaRegen = 3;
    double ATK = 0;
    double DEF = 0;
    double ACC = 0;
    double EVA = 0;

    void StatusUpdate() {
        HashMap<StatusParameter, Double> equipStatus = new HashMap<>();
        for (StatusParameter param : StatusParameter.values()) {
            equipStatus.put(param, 0d);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (StatusParameter param : StatusParameter.values()) {
                equipStatus.put(param, equipStatus.get(param) + playerData.Equipment.getEquip(slot).Parameter().get(param));
            }
        }
        MaxHealth = 100 + equipStatus.get(StatusParameter.MaxHealth);
        HealthRegen = 0.1 + equipStatus.get(StatusParameter.HealthRegen);
        MaxMana = 100 + equipStatus.get(StatusParameter.MaxMana);
        ManaRegen = 3 + equipStatus.get(StatusParameter.ManaRegen);
        ATK = equipStatus.get(StatusParameter.ATK);
        DEF = equipStatus.get(StatusParameter.DEF);
        ACC = 10 + equipStatus.get(StatusParameter.ACC);
        EVA = 2 + equipStatus.get(StatusParameter.EVA);

        player.setPlayerListName(colored("&b&l[" + playerData.Classes.topClass().Nick + "&b&l] &f&l" + playerData.Nick));
    }

    BukkitTask tickUpdateTask;
    private final List<String> ScoreKey = new ArrayList<>();
    void tickUpdate() {
        player.setHealthScaled(true);
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

                Classes classes = playerData.Classes;
                ClassData topClass = classes.topClass();
                int Level = classes.getLevel(topClass);
                int Exp = classes.getExp(topClass);
                int Tier = topClass.Tier;
                int ReqExp = classes.ReqExp(Level, Tier);
                float ExpPercent = (float) Exp / ReqExp * 100;
                float ExpPercentBar = ExpPercent/100;
                if (ExpPercentBar < 0.01) ExpPercentBar = 0.01f;
                player.setLevel(Level);
                player.setExp(ExpPercentBar);
                player.sendActionBar(colored("&6&l《" + topClass.Display + " Lv" + Level + "&6&l》" +
                        "&c&l《Health: " + (int) Math.round(Health) + "/" + (int) Math.round(MaxHealth) + "》" +
                        "&b&l《Mana: " + (int) Math.round(Mana) + "/" + (int) Math.round(MaxMana) + "》" +
                        "&a&l《Exp: " + String.format("%.3f", ExpPercent) + "%》"
                ));

                for (String scoreName : ScoreKey) {
                    board.resetScores(scoreName);
                }
                ScoreKey.clear();
                ScoreKey.add(colored(decoLore("メル") + playerData.Mel));
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

                Health += HealthRegen/20;
                Mana += ManaRegen/20;

                if (Health > MaxHealth) Health = MaxHealth;
                if (Mana > MaxMana) Mana = MaxMana;

                double ManaPercent = Mana / MaxMana;

                Bukkit.getScheduler().runTask(System.plugin, () -> {
                    player.setMaxHealth(MaxHealth);
                    player.setHealth(Health);
                    player.setFoodLevel((int) Math.ceil(ManaPercent * 20));
                });
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
