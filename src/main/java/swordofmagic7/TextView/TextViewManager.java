package swordofmagic7.TextView;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Pet.PetParameter;

import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.unDecoText;

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
            ItemStack itemView = null;
            int amount = 1;
            boolean isLoreHide = false;
            if (type.equalsIgnoreCase("MainHand")) {
                itemView = player.getInventory().getItemInMainHand();
                isLoreHide = playerData.Equipment.getEquip(EquipmentSlot.MainHand).isLoreHide;
            } else if (type.equalsIgnoreCase("OffHand")) {
                itemView = player.getInventory().getItemInOffHand();
                isLoreHide = playerData.Equipment.getEquip(EquipmentSlot.OffHand).isLoreHide;
            } else if (type.equalsIgnoreCase("Armor")) {
                itemView = player.getInventory().getChestplate();
                isLoreHide = playerData.Equipment.getEquip(EquipmentSlot.Armor).isLoreHide;
            } else if (index > -1 && type.equalsIgnoreCase("Item") && playerData.ItemInventory.getList().size() > index) {
                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index);
                itemView = stack.viewItem(playerData.ViewFormat());
                amount = stack.Amount;
                isLoreHide = stack.itemParameter.isLoreHide;
            } else if (index > -1 && type.equalsIgnoreCase("Rune") && playerData.RuneInventory.getList().size() > index) {
                itemView = playerData.RuneInventory.getRuneParameter(index).viewRune(playerData.ViewFormat());
            } else if (index > -1 && type.equalsIgnoreCase("Pet") && playerData.PetInventory.getList().size() > index) {
                itemView = playerData.PetInventory.getPetParameter(index).viewPet(playerData.ViewFormat());
            }
            if (itemView != null) {
                itemView.setAmount(amount);
                player.chat(itemDecoString(itemView, isLoreHide));
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

    public static String itemDecoString(ItemParameterStack stack, String format, boolean isLoreHide) {
        ItemStack itemView = stack.itemParameter.viewItem(stack.Amount, format);
        itemView.setAmount(stack.Amount);
        return itemDecoString(itemView, isLoreHide);
    }


    public static String itemDecoString(ItemParameter itemParameter, String format, boolean isLoreHide) {
        return itemDecoString(itemParameter.viewItem(1, format), isLoreHide);
    }

    public static String itemDecoString(RuneParameter runeParameter, String format, boolean isLoreHide) {
        return itemDecoString(runeParameter.viewRune(format), isLoreHide);
    }

    public static String itemDecoString(PetParameter petParameter, String format, boolean isLoreHide) {
        return itemDecoString(petParameter.viewPet(format), isLoreHide);
    }

    public static String itemDecoString(ItemStack itemView, boolean isLoreHide) {
        String decoString = "[None]";
        if (itemView.getType() != Material.AIR && itemView.hasItemMeta()) {
            ItemMeta meta = itemView.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                String Display = unDecoText(meta.getDisplayName());
                StringBuilder Lore = new StringBuilder(meta.getDisplayName());
                if (!isLoreHide) for (String str : meta.getLore()) {
                    Lore.append("<nl>").append(str);
                } else {
                    Lore.append("<nl>").append("§c§lこの情報へのアクセス権限がありません");
                }
                String txt = "";
                if (itemView.getAmount() > 1) txt = "§ax" + itemView.getAmount();
                decoString = "§e[" + Display + txt + "§e]<tag>" + Lore + "<end>";
            }
        }
        return decoString;
    }
}
