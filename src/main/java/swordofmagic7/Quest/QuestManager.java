package swordofmagic7.Quest;

import org.bukkit.entity.Player;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.Data.DataBase.getItemParameter;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Sound.CustomSound.playSound;

public class QuestManager {
    private final Player player;
    private final PlayerData playerData;
    private final static int MaxQuest = 9;

    public HashMap<QuestData, QuestProcess> QuestList = new HashMap<>();

    public QuestManager(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void takeQuest(QuestData questData) {
        if (QuestList.size() < MaxQuest) {
            QuestList.put(questData, new QuestProcess(questData));
            player.sendMessage("§e[" + questData.Display + "]§aを§b受注§aしました");
            playSound(player, SoundList.Accept);
        } else {
            player.sendMessage("§e[クエスト]§aは同時に§c[" + MaxQuest + "個]§aまでしか受けれません");
            playSound(player, SoundList.Nope);
        }
    }

    public void discardQuest(QuestData questData) {
        if (QuestList.containsKey(questData)) {
            QuestList.remove(questData);
            player.sendMessage("§e[" + questData.Display + "]§aを§c破棄§aしました");
            playSound(player, SoundList.Accept);
        }
    }

    public void processQuest(QuestData questData, QuestReqContentKey key, int value) {
        if (QuestList.containsKey(questData) && QuestList.get(questData).ProcessContent.containsKey(key)) {
            QuestProcess questProcess = QuestList.get(questData);
            int questValue = questProcess.ProcessContent.get(key) + value;
            questProcess.ProcessContent.put(key, questValue);
            player.sendMessage("§e[Q]§e" + key.toString(questData.type) + "§7: §a" + questValue + "/" + questData.ReqContent.get(key));
        } else {
            Log("§cQuest Illegality Data [1] -> " + questData.Id + " at " + player.getName());
        }
    }

    public void checkQuest(QuestData questData) {
        if (QuestList.containsKey(questData)) {
            boolean check = true;
            for (Map.Entry<QuestReqContentKey, Integer> reqContent : questData.ReqContent.entrySet()) {
                QuestReqContentKey key = reqContent.getKey();
                if (QuestList.get(questData).ProcessContent.containsKey(key)) {
                    if (questData.type.isItem()) {
                        ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(getItemParameter(key.mainKey));
                        int Amount = stack.Amount;
                        QuestList.get(questData).ProcessContent.put(key, Amount);
                    }
                    if (QuestList.get(questData).ProcessContent.get(key) < reqContent.getValue()) {
                        check = false;
                        break;
                    }
                } else {
                    Log("§cQuest Illegality Data [2] -> " + questData.Id + " at " + player.getName());
                    check = false;
                    break;
                }
            }
            if (check) {
                clearQuest(questData);
            }
        } else {
            Log("§cQuest Illegality Data [3] -> " + questData.Id + " at " + player.getName());
        }
    }

    public void clearQuest(QuestData questData) {
        player.sendMessage(decoText("クエスト報酬[" + questData.Display + "]"));
        player.sendMessage("§7・§eプレイヤー経験値 §a+" + questData.RewardExp);
        player.sendMessage("§7・§eクラス経験値 §a+" + questData.RewardClassExp);
        for (ItemParameterStack stack : questData.RewardItemStack) {
            playerData.ItemInventory.addItemParameter(stack);
            player.sendMessage("§7・" + stack.itemParameter.Display + "§ax" + stack.Amount);
        }
        playerData.addPlayerExp(questData.RewardExp);
        for (ClassData classData : playerData.Classes.classSlot) {
            if (classData != null) {
                playerData.Classes.addClassExp(classData, questData.RewardClassExp);
            }
        }
    }
}
