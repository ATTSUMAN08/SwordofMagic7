package swordofmagic7.Shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class AccessoryShop {
    private static final String AccessoryShopDisplay = "§lアクセサリショップ";
    public static final String ReLotteryDisplay = "§lリロール";
    private static final ItemStack ReLotteryIcon = new ItemStackData(Material.CLOCK, decoText("リロール"), "§a§lアクセサリのステータスを再抽選します").view();

    private final Player player;
    private final PlayerData playerData;

    public AccessoryShop(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void AccessoryMenuView() {
        Inventory inv = decoInv(AccessoryShopDisplay, 1);
        inv.setMaxStackSize(128);
        inv.setItem(0, ReLotteryIcon);
        player.openInventory(inv);
        playSound(player, SoundList.MENU_OPEN);
    }

    public void ReLotteryView() {
        Inventory inv = decoInv(ReLotteryDisplay, 1);
        ItemStack item = new ItemStackData(Material.IRON_BARS, "§a§l強化したいアクセサリを選択してください").view();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, item);
        }
        playerData.setView(ViewInventoryType.ItemInventory, false);
        player.openInventory(inv);
    }

    public ItemParameter AccessoryCache;
    private final ItemParameter ReLotteryItem = DataBase.getItemParameter("再抽選結晶");
    public void AccessoryMenuClick(InventoryView view, Inventory ClickInventory, ClickType clickType, ItemStack currentItem, int index, int Slot) {
        if (equalInv(view, AccessoryShopDisplay)) {
            if (equalItem(currentItem, ReLotteryIcon)) {
                ReLotteryView();
            }
            playSound(player, SoundList.CLICK);
        } else if (equalInv(view, ReLotteryDisplay) && playerData.ViewInventory.isItem()) {
            if (ClickInventory == view.getBottomInventory()) {
                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index);
                if (stack == null) return;
                ItemParameter item = stack.itemParameter;
                int Mel = 500;
                int Amount = 1;
                if (item.Category.isEquipment() && item.itemEquipmentData.equipmentCategory == EquipmentCategory.Accessory) {
                    if (playerData.Mel >= Mel && playerData.ItemInventory.hasItemParameter(ReLotteryItem, Amount)) {
                        playerData.ItemInventory.removeItemParameter(item, 1);
                        ItemParameter addItem = item.clone();
                        addItem.itemEquipmentData.itemAccessory.randomize();
                        playerData.ItemInventory.addItemParameter(addItem, 1);
                        playerData.Mel -= Mel;
                        playerData.ItemInventory.removeItemParameter(ReLotteryItem, Amount);
                        sendMessage(player, "§e[" + item.Display + "]§aを§e再抽選§aしました §c[-" + Mel + "メル]", SoundList.LEVEL_UP);
                    } else {
                        sendMessage(player, "§e" + Mel + "メル§aと§e" + ReLotteryItem.Display + "§aが§c必要§aです", SoundList.NOPE);
                    }
                } else sendMessage(player, "§eアクセサリ§aを§e選択§aしてください", SoundList.NOPE);
            }
        }
    }
}
