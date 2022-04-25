package swordofmagic7.Item.ItemUseList;

import swordofmagic7.Client;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.RewardBoxList;
import static swordofmagic7.Data.DataBase.getItemParameter;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.SomCore.random;

public class RewardBox {

    public static void rewardBoxOpen(PlayerData playerData, ItemParameter item) {
        playerData.ItemInventory.removeItemParameter(item, 1);
        List<String> message = new ArrayList<>();
        message.add(decoText(item.Display));
        for (RewardBoxData rewardBoxData : RewardBoxList.get(item.Id)) {
            if (random.nextDouble() < rewardBoxData.percent) {
                if (rewardBoxData.id.equals("メル")) {
                    playerData.Mel += rewardBoxData.amount;
                    message.add("§7・§e" + rewardBoxData.amount + "メル");
                    if (rewardBoxData.percent <= 0.001) {
                        TextView textView = new TextView(playerData.getNick() + "§aさんが");
                        textView.addView(item.getTextView(1, playerData.ViewFormat())).addText("§aから").addText("§e[" + rewardBoxData.amount + "メル]").addText("§aを§b獲得§aしました");
                        textView.setSound(SoundList.Tick);
                        Client.sendDisplay(playerData.player, textView);
                    }
                } else if (DataBase.ItemList.containsKey(rewardBoxData.id)) {
                    ItemParameter getItem = getItemParameter(rewardBoxData.id);
                    playerData.ItemInventory.addItemParameter(getItem, rewardBoxData.amount);
                    message.add("§7・§e" + rewardBoxData.id + "§ax" + rewardBoxData.amount);
                    if (rewardBoxData.percent <= 0.003) {
                        TextView textView = new TextView(playerData.getNick() + "§aさんが");
                        textView.addView(item.getTextView(1, playerData.ViewFormat())).addText("§aから");
                        textView.addView(getItem.getTextView(rewardBoxData.amount, playerData.ViewFormat())).addText("§aを§b獲得§aしました");
                        textView.setSound(SoundList.Tick);
                        Client.sendDisplay(playerData.player, textView);
                    }
                } else {
                    message.add("§c設定ミスのアイテムがあります。運営に報告してください -> " + rewardBoxData.id);
                }
            }
        }
        sendMessage(playerData.player, message, SoundList.LevelUp);
    }
}
