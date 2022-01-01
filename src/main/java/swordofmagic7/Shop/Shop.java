package swordofmagic7.Shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.ShopFlame;
import static swordofmagic7.Function.decoInv;
import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Shop {
    private final Player player;
    private final PlayerData playerData;
    public boolean AmountReset = true;
    public SellInventory SellInventory;
    public final static String ShopSellDisplay = "§lアイテム売却";
    public final static String ShopBuyPrefix = "購入数";
    public final static String ShopSellPrefix = "売却/買戻数";

    public void AmountReset() {
        AmountReset = !AmountReset;
        String msg = "§e[ショップ購入数初期化]§aを";
        if (AmountReset) msg += "§b[有効]";
        else msg += "§c[無効]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
    }

    public static ItemStack ItemFlame(int i) {
        if (i > 0) {
            return new ItemStackData(Material.YELLOW_STAINED_GLASS_PANE, "§e§l[+" + i + "]").view();
        } else {
            return new ItemStackData(Material.YELLOW_STAINED_GLASS_PANE, "§e§l[" + i + "]").view();
        }
    }

    public static ItemStack ItemFlameAmount(String prefix, int i) {
        if (i > 0) {
            return new ItemStackData(Material.GOLD_NUGGET, "§e§l" + prefix + "[" + i + "]").view();
        } else {
            return new ItemStackData(Material.YELLOW_STAINED_GLASS_PANE, "§e§l[" + i + "]").view();
        }
    }

    public Shop(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        this.SellInventory = new SellInventory(player, playerData);
    }

    private ShopData ShopDataCache;
    public void ShopOpen(ShopData Shop) {
        playerData.Menu.ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.ItemInventory, false);
        if (AmountReset) BuyAmount = 1;
        Inventory inv = Shop.view(1, playerData.ViewFormat());
        inv.setItem(49, ItemFlameAmount(ShopBuyPrefix, BuyAmount));
        player.openInventory(inv);
        ShopDataCache = Shop.clone();
        playSound(player, SoundList.MenuOpen);
    }

    public void ShopSellOpen() {
        playerData.Menu.ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.ItemInventory, false);
        if (AmountReset) SellAmount = 1;
        Inventory inv = SellInventory.viewInventory(SellAmount);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
    }

    private int BuyAmount = 1;
    public void ShopClick(InventoryView view, int Slot) {
        if (ShopDataCache != null && equalInv(view, "§l" + ShopDataCache.Display)) {
            if (Slot < 45) {
                if (ShopDataCache.Data.containsKey(Slot)) {
                    ShopSlot data = ShopDataCache.Data.get(Slot);
                    int Mel = data.Mel * BuyAmount;
                    if (playerData.Mel >= Mel) {
                        playerData.Mel -= Mel;
                        playerData.ItemInventory.addItemParameter(data.itemParameter.clone(), 1);
                        player.sendMessage("§e[" + data.itemParameter.Display + "§ax" + BuyAmount +"§e]§aを§b購入§aしました §c[-" + Mel + "メル]");
                        playSound(player, SoundList.LevelUp);
                    } else {
                        player.sendMessage("§eメル§aが足りません §c不足[" + (Mel-playerData.Mel) + "メル]");
                        playSound(player, SoundList.Nope);
                    }
                }
            } else {
                switch (Slot) {
                    case 46 -> BuyAmount-=100;
                    case 47 -> BuyAmount-=10;
                    case 48 -> BuyAmount--;
                    case 50 -> BuyAmount++;
                    case 51 -> BuyAmount+=10;
                    case 52 -> BuyAmount+=100;
                }
                if (BuyAmount < 1) BuyAmount = 1;
                if (BuyAmount > 10000) BuyAmount = 10000;
                player.getOpenInventory().getTopInventory().setItem(49, ItemFlameAmount(ShopBuyPrefix, BuyAmount));
                playSound(player, SoundList.Click);
            }
        }
    }

    private int SellAmount = 1;
    public void ShopSellClick(InventoryView view, Inventory ClickInventory, int index, int Slot) {
        if (equalInv(view, ShopSellDisplay) && playerData.ViewInventory.isItem()) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot < 45) {
                    if (SellInventory.getList().size() > Slot) {
                        ItemParameterStack stack = SellInventory.getList().get(Slot);
                        int Amount = Math.min(SellAmount, stack.Amount);
                        int Mel = stack.itemParameter.Sell * Amount;
                        if (playerData.Mel >= Mel) {
                            playerData.Mel -= Mel;
                            playerData.ItemInventory.addItemParameter(stack.itemParameter.clone(), Amount);
                            SellInventory.removeItemParameter(stack.itemParameter, Amount);
                            player.sendMessage("§e[" + stack.itemParameter.Display + "§ax" + Amount +"§e]§aを§b買戻§aしました §c[-" + Mel + "メル]");
                            playSound(player, SoundList.LevelUp);
                        } else {
                            player.sendMessage("§eメル§aが足りません §c不足[" + (Mel-playerData.Mel) + "メル]");
                            playSound(player, SoundList.Nope);
                        }
                    }
                } else {
                    switch (Slot) {
                        case 46 -> SellAmount-=100;
                        case 47 -> SellAmount-=10;
                        case 48 -> SellAmount--;
                        case 50 -> SellAmount++;
                        case 51 -> SellAmount+=10;
                        case 52 -> SellAmount+=100;
                    }
                    if (SellAmount < 1) SellAmount = 1;
                    if (SellAmount > 10000) SellAmount = 10000;
                    playSound(player, SoundList.Click);
                }
            } else if (ClickInventory == player.getInventory() && index > -1) {
                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index);
                ItemParameter item = stack.itemParameter;
                int Amount = Math.min(stack.Amount, SellAmount);
                SellInventory.addItemParameter(item, Amount);
                playerData.ItemInventory.removeItemParameter(item, Amount);
                int Mel = item.Sell * Amount;
                playerData.Mel += Mel;
                player.sendMessage("§e[" + item.Display + "§ax" + Amount +"§e]§aを§c売却§aしました §e[+" + Mel + "メル]");
                playSound(player, SoundList.LevelUp);
            }
            player.getOpenInventory().getTopInventory().setContents(SellInventory.viewInventory(SellAmount).getStorageContents());
        }
    }

    public void ShopClose() {
        if (ShopDataCache != null) {
            ShopDataCache = null;
        }
    }
}