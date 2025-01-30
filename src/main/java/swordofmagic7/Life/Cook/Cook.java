package swordofmagic7.Life.Cook;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Life.LifeType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Shop.Shop;
import swordofmagic7.Sound.SoundList;

import java.util.*;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Shop.Shop.ItemFlame;
import static swordofmagic7.Shop.Shop.ItemFlameAmount;
import static swordofmagic7.Sound.CustomSound.playSound;
import static net.somrpg.swordofmagic7.SomCore.random;

public class Cook {

    private final Player player;
    private final PlayerData playerData;
    private final CookData[] CookArray = new CookData[45];
    private int page;
    private int CookAmount = 1;
    private static final String CookPrefix = "料理数";

    public Cook(PlayerData playerData) {
        player = playerData.player;
        this.playerData = playerData;
    }

    public static String CookMenuDisplay = "§l料理メニュー";

    public void CookMenuView() {
        playerData.statistics.checkTitle();
        Inventory inv = decoInv(CookMenuDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MENU_OPEN);
        MultiThread.TaskRunLater(() -> CookMenuView(0), 1, "CookMenuView");
    }

    public int MaxPage() {
        return (int) Math.ceil(CookDataList.size()/45f);
    }

    public void CookMenuView(int page) {
        if (equalInv(player.getOpenInventory(), CookMenuDisplay)) {
            this.page = page;
            ItemStack[] itemStacks = new ItemStack[54];
            List<CookData> cookList = new ArrayList<>(DataBase.CookDataList.values());
            cookList.sort(new CookSort());
            int index = page * 45;
            for (int i = 0; i < 45; i++) {
                if (cookList.size() > index) {
                    CookData cookData = cookList.get(index);
                    itemStacks[i] = cookData.view(playerData.LifeStatus.getLevel(LifeType.Cook), playerData.ViewFormat());
                    CookArray[i] = cookList.get(index);
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
            itemStacks[49] = ItemFlameAmount(CookPrefix, CookAmount);
            MultiThread.TaskRunSynchronized(() -> player.getOpenInventory().getTopInventory().setContents(itemStacks));
            playSound(player, SoundList.TICK);
        }
    }

    public void CookMenuClick(InventoryView view, ItemStack currentItem, ClickType clickType, int Slot) {
        if (equalInv(view, CookMenuDisplay)) {
            if (Slot < 45) {
                if (currentItem != null) {
                    if (CookArray[Slot] != null) {
                        CookData data = CookArray[Slot];
                        if (playerData.LifeStatus.getLevel(LifeType.Cook) >= data.ReqLevel) {
                            List<String> reqList = new ArrayList<>();
                            boolean cookAble = true;
                            for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                if (playerData.ItemInventory.hasItemParameter(stack.itemParameter, stack.Amount * CookAmount)) {
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount * CookAmount + " §b✔");
                                } else {
                                    cookAble = false;
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount * CookAmount + " §c(" + playerData.ItemInventory.getItemParameterStack(stack.itemParameter).Amount + ")");
                                }
                            }
                            if (cookAble) {
                                if (data.itemRecipe != null) {
                                    for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                        playerData.ItemInventory.removeItemParameter(stack.itemParameter, stack.Amount * CookAmount);
                                    }
                                }
                                MultiThread.TaskRun(() -> {
                                    HashMap<String, Integer> rewardList = new HashMap<>();
                                    for (int i = 0; i < CookAmount; i++) {
                                        for (CookItemData loopData : data.CookItemData) {
                                            if (random.nextDouble() < loopData.getPercent(data.ReqLevel, playerData.LifeStatus.getLevel(LifeType.Cook))) {
                                                String key = loopData.itemParameter.Id;
                                                rewardList.put(key, rewardList.getOrDefault(key, 0) + loopData.Amount);
                                                break;
                                            }
                                        }
                                    }
                                    playerData.statistics.CookCount += CookAmount;
                                    for (Map.Entry<String, Integer> reward : rewardList.entrySet()) {
                                        ItemParameter item = DataBase.getItemParameter(reward.getKey());
                                        playerData.ItemInventory.addItemParameter(item, reward.getValue());
                                        player.sendMessage("§e[" + item.Display + "§ax" + reward.getValue() + "§e]§aを§e料理§aしました");
                                    }
                                    playerData.LifeStatus.addLifeExp(LifeType.Cook, data.Exp * CookAmount);
                                    playSound(player, SoundList.LEVEL_UP);
                                }, "Cook");
                            } else {
                                player.sendMessage(decoText("必要物リスト"));
                                for (String message : reqList) {
                                    player.sendMessage(message);
                                }
                                playSound(player, SoundList.NOPE);
                            }
                        } else {
                            player.sendMessage("§e[料理レベル]§aが足りません");
                            playSound(player, SoundList.NOPE);
                        }
                    }
                }
            } else {
                if (equalItem(currentItem, PreviousPageItem)) {
                    page--;
                    CookMenuView(page);
                } else if (equalItem(currentItem, NextPageItem)) {
                    page++;
                    CookMenuView(page);
                } else {
                    int cookAmount = 0;
                    switch (Slot) {
                        case 46 -> cookAmount -= 100;
                        case 47 -> cookAmount -= 10;
                        case 48 -> cookAmount--;
                        case 50 -> cookAmount++;
                        case 51 -> cookAmount += 10;
                        case 52 -> cookAmount += 100;
                    }
                    if (clickType.isShiftClick()) cookAmount *= 1000;
                    if (cookAmount != 0) CookAmount += cookAmount;
                    if (CookAmount < 1) CookAmount = 1;
                    if (CookAmount > Shop.MaxSelectAmount) CookAmount = Shop.MaxSelectAmount;
                    playSound(player, SoundList.CLICK);
                }
                view.getTopInventory().setItem(49, ItemFlameAmount(CookPrefix, CookAmount));
            }
        }
    }
}

class CookSort implements Comparator<CookData> {
    public int compare(CookData cook, CookData cook2) {
        return cook.ReqLevel - cook2.ReqLevel;
    }
}