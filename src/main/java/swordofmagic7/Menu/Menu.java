package swordofmagic7.Menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.ItemCategory;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;

import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.*;
import static swordofmagic7.Shop.PetShop.PetSellDisplay;
import static swordofmagic7.Shop.PetShop.PetSyntheticDisplay;
import static swordofmagic7.Shop.RuneShop.RuneEquipDisplay;
import static swordofmagic7.Shop.RuneShop.RuneShopMenuDisplay;
import static swordofmagic7.Shop.Shop.ShopSellDisplay;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Menu {

    private final Player player;
    private final PlayerData playerData;
    public final StatusInfo StatusInfo;
    public final Setting Setting;
    public final Trigger Trigger;
    public final Smith Smith;

    public Menu(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        StatusInfo = new StatusInfo(player, playerData);
        Setting = new Setting(player, playerData);
        Trigger = new Trigger(player, playerData);
        Smith = new Smith(player, playerData);
    }

    public void UserMenuView() {
        Inventory inv = decoInv(UserMenuDisplay, 1);
        inv.setItem(0, UserMenu_ItemInventory);
        inv.setItem(1, UserMenu_RuneInventory);
        inv.setItem(2, UserMenu_PetInventory);
        inv.setItem(3, UserMenu_HotBar);
        inv.setItem(4, UserMenu_AttributeMenuIcon);
        inv.setItem(5, UserMenu_SkillMenuIcon);
        inv.setItem(6, UserMenu_TriggerMenuIcon);
        inv.setItem(7, UserMenu_StatusInfoIcon);
        inv.setItem(8, UserMenu_SettingMenuIcon);
        player.openInventory(inv);
    }

    public static boolean ignoreSlot(int slot) {
        return (8 < slot && slot < 36 && slot != 17 && slot != 26 && slot != 35);
    }

    static int slotToIndex(int slot) {
        if (26 < slot && slot < 35) {
            slot -= 3;
        } else if (17 < slot && slot < 26) {
            slot -= 2;
        } else if (8 < slot && slot < 17) {
            slot -= 1;
        }
        return slot;
    }

    public ViewInventoryType ViewInventoryCache;

    public boolean EquipAble() {
        InventoryView view = player.getOpenInventory();
        return !(equalInv(view, RuneEquipDisplay)
                || equalInv(view, UpgradeDisplay)
                || equalInv(view, ShopSellDisplay)
                || equalInv(view, PetSyntheticDisplay)
                || equalInv(view, PetSellDisplay)
                );
    }

    public void MenuClick(InventoryClickEvent event) {
        final InventoryView view = event.getView();
        final ItemStack currentItem = event.getCurrentItem();
        final InventoryAction action = event.getAction();
        final Inventory ClickInventory = event.getClickedInventory();
        final int Slot = event.getSlot();
        event.setCancelled(true);

        if (Slot == 39) {
            playerData.Menu.StatusInfo.StatusInfoView(player);
            return;
        }
        int index = -1;
        if (currentItem != null && ClickInventory == view.getBottomInventory()) {
            if (ignoreSlot(Slot) && currentItem.hasItemMeta() && playerData.ViewInventory != ViewInventoryType.HotBar) {
                ItemMeta meta = currentItem.getItemMeta();
                if (meta.hasLore()) {
                    List<String> Lore = meta.getLore();
                    index = Integer.parseInt(Lore.get(Lore.size() - 1).replace("§8", ""));
                }
            }
            switch (Slot) {
                case 0,1,2,3,4,5,6,7 -> {
                    playerData.HotBar.setSelectSlot(slotToIndex(Slot));
                    Trigger.TriggerMenuView();
                    playSound(player, SoundList.Click);
                }
                case 8 -> playerData(player).Equipment.unEquip(EquipmentSlot.MainHand);
                case 40 -> playerData(player).Equipment.unEquip(EquipmentSlot.OffHand);
                case 38 -> playerData(player).Equipment.unEquip(EquipmentSlot.Armor);
            }

            if (playerData.ViewInventory.isItem()) {
                switch (Slot) {
                    case 17 -> playerData.ItemInventory.upScrollTick();
                    case 35 -> playerData.ItemInventory.downScrollTick(playerData.ItemInventory.getList().size());
                    default -> {
                        if (index > -1) {
                            ItemParameter clickedItem = playerData.ItemInventory.getItemParameter(index);
                            if (clickedItem != null) {
                                if (EquipAble() && clickedItem.Category == ItemCategory.Equipment) {
                                    playerData(player).Equipment.Equip(clickedItem.itemEquipmentData.EquipmentSlot, clickedItem);
                                    playSound(player, SoundList.Click);
                                } else if (clickedItem.Category.isPetEgg()) {
                                    clickedItem.itemPetEgg.usePetEgg(player, clickedItem);
                                    playSound(player, SoundList.Click);
                                } else if (clickedItem.Category.isPotion()) {
                                    clickedItem.itemPotion.usePotion(player, clickedItem);
                                } else if (clickedItem.Category.isTool()) {
                                    playerData(player).Equipment.Equip(EquipmentSlot.MainHand, clickedItem);
                                    playSound(player, SoundList.Click);
                                } else if (clickedItem.Category.isPetFood()) {
                                    clickedItem.itemPetFood.usePetFood(player, clickedItem);
                                }
                            }
                        }
                    }
                }
            } else if (playerData.ViewInventory.isRune()) {
                switch (Slot) {
                    case 17 -> playerData.RuneInventory.upScrollTick();
                    case 35 -> playerData.RuneInventory.downScrollTick(playerData.RuneInventory.getList().size());
                }
            } else if (playerData.ViewInventory.isPet()) {
                switch (Slot) {
                    case 17 -> playerData.PetInventory.upScrollTick();
                    case 35 -> playerData.PetInventory.downScrollTick(playerData.PetInventory.getList().size());
                    default -> {
                        if (EquipAble() && index > -1) {
                            playerData.PetInventory.getPetParameter(index).spawn();
                        }
                    }
                }
            } else if (playerData.ViewInventory.isHotBar()) {
                if (8 < Slot && Slot < 36)
                switch (Slot) {
                    case 17, 26, 35 -> {}
                    default -> {
                        playerData.HotBar.setSelectSlot(slotToIndex(Slot));
                        Trigger.TriggerMenuView();
                        playSound(player, SoundList.Click);
                    }
                }
            }

            if (Slot == 26) {
                if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    switch (playerData.ViewInventory) {
                        case ItemInventory -> playerData.setView(ViewInventoryType.RuneInventory);
                        case RuneInventory -> playerData.setView(ViewInventoryType.PetInventory);
                        case PetInventory -> playerData.setView(ViewInventoryType.HotBar);
                        case HotBar -> playerData.setView(ViewInventoryType.ItemInventory);
                    }
                    playSound(player, SoundList.Click);
                } else {
                    playerData.Menu.UserMenuView();
                    playSound(player, SoundList.MenuOpen);
                }
            }
        }

        if (currentItem != null) {
            playerData.RuneShop.RuneMenuClick(view, ClickInventory, currentItem, index, Slot);
            playerData.PetShop.PetShopClick(view, ClickInventory, currentItem, index, Slot);
            playerData.Upgrade.UpgradeClick(view, ClickInventory, index, Slot);
            playerData.Shop.ShopSellClick(view, ClickInventory, index, Slot);
            if (ClickInventory == view.getTopInventory()) {
                playerData.Classes.ClassSelectClick(event.getView(), event.getSlot());
                playerData.Attribute.AttributeMenuClick(event.getView(), event.getCurrentItem());
                playerData.Skill.SkillMenuClick(view, Slot);
                playerData.Shop.ShopClick(view, Slot);
                playerData.MapManager.TeleportGateMenuClick(view, Slot);
                Setting.SettingMenuClick(view, currentItem);
                Trigger.TriggerMenuClick(view, currentItem, Slot);
                Smith.SmithMenuClick(view, currentItem);
            } else if (ClickInventory == view.getBottomInventory()) {

            }
        }

        if (equalInv(view, UserMenuDisplay) && ClickInventory == view.getTopInventory()) {
            if (currentItem != null) {
                if (ClickInventory == view.getTopInventory()) {
                    if (equalItem(currentItem, UserMenu_ItemInventory)) {
                        playerData.setView(ViewInventoryType.ItemInventory);
                    } else if (equalItem(currentItem, UserMenu_RuneInventory)) {
                        playerData.setView(ViewInventoryType.RuneInventory);
                    } else if (equalItem(currentItem, UserMenu_PetInventory)) {
                        playerData.setView(ViewInventoryType.PetInventory);
                    } else if (equalItem(currentItem, UserMenu_HotBar)) {
                        playerData.setView(ViewInventoryType.HotBar);
                    } else if (equalItem(currentItem, UserMenu_SettingMenuIcon)) {
                        Setting.SettingMenuView();
                    } else if (equalItem(currentItem, UserMenu_SkillMenuIcon)) {
                        playerData.Skill.SkillMenuView();
                    } else if (equalItem(currentItem, UserMenu_RuneMenuIcon)) {
                        playerData.RuneShop.RuneMenuView();
                    } else if (equalItem(currentItem, UserMenu_TriggerMenuIcon)) {
                        Trigger.TriggerMenuView();
                    } else if (equalItem(currentItem, UserMenu_AttributeMenuIcon)) {
                        playerData.Attribute.AttributeMenuView();
                    } else if (equalItem(currentItem, UserMenu_StatusInfoIcon)) {
                        StatusInfo.StatusInfoView(player);
                    }
                    playSound(player, SoundList.Click);
                }
            }
        }
    }

    public void MenuClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        player.setItemOnCursor(AirItem);
        playerData.RuneShop.RuneMenuClose(view);
        playerData.Upgrade.UpgradeClose(view);
        playerData.Shop.ShopClose();
        playerData.PetShop.PetSyntheticClose(view);
        if (equalInv(view, TriggerMenuDisplay)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!equalInv(player.getOpenInventory(), TriggerMenuDisplay)) {
                    playerData.HotBar.setSelectSlot(-1);
                    playerData.viewUpdate();
                }
            }, 1);
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                playSound(player, SoundList.MenuClose);
                if (EquipAble()) {
                    if (ViewInventoryCache != null && playerData.ViewInventory != ViewInventoryCache) {
                        playerData.setView(ViewInventoryCache, false);
                        ViewInventoryCache = null;
                    }
                }
            }
        }, 1);
        if (!playerData.CastMode.isHold()) {
            player.getInventory().setHeldItemSlot(8);
        }
    }
}
