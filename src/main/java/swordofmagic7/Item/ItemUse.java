package swordofmagic7.Item;

import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemUseList.BigAusSlime;
import swordofmagic7.Item.ItemUseList.RewardBox;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Data.DataBase.RewardBoxList;
import static swordofmagic7.Function.sendMessage;

public class ItemUse {

    public static void useItem(PlayerData playerData, ItemParameterStack stack) {
        ItemParameter item = stack.itemParameter;
        if (RewardBoxList.containsKey(item.Id)) {
            RewardBox.rewardBoxOpen(playerData, item);
        } else if (item.Id.equalsIgnoreCase("スライムの王冠")) {
            BigAusSlime.trigger(playerData, item);
        } else {
            sendMessage(playerData.player, "§a使用できない§eアイテム§aです", SoundList.Nope);
        }
    }
}