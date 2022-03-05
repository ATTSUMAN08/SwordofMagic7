package swordofmagic7.ViewBar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.PlayerList;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Status.Status;
import swordofmagic7.System;
import swordofmagic7.TagGame;
import swordofmagic7.ViewBar.SideBarToDo.SideBarToDoData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.System.BTTSet;

public class ViewBar {

    private final Player player;
    private final PlayerData playerData;
    private final Status status;

    public HashMap<String, List<String>> SideBar = new HashMap<>();

    public ViewBar(Player player, PlayerData playerData, Status status) {
        this.player = player;
        this.playerData = playerData;
        this.status = status;
    }

    public void setSideBar(String key, String data) {
        List<String> list = new ArrayList<>();
        list.add(data);
        SideBar.put(key, list);
    }

    public void setSideBar(String key, List<String> data) {
        SideBar.put(key, data);
    }

    public void resetSideBar(String key) {
        SideBar.remove(key);
    }

    public BukkitTask tickUpdateTask;
    public Team team;
    public double HealthPercent = 1;
    public String HealthPercentColor = "§a§l";
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
        team = board.registerNewTeam(player.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.setCanSeeFriendlyInvisibles(true);
        if (tickUpdateTask != null) tickUpdateTask.cancel();
        tickUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) this.cancel();
                if (playerData.PlayMode) {
                    playerData.RefreshHologram();
                    int Level = playerData.Level;
                    int Exp = playerData.Exp;
                    int ReqExp = Classes.ReqExp(Level);
                    float ExpPercent = (float) Exp / ReqExp * 100;
                    if (Float.isNaN(ExpPercent)) ExpPercent = 1f;
                    float ExpPercentBar = ExpPercent / 100;
                    if (ExpPercentBar < 0.01) ExpPercentBar = 0.01f;
                    if (ExpPercentBar > 0.99) ExpPercentBar = 0.99f;
                    HealthPercent = status.Health/status.MaxHealth;
                    if (HealthPercent < 0.2) HealthPercentColor = "§c§l";
                    else if (HealthPercent < 0.5) HealthPercentColor = "§e§l";
                    else HealthPercentColor = "§a§l";
                    player.setLevel(Level);
                    player.setExp(ExpPercentBar);
                    player.sendActionBar("§6§l《§e§l" + playerData.getNick(true) + " Lv" + Level + "§6§l》" +
                            "§c§l《Health: " + (int) Math.round(status.Health) + "/" + (int) Math.round(status.MaxHealth) + "》" +
                            "§b§l《Mana: " + (int) Math.round(status.Mana) + "/" + (int) Math.round(status.MaxMana) + "》" +
                            "§a§l《Exp: " + String.format("%.3f", ExpPercent) + "%》"
                    );

                    for (String scoreName : ScoreKey) {
                        board.resetScores(scoreName);
                    }
                    ScoreKey.clear();
                    ScoreKey.add(decoLore("メル") + playerData.Mel);
                    playerData.SideBarToDo.refresh();
                    if (playerData.Party != null) {
                        List<String> data = new ArrayList<>();
                        data.add(decoText("パーティメンバー"));
                        for (Player member : playerData.Party.Members) {
                            PlayerData memberData = playerData(member);
                            data.add(decoLore(memberData.getNick(true)) + memberData.ViewBar.HealthPercentColor + String.format("%.0f", memberData.ViewBar.HealthPercent*100) + "%");
                        }
                        setSideBar("Party", data);
                    } else resetSideBar("Party");
                    if (TagGame.isPlayer(player)) {
                        List<String> data = new ArrayList<>();
                        data.add(decoText("鬼ごっこ"));
                        data.addAll(List.of(TagGame.info()));
                        setSideBar("TagGame", data);
                    }
                    List<String> SkillCoolTime = new ArrayList<>();
                    for (SkillData skillData : playerData.Classes.getActiveSkillList()) {
                        int cooltime = playerData.Skill.getSkillCoolTime(skillData);
                        if (cooltime > 0) {
                            SkillCoolTime.add(decoLore(skillData.Display) + String.format("%.1f", cooltime / 20f) + "秒");
                        }
                    }
                    if (SkillCoolTime.size() > 0) {
                        List<String> data = new ArrayList<>();
                        data.add(decoText("スキルクールタイム"));
                        data.addAll(SkillCoolTime);
                        setSideBar("SkillCoolTime", data);
                    } else {
                        resetSideBar("SkillCoolTime");
                    }
                    List<String> EffectList = new ArrayList<>();
                    for (Map.Entry<EffectType, EffectData> effect : playerData.EffectManager.Effect.entrySet()) {
                        EffectList.add(decoLore(effect.getKey().Display) + String.format("%.1f", effect.getValue().time / 20f) + "秒");
                    }
                    if (EffectList.size() > 0) {
                        List<String> data = new ArrayList<>();
                        data.add(decoText("バフ・デバフ"));
                        data.addAll(EffectList);
                        setSideBar("EffectList", data);
                    } else {
                        resetSideBar("EffectList");
                    }
                    for (List<String> textList : SideBar.values()) {
                        ScoreKey.addAll(textList);
                    }
                    int i = 15;
                    for (String scoreName : ScoreKey) {
                        Score sidebarScore = sidebarObject.getScore(scoreName);
                        sidebarScore.setScore(i);
                        i--;
                        if (i < 1) break;
                    }
                    for (Player player : PlayerList.get()) {
                        if (!team.hasEntry(player.getName())) {
                            team.addEntry(player.getName());
                        }
                    }
                    player.setScoreboard(board);

                    status.Health += status.HealthRegen / 20;
                    status.Mana += status.ManaRegen / 20;

                    Bukkit.getScheduler().runTask(swordofmagic7.System.plugin, () -> {
                        status.Health = Math.min(Math.max(status.Health, 0), status.MaxHealth);
                        status.Mana = Math.min(Math.max(status.Mana, 0), status.MaxMana);
                        double ManaPercent = status.Mana / status.MaxMana;
                        player.setAbsorptionAmount(0);
                        player.setMaxHealth(status.MaxHealth);
                        player.setHealth(status.Health);
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
