package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.SoundList.*;
import static swordofmagic7.System.plugin;

class ItemStackData {
    Material material;
    int CustomModelData = 0;
    String Display  = "";
    List<String> Lore = new ArrayList<>();

    ItemStackData(Material material) {
        this.material = material;
    }

    ItemStackData(Material material, String Display) {
        this.material = material;
        this.Display = Display;
    }

    ItemStackData(Material material, String Display, List<String> Lore) {
        this.material = material;
        this.Display = Display;
        this.Lore = Lore;
    }

    ItemStackData(Material material, String Display, String Lore) {
        this.material = material;
        this.Display = Display;
        String[] LoreData = Lore.split("\n");
        this.Lore = List.of(LoreData);
    }

    ItemStack view() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(CustomModelData);
        meta.setDisplayName(Display);
        meta.setLore(Lore);
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}

public class Menu {

    private final Player player;
    private final PlayerData playerData;

    private final String UserMenuDisplay = "§lユーザーメニュー";
    private final ItemStack UserMenu_ItemInventory = new ItemStackData(Material.CHEST, decoText("アイテムインベントリ"), "§a§lインベントリ表示を[アイテムインベントリ]に切り替えます").view();
    private final ItemStack UserMenu_RuneInventory = new ItemStackData(Material.ENDER_CHEST, decoText("ルーンインベントリ"), "§a§lインベントリ表示を[ルーンインベントリ]に切り替えます").view();
    private final ItemStack UserMenu_PetInventory = new ItemStackData(Material.NOTE_BLOCK, decoText("ペットケージ"), "§a§lインベントリ表示を[ペットケージ]に切り替えます").view();
    private final ItemStack UserMenu_HotBar = new ItemStackData(Material.ITEM_FRAME, decoText("ホットバー"), "§a§lインベントリ表示を[ホットバー]に切り替えます").view();
    private final ItemStack UserMenu_SkillMenuIcon = new ItemStackData(Material.ENCHANTED_BOOK, decoText("スキルメニュー"), "§a§lスキルメニューを開きます").view();
    private final ItemStack UserMenu_RuneMenuIcon = new ItemStackData(Material.PAPER, decoText("ルーンメニュー"), "§a§lルーンメニューを開きます").view();
    private final ItemStack UserMenu_TriggerMenuIcon = new ItemStackData(Material.COMPARATOR, decoText("トリガーメニュー"), "§a§lトリガーメニューを開きます").view();
    private final ItemStack UserMenu_AttributeMenuIcon = new ItemStackData(Material.RED_DYE, decoText("アトリビュートメニュー"), "§a§lアトリビュートメニューを開きます").view();
    private final ItemStack UserMenu_StatusInfoIcon = new ItemStackData(Material.PAINTING, decoText("ステータス情報"), "§a§lステータス情報を開きます").view();
    private final ItemStack UserMenu_SettingMenuIcon = new ItemStackData(Material.CRAFTING_TABLE, decoText("設定メニュー"), "§a§l設定メニューを開きます").view();

    private final String SkillMenuDisplay = "§lスキルメニュー";

    private final String RuneMenuDisplay = "§lルーンメニュー";

    private final String TriggerMenuDisplay = "§lトリガーメニュー";
    private final ItemStack TriggerMenu_Reset = new ItemStackData(Material.BARRIER, "§c§lスロットを空にする").view();

    private final String AttributeMenuDisplay = "§lアトリビュートメニュー";

    private final String StatusInfoDisplay = "§lステータス情報";

    private final String SettingMenuDisplay = "§l設定メニュー";
    private final ItemStack SettingMenu_DamageLogIcon = new ItemStackData(Material.RED_DYE, decoText("ダメージログ"), "§a§lダメージログ表記を切り替えます").view();
    private final ItemStack SettingMenu_ExpLogIcon = new ItemStackData(Material.EXPERIENCE_BOTTLE, decoText("経験値ログ"), "§a§l経験値ログ表記を切り替えます").view();
    private final ItemStack SettingMenu_DropLogIcon = new ItemStackData(Material.CHEST, decoText("ドロップログ"), "§a§lドロップログ表記を切り替えます").view();
    private final ItemStack SettingMenu_StrafeModeIcon = new ItemStackData(Material.FEATHER, decoText("ストレイフモード"), "§a§lストレイフの発動条件を切り替えます").view();
    private final ItemStack SettingMenu_CastModeIcon = new ItemStackData(Material.END_CRYSTAL, decoText("キャストモード"), "§a§lスキルの発動方法を切り替えます").view();
    private final ItemStack SettingMenu_PvPModeIcon = new ItemStackData(Material.IRON_SWORD, decoText("PvPモード"), "§a§lPvPモードを切り替えます").view();

