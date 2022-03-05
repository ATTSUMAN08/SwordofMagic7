package swordofmagic7.Trade;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.decoInv;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Sound.CustomSound.playSound;

public class TradeData {
    public final UUID uuid = UUID.randomUUID();
    public final Player[] player = new Player[2];
    public final PlayerData[] playerData = new PlayerData[2];
    public int[] Mel = new int[2];
    public List<ItemParameterStack>[] TradeList = new List[2];
    public int[] x = new int[2];
    public Inventory[] inv = new Inventory[2];
    public boolean[] ready = new boolean[2];

    public TradeData(Player player, Player player2) {
        this.player[0] = player;
        this.player[1] = player2;
        this.playerData[0] = PlayerData.playerData(player);
        this.playerData[1] = PlayerData.playerData(player2);
        Mel[0] = 0;
        Mel[1] = 0;
        TradeList[0] = new ArrayList<>();
        TradeList[1] = new ArrayList<>();
        ready[0] = false;
        ready[1] = false;
    }

    public void requestTrade() {
        TextComponent inviteMessage = new TextComponent(playerData[0].getNick() + "§aさんから§e[トレード]§aを§b申請§aされました ");
        TextComponent accept = new TextComponent("§b[/trade accept]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept"));
        inviteMessage.addExtra(accept);
        player[1].spigot().sendMessage(inviteMessage);
        player[0].sendMessage(playerData[1].getNick() + "§aさんに§e[トレード]§aを§b申請§aしました");
    }

    public ItemStack readyView(boolean bool) {
        if (bool) return new ItemStackData(Material.LIGHT_BLUE_WOOL, "§b§l準備完了✔").view();
        else return new ItemStackData(Material.RED_WOOL, "§c§l準備中✖").view();
    }

    public void startTrade() {
        for (int i = 0; i < 2; i++) {
            int rev;
            if (i == 0) rev = 1;
            else rev = 0;
            inv[i] = decoInv("トレード - " + uuid, 6);
            for (int s = 4; s < 45; s+=9) { inv[i].setItem(s, TradeFlame); }
            for (int s = 0; s < 54; s+=9) { inv[i].setItem(s, TradeFlame); }
            for (int s = 8; s < 54; s+=9) { inv[i].setItem(s, TradeFlame); }
            inv[i].setItem(2, ItemStackPlayerHead(player[i], playerData[i].getNick()));
            inv[i].setItem(6, ItemStackPlayerHead(player[rev], playerData[rev].getNick()));
            player[i].openInventory(inv[i]);
        }
    }

    public void updateView() {
        for (int i = 0; i < 2; i++) {
            int rev;
            if (i == 0) rev = 1;
            else rev = 0;
            inv[i].setItem(1, new ItemStackData(Material.GOLD_NUGGET, decoLore("メル") + Mel[i]).view());
            inv[i].setItem(5, new ItemStackData(Material.GOLD_NUGGET, decoLore("メル") + Mel[rev]).view());
            inv[i].setItem(3, readyView(ready[i]));
            inv[i].setItem(7, readyView(ready[rev]));
        }
    }

    public void requestAccept() {
        player[0].sendMessage(playerData[0].getNick() + "§aさんが§e[トレード申請]§aを§b承認§aしました");
        player[1].sendMessage(playerData[0].getNick() + "§aさんからの§e[トレード申請]§aを§b承認§aしました");
        TradeManager.TradeRequest.remove(player[1]);
        playSound(player[0], SoundList.Tick);
        playSound(player[1], SoundList.Tick);
        TradeManager.TradeList.put(uuid, this);
        startTrade();
    }

    public void requestDecline() {
        player[0].sendMessage(playerData[0].getNick() + "§aさんが§e[トレード申請]§aを§c拒否§aしました");
        player[1].sendMessage(playerData[0].getNick() + "§aさんからの§e[トレード申請]§aを§c拒否§aしました");
        TradeManager.TradeRequest.remove(player[1]);
        playSound(player[0], SoundList.Tick);
        playSound(player[1], SoundList.Tick);
    }
}
