package swordofmagic7.Menu;

import net.somrpg.swordofmagic7.SomCore;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.geysermc.floodgate.api.FloodgateApi;
import swordofmagic7.Client;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneInfo;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Life.Cook.Cook;
import swordofmagic7.Life.Smith.Smelt;
import swordofmagic7.Life.Smith.SmithEquipment;
import swordofmagic7.Life.Smith.SmithMake;
import swordofmagic7.Market.Market;
import swordofmagic7.Mob.MobInfo;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Shop.AccessoryShop;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TagGame;
import swordofmagic7.Tutorial;

import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Data.DataBase.ItemFlame;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Item.ItemUse.useItem;
import static swordofmagic7.Life.Smith.SmithEquipment.SmeltEquipmentMaterializationDisplay;
import static swordofmagic7.Menu.Data.*;
import static swordofmagic7.Shop.PetShop.*;
import static swordofmagic7.Shop.RuneShop.RuneEquipDisplay;
import static swordofmagic7.Shop.Shop.ShopSellDisplay;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Menu {

    private final Player player;
    private final PlayerData playerData;
    public final StatusInfo StatusInfo;
    public final Setting Setting;
    public final Trigger Trigger;
    public final Smith Smith;
    public final TitleMenu TitleMenu;
    public final Cook Cook;
    public final Smelt Smelt;
    public final Market Market;
    public final SmithEquipment SmithEquipment;
    public final SmithMake smithMake;
    public final MobInfo mobInfo;
    public final RuneInfo runeInfo;

    public Menu(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        StatusInfo = new StatusInfo(player, playerData);
        Setting = new Setting(player, playerData);
        Trigger = new Trigger(player, playerData);
        Smith = new Smith(player, playerData);
        TitleMenu = new TitleMenu(playerData);
        Cook = new Cook(playerData);
        Smelt = new Smelt(playerData);
        Market = new Market(playerData);
        SmithEquipment = new SmithEquipment(playerData);
        smithMake = new SmithMake(playerData);
        mobInfo = new MobInfo(playerData);
        runeInfo = new RuneInfo(playerData);
    }

    public void UserMenuView() {
        Inventory inv = decoInv(UserMenuDisplay, 3);
        inv.setItem(0, UserMenu_ItemInventory);
        inv.setItem(1, UserMenu_RuneInventory);
        inv.setItem(2, UserMenu_PetInventory);
        inv.setItem(3, UserMenu_HotBar);
        inv.setItem(8, UserMenu_SpawnIcon);

        inv.setItem(18, UserMenu_AttributeMenuIcon);
        inv.setItem(19, UserMenu_SkillMenuIcon);
        inv.setItem(20, UserMenu_TriggerMenuIcon);
        inv.setItem(21, UserMenu_StatusInfoIcon);
        inv.setItem(22, UserMenu_TitleMenuIcon);
        inv.setItem(23, UserMenu_SettingMenuIcon);

        for (int i = 9; i < 18; i++) {
            inv.setItem(i, ItemFlame);
        }
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
                || equalInv(view, PetEvolutionDisplay)
                || equalInv(view, SmeltEquipmentMaterializationDisplay)
                || equalInv(view, AccessoryShop.ReLotteryDisplay)
                );
    }

    public synchronized void MenuClick(InventoryClickEvent event) {
        final InventoryView view = event.getView();
        final ItemStack currentItem = event.getCurrentItem();
        final InventoryAction action = event.getAction();
        final ClickType clickType = event.getClick();
        final Inventory ClickInventory = event.getClickedInventory();
        final int Slot = event.getSlot();
        event.setCancelled(true);
        if (clickType.isKeyboardClick()) return;

        int index = -1;
        try {
            if (currentItem != null && currentItem.hasItemMeta()) {
                ItemMeta meta = currentItem.getItemMeta();
                if (meta.hasLore()) {
                    List<String> Lore = meta.getLore();
                    index = Integer.parseInt(Lore.getLast().replace("§8SlotID:", ""));
                }
            }
        } catch (Exception ignored) {}
        if (!FloodgateApi.getInstance().isFloodgateId(player.getUniqueId()) && ClickInventory != null && ClickInventory.getType() == InventoryType.CRAFTING) {
            switch (Slot) {
                case 1 -> playerData.setView(ViewInventoryType.ItemInventory);
                case 2 -> playerData.setView(ViewInventoryType.RuneInventory);
                case 3 -> playerData.setView(ViewInventoryType.PetInventory);
                case 4 -> playerData.setView(ViewInventoryType.HotBar);
            }
        }
        if (currentItem != null && ClickInventory == view.getBottomInventory()) {
            switch (Slot) {
                case 0,1,2,3,4,5,6,7 -> {
                    if (clickType.isRightClick()) {
                        playerData.HotBar.use(slotToIndex(Slot));
                    } else {
                        playerData.HotBar.setSelectSlot(slotToIndex(Slot));
                        Trigger.TriggerMenuView();
                        playSound(player, SoundList.CLICK);
                    }
                }
                case 8 -> playerData.Equipment.unEquip(EquipmentSlot.MainHand);
                case 40 -> playerData.Equipment.unEquip(EquipmentSlot.OffHand);
                case 36 -> playerData.Equipment.unEquip(EquipmentSlot.Armor);
                case 37 -> playerData.Equipment.unEquip(EquipmentSlot.Accessory);
                case 39 -> playerData.Menu.StatusInfo.StatusInfoView(player);
            }

            boolean viewAble = view.getType() == InventoryType.CRAFTING || equalInv(view, UserMenuDisplay);
            if (playerData.ViewInventory.isItem()) {
                switch (Slot) {
                    case 17 -> playerData.ItemInventory.upScrollTick();
                    case 35 -> playerData.ItemInventory.downScrollTick(playerData.ItemInventory.getList().size());
                    default -> {
                        if (index > -1 && Slot != 26) {
                            ItemParameterStack clickedItemStack = playerData.ItemInventory.getItemParameterStack(index);
                            ItemParameter clickedItem = playerData.ItemInventory.getItemParameter(index);
                            if (clickedItem != null) {
                                if (viewAble && clickType.isShiftClick() && clickType.isRightClick()) {
                                    Client.sendPlayerChat(player, clickedItem.getTextView(clickedItemStack.Amount, playerData.ViewFormat()));
                                } else if (EquipAble() && clickedItem.Category.isEquipment()) {
                                    playerData(player).Equipment.Equip(clickedItem.itemEquipmentData.EquipmentSlot, clickedItem);
                                    playSound(player, SoundList.CLICK);
                                } else if (EquipAble() && clickedItem.Category.isPetEgg()) {
                                    clickedItem.itemPetEgg.usePetEgg(player, clickedItem);
                                    playSound(player, SoundList.CLICK);
                                } else if (EquipAble() && clickedItem.Category.isPotion()) {
                                    clickedItem.itemPotion.usePotion(player, clickedItem);
                                } else if (EquipAble() && clickedItem.Category.isCook()) {
                                    clickedItem.itemCook.useCook(player, clickedItem);
                                } else if (EquipAble() && clickedItem.Category.isTool()) {
                                    playerData(player).Equipment.Equip(EquipmentSlot.MainHand, clickedItem);
                                    playSound(player, SoundList.CLICK);
                                } else if (EquipAble() && clickedItem.Category.isPetFood()) {
                                    clickedItem.itemPetFood.usePetFood(player, clickedItem);
                                } else if (EquipAble() && clickedItem.Category.isItem()) {
                                    useItem(playerData, clickedItemStack);
                                }
                            }
                        }
                    }
                }
            } else if (playerData.ViewInventory.isRune()) {
                switch (Slot) {
                    case 17 -> playerData.RuneInventory.upScrollTick();
                    case 35 -> playerData.RuneInventory.downScrollTick(playerData.RuneInventory.getList().size());
                    default -> {
                        if (viewAble && index > -1 && clickType.isShiftClick() && clickType.isRightClick() && Slot != 26) {
                            RuneParameter rune = playerData.RuneInventory.getRuneParameter(index);
                            Client.sendPlayerChat(player, rune.getTextView(playerData.ViewFormat()));
                        }
                    }
                }
            } else if (playerData.ViewInventory.isPet()) {
                switch (Slot) {
                    case 17 -> playerData.PetInventory.upScrollTick();
                    case 35 -> playerData.PetInventory.downScrollTick(playerData.PetInventory.getList().size());
                    default -> {
                        if (viewAble && index > -1 && clickType.isShiftClick() && clickType.isRightClick() && Slot != 26) {
                            PetParameter pet = playerData.PetInventory.getPetParameter(index);
                            Client.sendPlayerChat(player, pet.getTextView(playerData.ViewFormat()));
                        } else if (EquipAble() && index > -1) {
                            PetParameter pet = playerData.PetInventory.getPetParameter(index);
                            if (pet != null) pet.spawn();
                        }
                    }
                }
            } else if (playerData.ViewInventory.isHotBar()) {
                if (8 < Slot && Slot < 36) {
                    switch (Slot) {
                        case 26 -> {}
                        case 17 -> playerData.HotBar.ScrollUp();
                        case 35 -> playerData.HotBar.ScrollDown();
                        default -> {
                            if (clickType.isRightClick()) {
                                playerData.HotBar.use(slotToIndex(Slot));
                            } else {
                                playerData.HotBar.setSelectSlot(slotToIndex(Slot));
                                Trigger.TriggerMenuView();
                                playSound(player, SoundList.CLICK);
                            }
                        }
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
                    playSound(player, SoundList.CLICK);
                } else {
                    playerData.Menu.UserMenuView();
                    playSound(player, SoundList.MENU_OPEN);
                }
            }
        }

        if (currentItem != null) {
            playerData.RuneShop.RuneMenuClick(view, ClickInventory, clickType, currentItem, index, Slot);
            playerData.accessoryShop.AccessoryMenuClick(view, ClickInventory, clickType, currentItem, index, Slot);
            playerData.PetShop.PetShopClick(view, ClickInventory, currentItem, index, Slot);
            playerData.PetEvolution.PetEvolutionClick(view, ClickInventory, index, Slot);
            playerData.Upgrade.UpgradeClick(view, ClickInventory, index, Slot);
            playerData.Shop.ShopSellClick(view, ClickInventory, clickType, index, Slot);
            SmithEquipment.SmeltMenuClick(view, ClickInventory, index, Slot);
            if (ClickInventory == view.getTopInventory()) {
                playerData.Classes.classSelectClick(view, Slot);
                playerData.Attribute.AttributeMenuClick(view, clickType, currentItem);
                playerData.Skill.SkillMenuClick(view, Slot);
                playerData.Shop.ShopClick(view, currentItem, clickType, Slot, index);
                playerData.MapManager.TeleportGateMenuClick(view, Slot);
                Setting.SettingMenuClick(view, currentItem);
                Trigger.TriggerMenuClick(view, currentItem, Slot);
                Smith.SmithMenuClick(view, currentItem);
                TitleMenu.TitleMenuClick(view, currentItem, Slot);
                Cook.CookMenuClick(view, currentItem, clickType, Slot);
                Smelt.SmeltMenuClick(view, currentItem, Slot);
                Market.MarketMenuClick(view, currentItem, Slot);
                smithMake.MakeMenuClick(view, currentItem, Slot);
                mobInfo.MobInfoClick(view, currentItem, Slot);
                runeInfo.RuneInfoClick(view, currentItem, Slot);
                playerData.Skill.getAlchemist().AlchemyClick(view, currentItem, Slot);
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
                    } else if (equalItem(currentItem, UserMenu_TitleMenuIcon)) {
                        TitleMenu.TitleMenuView();
                    } else if (equalItem(currentItem, UserMenu_SpawnIcon)) {
                        if (TagGame.isTagPlayerNonMessage(player)) return;
                        if (playerData.isPvPModeNonMessage()) return;
                        if (Tutorial.TutorialProcess.containsKey(player)) {
                            player.sendMessage("§eチュートリアル中§aは使用できません");
                            playSound(player, SoundList.NOPE);
                        } else SomCore.instance.spawnPlayer(player);
                    }
                    playSound(player, SoundList.CLICK);
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
        playerData.PetEvolution.PetEvolutionClose(view);
        SmithEquipment.SmeltMenuClose(view);
        if (equalInv(view, TriggerMenuDisplay)) {
            MultiThread.TaskRunSynchronizedLater(() -> {
                if (!equalInv(player.getOpenInventory(), TriggerMenuDisplay)) {
                    playerData.HotBar.setSelectSlot(-1);
                    playerData.viewUpdate();
                }
            }, 1);
        }
        MultiThread.TaskRun(() -> {
            MultiThread.sleepTick(1);
            if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                playSound(player, SoundList.MENU_CLOSE);
                if (EquipAble()) {
                    if (ViewInventoryCache != null && playerData.ViewInventory != ViewInventoryCache) {
                        playerData.setView(ViewInventoryCache, false);
                        ViewInventoryCache = null;
                    }
                }
            }
        }, "MenuClose");
        if (!playerData.CastMode.isHold()) {
            player.getInventory().setHeldItemSlot(8);
        }
    }
}
