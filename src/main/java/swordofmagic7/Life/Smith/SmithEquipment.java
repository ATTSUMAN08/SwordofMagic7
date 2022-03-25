package swordofmagic7.Life.Smith;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Function;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class SmithEquipment {

    private final Player player;
    private final PlayerData playerData;

    public static final String SmeltEquipmentMaterializationDisplay = "§l武具素材化";

    public SmithEquipment(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.player;
    }

    public ItemParameter[] MaterializationCache = new ItemParameter[2];
    public void Materialization() {
        Inventory inv = decoAnvil(SmeltEquipmentMaterializationDisplay);
        playerData.setView(ViewInventoryType.ItemInventory, false);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
    }

    public void SmeltMenuClick(InventoryView view, Inventory ClickInventory, int index, int Slot) {
        if (equalInv(view, SmeltEquipmentMaterializationDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot == AnvilUISlot[0] && MaterializationCache[0] != null) {
                    playerData.ItemInventory.addItemParameter(MaterializationCache[0], 1);
                    MaterializationCache[0] = null;
                    MaterializationCache[1] = null;
                } else if (Slot == AnvilUISlot[2] && MaterializationCache[1] != null) {
                    playerData.ItemInventory.addItemParameter(MaterializationCache[1], 1);
                    Function.sendMessage(player, "§e[" + MaterializationCache[0].Display + "]§aを素材化しました", SoundList.LevelUp);
                    MaterializationCache[0] = null;
                    MaterializationCache[1] = null;
                }
            } else {
                MaterializationCache[1] = null;
                ItemParameter item = playerData.ItemInventory.getItemParameter(index);
                if (item.itemEquipmentData.Rune.size() > 0) {
                    Function.sendMessage(player, "§eルーン§aを外してください", SoundList.Nope);
                    return;
                }
                String id = item.Id;
                for (EquipmentCategory category : EquipmentCategory.values()) {
                    if (category.DefaultEquipmentSlot == EquipmentSlot.MainHand) {
                        id = "武器素材" + id.replace(category.Display2, "");
                    } else {
                        id = "防具素材" + id.replace(category.Display2, "");
                    }
                    if (DataBase.getItemList().containsKey(id)) {
                        MaterializationCache[1] = DataBase.getItemParameter(id);
                        break;
                    }
                }
                if (MaterializationCache[1] != null) {
                    if (MaterializationCache[0] != null) {
                        playerData.ItemInventory.addItemParameter(MaterializationCache[0], 1);
                    }
                    MaterializationCache[0] = item;
                    playerData.ItemInventory.removeItemParameter(item, 1);
                } else {
                    Function.sendMessage(player, "§aこの装備は素材化出来ません", SoundList.Nope);
                    return;
                }
            }
            if (MaterializationCache[0] != null) {
                view.getTopInventory().setItem(AnvilUISlot[0], MaterializationCache[0].viewItem(1, playerData.ViewFormat()));
                view.getTopInventory().setItem(AnvilUISlot[1], AirItem);
                view.getTopInventory().setItem(AnvilUISlot[2], MaterializationCache[1].viewItem(1, playerData.ViewFormat()));
            } else {
                view.getTopInventory().setItem(AnvilUISlot[0], AirItem);
                view.getTopInventory().setItem(AnvilUISlot[1], AirItem);
                view.getTopInventory().setItem(AnvilUISlot[2], AirItem);
            }
        }
    }

    public void SmeltMenuClose(InventoryView view) {
        if (equalInv(view, SmeltEquipmentMaterializationDisplay)) {
            if (MaterializationCache[0] != null) playerData.ItemInventory.addItemParameter(MaterializationCache[0], 1);
            MaterializationCache[0] = null;
            MaterializationCache[1] = null;
        }
    }
}