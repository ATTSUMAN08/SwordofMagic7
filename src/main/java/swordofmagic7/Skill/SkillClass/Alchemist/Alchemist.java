package swordofmagic7.Skill.SkillClass.Alchemist;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Shop.Shop.ItemFlame;
import static swordofmagic7.Shop.Shop.ItemFlameAmount;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Alchemist {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;
    private final AlchemyData[] AlchemyArray = new AlchemyData[45];
    private int page;
    private int AlchemyAmount = 1;
    private static final String AlchemyPrefix = "調合数";

    public Alchemist(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public static String AlchemyShopDisplay = "アルケミーショップ";

    public void AlchemyView() {
        MultiThread.TaskRunSynchronized(() -> {
            Inventory inv = decoInv(AlchemyShopDisplay, 6);
            player.openInventory(inv);
            playSound(player, SoundList.MENU_OPEN);
            MultiThread.TaskRunSynchronizedLater(() -> AlchemyView(0), 1, "AlchemyView");
        });
    }

    public int MaxPage() {
        return (int) Math.ceil(AlchemyDataList.size()/45f);
    }

    public void AlchemyView(int page) {
        if (equalInv(player.getOpenInventory(), AlchemyShopDisplay)) {
            this.page = page;
            ItemStack[] itemStacks = new ItemStack[54];
            for (Map.Entry<Integer, String> entry : AlchemyShopMap.entrySet()) {
                int i = entry.getKey() - (page * 45);
                if (45 > i && i > -1) {
                    AlchemyData data = AlchemyDataList.get(entry.getValue());
                    itemStacks[i] = data.view(playerData.ViewFormat());
                    AlchemyArray[i] = data;
                }
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
            itemStacks[49] = ItemFlameAmount(AlchemyPrefix, AlchemyAmount);
            player.getOpenInventory().getTopInventory().setContents(itemStacks);
            playSound(player, SoundList.TICK);
        }
    }

    public void AlchemyClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, AlchemyShopDisplay)) {
            if (Slot < 45) {
                if (currentItem != null) {
                    if (AlchemyArray[Slot] != null) {
                        AlchemyData data = AlchemyArray[Slot];
                        if (playerData.Classes.getClassLevel(getClassData("Alchemist")) >= data.ReqLevel) {
                            List<String> reqList = new ArrayList<>();
                            boolean cookAble = true;
                            for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                if (playerData.ItemInventory.hasItemParameter(stack.itemParameter, stack.Amount * AlchemyAmount)) {
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount * AlchemyAmount + " §b✔");
                                } else {
                                    cookAble = false;
                                    reqList.add(decoLore(stack.itemParameter.Id) + stack.Amount * AlchemyAmount + " §c(" + playerData.ItemInventory.getItemParameterStack(stack.itemParameter).Amount + ")");
                                }
                            }
                            if (cookAble) {
                                if (data.itemRecipe != null) {
                                    for (ItemParameterStack stack : data.itemRecipe.ReqStack) {
                                        playerData.ItemInventory.removeItemParameter(stack.itemParameter, stack.Amount * AlchemyAmount);
                                    }
                                }
                                ItemParameter item = data.itemParameter;
                                int Amount = AlchemyAmount;
                                playerData.ItemInventory.addItemParameter(item, Amount);
                                player.sendMessage("§e[" + item.Display + "§ax" + Amount + "§e]§aを§e調合§aしました");
                                playerData.Classes.addClassExp(DataBase.getClassData("Alchemist"), data.Exp * AlchemyAmount);
                                playerData.statistics.MakePotionCount += AlchemyAmount;
                                playSound(player, SoundList.LEVEL_UP);
                            } else {
                                player.sendMessage(decoText("必要物リスト"));
                                for (String message : reqList) {
                                    player.sendMessage(message);
                                }
                                playSound(player, SoundList.NOPE);
                            }
                        } else {
                            player.sendMessage("§e[レベル]§aが足りません");
                            playSound(player, SoundList.NOPE);
                        }
                    }
                }
            } else {
                if (equalItem(currentItem, PreviousPageItem)) {
                    page--;
                    AlchemyView(page);
                } else if (equalItem(currentItem, NextPageItem)) {
                    page++;
                    AlchemyView(page);
                } else {
                    switch (Slot) {
                        case 46 -> AlchemyAmount -= 100;
                        case 47 -> AlchemyAmount -= 10;
                        case 48 -> AlchemyAmount--;
                        case 50 -> AlchemyAmount++;
                        case 51 -> AlchemyAmount += 10;
                        case 52 -> AlchemyAmount += 100;
                    }
                    if (AlchemyAmount < 1) AlchemyAmount = 1;
                    if (AlchemyAmount > 10000) AlchemyAmount = 10000;
                    playSound(player, SoundList.CLICK);
                }
                view.getTopInventory().setItem(49, ItemFlameAmount(AlchemyPrefix, AlchemyAmount));
            }
        }
    }
}
