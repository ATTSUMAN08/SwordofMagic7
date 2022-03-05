package swordofmagic7.TextView;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;

import static swordofmagic7.Function.*;

public class TextViewManager {

    public static void TextView(Player player, String[] args) {
        int index = -1;
        try {
            String type = args[0];
            if (args.length == 2) index = Integer.parseInt(args[1]);
            PlayerData playerData = PlayerData.playerData(player);
            ItemStack itemView = null;
            int Amount = 1;
            if (type.equalsIgnoreCase("MainHand")) {
                itemView = player.getInventory().getItemInMainHand();
            } else if (type.equalsIgnoreCase("OffHand")) {
                itemView = player.getInventory().getItemInOffHand();
            } else if (type.equalsIgnoreCase("Armor")) {
                itemView = player.getInventory().getChestplate();
            } else if (index > -1 && type.equalsIgnoreCase("Item") && playerData.ItemInventory.getList().size() > index) {
                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index);
                itemView = stack.viewItem(playerData.ViewFormat());
                Amount = stack.Amount;
            } else if (index > -1 && type.equalsIgnoreCase("Rune") && playerData.RuneInventory.getList().size() > index) {
                itemView = playerData.RuneInventory.getRuneParameter(index).viewRune(playerData.ViewFormat());
            } else if (index > -1 && type.equalsIgnoreCase("Pet") && playerData.PetInventory.getList().size() > index) {
                itemView = playerData.PetInventory.getPetParameter(index).viewPet(playerData.ViewFormat());
            }
            if (itemView != null) {
                if (itemView.getType() != Material.AIR && itemView.hasItemMeta()) {
                    ItemMeta meta = itemView.getItemMeta();
                    if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                        String Display = unDecoText(meta.getDisplayName());
                        StringBuilder Lore = new StringBuilder(meta.getDisplayName());
                        for (String str : meta.getLore()) {
                            Lore.append("<nl>").append(str);
                        }
                        String txt = "";
                        if (Amount > 1) txt = "§ax" + Amount;
                        player.chat("§e[" + Display + txt + "§e]<tag>" + Lore + "<end>");
                    }
                }
                return;
            }
        } catch (Exception ignored) {}
        player.sendMessage(decoLore("/textView MainHand") + "メインハンドをチャットに表示します");
        player.sendMessage(decoLore("/textView OffHand") + "オフハンドをチャットに表示します");
        player.sendMessage(decoLore("/textView Armor") + "アーマーをチャットに表示します");
        player.sendMessage(decoLore("/textView Item <SlotId>") + "アイテムをチャットに表示します");
        player.sendMessage(decoLore("/textView Rune <SlotId>") + "ルーンをチャットに表示します");
        player.sendMessage(decoLore("/textView Pet <SlotId>") + "ペットをチャットに表示します");
    }
}
