package swordofmagic7;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static swordofmagic7.Function.colored;

enum EquipmentSlot {
    MainHand("メインハンド"),
    OffHand("オフハンド"),
    Armor("アーマー"),
    ;

    String Display;

    EquipmentSlot(String Display) {
        this.Display = Display;
    }

    EquipmentSlot getEquipmentSlot(String str) {
        for (EquipmentSlot loop : EquipmentSlot.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return EquipmentSlot.MainHand;
    }
}

public class Equipment {
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<EquipmentSlot, ItemParameter> EquipSlot = new HashMap<>();

    public Equipment(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    boolean isEquip(EquipmentSlot slot) {
        return EquipSlot.containsKey(slot);
    }

    ItemParameter getEquip(EquipmentSlot slot) {
        return EquipSlot.getOrDefault(slot, new ItemParameter());
    }

    void viewEquip() {
        if (isEquip(EquipmentSlot.MainHand)) {
            player.getInventory().setItem(8, EquipSlot.get(EquipmentSlot.MainHand).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(8, new ItemStack(Material.AIR));

        if (isEquip(EquipmentSlot.OffHand)) {
            player.getInventory().setItem(40, EquipSlot.get(EquipmentSlot.OffHand).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(40, new ItemStack(Material.AIR));

        if (isEquip(EquipmentSlot.Armor)) {
            player.getInventory().setItem(38, EquipSlot.get(EquipmentSlot.Armor).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(38, new ItemStack(Material.AIR));
        playerData.Status.StatusUpdate();
    }

    void Equip(EquipmentSlot slot, ItemParameter param) {
        param = param.clone();
        if (param.isEmpty()) return;

        if (EquipSlot.containsKey(slot)) {
            playerData.ItemInventory.addItemParameter(EquipSlot.get(slot), 1);
        }

        EquipSlot.put(slot, param.clone());
        playerData.ItemInventory.removeItemParameter(param, 1);

        player.sendMessage(colored("&e[" + param.Display + "]&aを&e装備&aしました"));
    }

    void unEquip(EquipmentSlot slot) {
        if (EquipSlot.containsKey(slot)) {
            playerData.ItemInventory.addItemParameter(EquipSlot.get(slot), 1);
            player.sendMessage(colored("&e[" + getEquip(slot).Display + "]&aを外しました"));
            EquipSlot.remove(slot);
        }
    }
}
