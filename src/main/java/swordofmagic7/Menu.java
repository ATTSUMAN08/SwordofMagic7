package swordofmagic7;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        item.setItemMeta(meta);
        return item;
    }
}

public class Menu {

    private final Player player;
    private final PlayerData playerData;

    private final String UserMenuDisplay = colored("&lユーザーメニュー");
    private final ItemStack UserMenu_ItemInventory = new ItemStackData(Material.CHEST, decoText("&e&lアイテムインベントリ"), colored("&a&lインベントリ表示を[アイテムインベントリ]に切り替えます")).view();
    private final ItemStack UserMenu_ModuleInventory = new ItemStackData(Material.ENDER_CHEST, decoText("&e&lモジュールインベントリ"), colored("&a&lインベントリ表示を[モジュールインベントリ]に切り替えます")).view();
    private final ItemStack UserMenu_HotBar = new ItemStackData(Material.ITEM_FRAME, decoText("&e&lホットバー"), colored("&a&lインベントリ表示を[ホットバー]に切り替えます")).view();
    private final ItemStack UserMenu_SkillMenuIcon = new ItemStackData(Material.ENCHANTED_BOOK, decoText("&e&lスキルメニュー"), colored("&a&lスキルメニューを開きます")).view();
    private final ItemStack UserMenu_ModuleMenuIcon = new ItemStackData(Material.PAPER, decoText("&e&lモジュールメニュー"), colored("&a&lモジュールメニューを開きます")).view();
    private final ItemStack UserMenu_TriggerMenuIcon = new ItemStackData(Material.COMPARATOR, decoText("&e&lトリガーメニュー"), colored("&a&lトリガーメニューを開きます")).view();
    private final ItemStack UserMenu_AttributeMenuIcon = new ItemStackData(Material.RED_DYE, decoText("&e&lアトリビュートメニュー"), colored("&a&lアトリビュートメニューを開きます")).view();

    private final String SkillMenuDisplay = colored("&lスキルメニュー");

    private final String ModuleMenuDisplay = colored("&lモジュールメニュー");

    private final String TriggerMenuDisplay = colored("&lトリガーメニュー");
    private final ItemStack TriggerMenu_Reset = new ItemStackData(Material.BARRIER, colored("&c&lスロットを空にする")).view();