    private final String PetShopDisplay = "§lペットショップ";

    private final String UpgradeDisplay = "§l装備強化";

    boolean equalInv(InventoryView view, String name) {
        return view.getTitle().equalsIgnoreCase(name);
    }

    boolean equalItem(ItemStack item, ItemStack item2) {
        return unColored(item.getItemMeta().getDisplayName()).equals(unColored(item2.getItemMeta().getDisplayName()));
    }

    Menu(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    void UserMenuView() {
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

    private final HashMap<Integer, String> SkillMenuCache = new HashMap<>();
    void SkillMenuView() {
        SkillMenuCache.clear();
        Inventory inv = decoInv(SkillMenuDisplay, 3);
        int slot = 0;
        int tier = 0;
        while (playerData.Classes.classTier[tier] != null) {
            for (SkillData skill : playerData.Classes.classTier[tier].SkillList) {
                inv.setItem(slot, skill.view());
                SkillMenuCache.put(slot, skill.Id);
                slot++;
            }
            tier++;
            slot = tier*9;
        }
        player.openInventory(inv);
    }

    void RuneMenuView() {
        Inventory inv = decoInv(RuneMenuDisplay, 5);
        ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventory.ItemInventory);
        for (int slot = 0; slot < 45; slot++) {
            if (slot != 20 && !RuneSlotIndex().contains(slot)) {
                inv.setItem(slot, new ItemStack(Material.BROWN_STAINED_GLASS_PANE));
            }
        }
        player.openInventory(inv);
    }

    private final HashMap<Integer, HotBarData> TriggerMenuCache = new HashMap<>();
    void TriggerMenuView() {
        Inventory inv = decoInv(TriggerMenuDisplay, 6);
        ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventory.HotBar, false);
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

    private Inventory AttributeMenuCache;
    void AttributeMenuView() {
        AttributeMenuCache = decoInv(AttributeMenuDisplay, 1);
        AttributeMenuLoad();
        player.openInventory(AttributeMenuCache);
    }

    void AttributeMenuLoad() {
        Attribute attribute = playerData.Attribute;
        int slot = 0;
        for (AttributeType attr : AttributeType.values()) {
            AttributeMenuCache.setItem(slot, attribute.attributeView(attr));
            slot++;
        }
        List<String> lore = new ArrayList<>();
        lore.add(decoLore("ポイント") + attribute.getAttributePoint());
        lore.add("");
        lore.add("§c§l※クリックでアトリビュートをリセット");
        ItemStack point = new ItemStackData(Material.EXPERIENCE_BOTTLE, decoText("アトリビュート"), lore).view();
        AttributeMenuCache.setItem(8, point);
    }

    void StatusInfoView(Player player) {
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
                    statusLore.add(StatusParameter.SkillCastTime.DecoDisplay + String.format(format, status.SkillCastTime));
                    statusLore.add(StatusParameter.SkillRigidTime.DecoDisplay + String.format(format, status.SkillRigidTime));
                    statusLore.add(StatusParameter.SkillCooltime.DecoDisplay + String.format(format, status.SkillCooltime));
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

    private ShopData ShopDataCache;
    void ShopOpen(ShopData Shop) {
        ViewInventoryCache = playerData.ViewInventory;
        player.openInventory(Shop.view(1, playerData.ViewFormat()));
        playerData.setView(ViewInventory.ItemInventory, false);
        ShopDataCache = Shop.clone();
        playSound(player, SoundList.MenuOpen);
    }

    void PetShop() {
        ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventory.PetInventory, false);
        Inventory inv = decoInv(PetShopDisplay, 1);
        inv.setItem(0, new ItemStackData(Material.WOLF_SPAWN_EGG, decoText("オオカミペット"), "§a§l配布の無料ペットです").view());
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
    }

    void UpgradeView() {
        ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventory.ItemInventory, false);
        Inventory inv = decoInv(UpgradeDisplay, 3);
        for (int slot = 0; slot < 27; slot++) {
            inv.setItem(slot, new ItemStack(Material.BROWN_STAINED_GLASS_PANE));
        }
        player.openInventory(inv);
    }

