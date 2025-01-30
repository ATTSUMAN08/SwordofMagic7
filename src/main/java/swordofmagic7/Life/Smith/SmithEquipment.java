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
import swordofmagic7.Item.Upgrade;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class SmithEquipment {

    private final Player player;
    private final PlayerData playerData;

    public static final String SmeltEquipmentMaterializationDisplay = "§l装備素材化";
    public static final String SmeltEquipmentDecryptionDisplay = "§l素材化復号";

    public SmithEquipment(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.player;
    }

    public ItemParameter[] MaterializationCache = new ItemParameter[2];
    public ItemParameter[] DecryptionCache = new ItemParameter[8];
    public void Materialization() {
        Inventory inv = decoAnvil(SmeltEquipmentMaterializationDisplay);
        playerData.setView(ViewInventoryType.ItemInventory, false);
        player.openInventory(inv);
        playSound(player, SoundList.MENU_OPEN);
    }

    EquipmentCategory selectCategory = EquipmentCategory.Blade;
    public void Decryption() {
        selectCategory = EquipmentCategory.Blade;
        Inventory inv = decoInv(SmeltEquipmentDecryptionDisplay, 1);
        inv.setItem(1, ItemFlame);
        playerData.setView(ViewInventoryType.ItemInventory, false);
        player.openInventory(inv);
        playSound(player, SoundList.MENU_OPEN);
    }

    public void SmeltMenuClick(InventoryView view, Inventory ClickInventory, int index, int Slot) {
        if (equalInv(view, SmeltEquipmentMaterializationDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot == AnvilUISlot[0] && MaterializationCache[0] != null) {
                    playerData.ItemInventory.addItemParameter(MaterializationCache[0], 1);
                    MaterializationCache[0] = null;
                    MaterializationCache[1] = null;
                } else if (Slot == AnvilUISlot[2] && MaterializationCache[0] != null && MaterializationCache[1] != null) {
                    playerData.ItemInventory.addItemParameter(MaterializationCache[1], MaterializationCache[0].itemEquipmentData.EquipmentSlot == EquipmentSlot.MainHand ? 2 : 1);
                    Function.sendMessage(player, "§e[" + MaterializationCache[0].Display + "]§aを素材化しました", SoundList.LEVEL_UP);
                    if (MaterializationCache[0].Category.isEquipment()) {
                        int plus = MaterializationCache[0].itemEquipmentData.Plus;
                        if (plus >= 10) {
                            int upgradeCost = MaterializationCache[0].itemEquipmentData.UpgradeCost;
                            int returnCost = Math.round(upgradeCost/2f * plus);
                            if (returnCost > 0) playerData.ItemInventory.addItemParameter(Upgrade.UpgradeStone, returnCost);
                            ItemGetLog(player, Upgrade.UpgradeStone, returnCost);
                        }
                    }
                    MaterializationCache[0] = null;
                    MaterializationCache[1] = null;
                }
            } else if (index > -1) {
                MaterializationCache[1] = null;
                ItemParameter item = playerData.ItemInventory.getItemParameter(index);
                if (item.itemEquipmentData.Rune.size() > 0) {
                    Function.sendMessage(player, "§eルーン§aを外してください", SoundList.NOPE);
                    return;
                }
                if (item.Materialization != null) {
                    if (ItemList.containsKey("素材化装備" + item.Materialization)) {
                        MaterializationCache[1] = DataBase.getItemParameter("素材化装備" + item.Materialization);
                        if (MaterializationCache[0] != null)
                            playerData.ItemInventory.addItemParameter(MaterializationCache[0], 1);
                        MaterializationCache[0] = item;
                        playerData.ItemInventory.removeItemParameter(item, 1);
                    } else {
                        Function.sendMessage(player, "§a素材化アイテムが未実装です", SoundList.NOPE);
                    }
                } else {
                    Function.sendMessage(player, "§aこの装備は素材化出来ません", SoundList.NOPE);
                }
            }
            if (MaterializationCache[0] != null && MaterializationCache[1] != null) {
                view.getTopInventory().setItem(AnvilUISlot[0], MaterializationCache[0].viewItem(1, playerData.ViewFormat()));
                view.getTopInventory().setItem(AnvilUISlot[1], AirItem);
                view.getTopInventory().setItem(AnvilUISlot[2], MaterializationCache[1].viewItem(MaterializationCache[0].itemEquipmentData.EquipmentSlot == EquipmentSlot.MainHand ? 2 : 1, playerData.ViewFormat()));
            } else {
                view.getTopInventory().setItem(AnvilUISlot[0], AirItem);
                view.getTopInventory().setItem(AnvilUISlot[1], AirItem);
                view.getTopInventory().setItem(AnvilUISlot[2], AirItem);
            }
        } else if (equalInv(view, SmeltEquipmentDecryptionDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (DecryptionCache[0] != null) {
                    int index2 = Slot-1;
                   if (0 == Slot) {
                        DecryptionCache[0] = null;
                   } else if (1 < Slot && DecryptionCache[index2] != null) {
                        int amount = DecryptionCache[index2].itemEquipmentData.EquipmentSlot == EquipmentSlot.MainHand ? 2 : 1;
                        if (playerData.ItemInventory.hasItemParameter(DecryptionCache[0], amount)) {
                            playerData.ItemInventory.addItemParameter(DecryptionCache[index2], 1);
                            playerData.ItemInventory.removeItemParameter(DecryptionCache[0], amount);
                            sendMessage(player, "§e[" + DecryptionCache[index2].Display + "§e]§aを§b復号§aしました");
                        } else {
                            sendMessage(player, "§e[" + DecryptionCache[0].Display + "§ax" + amount + "§e]§aが必要です");
                        }
                   }
                }
            } else if (index > -1) {
                ItemParameter item = playerData.ItemInventory.getItemParameter(index);
                if (item.Category.isMaterialization()) {
                    DecryptionCache[0] = item;
                } else {
                    player.sendMessage("§e素材化装備§aを選択してください");
                    playSound(player, SoundList.NOPE);
                }
            }
            if (DecryptionCache[0] != null) {
                view.getTopInventory().setItem(0, DecryptionCache[0].viewItem(1, playerData.ViewFormat()));
                int i = 1;
                for (String itemId : MaterializationMap.get(DecryptionCache[0].Materialization)) {
                    DecryptionCache[i] = DataBase.getItemParameter(itemId);
                    view.getTopInventory().setItem(i+1, DecryptionCache[i].viewItem(1, playerData.ViewFormat()));
                    i++;
                    if (DecryptionCache.length == i) break;
                }
            } else {
                view.getTopInventory().setItem(0, AirItem);
                for (int i = 2; i < 9; i++) {
                    view.getTopInventory().setItem(i, AirItem);
                }
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