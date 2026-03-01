package swordofmagic7.Menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Function.decoInv;
import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Function.equalItem;
import static swordofmagic7.Menu.Data.SettingMenuDisplay;
import static swordofmagic7.Menu.Data.SettingMenu_CastModeIcon;
import static swordofmagic7.Menu.Data.SettingMenu_DamageLogIcon;
import static swordofmagic7.Menu.Data.SettingMenu_DropLogIcon;
import static swordofmagic7.Menu.Data.SettingMenu_ExpLogIcon;
import static swordofmagic7.Menu.Data.SettingMenu_FishingDisplayNumIcon;
import static swordofmagic7.Menu.Data.SettingMenu_FishingUseCombo;
import static swordofmagic7.Menu.Data.SettingMenu_HoloSelfViewIcon;
import static swordofmagic7.Menu.Data.SettingMenu_ItemInventorySort;
import static swordofmagic7.Menu.Data.SettingMenu_ItemInventorySortReverse;
import static swordofmagic7.Menu.Data.SettingMenu_NaturalMessageIcon;
import static swordofmagic7.Menu.Data.SettingMenu_PetInventorySort;
import static swordofmagic7.Menu.Data.SettingMenu_PetInventorySortReverse;
import static swordofmagic7.Menu.Data.SettingMenu_PvPModeIcon;
import static swordofmagic7.Menu.Data.SettingMenu_RuneInventorySort;
import static swordofmagic7.Menu.Data.SettingMenu_RuneInventorySortReverse;
import static swordofmagic7.Menu.Data.SettingMenu_ShopAmountResetIcon;
import static swordofmagic7.Menu.Data.SettingMenu_StrafeModeIcon;
import static swordofmagic7.Menu.Data.SettingMenu_ViewFormat;

public class Setting {

    private final Player player;
    private final PlayerData playerData;

    Setting(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }
    public void SettingMenuView() {
        Inventory inv = decoInv(SettingMenuDisplay, 2);
        inv.setItem(0, SettingMenu_ItemInventorySort);
        inv.setItem(9, SettingMenu_ItemInventorySortReverse);
        inv.setItem(1, SettingMenu_RuneInventorySort);
        inv.setItem(10, SettingMenu_RuneInventorySortReverse);
        inv.setItem(2, SettingMenu_PetInventorySort);
        inv.setItem(11, SettingMenu_PetInventorySortReverse);

        inv.setItem(3, SettingMenu_DamageLogIcon);
        inv.setItem(4, SettingMenu_ExpLogIcon);
        inv.setItem(5, SettingMenu_DropLogIcon);
        inv.setItem(6, SettingMenu_NaturalMessageIcon);
        inv.setItem(7, SettingMenu_StrafeModeIcon);
        inv.setItem(8, SettingMenu_CastModeIcon);
        inv.setItem(12, SettingMenu_ShopAmountResetIcon);
        inv.setItem(13, SettingMenu_PvPModeIcon);
        inv.setItem(14, SettingMenu_ViewFormat);
        inv.setItem(15, SettingMenu_HoloSelfViewIcon);
        inv.setItem(16, SettingMenu_FishingDisplayNumIcon);
        inv.setItem(17, SettingMenu_FishingUseCombo);
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
            } else if (equalItem(currentItem, SettingMenu_NaturalMessageIcon)) {
                playerData.NaturalMessage();
            } else if (equalItem(currentItem, SettingMenu_ShopAmountResetIcon)) {
                playerData.Shop.AmountReset();
            } else if (equalItem(currentItem, SettingMenu_PvPModeIcon)) {
                playerData.PvPMode();
            } else if (equalItem(currentItem, SettingMenu_ViewFormat)) {
                playerData.changeViewFormat();
            } else if (equalItem(currentItem, SettingMenu_FishingDisplayNumIcon)) {
                playerData.FishingDisplayNum();
            } else if (equalItem(currentItem, SettingMenu_FishingUseCombo)) {
                playerData.FishingUseCombo();
            } else if (equalItem(currentItem, SettingMenu_HoloSelfViewIcon)) {
                playerData.HoloSelfView();
            } else if (equalItem(currentItem, SettingMenu_ItemInventorySort)) {
                playerData.ItemInventory.ItemInventorySort();
            } else if (equalItem(currentItem, SettingMenu_ItemInventorySortReverse)) {
                playerData.ItemInventory.ItemInventorySortReverse();
            } else if (equalItem(currentItem, SettingMenu_RuneInventorySort)) {
                playerData.RuneInventory.RuneInventorySort();
            } else if (equalItem(currentItem, SettingMenu_RuneInventorySortReverse)) {
                playerData.RuneInventory.RuneInventorySortReverse();
            } else if (equalItem(currentItem, SettingMenu_PetInventorySort)) {
                playerData.PetInventory.PetInventorySort();
            } else if (equalItem(currentItem, SettingMenu_PetInventorySortReverse)) {
                playerData.PetInventory.PetInventorySortReverse();
            }
        }
    }
}
