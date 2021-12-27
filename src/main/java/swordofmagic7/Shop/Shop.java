package swordofmagic7.Shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Shop {
    private final Player player;
    private final PlayerData playerData;

    public Shop(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    private ShopData ShopDataCache;
    public void ShopOpen(ShopData Shop) {
        playerData.Menu.ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.ItemInventory, false);
        player.openInventory(Shop.view(1, playerData.ViewFormat()));
        ShopDataCache = Shop.clone();
        playSound(player, SoundList.MenuOpen);
    }

    public void ShopClick(InventoryView view, int Slot) {
        if (ShopDataCache != null && equalInv(view, ShopDataCache.Display)) {
            if (Slot < 45) {
                if (ShopDataCache.Data.containsKey(Slot)) {
                    ShopSlot data = ShopDataCache.Data.get(Slot);
                    if (playerData.Mel >= data.Mel) {
                        playerData.Mel -= data.Mel;
                        playerData.ItemInventory.addItemParameter(data.itemParameter.clone(), 1);
                        player.sendMessage("§e§l[" + data.itemParameter.Display + "]§aを§b購入§aしました");
                        playSound(player, SoundList.LevelUp);
                    } else {
                        player.sendMessage("§eメル§aが足りません");
                        playSound(player, SoundList.Nope);
                    }
                }
            }
        }
    }

    public void ShopClose() {
        if (ShopDataCache != null) {
            ShopDataCache = null;
        }
    }
}