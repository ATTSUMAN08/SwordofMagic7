package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import swordofmagic7.Effect.EffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static swordofmagic7.Data.PlayerData.playerData;

public class TagGame {
    public final String Prefix = "§c[鬼ごっこ]§r ";
    public Player Tag;
    public int tagTime = 0;
    public List<Player> Players = new ArrayList<>();
    public HashMap<Player, Boolean> Stun = new HashMap<>();

    TagGame() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(System.plugin, () -> {
            if (Tag != null) tagTime++;
            for (Player player : Players) {
                player.sendTitle("", "§c現在の鬼: §a" + Tag.getDisplayName(), 0, 21, 0);
            }
        }, 0, 20);
    }

    public void join(Player player) {
        if (!isPlayer(player)) {
            Players.add(player);
            player.setGlowing(true);
            for (Player temp : Players) {
                temp.sendMessage(Prefix + player.getDisplayName() + "§aさんが参加しました");
            }
            tagCheck();
        } else {
            player.sendMessage(Prefix + "§a既に参加しています");
        }
    }

    public void leave(Player player) {
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

    void tagCheck() {
        if (Players.size() > 0 && !isPlayer(Tag)) {
            int i = 0;
            if (Players.size() > 2) {
                i = new Random().nextInt(Players.size() - 1);
            }
            tagChange(null, Players.get(i));
        }
    }

    void tagChange(Player attacker, Player victim) {
        if (attacker == null) {
            Tag = victim;
        } else if (isPlayer(attacker) && isPlayer(victim) && Tag == attacker) {
            if (playerData(attacker).EffectManager.hasEffect(EffectType.Stun)) return;
            Tag = victim;
            victim.setGlowing(false);
            attacker.setGlowing(true);
            Stun.put(victim, true);
            Tag.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false));
            Tag.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
            Bukkit.getScheduler().runTaskLater(System.plugin, () -> {
                Stun.put(victim, false);
            }, 40);
            for (Player temp : Players) {
                temp.sendMessage(Prefix + victim.getDisplayName() + "§aさんが§4鬼§aになりました");
            }
            tagTime = 0;
        }
    }

    public String[] info() {
        String[] str = new String[3];
        String Nick = "なし";
        if (Tag != null) Nick = Tag.getName();
        str[0] = "§7・§e§l現在の鬼§7: §a§l" + Nick;
        str[1] = "§7・§e§l鬼の時間§7: §a§l" + tagTime + "秒";
        str[2] = "§7・§e§l参加人数§7: §a§l" + Players.size();
        return str;
    }

    public boolean isPlayer(Player player) {
        return Players.contains(player);
    }
}
