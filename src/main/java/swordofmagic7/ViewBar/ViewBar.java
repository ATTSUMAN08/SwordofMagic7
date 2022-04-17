package swordofmagic7.ViewBar;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Status.Status;
import swordofmagic7.TagGame;

import java.util.*;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.SomCore.plugin;

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

    public static void setSideBar(Set<Player> players, String key, List<String> data) {
        for (Player player : players) {
            if (player.isOnline()) {
                playerData(player).ViewBar.setSideBar(key, data);
            }
        }
    }

    public static void resetSideBar(Set<Player> players, String key) {
        for (Player player : players) {
            if (player.isOnline()) {
                playerData(player).ViewBar.resetSideBar(key);
            }
        }
    }

    public static void setBossBarTimer(Set<Player> players, String text, float progress) {
        for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                playerData.BossBarTimer.name(Component.text(text));
                playerData.BossBarTimer.progress(Math.min(Math.max(progress, 0), 1));
                player.showBossBar(playerData.BossBarTimer);
            }
        }
    }

    public static void resetBossBarTimer(Set<Player> players) {
        for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                player.hideBossBar(playerData.BossBarTimer);
            }
        }
    }

    public static void setBossBarOther(Set<Player> players, String text, float progress) {
        for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                playerData.BossBarOther.name(Component.text(text));
                playerData.BossBarOther.progress(Math.min(Math.max(progress, 0), 1));
                player.showBossBar(playerData.BossBarOther);
            }
        }
    }

    public static void resetBossBarOther(Set<Player> players) {
        for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                player.hideBossBar(playerData.BossBarOther);
            }
        }
    }

    public static void setBossBarOverrideTargetInfo(Set<Player> players, LivingEntity entity) {
        for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                playerData.overrideTargetEntity = entity;
            }
        }
    }

    public static void resetBossBarOverrideTargetInfo(Set<Player> players) {
        for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                playerData.overrideTargetEntity = null;
            }
        }
    }

    public static void setBossBarOtherTargetInfo(Set<Player> players, LivingEntity entity) {
        if (entity != null) for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                playerData.otherTargetEntity = entity;
            }
        }
    }

    public static void resetBossBarOtherTargetInfo(Set<Player> players) {
        for (Player player : players) {
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                playerData.otherTargetEntity = null;
            }
        }
    }

    public boolean tickUpdate = false;
    public Team team;
    double HealthPercent = 1;
    double ManaPercent = 1;
    String HealthPercentColor = "§a§l";
    private final List<String> ScoreKey = new ArrayList<>();
    private Scoreboard board;
    private Objective sidebarObject;
    public void tickUpdate() {
        if (tickUpdate) return;
        tickUpdate = true;
        player.setHealthScaled(true);
        player.setGlowing(false);
        if (playerData.PlayMode) {
            player.setFlying(false);
            player.setAllowFlight(false);
            player.setGameMode(GameMode.SURVIVAL);
        }
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebarObject = board.registerNewObjective("Sidebar", "dummy", decoText("§bSword of Magic Ⅶ"));
        sidebarObject.setDisplaySlot(DisplaySlot.SIDEBAR);
        team = board.registerNewTeam(player.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        team.addEntry(player.getName());
        team.setCanSeeFriendlyInvisibles(true);
        int period = 5;
        double regen = 200d/period;
        MultiThread.TaskRun(() -> {
            if (status.Health < 0) status.Health = status.MaxHealth;
            if (status.Mana < 0) status.Mana = status.MaxMana;
            while (player.isOnline() && plugin.isEnabled()) {
                try {
                    if (playerData.PlayMode && playerData.isLoaded) {
                        if (playerData.HealthRegenDelay > 0) {
                            playerData.HealthRegenDelay -= period;
                        } else {
                            double regenTick = regen;
                            if (playerData.PvPMode) regenTick *= Damage.PvPHealDecay;
                            status.Health += status.HealthRegen / regenTick;
                            status.Mana += status.ManaRegen / regenTick;
                        }
                        int Level = playerData.Level;
                        int Exp = playerData.Exp;
                        int ReqExp = Classes.ReqExp(Level);
                        float ExpPercent = (float) Exp / ReqExp;
                        if (Float.isNaN(ExpPercent)) ExpPercent = 0.999f;
                        ExpPercent = Math.min(0.001f, Math.max(0.999f, ExpPercent));
                        status.Health = Math.min(Math.max(status.Health, 0), status.MaxHealth);
                        status.Mana = Math.min(Math.max(status.Mana, 0), status.MaxMana);
                        HealthPercent = status.Health / status.MaxHealth;
                        ManaPercent = status.Mana / status.MaxMana;
                        if (HealthPercent < 0.2) HealthPercentColor = "§c§l";
                        else if (HealthPercent < 0.5) HealthPercentColor = "§e§l";
                        else HealthPercentColor = "§a§l";
                        player.setLevel(Level);
                        player.setExp(ExpPercent);
                        player.sendActionBar("§6§l《" + playerData.getNick() + " Lv" + Level + "§6§l》" +
                                "§c§l《§cHealth: " + (int) Math.round(status.Health) + "/" + (int) Math.round(status.MaxHealth) + "§c§l》" +
                                "§b§l《§bMana: " + (int) Math.round(status.Mana) + "/" + (int) Math.round(status.MaxMana) + "§b§l》" +
                                "§a§l《§aExp: " + playerData.viewExpPercent() + "%§a§l》" +
                                "§e§l《§eDPS: " + playerData.getDPS() + "§e§l》"
                        );

                        if (playerData.visibilityManager != null && !playerData.hologram.isDeleted()) {
                            int x = (int) Math.min(20, Math.floor(HealthPercent * 20));
                            playerData.hologramLine[1].setText(HealthPercentColor + "|".repeat(Math.max(0, x)) + "§7§l" + "|".repeat(Math.max(0, 20 - x)));

                            if (!isAlive(player) || player.isSneaking() || playerData.EffectManager.hasEffect(EffectType.Cloaking) || playerData.Map.Id.equals("DefenseBattle")) {
                                if (playerData.visibilityManager.isVisibleByDefault()) {
                                    playerData.visibilityManager.setVisibleByDefault(false);
                                }
                            } else if (!playerData.visibilityManager.isVisibleByDefault()) {
                                playerData.visibilityManager.setVisibleByDefault(true);
                            }
                        }

                        ViewSideBar();
                        playerData.HotBar.UpdateHotBar();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MultiThread.sleepTick(period);
            }
        }, "TickUpdate");
        MultiThread.TaskRunSynchronizedTimer(() -> {
            player.setAbsorptionAmount(0);
            player.setMaxHealth(status.MaxHealth);
            player.setHealth(Math.min(Math.max(status.Health, 0.5), status.MaxHealth));
            player.setFoodLevel((int) Math.min(Math.max(Math.ceil(ManaPercent * 20), 0), 20));
        }, 10, "TickUpdateTimer");
    }

    public void ViewSideBar() {
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
                data.add(decoLore(memberData.getNick(true)) + memberData.ViewBar.HealthPercentColor + String.format("%.0f", memberData.ViewBar.HealthPercent * 100) + "%");
            }
            setSideBar("Party", data);
        } else resetSideBar("Party");
        if (TagGame.isPlayer(player)) {
            List<String> data = new ArrayList<>();
            data.add(decoText("鬼ごっこ"));
            data.addAll(List.of(TagGame.info()));
            setSideBar("TagGame", data);
        } else resetSideBar("TagGame");
        List<String> EffectList = new ArrayList<>();
        HashMap<EffectType, EffectData> effectMap = (HashMap<EffectType, EffectData>) playerData.EffectManager.Effect.clone();
        for (Map.Entry<EffectType, EffectData> effect : effectMap.entrySet()) {
            String amount = "";
            if (effect.getKey().view) {
                if (effect.getValue().stack > 1) amount = "[" + effect.getValue().stack + "]";
                String color = effect.getKey().Buff ? "§e§l" : "§c§l";
                EffectList.add(decoLore(color + effect.getKey().Display + amount) + String.format("%.1f", effect.getValue().time / 20f) + "秒");
            }
        }
        if (EffectList.size() > 0) {
            List<String> data = new ArrayList<>();
            data.add(decoText("バフ・デバフ"));
            data.addAll(EffectList);
            setSideBar("EffectList", data);
        } else resetSideBar("EffectList");
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
    }
}
