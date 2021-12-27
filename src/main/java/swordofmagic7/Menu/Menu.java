package swordofmagic7.Menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.HotBar.HotBarCategory;
import swordofmagic7.HotBar.HotBarData;
import swordofmagic7.Item.ItemCategory;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Pet.PetData;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Shop.ShopData;
import swordofmagic7.Shop.ShopSlot;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.Status;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Menu {

    private Player player;
    private PlayerData playerData;

    public Menu(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
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

    void SettingMenuView() {
        Inventory inv = decoInv(SettingMenuDisplay, 1);
        inv.setItem(0, SettingMenu_DamageLogIcon);
        inv.setItem(1, SettingMenu_ExpLogIcon);
        inv.setItem(2, SettingMenu_DropLogIcon);
        inv.setItem(3, SettingMenu_StrafeModeIcon);
        inv.setItem(4, SettingMenu_CastModeIcon);
        inv.setItem(5, SettingMenu_PvPModeIcon);
        player.openInventory(inv);
    }


    private final HashMap<Integer, HotBarData> TriggerMenuCache = new HashMap<>();
    void TriggerMenuView() {
        Inventory inv = decoInv(TriggerMenuDisplay, 6);
        ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.HotBar, false);
        int slot = 0;
        int tier = 0;
        while (playerData.Classes.classTier[tier] != null) {
            for (SkillData skill : playerData.Classes.classTier[tier].SkillList) {
                inv.setItem(slot, skill.view());
                TriggerMenuCache.put(slot, new HotBarData(skill));
                slot++;
            }
            tier++;
        }
        inv.setItem(53, TriggerMenu_Reset);
        player.openInventory(inv);
    }

    public void StatusInfoView(Player player) {
        final Inventory inv = decoInv(StatusInfoDisplay, 1);
        final PlayerData playerData = playerData(player);
        final String format = playerData.ViewFormat();
        final Status status = playerData.Status;
        final Player Viewer = this.player;
        new BukkitRunnable() {
            final ItemStack statusIcon = ItemStackPlayerHead(player);
            final ItemMeta statusMeta = statusIcon.getItemMeta();
            @Override
            public void run() {
                if (Viewer.getOpenInventory().getTopInventory().equals(inv)) {
                    statusMeta.setDisplayName(decoText(playerData.Nick));
                    List<String> statusLore = new ArrayList<>();
                    statusLore.add(decoLore("戦闘力") + String.format(format, playerData.Status.getCombatPower()));
                    statusLore.add(decoLore(StatusParameter.MaxHealth.Display) + String.format(format, status.Health) + "/" + String.format(format, status.MaxHealth) + " (" + String.format(format, status.BaseStatus(StatusParameter.MaxHealth)) + ")");
                    statusLore.add(StatusParameter.HealthRegen.DecoDisplay + String.format(format, status.HealthRegen) + " (" + String.format(format, status.BaseStatus(StatusParameter.HealthRegen)) + ")");
                    statusLore.add(decoLore(StatusParameter.MaxMana.Display) + String.format(format, status.Mana) + "/" + String.format(format, status.MaxMana) + " (" + String.format(format, status.BaseStatus(StatusParameter.MaxMana)) + ")");
                    statusLore.add(StatusParameter.ManaRegen.DecoDisplay + String.format(format, status.HealthRegen) + " (" + String.format(format, status.BaseStatus(StatusParameter.HealthRegen)) + ")");
                    statusLore.add(StatusParameter.ATK.DecoDisplay + String.format(format, status.ATK) + " (" + String.format(format, status.BaseStatus(StatusParameter.ATK)) + ")");
                    statusLore.add(StatusParameter.DEF.DecoDisplay + String.format(format, status.DEF) + " (" + String.format(format, status.BaseStatus(StatusParameter.DEF)) + ")");
                    statusLore.add(StatusParameter.ACC.DecoDisplay + String.format(format, status.ACC) + " (" + String.format(format, status.BaseStatus(StatusParameter.ACC)) + ")");
                    statusLore.add(StatusParameter.EVA.DecoDisplay + String.format(format, status.EVA) + " (" + String.format(format, status.BaseStatus(StatusParameter.EVA)) + ")");
                    statusLore.add(StatusParameter.CriticalRate.DecoDisplay + String.format(format, status.CriticalRate) + " (" + String.format(format, status.BaseStatus(StatusParameter.CriticalRate)) + ")");
                    statusLore.add(StatusParameter.CriticalResist.DecoDisplay + String.format(format, status.CriticalResist) + " (" + String.format(format, status.BaseStatus(StatusParameter.CriticalResist)) + ")");
                    //statusLore.add(StatusParameter.SkillCastTime.DecoDisplay + String.format(format, status.SkillCastTime));
                    //statusLore.add(StatusParameter.SkillRigidTime.DecoDisplay + String.format(format, status.SkillRigidTime));
                    //statusLore.add(StatusParameter.SkillCooltime.DecoDisplay + String.format(format, status.SkillCooltime));
                    statusLore.add(decoLore("クリティカルダメージ") + String.format(format, status.CriticalMultiply*100) + "%");
                    statusLore.add(decoLore("物理与ダメージ") + String.format(format, status.DamageCauseMultiply.get(DamageCause.ATK)*100) + "%");
                    statusLore.add(decoLore("魔法与ダメージ") + String.format(format, status.DamageCauseMultiply.get(DamageCause.MAT)*100) + "%");
                    statusLore.add(decoLore("物理被ダメージ耐性") + String.format(format, 1/status.DamageCauseResistance.get(DamageCause.ATK)*100) + "%");
                    statusLore.add(decoLore("魔法被ダメージ耐性") + String.format(format, 1/status.DamageCauseResistance.get(DamageCause.MAT)*100) + "%");
                    statusMeta.setLore(statusLore);
                    statusIcon.setItemMeta(statusMeta);
                    inv.setItem(0, statusIcon);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
        Viewer.openInventory(inv);
    }

    void UpgradeView() {
        ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.ItemInventory, false);
        Inventory inv = decoInv(UpgradeDisplay, 3);
        for (int slot = 0; slot < 27; slot++) {
            inv.setItem(slot, new ItemStack(Material.BROWN_STAINED_GLASS_PANE));
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

    public void MenuClick(InventoryClickEvent event) {
        final InventoryView view = event.getView();
        final ItemStack currentItem = event.getCurrentItem();
        final InventoryAction action = event.getAction();
        final Inventory ClickInventory = event.getClickedInventory();
        final int Slot = event.getSlot();
        event.setCancelled(true);

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
                case 8 -> playerData(player).Equipment.unEquip(EquipmentSlot.MainHand);
                case 40 -> playerData(player).Equipment.unEquip(EquipmentSlot.OffHand);
                case 38 -> playerData(player).Equipment.unEquip(EquipmentSlot.Armor);
            }

            if (playerData.ViewInventory.isItem()) {
                switch (Slot) {
                    case 17 -> playerData.ItemInventory.upScrollTick();
                    case 35 -> playerData.ItemInventory.downScrollTick(playerData.ItemInventory.getList().size());
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
                        if (index > -1) {
                            PetParameter pet = playerData.PetInventory.getPetParameter(index);
                            if (playerData.PetSummon.size() == 0) {
                                pet.spawn(player.getLocation());
                            } else if (pet.Summoned) {
                                pet.cage();
                            }
                        }
                    }
                }
            } else if (playerData.ViewInventory.isHotBar()) {
                if (Slot < 36)
                switch (Slot) {
                    case 8, 17, 26, 35 -> {}
                    default -> {
                        playerData.HotBar.setSelectSlot(slotToIndex(Slot));
                        TriggerMenuView();
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

        if (event.getCurrentItem() != null) {
            if (ClickInventory == view.getTopInventory()) {
                playerData.Classes.ClassSelectClick(event.getView(), event.getSlot());
                playerData.Attribute.AttributeMenuClick(event.getView(), event.getCurrentItem());
                playerData.RuneInventory.RuneMenuClick(view, ClickInventory, currentItem, index, Slot);
                playerData.Skill.SkillMenuClick(view, Slot);
                playerData.Shop.ShopClick(view, Slot);
                playerData.PetManager.PetShopClick(view, currentItem);
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
                        SettingMenuView();
                    } else if (equalItem(currentItem, UserMenu_SkillMenuIcon)) {
                        playerData.Skill.SkillMenuView();
                    } else if (equalItem(currentItem, UserMenu_RuneMenuIcon)) {
                        playerData.RuneInventory.RuneMenuView();
                    } else if (equalItem(currentItem, UserMenu_TriggerMenuIcon)) {
                        TriggerMenuView();
                    } else if (equalItem(currentItem, UserMenu_AttributeMenuIcon)) {
                        playerData.Attribute.AttributeMenuView();
                    } else if (equalItem(currentItem, UserMenu_StatusInfoIcon)) {
                        StatusInfoView(player);
                    }
                    playSound(player, SoundList.Click);
                }
            }
        } else if (equalInv(view, SettingMenuDisplay)) {
            if (currentItem != null) {
                if (ClickInventory == view.getTopInventory()) {
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
                    } else if (equalItem(currentItem, SettingMenu_PvPModeIcon)) {
                        playerData.PvPMode();
                    }
                }
            }
        } else if (equalInv(view, TriggerMenuDisplay)) {
            if (ClickInventory == view.getTopInventory()) {
                if (currentItem != null) {
                    if (playerData.HotBar.getSelectSlot() != -1) {
                        if (Slot == 53)  {
                            playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), new HotBarData());
                        } else {
                            HotBarData hotBar = TriggerMenuCache.get(Slot);
                            if (hotBar.category == HotBarCategory.Skill && getSkillData(hotBar.Icon).SkillType.isPassive()) {
                                player.sendMessage("§e[" + getSkillData(hotBar.Icon).Display + "]§aは§eパッシブスキル§aです");
                                playSound(player, SoundList.Nope);
                            } else {
                                playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), TriggerMenuCache.get(Slot));
                            }
                        }
                        playerData.HotBar.unSelectSlot();
                        TriggerMenuCache.clear();
                        TriggerMenuView();
                        playSound(player, SoundList.Click);
                    } else {
                        player.sendMessage("§eスロット§aを§e選択§aしてください");
                        playSound(player, SoundList.Nope);
                    }
                }
            }
        } else if (ClickInventory == player.getInventory()) {
            if (playerData.ViewInventory.isItem()) {
                if (index > -1) {
                    ItemParameter param = playerData(player).ItemInventory.getItemParameter(index);
                    if (param.Category == ItemCategory.Equipment) {
                        playerData(player).Equipment.Equip(param.EquipmentSlot, param);
                        playSound(player, SoundList.Click);
                    }
                }
            }
        }
    }

    public void MenuClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        player.setItemOnCursor(AirItem);
        playerData.RuneInventory.RuneMenuClose(event);
        playerData.Shop.ShopClose();
        if (ViewInventoryCache != null && playerData.ViewInventory != ViewInventoryCache) {
            playerData.setView(ViewInventoryCache, false);
            ViewInventoryCache = null;
        }
        if (equalInv(view, TriggerMenuDisplay)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!equalInv(view, TriggerMenuDisplay)) {
                    player.sendMessage("TriggerMenu");
                    playerData.HotBar.setSelectSlot(-1);
                    playerData.viewUpdate();
                }
            }, 1);
        }
        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                    playSound(player, SoundList.MenuClose);
                }
            }, 1);
        }
        if (!playerData.CastMode.isHold()) {
            player.getInventory().setHeldItemSlot(8);
        }
    }
}
