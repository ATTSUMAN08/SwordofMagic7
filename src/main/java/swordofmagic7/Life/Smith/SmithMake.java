package swordofmagic7.Life.Smith;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
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
import static swordofmagic7.Data.DataLoader.MaxMakeSlot;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.TitleMenu.nonSlotVertical;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.random;

public class SmithMake {
    private final Player player;
    private final PlayerData playerData;
    private final MakeData[] MakeArray = new MakeData[45];
    private int Scroll;
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

    public void MakeMenuView(int scroll) {
        if (equalInv(player.getOpenInventory(), MakeMenuDisplay)) {
            Scroll = scroll;
            ItemStack[] itemStacks = new ItemStack[54];
            int slot = 0;
            int index = scroll * 9;
            for (int i = 0; i < 48; i++) {
                if (MakeGUIMap.containsKey(index)) {
                    MakeData makeData = MakeDataList.get(MakeGUIMap.get(index));
                    itemStacks[slot] = makeData.view(playerData.ViewFormat());
                    MakeArray[slot] = makeData;
                }
                index++;
                slot++;
                if (nonSlotVertical(slot)) {
                    itemStacks[slot] = BrownItemFlame;
                    slot++;
                    index++;
                }
            }
            if (Scroll > 0) itemStacks[8] = UpScrollItem;
            if (MaxMakeSlot/9-5 > 0) itemStacks[53] = DownScrollItem;
            player.getOpenInventory().getTopInventory().setStorageContents(itemStacks);
            playSound(player, SoundList.Tick);
        }
    }

    public void MakeMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, MakeMenuDisplay)) {
            if (currentItem != null) {
                int index = Scroll*9 + Slot;
                if (MakeArray[index] != null) {
                    MakeData data = MakeArray[index];
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
                            playerData.statistics.MakeEquipmentCount++;
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
                } else {
                    if (equalItem(currentItem, PreviousPageItem)) {
                        Scroll--;
                        MakeMenuView(Scroll);
                    } else if (equalItem(currentItem, NextPageItem)) {
                        Scroll++;
                        MakeMenuView(Scroll);
                    }
                }
            }
        }
    }
}

class SmithMakeSort implements Comparator<MakeData> {
    public int compare(MakeData Smelt, MakeData Smelt2) {
        return Smelt.ReqLevel - Smelt2.ReqLevel;
    }
}
