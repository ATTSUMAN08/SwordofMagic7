package swordofmagic7.Item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.UpgradeDisplay;

public class Upgrade {

    private Player player;
    private PlayerData playerData;

    public Upgrade(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    void UpgradeView() {
        playerData.Menu.ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.ItemInventory, false);
        Inventory inv = decoAnvil(UpgradeDisplay);
        player.openInventory(inv);
    }

    private ItemParameter UpgradeCache;
    public void UpgradeClick(InventoryView view, Inventory ClickInventory, ItemStack currentItem, int index, int Slot) {
        if (equalInv(view, UpgradeDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot < 2) {
                    if (UpgradeCache != null) {
                        playerData.ItemInventory.addItemParameter(UpgradeCache, 1);
                        UpgradeCache = null;
                    }
                }
            } else if (index > -1) {
                ItemParameter itemParameter = playerData.ItemInventory.getItemParameter(index);
                if (itemParameter.Category == ItemCategory.Equipment) {
                    if (UpgradeCache != null) {
                        playerData.ItemInventory.addItemParameter(UpgradeCache, 1);
                    }
                    UpgradeCache = itemParameter.clone();
                }
            }
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (UpgradeCache != null) {
                inv.setItem(0, UpgradeCache.viewItem(1, playerData.ViewFormat()));
            } else {
                inv.setItem(0, AirItem);
                inv.setItem(1, AirItem);
                inv.setItem(2, AirItem);
            }
        }
    }
}
