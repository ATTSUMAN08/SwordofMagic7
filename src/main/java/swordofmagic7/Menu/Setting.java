package swordofmagic7.Menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Function.*;
import static swordofmagic7.Function.equalItem;
import static swordofmagic7.Menu.Data.*;

public class Setting {

    private Player player;
    private PlayerData playerData;

    Setting(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }
    public void SettingMenuView() {
        Inventory inv = decoInv(SettingMenuDisplay, 1);
        inv.setItem(0, SettingMenu_DamageLogIcon);
        inv.setItem(1, SettingMenu_ExpLogIcon);
        inv.setItem(2, SettingMenu_DropLogIcon);
        inv.setItem(3, SettingMenu_StrafeModeIcon);
        inv.setItem(4, SettingMenu_CastModeIcon);
        inv.setItem(5, SettingMenu_ShopAmountResetIcon);
        inv.setItem(6, SettingMenu_PvPModeIcon);
        player.openInventory(inv);
    }

    public void SettingMenuClick(InventoryView view, ItemStack currentItem) {
        if (equalInv(view, SettingMenuDisplay)) {
            if (equalItem(currentItem, SettingMenu_DamageLogIcon)) {
                playerData.DamageLog();
            } else if (equalItem(currentItem, SettingMenu_ExpLogIcon)) {
                playerData.ExpLog();
            } else if (equalItem(currentItem, SettingMenu_DropLogIcon)) {
                playerData.DropLog();
            } else if (equalItem(currentItem, SettingMenu_StrafeModeIcon)) {
                playerData.StrafeMode();
            } else if (equalItem(currentItem, SettingMenu_CastModeIcon)) {
                playerData.CastMode();
            } else if (equalItem(currentItem, SettingMenu_ShopAmountResetIcon)) {
                playerData.Shop.AmountReset();
            } else if (equalItem(currentItem, SettingMenu_PvPModeIcon)) {
                playerData.PvPMode();
            }
        }
    }
}