    private final String AttributeMenuDisplay = colored("&lアトリビュートメニュー");

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
        inv.setItem(1, UserMenu_ModuleInventory);
        inv.setItem(2, UserMenu_HotBar);
        inv.setItem(3, UserMenu_SkillMenuIcon);
        inv.setItem(4, UserMenu_ModuleMenuIcon);
        //inv.setItem(5, UserMenu_TriggerMenuIcon);
        player.openInventory(inv);
    }

    private final HashMap<Integer, String> SkillMenuCache = new HashMap<>();
    void SkillMenuView() {
        SkillMenuCache.clear();
        Inventory inv = decoInv(SkillMenuDisplay, 3);
        int slot = 0;
        for (SkillData skill : playerData.Classes.classT0.SkillList) {
            inv.setItem(slot, skill.view());
            SkillMenuCache.put(slot, skill.Id);
            slot++;
        }
        player.openInventory(inv);
    }

    void ModuleMenuView() {
        Inventory inv = decoInv(ModuleMenuDisplay, 5);
        ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventory.ItemInventory);
        for (int slot = 0; slot < 45; slot++) {
            if (slot != 20 && !ModuleSlotIndex().contains(slot)) {
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
        for (SkillData skill : playerData.Classes.classT0.SkillList) {
            inv.setItem(slot, skill.view());
            TriggerMenuCache.put(slot, new HotBarData(skill));
            slot++;
        }
        inv.setItem(53, TriggerMenu_Reset);
        player.openInventory(inv);
    }

    void AttributeMenuView() {
        Inventory inv = decoInv(AttributeMenuDisplay, 1);
        int slot = 0;
        for (AttributeType attr : AttributeType.values()) {
            inv.setItem(slot, playerData.Attribute.attributeView(attr));
            slot++;
        }
        inv.setItem(53, TriggerMenu_Reset);
        player.openInventory(inv);
    }

    private ShopData ShopDataCache;
    void ShopOpen(ShopData Shop) {
        ViewInventoryCache = playerData.ViewInventory;
        player.openInventory(Shop.view(1, playerData.ViewFormat()));
        playerData.setView(ViewInventory.ItemInventory, false);
        ShopDataCache = Shop.clone();
        playSound(player, SoundList.MenuOpen);
    }

    private boolean ignoreSlot(int slot) {
        return (8 < slot && slot < 36 && slot != 17 && slot != 26 && slot != 35);
    }

    private List<Integer> ModuleSlotIndex() {
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

    private ItemParameter ModuleCache;
    private ViewInventory ViewInventoryCache;

    void MenuClick(InventoryClickEvent event) {
        final InventoryView view = event.getView();
        final ItemStack currentItem = event.getCurrentItem();
        final InventoryAction action = event.getAction();
        final Inventory ClickInventory = event.getClickedInventory();
        final int Slot = event.getSlot();
        event.setCancelled(true);

        if (playerData.ViewInventory.isHotBar()) {
            switch (action) {
                case DROP_ALL_CURSOR, DROP_ALL_SLOT, DROP_ONE_CURSOR, DROP_ONE_SLOT -> player.setItemOnCursor(AirItem);
            }
        }

        int index = -1;
        if (currentItem != null && ClickInventory == view.getBottomInventory()) {
            if (ignoreSlot(Slot) && currentItem.hasItemMeta() && playerData.ViewInventory != ViewInventory.HotBar) {
                ItemMeta meta = currentItem.getItemMeta();
                if (meta.hasLore()) {
                    List<String> Lore = meta.getLore();
                    index = Integer.parseInt(Lore.get(Lore.size() - 1).replace("§8", ""));
                }
            }

            if (playerData.ViewInventory.isItem()) {
                switch (Slot) {
                    case 8 -> playerData(player).Equipment.unEquip(EquipmentSlot.MainHand);
                    case 40 -> playerData(player).Equipment.unEquip(EquipmentSlot.OffHand);
                    case 38 -> playerData(player).Equipment.unEquip(EquipmentSlot.Armor);
                    case 17 -> playerData.ItemInventory.upScrollTick();
                    case 35 -> playerData.ItemInventory.downScrollTick(playerData.ItemInventory.getList().size());
                }
            } else if (playerData.ViewInventory.isModule()) {
                switch (Slot) {
                    case 17 -> playerData.ModuleInventory.upScrollTick();
                    case 35 -> playerData.ModuleInventory.downScrollTick(playerData.ModuleInventory.getList().size());
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
                        case ItemInventory -> playerData.setView(ViewInventory.ModuleInventory);
                        case ModuleInventory -> playerData.setView(ViewInventory.HotBar);
                        case HotBar -> playerData.setView(ViewInventory.ItemInventory);
                    }
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
                    } else if (equalItem(currentItem, UserMenu_ModuleInventory)) {
                        playerData.setView(ViewInventory.ModuleInventory);
                    } else if (equalItem(currentItem, UserMenu_HotBar)) {
                        playerData.setView(ViewInventory.HotBar);
                    } else if (equalItem(currentItem, UserMenu_SkillMenuIcon)) {
                        SkillMenuView();
                    } else if (equalItem(currentItem, UserMenu_ModuleMenuIcon)) {
                        ModuleMenuView();
                    } else if (equalItem(currentItem, UserMenu_TriggerMenuIcon)) {
                        TriggerMenuView();
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
                        player.sendMessage(colored("&e[" + skillData.Display + "]&aは&eパッシブスキル&aです"));
                    }
                }
            }
        } else if (equalInv(view, ModuleMenuDisplay)) {
            String format = playerData.ViewFormat();
            if (currentItem != null) {
                if (ClickInventory == player.getInventory() && ignoreSlot(Slot)) {
                    if (index > -1) {
                        if (playerData.ViewInventory.isItem()) {
                            if (ModuleCache != null) {
                                playerData.ItemInventory.addItemParameter(ModuleCache, 1);
                            }
                            ModuleCache = playerData.ItemInventory.getItemParameter(index);
                            playerData.ItemInventory.removeItemParameter(ModuleCache, 1);
                            playerData.setView(ViewInventory.ModuleInventory, false);
                            playSound(player, Click);
                        } else if (playerData.ViewInventory.isModule()) {
                            if (ModuleCache != null) {
                                if (ModuleCache.getModuleSize() < ModuleCache.ModuleSlot) {
                                    ModuleParameter moduleParameter = playerData.ModuleInventory.getModuleParameter(index);
                                    playerData.ModuleInventory.removeModuleParameter(index);
                                    ModuleCache.addModule(moduleParameter);
                                    playSound(player, Click);
                                } else {
                                    player.sendMessage(colored("&eモジュールスロット&aに空きがありません"));
                                    playSound(player, Nope);
                                }
                            } else {
                                player.sendMessage(colored("&e装備&aを&eセット&aしてください"));
                                playSound(player, Nope);
                            }
                        }
                    }
                } else if (view.getTopInventory() == ClickInventory) {
                    if (Slot == 20) {
                        playerData.ItemInventory.addItemParameter(ModuleCache, 1);
                        ModuleCache = null;
                        playerData.setView(ViewInventory.ItemInventory, false);
                        playSound(player, Click);
                    } else if ((14 <= Slot && Slot <= 16) || (23 <= Slot && Slot <= 25) || (32 <= Slot && Slot <= 34)) {
                        int ModuleIndex;
                        if (Slot <= 16) {
                            ModuleIndex = Slot - 14;
                        } else if (Slot <= 25) {
                            ModuleIndex = Slot - 20;
                        } else {
                            ModuleIndex = Slot - 26;
                        }
                        if (ModuleIndex < ModuleCache.getModuleSize()) {
                            playerData.ModuleInventory.addModuleParameter(ModuleCache.getModule(ModuleIndex));
                            ModuleCache.removeModule(ModuleIndex);
                            playSound(player, Click);
                        }
                    }
                }
                if (ModuleCache != null) {
                    view.getTopInventory().setItem(20, ModuleCache.viewItem(1, format));
                    int i = 0;
                    for (int slot : ModuleSlotIndex()) {
                        if (i < ModuleCache.ModuleSlot) {
                            if (i < ModuleCache.getModuleSize()) {
                                view.getTopInventory().setItem(slot, ModuleCache.getModule(i).viewModule(format));
                            } else {
                                view.getTopInventory().setItem(slot, AirItem);
                            }
                        } else view.getTopInventory().setItem(slot, new ItemStack(Material.RED_STAINED_GLASS_PANE));
                        i++;
                    }
                } else {
                    view.getTopInventory().setItem(20, AirItem);
                    for (int slot : ModuleSlotIndex()) {
                        view.getTopInventory().setItem(slot, AirItem);
                    }
                }
            }
        } else if (equalInv(view, TriggerMenuDisplay)) {
            if (ClickInventory == view.getTopInventory()) {
                if (currentItem != null) {
                    if (playerData.HotBar.getSelectSlot() != -1) {
                        if (Slot == 53) playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), new HotBarData());
                        else playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), TriggerMenuCache.get(Slot));
                        playerData.HotBar.unSelectSlot();
                        TriggerMenuCache.clear();
                        TriggerMenuView();
                        playSound(player, Click);
                    } else {
                        player.sendMessage(colored("&eスロット&aを&e選択&aしてください"));
                        playSound(player, Nope);
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
                            player.sendMessage(colored("&e&l[" + data.itemParameter.Display + "]&aを&b購入&aしました"));
                            playSound(player, LevelUp);
                        } else {
                            player.sendMessage(colored("&eメル&aが足りません"));
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
        if (equalInv(view, ModuleMenuDisplay)) {
            if (ModuleCache != null) {
                playerData.ItemInventory.addItemParameter(ModuleCache, 1);
                ModuleCache = null;
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
        if (!view.getTopInventory().isEmpty()) {
            playSound(player, MenuClose);
        }
        if (ShopDataCache != null) {
            ShopDataCache = null;
        }
    }
}