    void ClassSelectView(int tier) {
        Inventory inv = decoInv("クラスカウンター", 1);
        switch (tier) {
            case 0 -> {
                ItemStack tier1 = new ItemStackData(Material.END_CRYSTAL, decoText("クラス一覧 [T1]")).view();
                inv.setItem(0, tier1);
            }
            case 1 -> {

            }
        }
        player.openInventory(inv);
    }

    private boolean ignoreSlot(int slot) {
        return (8 < slot && slot < 36 && slot != 17 && slot != 26 && slot != 35);
    }

    private List<Integer> RuneSlotIndex() {
        List<Integer> List = new ArrayList<>();
        List.add(14);
        List.add(15);
        List.add(16);
        List.add(23);
        List.add(24);
        List.add(25);
        List.add(32);
        List.add(33);
        List.add(34);
        return List;
    }

    private int slotToIndex(int slot) {
        if (26 < slot && slot < 35) {
            slot -= 3;
        } else if (17 < slot && slot < 26) {
            slot -= 2;
        } else if (8 < slot && slot < 17) {
            slot -= 1;
        }
        return slot;
    }

    private ItemParameter UpgradeCache;
    private ItemParameter RuneCache;
    private ViewInventory ViewInventoryCache;

    void MenuClick(InventoryClickEvent event) {
        final InventoryView view = event.getView();
        final ItemStack currentItem = event.getCurrentItem();
        final InventoryAction action = event.getAction();
        final Inventory ClickInventory = event.getClickedInventory();
        final int Slot = event.getSlot();
        event.setCancelled(true);

        int index = -1;
        if (currentItem != null && ClickInventory == view.getBottomInventory()) {
            if (ignoreSlot(Slot) && currentItem.hasItemMeta() && playerData.ViewInventory != ViewInventory.HotBar) {
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
                        playSound(player, Click);
                    }
                }
            }

