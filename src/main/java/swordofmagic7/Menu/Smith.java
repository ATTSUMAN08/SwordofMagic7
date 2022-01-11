package swordofmagic7.Menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Smith {

    private final Player player;
    private final PlayerData playerData;

    public Smith(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void SmithMenuView() {
        Inventory inv = decoInv(SmithMenuDisplay, 1);
        inv.setItem(0, SmithMenu_SmeltingIcon);
        inv.setItem(1, SmithMenu_CreateEquipmentIcon);
        inv.setItem(2, SmithMenu_UpgradeEquipmentIcon);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
    }

    public void SmithMenuClick(InventoryView view, ItemStack currentItem) {
        if (equalInv(view, SmithMenuDisplay)) {
            if (equalItem(currentItem, SmithMenu_SmeltingIcon)) {

            } else if (equalItem(currentItem, SmithMenu_CreateEquipmentIcon)) {

            } else if (equalItem(currentItem, SmithMenu_UpgradeEquipmentIcon)) {
                playerData.Upgrade.UpgradeView();
            }
            playSound(player, SoundList.Click);
        }
    }
}
