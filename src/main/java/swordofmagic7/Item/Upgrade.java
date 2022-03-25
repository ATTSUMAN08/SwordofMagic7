package swordofmagic7.Item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.UpgradeDisplay;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.random;

public class Upgrade {

    private final Player player;
    private final PlayerData playerData;

    public Upgrade(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void UpgradeView() {
        playerData.setView(ViewInventoryType.ItemInventory, false);
        Inventory inv = decoAnvil(UpgradeDisplay);
        player.openInventory(inv);
    }

    private int UpgradeCost(ItemParameter item) {
        double cost = item.itemEquipmentData.UpgradeCost * Math.pow(3, (item.itemEquipmentData.Plus/3f + 1) /3);
        return (int) Math.round(cost);
    }

    private int UpgradeMinCost(ItemParameter item) {
        return Math.round(UpgradeCost(item)/2f * (1/(1+playerData.LifeStatus.getLevel(LifeType.Smith)/30f)));
    }

    private final ItemParameter[] UpgradeCache = new ItemParameter[2];
    private final ItemParameter UpgradeStone = getItemParameter("強化石");
    public void UpgradeClick(InventoryView view, Inventory ClickInventory, int index, int Slot) {
        if (equalInv(view, UpgradeDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot == AnvilUISlot[2]) {
                    if (UpgradeCache[0] != null) {
                        int cost = UpgradeCost(UpgradeCache[0]);
                        if (playerData.ItemInventory.hasItemParameter(UpgradeStone, cost)) {
                            int minCost = UpgradeMinCost(UpgradeCache[0]);
                            int removeCost = (int) Math.round(minCost * random.nextDouble() + minCost);
                            playerData.ItemInventory.removeItemParameter(UpgradeStone, removeCost);
                            if (UpgradeCache[1].itemEquipmentData.Plus < 10) {
                                UpgradeCache[0] = UpgradeCache[1].clone();
                            } else {
                                UpgradeCache[0] = null;
                                playerData.ItemInventory.addItemParameter(UpgradeCache[1], 1);
                            }
                            player.sendMessage("§e[強化石]§aを§e[" + removeCost + "個]§a消費しました");
                            playerData.LifeStatus.addLifeExp(LifeType.Smith, cost);
                            playSound(player, SoundList.LevelUp);
                        } else {
                            player.sendMessage("§e[強化石]§aが§e[" + cost + "個]§a必要です");
                            playSound(player, SoundList.Nope);
                        }
                    }
                } else if (Slot == AnvilUISlot[0] && UpgradeCache[0] != null) {
                    playerData.ItemInventory.addItemParameter(UpgradeCache[0], 1);
                    UpgradeCache[0] = null;
                    playSound(player, SoundList.Click);
                }
            } else if (index > -1) {
                ItemParameter itemParameter = playerData.ItemInventory.getItemParameter(index);
                if (itemParameter.Category == ItemCategory.Equipment) {
                    if (itemParameter.itemEquipmentData.Plus < 10) {
                        if (UpgradeCache[0] != null) {
                            playerData.ItemInventory.addItemParameter(UpgradeCache[0], 1);
                        }
                        playerData.ItemInventory.removeItemParameter(itemParameter, 1);
                        UpgradeCache[0] = itemParameter;
                        playSound(player, SoundList.Click);
                    } else {
                        player.sendMessage("§c[強化上限]§aです");
                        playSound(player, SoundList.Nope);
                    }
                }
            }
            String format = playerData.ViewFormat();
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (UpgradeCache[0] != null) {
                inv.setItem(AnvilUISlot[0], UpgradeCache[0].viewItem(1, format));
                List<String> Lore = new ArrayList<>();
                int cost = UpgradeCost(UpgradeCache[0]);
                int minCost = UpgradeMinCost(UpgradeCache[0]);
                Lore.add(decoLore("必要強化石") + cost + "個");
                Lore.add(decoLore("消費強化石") + minCost + "～" + cost + "個");
                ItemStack viewCost = new ItemStackData(Material.AMETHYST_SHARD, decoText("強化コスト"), Lore).view();
                inv.setItem(AnvilUISlot[1], viewCost);
                UpgradeCache[1] = UpgradeCache[0].clone();
                UpgradeCache[1].itemEquipmentData.Plus++;
                inv.setItem(AnvilUISlot[2], UpgradeCache[1].viewItem(1, format));
            } else {
                inv.setItem(AnvilUISlot[0], AirItem);
                inv.setItem(AnvilUISlot[1], AirItem);
                inv.setItem(AnvilUISlot[2], AirItem);
                UpgradeCache[1] = null;
            }
        }
    }

    public void UpgradeClose(InventoryView view) {
        player.setItemOnCursor(AirItem);
        if (equalInv(view, UpgradeDisplay)) {
            if (UpgradeCache[0] != null) {
                playerData.ItemInventory.addItemParameter(UpgradeCache[0], 1);
                UpgradeCache[0] = null;
            }
            UpgradeCache[1] = null;
        }
    }
}
