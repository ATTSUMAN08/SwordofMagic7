package swordofmagic7.Life.Smith;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Life.LifeType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Shop.Shop.ItemFlame;
import static swordofmagic7.Shop.Shop.ItemFlameAmount;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Smelt {
    private final Player player;
    private final PlayerData playerData;
    private final SmeltData[] SmeltArray = new SmeltData[45];
    private int page;
    private int SmeltAmount = 1;
    private static final String SmeltPrefix = "精錬数";

    public Smelt(PlayerData playerData) {
        player = playerData.player;
        this.playerData = playerData;
    }

    public static String SmeltMenuDisplay = "精錬メニュー";

    public void SmeltMenuView() {
        Inventory inv = decoInv(SmeltMenuDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
        MultiThread.TaskRunLater(() -> SmeltMenuView(0), 1, "SmithMenuView: " + player.getName());
    }

    public int MaxPage() {
        return (int) Math.ceil(SmeltDataList.size()/45f);
    }

    public void SmeltMenuView(int page) {
        if (equalInv(player.getOpenInventory(), SmeltMenuDisplay)) {
            this.page = page;
            ItemStack[] itemStacks = new ItemStack[54];
            List<SmeltData> SmeltList = new ArrayList<>(DataBase.SmeltDataList.values());
            SmeltList.sort(new swordofmagic7.Life.Smith.SmeltSort());
            int index = page * 45;
            for (int i = 0; i < 45; i++) {
                if (SmeltList.size() > index) {
                    SmeltData SmeltData = SmeltList.get(index);
                    itemStacks[i] = SmeltData.view(playerData.ViewFormat());
                    SmeltArray[i] = SmeltList.get(index);
                    index++;
                } else break;
            }
            itemStacks[45] = ShopFlame;
            itemStacks[46] = ItemFlame(-100);
            itemStacks[47] = ItemFlame(-10);
            itemStacks[48] = ItemFlame(-1);
            itemStacks[50] = ItemFlame(1);
            itemStacks[51] = ItemFlame(10);
            itemStacks[52] = ItemFlame(100);
            itemStacks[53] = ShopFlame;
            if (page > 1) itemStacks[45] = PreviousPageItem;
            if (page < MaxPage()) itemStacks[53] = NextPageItem;
            itemStacks[49] = ItemFlameAmount(SmeltPrefix, SmeltAmount);
            player.getOpenInventory().getTopInventory().setContents(itemStacks);
            playSound(player, SoundList.Tick);
        }
    }

    public void SmeltMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, SmeltMenuDisplay)) {
            if (Slot < 45) {
                if (currentItem != null) {
                    if (SmeltArray[Slot] != null) {
                        SmeltData data = SmeltArray[Slot];
                        if (playerData.LifeStatus.getLevel(LifeType.Smith) >= data.ReqLevel) {
                            List<String> reqList = new ArrayList<>();
                            boolean SmeltAble = true;
                            for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                if (playerData.ItemInventory.hasItemParameter(stack.itemParameter, stack.Amount * SmeltAmount)) {
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount * SmeltAmount + " §b✔");
                                } else {
                                    SmeltAble = false;
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount * SmeltAmount + " §c(" + playerData.ItemInventory.getItemParameterStack(stack.itemParameter).Amount + ")");
                                }
                            }
                            if (SmeltAble) {
                                if (data.itemRecipe != null) {
                                    for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                        playerData.ItemInventory.removeItemParameter(stack.itemParameter, stack.Amount * SmeltAmount);
                                    }
                                }
                                playerData.statistics.SmeltCount += SmeltAmount;
                                playerData.ItemInventory.addItemParameter(data.itemParameter.clone(), data.Amount * SmeltAmount);
                                playerData.LifeStatus.addLifeExp(LifeType.Smith, data.Exp * SmeltAmount);
                                player.sendMessage("§e[" + data.itemParameter.Display + "§ax" + data.Amount * SmeltAmount + "§e]§aを§e精錬§aしました");
                                playSound(player, SoundList.LevelUp);
                            } else {
                                player.sendMessage(decoText("必要素材リスト"));
                                for (String message : reqList) {
                                    player.sendMessage(message);
                                }
                                playSound(player, SoundList.Nope);
                            }
                        } else {
                            player.sendMessage("§e[鍛冶レベル]§aが足りません");
                            playSound(player, SoundList.Nope);
                        }
                    }
                }
            } else {
                if (equalItem(currentItem, PreviousPageItem)) {
                    page--;
                    SmeltMenuView(page);
                } else if (equalItem(currentItem, NextPageItem)) {
                    page++;
                    SmeltMenuView(page);
                } else {
                    switch (Slot) {
                        case 46 -> SmeltAmount -= 100;
                        case 47 -> SmeltAmount -= 10;
                        case 48 -> SmeltAmount--;
                        case 50 -> SmeltAmount++;
                        case 51 -> SmeltAmount += 10;
                        case 52 -> SmeltAmount += 100;
                    }
                    if (SmeltAmount < 1) SmeltAmount = 1;
                    if (SmeltAmount > 10000) SmeltAmount = 10000;
                    playSound(player, SoundList.Click);
                }
                view.getTopInventory().setItem(49, ItemFlameAmount(SmeltPrefix, SmeltAmount));
            }
        }
    }
}

class SmeltSort implements Comparator<SmeltData> {
    public int compare(SmeltData Smelt, SmeltData Smelt2) {
        return Smelt.ReqLevel - Smelt2.ReqLevel;
    }
}
