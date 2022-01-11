package swordofmagic7.Party;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.ItemStackPlayerHead;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Party.PartyManager.PartyInvites;
import static swordofmagic7.Party.PartyManager.PartyList;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class PartyData {
    public static final int MaxPlayer = 5;
    public static final String MaxPlayerError = "§c[パーティ人数上限]§aは§c[" + MaxPlayer + "人]§aです";
    public Player Leader;
    public List<Player> Members = new ArrayList<>();
    public String Display;
    public List<String> Lore = new ArrayList<>();
    public boolean Public = false;

    public PartyData(Player player, String Display, String Lore) {
        Leader = player;
        Members.add(player);
        this.Display = Display;
        setLore(Lore);
        player.sendMessage("§e[" + Display + "]§aを作成しました");
        playSound(player, SoundList.Click);
    }

    public void setLore(String lore) {
        for (String str : lore.split("\n")) {
            this.Lore.add("§a§l" + str);
        }
    }

    public void Join(Player player) {
        PlayerData playerData = playerData(player);
        if (playerData.Party == null) {
            if (Members.size() < MaxPlayer) {
                Members.add(player);
                Message(playerData.getNick() + "§aさんが§e[" + Display + "]§aに§b参加§aしました");
                playerData.Party = this;
                PartyInvites.remove(player);
            } else {
                player.sendMessage(MaxPlayerError);
                playSound(player, SoundList.Nope);
            }
        } else {
            player.sendMessage("§e[" + playerData.Party.Display + "]§aに参加しています");
            playSound(player, SoundList.Nope);
        }
    }

    public void Quit(Player player) {
        if (Members.contains(player)) {
            PlayerData playerData = playerData(player);
            Message(playerData.getNick() + "§aさんが§e[" + Display + "]§aから§c脱退§aしました");
            Members.remove(player);
            playerData.Party = null;
            if (Members.size() == 0) {
                PartyList.remove(Display);
                player.sendMessage("§e[" + Display + "]§aを§c解散§aしました");
            } else if (Leader == player) {
                Promote(Members.get(0));
            }
        }
    }

    public void Invite(Player player) {
        PlayerData playerData = playerData(player);
        if (!PartyInvites.containsKey(player)) {
            if (Members.size() < MaxPlayer) {
                Message(playerData.getNick() + "§aさんを§e[" + Display + "]§aに§e招待§aしました");
                TextComponent inviteMessage = new TextComponent(playerData(Leader).getNick() + "§aさんから§e[" + Display + "]§aに§e招待§aされました ");
                TextComponent accept = new TextComponent("§b[/party accept]");
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept"));
                inviteMessage.addExtra(accept);
                player.spigot().sendMessage(inviteMessage);
                playSound(player, SoundList.Tick);
                PartyInvites.put(player, this);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (PartyInvites.containsKey(player)) {
                        Message(playerData.getNick() + "§aさんへの§e招待§aが§cタイムアウト§aしました");
                        player.sendMessage("§e[" + Display + "]§aからの§e招待§aが§cタイムアウト§aしました");
                        PartyInvites.remove(player);
                    }
                }, 600);
            } else {
                Leader.sendMessage(MaxPlayerError);
                playSound(Leader, SoundList.Nope);
            }
        } else {
            Leader.sendMessage(playerData.getNick() + "§aは§e[" + PartyInvites.get(player).Display + "]§aからの§e招待§aに返答中です");
            playSound(Leader, SoundList.Nope);
        }
    }

    public void Message(String msg) {
        for (Player player : Members) {
            player.sendMessage(msg);
            playSound(player, SoundList.Tick);
        }
    }

    public void Promote(Player player) {
        if (Members.contains(player)) {
            Leader = player;
            Message(playerData(player).getNick() + "§aさんが§eリーダー§aになりました");
        }
    }

    public List<String> view() {
        List<String> info = new ArrayList<>();
        info.add(decoText(Display));
        info.addAll(Lore);
        info.add(decoText("§3§lパーティー情報"));
        if (Public) info.add(decoLore("公開設定") + "§b§l公開");
        else info.add(decoLore("公開設定") + "§c§l非公開");
        info.add(decoLore("リーダー") + playerData(Leader).getNick(true));
        info.add(decoText("§3§lパーティーメンバー"));
        for (Player member : Members) {
            PlayerData playerData  = playerData(member);
            info.add("§7・" + playerData.getNick(true));
        }
        return info;
    }

    public ItemStack viewItem() {
        List<String> lore = view();
        ItemStack item = ItemStackPlayerHead(Leader);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(lore.get(0));
        lore.remove(0);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
