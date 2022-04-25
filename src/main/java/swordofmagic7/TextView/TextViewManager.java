package swordofmagic7.TextView;

import org.bukkit.entity.Player;
import swordofmagic7.Client;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Pet.PetParameter;

import static swordofmagic7.Function.decoLore;

public class TextViewManager {

    public static void TextView(Player player, String[] args) {
        if (args.length >= 1) {
            int index = -1;
            try {
                if (args.length == 2) index = Integer.parseInt(args[1]);
            } catch (Exception ignored) {
            }
            String type = args[0];
            PlayerData playerData = PlayerData.playerData(player);
            TextView textView = new TextView();
            int amount = 1;
            boolean isLoreHide = false;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (type.equalsIgnoreCase(slot.toString()) && playerData.Equipment.isEquip(slot)) {
                    ItemParameter item = playerData.Equipment.getEquip(slot);
                    textView.addView(item.getTextView(amount, playerData.ViewFormat()));
                }
            }
            if (index > -1 && type.equalsIgnoreCase("Item") && playerData.ItemInventory.getList().size() > index) {
                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index);
                textView.addView(stack.itemParameter.getTextView(amount, playerData.ViewFormat()));
            } else if (index > -1 && type.equalsIgnoreCase("Rune") && playerData.RuneInventory.getList().size() > index) {
                RuneParameter rune = playerData.RuneInventory.getRuneParameter(index);
                textView.addView(rune.getTextView(playerData.ViewFormat()));
            } else if (index > -1 && type.equalsIgnoreCase("Pet") && playerData.PetInventory.getList().size() > index) {
                PetParameter pet = playerData.PetInventory.getPetParameter(index);
                textView.addView(pet.getTextView(playerData.ViewFormat()));
            }
            if (!textView.isEmpty()) {
                Client.sendPlayerChat(player, textView);
                return;
            }
        }
        player.sendMessage(decoLore("/textView MainHand") + "メインハンドをチャットに表示します");
        player.sendMessage(decoLore("/textView OffHand") + "オフハンドをチャットに表示します");
        player.sendMessage(decoLore("/textView Armor") + "アーマーをチャットに表示します");
        player.sendMessage(decoLore("/textView Item <SlotId>") + "アイテムをチャットに表示します");
        player.sendMessage(decoLore("/textView Rune <SlotId>") + "ルーンをチャットに表示します");
        player.sendMessage(decoLore("/textView Pet <SlotId>") + "ペットをチャットに表示します");
    }
}
