package swordofmagic7;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.MultiThread.MultiThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.SomCore.plugin;

public class TagGame {
    public static final String Prefix = "§c[鬼ごっこ]§r ";
    public static Player Tag;
    public static int tagTime = 0;
    public static List<Player> Players = new ArrayList<>();
    public static HashMap<Player, Boolean> Stun = new HashMap<>();

    TagGame() {
        MultiThread.TaskRun(() -> {
            while (plugin.isEnabled()) {
                if (Tag != null) {
                    tagTime++;
                    if (tagTime >= 120) {
                        for (Player temp : Players) {
                            temp.sendMessage(Prefix + Tag.getDisplayName() + "§aさんが§c脱落§aしました");
                        }
                        Players.remove(Tag);
                        Tag = null;
                        tagCheck();
                    }
                }
                MultiThread.sleepTick(20);
            }
        }, "TagGame");
    }

    public static void join(Player player) {
        if (!isPlayer(player)) {
            Players.add(player);
            if (Tag != null) {
                player.setGlowing(true);
            }
            for (Player temp : Players) {
                temp.sendMessage(Prefix + player.getDisplayName() + "§aさんが参加しました");
            }
            tagCheck();
        } else {
            player.sendMessage(Prefix + "§a既に参加しています");
        }
    }

    public static void leave(Player player) {
        if (isPlayer(player)) {
            player.setGlowing(false);
            if (Tag == null) Tag = player;
            for (Player temp : Players) {
                temp.sendMessage(Prefix + player.getDisplayName() + "§aさんがやめました");
            }
            Players.remove(player);
            tagCheck();
        } else {
            player.sendMessage(Prefix + "§a参加していません");
        }
    }

    static void tagCheck() {
        if (Players.size() > 0 && !isPlayer(Tag)) {
            int i = 0;
            if (Players.size() > 2) {
                i = new Random().nextInt(Players.size() - 1);
            }
            tagChange(null, Players.get(i));
        }
    }

    static void tagChange(Player attacker, Player victim) {
        if (attacker != null) {
            if (isPlayer(attacker) && isPlayer(victim) && Tag == attacker) {
                EffectManager attackerEffect = playerData(attacker).EffectManager;
                EffectManager victimEffect = playerData(victim).EffectManager;
                if (attackerEffect.hasEffect(EffectType.Stun)) return;
                tagTime = 0;
                Tag = victim;
                attacker.setGlowing(true);
                victim.setGlowing(false);
                victimEffect.addEffect(EffectType.Stun, 60);
                Tag.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 9, false, false));
                Tag.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false));
                for (Player temp : Players) {
                    temp.sendMessage(Prefix + victim.getDisplayName() + "§aさんが§4鬼§aになりました");
                }
            }
        } else {
            tagTime = 0;
            Tag = victim;
            victim.setGlowing(false);
        }
    }

    public static String[] info() {
        String[] str = new String[3];
        String Nick = "なし";
        if (Tag != null) Nick = Tag.getName();
        str[0] = "§7・§e§l現在の鬼§7: §a§l" + Nick;
        str[1] = "§7・§e§l鬼の時間§7: §a§l" + tagTime + "秒 (120)";
        str[2] = "§7・§e§l参加人数§7: §a§l" + Players.size();
        return str;
    }

    public static boolean isPlayer(Player player) {
        return Players.contains(player);
    }
}