            if (Slot == 26) {
                if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    switch (playerData.ViewInventory) {
                        case ItemInventory -> playerData.setView(ViewInventory.RuneInventory);
                        case RuneInventory -> playerData.setView(ViewInventory.PetInventory);
                        case PetInventory -> playerData.setView(ViewInventory.HotBar);
                        case HotBar -> playerData.setView(ViewInventory.ItemInventory);
                    }
                    playSound(player, Click);
                } else {
                    playerData.Menu.UserMenuView();
                    playSound(player, MenuOpen);
                }
            }
        }

        if (equalInv(view, UserMenuDisplay) && ClickInventory == view.getTopInventory()) {
            if (currentItem != null) {
                if (ClickInventory == view.getTopInventory()) {
                    if (equalItem(currentItem, UserMenu_ItemInventory)) {
                        playerData.setView(ViewInventory.ItemInventory);
                    } else if (equalItem(currentItem, UserMenu_RuneInventory)) {
                        playerData.setView(ViewInventory.RuneInventory);
                    } else if (equalItem(currentItem, UserMenu_PetInventory)) {
                        playerData.setView(ViewInventory.PetInventory);
                    } else if (equalItem(currentItem, UserMenu_HotBar)) {
                        playerData.setView(ViewInventory.HotBar);
                    } else if (equalItem(currentItem, UserMenu_SettingMenuIcon)) {
                        SettingMenuView();
                    } else if (equalItem(currentItem, UserMenu_SkillMenuIcon)) {
                        SkillMenuView();
                    } else if (equalItem(currentItem, UserMenu_RuneMenuIcon)) {
                        RuneMenuView();
                    } else if (equalItem(currentItem, UserMenu_TriggerMenuIcon)) {
                        TriggerMenuView();
                    } else if (equalItem(currentItem, UserMenu_AttributeMenuIcon)) {
                        AttributeMenuView();
                    } else if (equalItem(currentItem, UserMenu_StatusInfoIcon)) {
                        StatusInfoView(player);
                    }
                    playSound(player, Click);
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
        } else if (equalInv(view, SkillMenuDisplay)) {
            if (currentItem != null) {
                if (ClickInventory == view.getTopInventory() && view.getTopInventory() == ClickInventory) {
                    SkillData skillData = getSkillData(SkillMenuCache.get(Slot));
                    if (skillData.SkillType.isActive()) {
                        playerData.HotBar.addHotbar(new HotBarData(skillData));
                        playSound(player, Click);
                    } else {
                        player.sendMessage("§e[" + skillData.Display + "]§aは§eパッシブスキル§aです");
                        playSound(player, Nope);
                    }
                }
            }
        } else if (equalInv(view, RuneMenuDisplay)) {
            String format = playerData.ViewFormat();
            if (currentItem != null) {
                if (ClickInventory == player.getInventory() && ignoreSlot(Slot)) {
                    if (index > -1) {
                        if (playerData.ViewInventory.isItem()) {
                            if (RuneCache != null) {
                                playerData.ItemInventory.addItemParameter(RuneCache, 1);
                            }
                            RuneCache = playerData.ItemInventory.getItemParameter(index);
                            playerData.ItemInventory.removeItemParameter(RuneCache, 1);
                            playerData.setView(ViewInventory.RuneInventory, false);
                            playSound(player, Click);
                        } else if (playerData.ViewInventory.isRune()) {
                            if (RuneCache != null) {
                                if (RuneCache.getRuneSize() < RuneCache.RuneSlot) {
                                    RuneParameter runeParameter = playerData.RuneInventory.getRuneParameter(index);
                                    playerData.RuneInventory.removeRuneParameter(index);
                                    RuneCache.addRune(runeParameter);
                                    playSound(player, Click);
                                } else {
                                    player.sendMessage("§eルーンスロット§aに空きがありません");
                                    playSound(player, Nope);
                                }
                            } else {
                                player.sendMessage("§e装備§aを§eセット§aしてください");
                                playSound(player, Nope);
                            }
                        }
                    }
                } else if (view.getTopInventory() == ClickInventory) {
                    if (Slot == 20) {
                        playerData.ItemInventory.addItemParameter(RuneCache, 1);
                        RuneCache = null;
                        playerData.setView(ViewInventory.ItemInventory, false);
                        playSound(player, Click);
                    } else if ((14 <= Slot && Slot <= 16) || (23 <= Slot && Slot <= 25) || (32 <= Slot && Slot <= 34)) {
                        int RuneIndex;
                        if (Slot <= 16) {
                            RuneIndex = Slot - 14;
                        } else if (Slot <= 25) {
                            RuneIndex = Slot - 20;
                        } else {
                            RuneIndex = Slot - 26;
                        }
                        if (RuneIndex < RuneCache.getRuneSize()) {
                            playerData.RuneInventory.addRuneParameter(RuneCache.getRune(RuneIndex));
                            RuneCache.removeRune(RuneIndex);
                            playSound(player, Click);
                        }
                    }
                }
                if (RuneCache != null) {
                    view.getTopInventory().setItem(20, RuneCache.viewItem(1, format));
                    int i = 0;
                    for (int slot : RuneSlotIndex()) {
                        if (i < RuneCache.RuneSlot) {
                            if (i < RuneCache.getRuneSize()) {
                                view.getTopInventory().setItem(slot, RuneCache.getRune(i).viewRune(format));
                            } else {
                                view.getTopInventory().setItem(slot, AirItem);
                            }
                        } else view.getTopInventory().setItem(slot, new ItemStack(Material.RED_STAINED_GLASS_PANE));
                        i++;
                    }
                } else {
                    view.getTopInventory().setItem(20, AirItem);
                    for (int slot : RuneSlotIndex()) {
                        view.getTopInventory().setItem(slot, AirItem);
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
                                playSound(player, Nope);
                            } else {
                                playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), TriggerMenuCache.get(Slot));
                            }
                        }
                        playerData.HotBar.unSelectSlot();
                        TriggerMenuCache.clear();
                        TriggerMenuView();
                        playSound(player, Click);
                    } else {
                        player.sendMessage("§eスロット§aを§e選択§aしてください");
                        playSound(player, Nope);
                    }
                }
            }
        } else if (equalInv(view, AttributeMenuDisplay)) {
            if (ClickInventory == view.getTopInventory()) {
                if (currentItem != null) {
                    Attribute attr = playerData.Attribute;
                    for (AttributeType attrType : AttributeType.values()) {
                        if (currentItem.getType() == attrType.Icon) {
                            attr.addAttribute(attrType, 1);
                        }
                    }
                    if (currentItem.getType() == Material.EXPERIENCE_BOTTLE) {
                        attr.resetAttribute();
                    }
                    AttributeMenuLoad();
                    playSound(player, Click);
                }
            }
        } else if (equalInv(view, PetShopDisplay)) {
            if (ClickInventory == view.getTopInventory()) {
                if (currentItem != null) {
                    if (currentItem.getType() == Material.WOLF_SPAWN_EGG) {
                        if (playerData.PetInventory.getList().size() == 0) {
                            PetData petData = getPetData("オースオオカミ");
                            Random random = new Random();
                            PetParameter petParameter = new PetParameter(player, playerData, petData, 1, 30, 0, random.nextDouble()+0.5);
                            playerData.PetInventory.addPetParameter(petParameter);
                            player.sendMessage("§e[" + petData.Display + "]§aを受け取りました");
                            playSound(player, LevelUp);
                        } else {
                            player.sendMessage("§aすでに§eペット§aを所持しています");
                            playSound(player, Nope);
                        }
                    }
                }
            }
        } else if (equalInv(view, UpgradeDisplay)) {
            if (currentItem != null) {
                if (ClickInventory == player.getInventory() && ignoreSlot(Slot)) {
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
        } else if (ClickInventory == player.getInventory()) {
            if (playerData.ViewInventory.isItem()) {
                if (index > -1) {
                    ItemParameter param = playerData(player).ItemInventory.getItemParameter(index);
                    if (param.Category == ItemCategory.Equipment) {
                        playerData(player).Equipment.Equip(param.EquipmentSlot, param);
                        playSound(player, Click);
                    }
                }
            }
        } else if (ShopDataCache != null && equalInv(view, ShopDataCache.Display)) {
            if (ClickInventory == view.getTopInventory()) {
                if (Slot < 45) {
                    if (ShopDataCache.Data.containsKey(Slot)) {
                        ShopSlot data = ShopDataCache.Data.get(Slot);
                        if (playerData.Mel >= data.Mel) {
                            playerData.Mel -= data.Mel;
                            playerData.ItemInventory.addItemParameter(data.itemParameter.clone(), 1);
                            player.sendMessage("§e§l[" + data.itemParameter.Display + "]§aを§b購入§aしました");
                            playSound(player, LevelUp);
                        } else {
                            player.sendMessage("§eメル§aが足りません");
                            playSound(player, Nope);
                        }
                    }
                }
            }
        }
    }

    void MenuClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        player.setItemOnCursor(AirItem);
        if (equalInv(view, RuneMenuDisplay)) {
            if (RuneCache != null) {
                playerData.ItemInventory.addItemParameter(RuneCache, 1);
                RuneCache = null;
            }
            if (ViewInventoryCache != null && playerData.ViewInventory != ViewInventoryCache) {
                playerData.setView(ViewInventoryCache, false);
                ViewInventoryCache = null;
            }
        } else if (equalInv(view, TriggerMenuDisplay)) {
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
                    playSound(player, MenuClose);
                }
            }, 1);
        }
        if (ShopDataCache != null) {
            ShopDataCache = null;
        }
        if (!playerData.CastMode.isHold()) {
            player.getInventory().setHeldItemSlot(8);
        }
    }
}
