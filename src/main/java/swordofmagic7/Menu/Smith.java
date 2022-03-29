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
        inv.setItem(1, SmithMenu_MakeEquipmentIcon);
        inv.setItem(2, SmithMenu_UpgradeEquipmentIcon);
        inv.setItem(3, SmithMenu_MaterializationIcon);
        inv.setItem(4, SmithMenu_DecryptionIcon);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
    }

    public void SmithMenuClick(InventoryView view, ItemStack currentItem) {
        if (equalInv(view, SmithMenuDisplay)) {
            if (equalItem(currentItem, SmithMenu_SmeltingIcon)) {
                playerData.Menu.Smelt.SmeltMenuView();
            } else if (equalItem(currentItem, SmithMenu_UpgradeEquipmentIcon)) {
                playerData.Upgrade.UpgradeView();
            } else if (equalItem(currentItem, SmithMenu_MaterializationIcon)) {
                playerData.Menu.SmithEquipment.Materialization();
            } else if (equalItem(currentItem, SmithMenu_MakeEquipmentIcon)) {
                playerData.Menu.smithMake.MakeMenuView();
            } else if (equalItem(currentItem, SmithMenu_DecryptionIcon)) {
                playerData.Menu.SmithEquipment.Decryption();
            }
            playSound(player, SoundList.Click);
        }
    }
}
