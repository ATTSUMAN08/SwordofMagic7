package swordofmagic7;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.SomCore.random;
import static swordofmagic7.SomCore.spawnPlayer;

public class TagGame {
    public static final String Prefix = "§c[鬼ごっこ]§r ";
    public static List<Player> Tag = new ArrayList<>();
    public static Player Master;
    public static int tagTime = 0;
    public static final int startTime = 45;
    public static List<Player> Players = new ArrayList<>();
    public static String PlayingTagGameNonMessage = "§c鬼ごっこ§a中は使用できません";
    public static List<Player> Joined = new ArrayList<>();
    public static BukkitTask task;
    public static boolean isStart = false;

    public static boolean isTagPlayerNonMessage(Player player) {
        if (Players.contains(player)) sendMessage(player, PlayingTagGameNonMessage);
        return Players.contains(player);
    }

    public static void message(String message) {
        for (Player temp : Joined) {
            if (temp.isOnline()) {
                temp.sendMessage(Prefix + message);
                Dungeon.Message(new HashSet<>(Joined), message, "", null, SoundList.Tick);
            }
        }
    }

    public static void resetTagGame() {
        message("§eゲーム§aを§bリセット§aします...");
        for (Player player : Players) {
            if (player.isOnline()) {
                player.getInventory().setHelmet(null);
            }
        }
        Joined.clear();
        Players.clear();
        Tag.clear();
        isStart = false;
        if (task != null) task.cancel();
    }

    public static void startTagGame() {
        if (task != null) task.cancel();
        MultiThread.TaskRun(() -> {
            for (Player player : Players) {
                spawnPlayer(player);
            }
            MultiThread.sleepTick(10);
            message("§e5秒後§aに§eゲーム§aを§b開始§aします");
            MultiThread.sleepTick(100);
            isStart = true;
            tagCheck();
            message("§eTagGame Start !");
            task = MultiThread.TaskRunTimer(() -> {
                if (Players.size() == 1) {
                    message(Players.get(0).getDisplayName() + "§aさんが§b勝利§aしました");
                    MultiThread.TaskRunLater(TagGame::resetTagGame, 100, "resetTagGame");
                    task.cancel();
                }
                if (Tag.size() > 0) {
                    tagTime--;
                    List<Player> players = new ArrayList<>(Players);
                    players.removeAll(Tag);
                    for (Player player : players) {
                        player.setGlowing(true);
                        player.getInventory().setHelmet(null);
                    }
                    for (Player player : Tag) {
                        player.setGlowing(false);
                        player.getInventory().setHelmet(new ItemStack(Material.TNT));
                    }
                    if (tagTime <= 0) {
                        for (Player player : Tag) {
                            message(player.getDisplayName() + "§aさんが§c脱落§aしました");
                            player.getInventory().setHelmet(null);
                        }
                        tagTime = startTime;
                        Players.removeAll(Tag);
                        Tag.clear();
                        tagCheck();
                    }
                }
            }, 20);
        }, "TagGameStart");
    }

    public static void join(Player player) {
        if (isStart) {
            sendMessage(player, "§aすでに§eゲーム§aが開始されています");
            return;
        }
        if (Joined.contains(player)) {
            sendMessage(player, "§aすでに§e参加履歴§aがあります");
            return;
        }
        if (!isPlayer(player)) {
            Players.add(player);
            message(player.getDisplayName() + "§aさんが参加しました");
            Joined.add(player);
            tagCheck();
        } else {
            player.sendMessage(Prefix + "§a既に参加しています");
        }
    }

    public static void leave(Player player) {
        if (isPlayer(player)) {
            player.setGlowing(false);
            message(player.getDisplayName() + "§aさんがやめました");
            Players.remove(player);
            tagCheck();
        } else {
            player.sendMessage(Prefix + "§a参加していません");
        }
    }

    public static void kick(Player player) {
        if (isPlayer(player)) {
            player.setGlowing(false);
            message(player.getDisplayName() + "§aさんが§c強制退場§aさせられました");
            Players.remove(player);
            tagCheck();
        }
    }

    static void tagCheck() {
        int reqTag = Math.round(Players.size()/4f)-Tag.size();
        for (int i = 0; i < reqTag; i++) {
            List<Player> list = new ArrayList<>(Players);
            list.removeAll(Tag);
            tagChange(null, list.get(random.nextInt(list.size())));
        }
    }

    static void tagChange(Player attacker, Player victim) {
        if (!isStart) return;
        if (attacker != null) {
            if (Tag.contains(attacker) && isPlayer(victim)) {
                EffectManager attackerEffect = playerData(attacker).EffectManager;
                EffectManager victimEffect = playerData(victim).EffectManager;
                if (attackerEffect.hasEffect(EffectType.Stun)) return;
                Tag.remove(attacker);
                Tag.add(victim);
                attacker.setGlowing(true);
                victim.setGlowing(false);
                victimEffect.addEffect(EffectType.Stun, 60);
                victimEffect.addEffect(EffectType.Blind, 60);
                message(victim.getDisplayName() + "§aさんが§4鬼§aになりました");
            }
        } else {
            tagTime = startTime;
            Tag.add(victim);
            victim.setGlowing(false);
            message(victim.getDisplayName() + "§aさんが§4鬼§aになりました");
        }
    }

    public static String[] info() {
        String[] str = new String[3];
        str[0] = "§7・§e§l次の脱落まで§7: §a§l" + tagTime + "秒 (" + startTime + ")";
        str[1] = "§7・§e§l残り人数§7: §a§l" + (Players.size()-Tag.size()) + "§8(" + Players.size() + ")";
        str[2] = "§7・§e§l鬼の人数§7: §a§l" + Tag.size();
        return str;
    }

    public static boolean isPlayer(Player player) {
        return Players.contains(player);
    }

    public static boolean isJoinedPlayer(Player player) {
        return Joined.contains(player);
    }
}
