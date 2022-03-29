package swordofmagic7.Life.Smith;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Life.LifeType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Shop.Shop.ItemFlameAmount;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.random;

public class SmithMake {
    private final Player player;
    private final PlayerData playerData;
    private final MakeData[] MakeArray = new MakeData[45];
    private int page;
    private int MakeAmount = 1;
    private static final String MakePrefix = "制作数";

    public SmithMake(PlayerData playerData) {
        player = playerData.player;
        this.playerData = playerData;
    }

    public static String MakeMenuDisplay = "§l制作メニュー";

    public void MakeMenuView() {
        Inventory inv = decoInv(MakeMenuDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
        MultiThread.TaskRunLater(() -> MakeMenuView(0), 1, "MakeMenuView: " + player.getName());
    }

    public int MaxPage() {
        return (int) Math.ceil(MakeDataList.size()/45f);
    }

    public void MakeMenuView(int page) {
        if (equalInv(player.getOpenInventory(), MakeMenuDisplay)) {
            this.page = page;
            ItemStack[] itemStacks = new ItemStack[54];
            List<MakeData> makeDataList = new ArrayList<>(DataBase.MakeDataList.values());
            makeDataList.sort(new SmithMakeSort());
            int index = page * 45;
            for (int i = 0; i < 45; i++) {
                if (makeDataList.size() > index) {
                    MakeData makeData = makeDataList.get(index);
                    itemStacks[i] = makeData.view(playerData.ViewFormat());
                    MakeArray[i] = makeDataList.get(index);
                    index++;
                } else break;
            }
            itemStacks[45] = ShopFlame;
            itemStacks[46] = ShopFlame; //ItemFlame(-100);
            itemStacks[47] = ShopFlame; //ItemFlame(-10);
            itemStacks[48] = ShopFlame; //ItemFlame(-1);
            itemStacks[50] = ShopFlame; //ItemFlame(1);
            itemStacks[51] = ShopFlame; //ItemFlame(10);
            itemStacks[52] = ShopFlame; //ItemFlame(100);
            itemStacks[53] = ShopFlame;
            if (page > 1) itemStacks[45] = PreviousPageItem;
            if (page < MaxPage()) itemStacks[53] = NextPageItem;
            itemStacks[49] = ItemFlameAmount(MakePrefix, MakeAmount);
            player.getOpenInventory().getTopInventory().setStorageContents(itemStacks);
            playSound(player, SoundList.Tick);
        }
    }

    public void MakeMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, MakeMenuDisplay)) {
            if (Slot < 45) {
                if (currentItem != null) {
                    if (MakeArray[Slot] != null) {
                        MakeData data = MakeArray[Slot];
                        if (playerData.LifeStatus.getLevel(LifeType.Smith) >= data.ReqLevel) {
                            List<String> reqList = new ArrayList<>();
                            boolean SmeltAble = true;
                            for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                if (playerData.ItemInventory.hasItemParameter(stack.itemParameter, stack.Amount)) {
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount + " §b✔");
                                } else {
                                    SmeltAble = false;
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount + " §c(" + playerData.ItemInventory.getItemParameterStack(stack.itemParameter).Amount + ")");
                                }
                            }
                            if (SmeltAble) {
                                if (data.itemRecipe != null) {
                                    for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                        playerData.ItemInventory.removeItemParameter(stack.itemParameter, stack.Amount);
                                    }
                                }
                                ItemParameter item = null;
                                int amount = 0;
                                double percent = random.nextDouble();
                                double p = 0;
                                for (MakeItemData makeData : data.makeList) {
                                    if (p < percent && percent < p + makeData.Percent) {
                                        item = makeData.itemParameter;
                                        amount = makeData.Amount;
                                        break;
                                    }
                                    p += makeData.Percent;
                                }
                                playerData.ItemInventory.addItemParameter(item.clone(), amount);
                                playerData.LifeStatus.addLifeExp(LifeType.Smith, data.Exp);
                                player.sendMessage("§e[" + item.Display + "§ax" + amount + "§e]§aを§e制作§aしました");
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
                    MakeMenuView(page);
                } else if (equalItem(currentItem, NextPageItem)) {
                    page++;
                    MakeMenuView(page);
                } else {
                    //switch (Slot) {
                    //    case 46 -> MakeAmount -= 100;
                    //    case 47 -> MakeAmount -= 10;
                    //    case 48 -> MakeAmount--;
                    //    case 50 -> MakeAmount++;
                    //    case 51 -> MakeAmount += 10;
                    //    case 52 -> MakeAmount += 100;
                    //}
                    if (MakeAmount < 1) MakeAmount = 1;
                    if (MakeAmount > 10000) MakeAmount = 10000;
                    playSound(player, SoundList.Click);
                }
                view.getTopInventory().setItem(49, ItemFlameAmount(MakePrefix, MakeAmount));
            }
        }
    }
}

class SmithMakeSort implements Comparator<MakeData> {
    public int compare(MakeData Smelt, MakeData Smelt2) {
        return Smelt.ReqLevel - Smelt2.ReqLevel;
    }
}
