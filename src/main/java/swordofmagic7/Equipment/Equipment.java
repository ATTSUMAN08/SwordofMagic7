package swordofmagic7.Equipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.DataBase.ItemFlame;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Equipment {
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<EquipmentSlot, ItemParameter> EquipSlot = new HashMap<>();

    public Equipment(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public boolean isEquip(EquipmentSlot slot) {
        return EquipSlot.containsKey(slot);
    }

    public boolean isWeaponEquip() {
        return EquipSlot.containsKey(EquipmentSlot.MainHand) && playerData.Equipment.getEquip(EquipmentSlot.MainHand).Category.isEquipment();
    }

    public ItemParameter getEquip(EquipmentSlot slot) {
        return EquipSlot.getOrDefault(slot, new ItemParameter());
    }

    public void viewEquip() {
        if (isEquip(EquipmentSlot.MainHand)) {
            player.getInventory().setItem(8, EquipSlot.get(EquipmentSlot.MainHand).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(8, new ItemStack(Material.AIR));

        if (isEquip(EquipmentSlot.OffHand)) {
            player.getInventory().setItem(40, EquipSlot.get(EquipmentSlot.OffHand).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(40, new ItemStack(Material.AIR));

        if (isEquip(EquipmentSlot.Armor)) {
            player.getInventory().setItem(36, EquipSlot.get(EquipmentSlot.Armor).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(36, ItemFlame);
        player.getInventory().setItem(37, ItemFlame);
        player.getInventory().setItem(38, ItemFlame);
        playerData.Status.StatusUpdate();
    }

    public boolean Equip(EquipmentSlot slot, ItemParameter param) {
        if (playerData.Skill.isCastReady()) {
            param = param.clone();
            boolean req = false;
            List<String> reqText = new ArrayList<>();
            if (param.isEmpty()) {
                req = true;
                reqText.add("§eアイテムデータ§aが§c不正§aです");
            }
            if (playerData.Level < param.itemEquipmentData.ReqLevel) {
                reqText.add("レベルが足りません");
                req = true;
            }
            if (req) {
                for (String msg : reqText) {
                    player.sendMessage(msg);
                }
                playSound(player, SoundList.Nope);
                return false;
            }

            if (EquipSlot.containsKey(slot)) {
                playerData.ItemInventory.addItemParameter(EquipSlot.get(slot), 1);
            }

            EquipSlot.put(slot, param.clone());
            playerData.ItemInventory.removeItemParameter(param, 1);

            player.sendMessage("§e[" + param.Display + "]§aを§e装備§aしました");
            return true;
        }
        return false;
    }

    public void unEquip(EquipmentSlot slot) {
        if (playerData.Skill.isCastReady()) {
            if (EquipSlot.containsKey(slot)) {
                playerData.ItemInventory.addItemParameter(EquipSlot.get(slot), 1);
                player.sendMessage("§e[" + getEquip(slot).Display + "]§aを外しました");
                EquipSlot.remove(slot);
            }
        }
    }
}
