package swordofmagic7.Item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Menu.Data.UpgradeDisplay;

public class Upgrade {

    private Player player;
    private PlayerData playerData;

    public Upgrade(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    private ItemParameter UpgradeCache;
    public void UpgradeClick(InventoryView view, ItemStack currentItem, int index) {
        if (equalInv(view, UpgradeDisplay)) {
            if (index > -1) {
                ItemParameter itemParameter = playerData.ItemInventory.getItemParameter(index);
                if (itemParameter.Category == ItemCategory.Equipment) {
                    if (UpgradeCache != null) {
                        playerData.ItemInventory.addItemParameter(UpgradeCache, 1);
                    }
                    UpgradeCache = itemParameter.clone();
                    player.getOpenInventory().getTopInventory().setItem(10, UpgradeCache.viewItem(1, playerData.ViewFormat()));
                }
            }
        }
    }
}
