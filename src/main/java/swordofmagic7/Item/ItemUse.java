package swordofmagic7.Item;

import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.RewardBoxList;
import static swordofmagic7.Data.DataBase.getItemParameter;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.SomCore.random;

public class ItemUse {

    public static void useItem(PlayerData playerData, ItemParameterStack stack) {
        ItemParameter item = stack.itemParameter;
        if (RewardBoxList.containsKey(item.Id)) {
            playerData.ItemInventory.removeItemParameter(item, 1);
            List<String> message = new ArrayList<>();
            message.add(decoText(item.Display));
            for (RewardBox rewardBox : RewardBoxList.get(item.Id)) {
                if (random.nextDouble() < rewardBox.percent) {
                    if (rewardBox.id.equals("メル")) {
                        playerData.Mel += rewardBox.amount;
                        message.add("§7・§e" + rewardBox.amount + "メル");
                    } else if (DataBase.ItemList.containsKey(rewardBox.id)) {
                        playerData.ItemInventory.addItemParameter(getItemParameter(rewardBox.id), rewardBox.amount);
                        message.add("§7・§e" + rewardBox.id + "§ax" + rewardBox.amount);
                    } else {
                        message.add("§c設定ミスのアイテムがあります。運営に報告してください -> " + rewardBox.id);
                    }
                }
            }
            sendMessage(playerData.player, message, SoundList.LevelUp);
        } else {
            sendMessage(playerData.player, "§a使用できない§eアイテム§aです", SoundList.Nope);
        }
    }
}