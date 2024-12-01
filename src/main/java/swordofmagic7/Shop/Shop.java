package swordofmagic7.Shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.NextPageItem;
import static swordofmagic7.Data.DataBase.PreviousPageItem;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.NonMel;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Shop {
    public static final int MaxSelectAmount = 100000;
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
    private int currentPage = 1;
    public void ShopOpen(ShopData Shop) {
        playerData.setView(ViewInventoryType.ItemInventory, false);
        if (AmountReset) BuyAmount = 1;
        currentPage = 1;
        Inventory inv = Shop.view(currentPage, playerData.ViewFormat());
        inv.setItem(49, ItemFlameAmount(ShopBuyPrefix, BuyAmount));
        player.openInventory(inv);
        ShopDataCache = Shop.clone();
    }

    public void ShopSellOpen() {
        playerData.setView(ViewInventoryType.ItemInventory, false);
        if (AmountReset) SellAmount = 1;
        Inventory inv = SellInventory.viewInventory(SellAmount);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
    }

    private int BuyAmount = 1;
    public void ShopClick(InventoryView view, ItemStack currentItem, ClickType clickType, int Slot, int index) {
        if (ShopDataCache != null && equalInv(view, "§l" + ShopDataCache.Display) && playerData.ViewInventory.isItem()) {
            if (Slot < 45) {
                if (ShopDataCache.Data.containsKey(index)) {
                    boolean buyAble = true;
                    List<String> reqList = new ArrayList<>();
                    ShopSlot data = ShopDataCache.Data.get(index);
                    int Mel = data.Mel * BuyAmount;
                    String buyText = "購入";
                    if (playerData.Mel >= Mel) {
                        reqList.add(decoLore("メル") + Mel + " §b✔");
                    } else {
                        buyAble = false;
                        reqList.add(decoLore("メル") + Mel + " §c(" + playerData.Mel + ")");
                    }
                    if (data.itemRecipe != null) {
                        buyText = "作成";
                        for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                            if (playerData.ItemInventory.hasItemParameter(stack.itemParameter, stack.Amount*BuyAmount)) {
                                reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount*BuyAmount + " §b✔");
                            } else {
                                buyAble = false;
                                reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount*BuyAmount + " §c(" + playerData.ItemInventory.getItemParameterStack(stack.itemParameter).Amount + ")");
                            }
                        }
                    }
                    if (buyAble) {
                        playerData.Mel -= Mel;
                        if (data.itemRecipe != null) {
                            for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                playerData.ItemInventory.removeItemParameter(stack.itemParameter, stack.Amount*BuyAmount);
                            }
                        }
                        playerData.ItemInventory.addItemParameter(data.itemParameter.clone(), data.Amount*BuyAmount);
                        player.sendMessage("§e[" + data.itemParameter.Display + "§ax" + data.Amount*BuyAmount +"§e]§aを§b" + buyText + "§aしました");
                        playSound(player, SoundList.LevelUp);
                    } else {
                        player.sendMessage(decoText("必要物リスト"));
                        for (String message : reqList) {
                            player.sendMessage(message);
                        }
                        playSound(player, SoundList.Nope);
                    }
                }
            } else {
                if (equalItem(currentItem, NextPageItem)) {
                    currentPage++;
                    view.getTopInventory().setContents(ShopDataCache.view(currentPage, playerData.ViewFormat()).getStorageContents());
                    playSound(player, SoundList.Click);
                } else if (equalItem(currentItem, PreviousPageItem)) {
                    currentPage--;
                    view.getTopInventory().setContents(ShopDataCache.view(currentPage, playerData.ViewFormat()).getStorageContents());
                    playSound(player, SoundList.Click);
                } else {
                    int buyAmount = 0;
                    switch (Slot) {
                        case 46 -> buyAmount -= 100;
                        case 47 -> buyAmount -= 10;
                        case 48 -> buyAmount--;
                        case 50 -> buyAmount++;
                        case 51 -> buyAmount += 10;
                        case 52 -> buyAmount += 100;
                    }
                    if (clickType.isShiftClick()) buyAmount *= 1000;
                    if (buyAmount != 0) BuyAmount += buyAmount;
                    if (BuyAmount < 1) BuyAmount = 1;
                    if (BuyAmount > MaxSelectAmount) BuyAmount = MaxSelectAmount;
                    playSound(player, SoundList.Click);
                }
                view.getTopInventory().setItem(49, ItemFlameAmount(ShopBuyPrefix, BuyAmount));
            }
        }
    }

    private int SellAmount = 1;
    public void ShopSellClick(InventoryView view, Inventory ClickInventory, ClickType clickType, int index, int Slot) {
        if (equalInv(view, ShopSellDisplay) && playerData.ViewInventory.isItem()) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot < 45) {
                    if (SellInventory.getList().size() > Slot) {
                        ItemParameterStack stack = SellInventory.getList().get(Slot);
                        int Amount = clickType.isShiftClick() ? stack.Amount : Math.min(SellAmount, stack.Amount);
                        int Mel = stack.itemParameter.Sell * Amount;
                        if (playerData.Mel >= Mel) {
                            playerData.Mel -= Mel;
                            playerData.ItemInventory.addItemParameter(stack.itemParameter.clone(), Amount);
                            SellInventory.removeItemParameter(stack.itemParameter, Amount);
                            player.sendMessage("§e[" + stack.itemParameter.Display + "§ax" + Amount +"§e]§aを§b買戻§aしました §c[-" + Mel + "メル]");
                            playSound(player, SoundList.LevelUp);
                        } else {
                            player.sendMessage(NonMel +  " §c不足[" + (Mel-playerData.Mel) + "メル]");
                            playSound(player, SoundList.Nope);
                        }
                    }
                } else {
                    int sellAmount = 0;
                    switch (Slot) {
                        case 46 -> sellAmount-=100;
                        case 47 -> sellAmount-=10;
                        case 48 -> sellAmount--;
                        case 50 -> sellAmount++;
                        case 51 -> sellAmount+=10;
                        case 52 -> sellAmount+=100;
                    }
                    if (clickType.isShiftClick()) sellAmount *= 1000;
                    if (sellAmount != 0) SellAmount += sellAmount;
                    if (SellAmount < 1) SellAmount = 1;
                    if (SellAmount > MaxSelectAmount) SellAmount = MaxSelectAmount;
                    playSound(player, SoundList.Click);
                }
            } else if (ClickInventory == player.getInventory() && index > -1) {
                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index);
                if (stack == null) return;
                ItemParameter item = stack.itemParameter;
                if (clickType.isRightClick()) {
                    if (item.Category.isEquipment()) {
                        if (item.itemEquipmentData.Plus >= 10) {
                            sendMessage(player, "§e装備§aの§c強化値§aが§b+10§a以上です", SoundList.Nope);
                            return;
                        } else if (!item.itemEquipmentData.Rune.isEmpty()) {
                            sendMessage(player, "§e装備§aに§eルーン§aが§e装着§aされています", SoundList.Nope);
                            return;
                        }
                    }
                }
                int Amount = clickType.isShiftClick() ? stack.Amount : Math.min(SellAmount, stack.Amount);
                SellInventory.addItemParameter(item, Amount);
                playerData.ItemInventory.removeItemParameter(item, Amount);
                int Mel = item.Sell * Amount;
                playerData.Mel += Mel;
                player.sendMessage("§e[" + item.Display + "§ax" + Amount +"§e]§aを§c売却§aしました §e[+" + Mel + "メル]");
                playSound(player, SoundList.LevelUp);
            }
            view.getTopInventory().setContents(SellInventory.viewInventory(SellAmount).getStorageContents());
        }
    }

    public void ShopClose() {
        if (ShopDataCache != null) {
            ShopDataCache = null;
        }
    }
}