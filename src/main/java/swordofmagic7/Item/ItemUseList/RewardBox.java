package swordofmagic7.Item.ItemUseList;

import swordofmagic7.Client;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static net.somrpg.swordofmagic7.SomCore.random;

public class RewardBox {

    public List<RewardBoxData> List = new ArrayList<>();
    public boolean isPartition = false;

    public static void rewardBoxOpen(PlayerData playerData, ItemParameter item) {
        playerData.ItemInventory.removeItemParameter(item, 1);
        RewardBox rewardBox = RewardBoxList.get(item.Id);
        if (rewardBox.isPartition) {
            double p = 0;
            double percent = random.nextDouble();
            for (RewardBoxData rewardBoxData : rewardBox.List) {
                if (p <= percent && percent < p+rewardBoxData.percent) {
                    giveReward(playerData, item, rewardBoxData);
                    break;
                }
                p += rewardBoxData.percent;
            }
        } else {
            for (RewardBoxData rewardBoxData : rewardBox.List) {
                if (random.nextDouble() < rewardBoxData.percent) {
                    giveReward(playerData, item, rewardBoxData);
                }
            }
        }
    }

    public static void giveReward(PlayerData playerData, ItemParameter item, RewardBoxData rewardBoxData) {
        List<String> message = new ArrayList<>();
        message.add(decoText(item.Display));
        if (rewardBoxData.id.equals("メル")) {
            playerData.Mel += rewardBoxData.amount;
            message.add("§7・§e" + rewardBoxData.amount + "メル");
            /*
            if (rewardBoxData.percent <= 0.001) {
                TextView textView = new TextView(playerData.getNick() + "§aさんが");
                textView.addView(item.getTextView(1, playerData.ViewFormat())).addText("§aから").addText("§e[" + rewardBoxData.amount + "メル]").addText("§aを§b獲得§aしました");
                textView.setSound(SoundList.Tick);
                Client.sendDisplay(playerData.player, textView);
            }
             */
        } else if (DataBase.ItemList.containsKey(rewardBoxData.id)) {
            ItemParameter getItem = getItemParameter(rewardBoxData.id);
            playerData.ItemInventory.addItemParameter(getItem, rewardBoxData.amount);
            ItemGetLog(playerData.player, getItem, rewardBoxData.amount);
            if (rewardBoxData.percent <= 0.001) {
                TextView textView = new TextView(playerData.getNick() + "§aさんが");
                textView.addView(item.getTextView(1, playerData.ViewFormat())).addText("§aから");
                textView.addView(getItem.getTextView(rewardBoxData.amount, playerData.ViewFormat())).addText("§aを§b獲得§aしました");
                textView.setSound(SoundList.TICK);
                Client.sendDisplay(playerData.player, textView);
            }
        } else if (RuneList.containsKey(rewardBoxData.id)) {
            RuneParameter rune = getRuneParameter(rewardBoxData.id);
            playerData.RuneInventory.addRuneParameter(rune);
            RuneGetLog(playerData.player, rune);
            /*
            if (rewardBoxData.percent <= 0.001) {
                TextView textView = new TextView(playerData.getNick() + "§aさんが");
                textView.addView(item.getTextView(1, playerData.ViewFormat())).addText("§aから");
                textView.addView(rune.getTextView(playerData.ViewFormat())).addText("§aを§b獲得§aしました");
                textView.setSound(SoundList.Tick);
                Client.sendDisplay(playerData.player, textView);
            }
             */
        } else if (DataBase.PetList.containsKey(rewardBoxData.id)) {
            PetParameter pet = new PetParameter(playerData.player, playerData, getPetData(rewardBoxData.id), rewardBoxData.Level, PlayerData.MaxLevel, 0, rewardBoxData.GrowthRate);
            playerData.PetInventory.addPetParameter(pet);
            message.add("§7・§e" + rewardBoxData.id);
            /*
            if (rewardBoxData.percent <= 0.001) {
                TextView textView = new TextView(playerData.getNick() + "§aさんが");
                textView.addView(item.getTextView(1, playerData.ViewFormat())).addText("§aから").addView(pet.getTextView(playerData.ViewFormat())).addText("§aを§b獲得§aしました");
                textView.setSound(SoundList.Tick);
                Client.sendDisplay(playerData.player, textView);
            }
             */
        } else {
            message.add("§c設定ミスのアイテムがあります。運営に報告してください -> " + rewardBoxData.id);
        }
        sendMessage(playerData.player, message, SoundList.LEVEL_UP);
    }
}
