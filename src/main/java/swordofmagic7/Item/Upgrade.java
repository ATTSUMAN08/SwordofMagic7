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
        double cost = item.itemEquipmentData.UpgradeCost * (1+item.itemEquipmentData.Plus/10f);
        return (int) Math.round(cost);
    }

    private int UpgradeMinCost(ItemParameter item) {
        return Math.round(UpgradeCost(item)/2f * (1/(1+playerData.LifeStatus.getLevel(LifeType.Smith)/30f)));
    }

    private int UpgradeMel(ItemParameter item) {
        return UpgradeMinCost(item)*33;
    }

    public double UpgradePercent(int plus) {
        double percent = plus >= 10 ? 0.5 : 1;
        if (5 <= plus && plus < 10) {
            percent = (14-plus)/10f;
        }
        return percent;
    }

    public final ItemParameter[] UpgradeCache = new ItemParameter[2];
    private final ItemParameter UpgradeStone = getItemParameter("強化石");
    public void UpgradeClick(InventoryView view, Inventory ClickInventory, int index, int Slot) {
        if (equalInv(view, UpgradeDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot == AnvilUISlot[2]) {
                    if (UpgradeCache[0] != null) {
                        int cost = UpgradeCost(UpgradeCache[0]);
                        int minCost = UpgradeMinCost(UpgradeCache[0]);
                        int mel = UpgradeMel(UpgradeCache[0]);
                        if (playerData.Mel >= mel) {
                            if (playerData.ItemInventory.hasItemParameter(UpgradeStone, cost)) {
                                double percent = UpgradePercent(UpgradeCache[0].itemEquipmentData.Plus);
                                int removeCost = (int) Math.round(minCost * random.nextDouble() + minCost);
                                playerData.ItemInventory.removeItemParameter(UpgradeStone, removeCost);
                                playerData.Mel -= mel;
                                int plus = UpgradeCache[1].itemEquipmentData.Plus;
                                String perText = plus > 10 ? "§b[" + Math.pow(0.5, (plus - 10)) * 100 + "%]" : "";
                                playerData.statistics.UpgradeUseCostCount += removeCost;
                                if (random.nextDouble() < percent) {
                                    UpgradeCache[0] = UpgradeCache[1].clone();
                                    String text = "§e[" + UpgradeCache[1].Display + "+" + plus + "]§aの強化に§b成功§aしました " + perText;
                                    if (plus >= 15) {
                                        BroadCast(playerData.getNick() + "§aさんが" + text);
                                        playerData.ItemInventory.addItemParameter(UpgradeCache[0], 1);
                                        UpgradeCache[0] = null;
                                    } else {
                                        player.sendMessage(text);
                                    }
                                    playSound(player, SoundList.LevelUp);
                                    for (int i = 15; i < 25; i++) {
                                        if (plus >= i) playerData.titleManager.addTitle("装備強化+" + i);
                                    }
                                } else {
                                    String text = "§e[" + UpgradeCache[1].Display + "+" + UpgradeCache[1].itemEquipmentData.Plus + "]§aの強化に§c失敗§aしました " + perText;
                                    if (UpgradeCache[1].itemEquipmentData.Plus >= 15) {
                                        BroadCast(playerData.getNick() + "§aさんが" + text);
                                    } else {
                                        player.sendMessage(text);
                                    }
                                    if (UpgradeCache[0].itemEquipmentData.Plus > 10) {
                                        UpgradeCache[0].itemEquipmentData.Plus = 10;
                                        player.sendMessage("§e[" + UpgradeCache[0].Display + "]§aの§e強化値§aが§e+10§aに落ちました");
                                    }
                                    playSound(player, SoundList.Tick);
                                }
                                player.sendMessage("§e[強化石]§aを§e[" + removeCost + "個]§a消費しました");
                                playerData.LifeStatus.addLifeExp(LifeType.Smith, cost);
                            } else {
                                player.sendMessage("§e[強化石]§aが§e[" + cost + "個]§a必要です");
                                playSound(player, SoundList.Nope);
                            }
                        } else {
                            player.sendMessage("§e" + mel + "メル§a必要です");
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
                    if (itemParameter.itemEquipmentData.Plus < 25) {
                        if (UpgradeCache[0] != null) {
                            playerData.ItemInventory.addItemParameter(UpgradeCache[0], 1);
                        }
                        playerData.ItemInventory.removeItemParameter(itemParameter, 1);
                        UpgradeCache[0] = itemParameter;
                        playSound(player, SoundList.Click);
                    } else {
                        sendMessage(player, "§c強化上限§aです", SoundList.Nope);
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
                Lore.add(decoLore("必要メル") + UpgradeMel(UpgradeCache[0]));
                Lore.add(decoLore("消費強化石") + minCost + "～" + cost + "個");
                Lore.add(decoLore("強化成功率") + String.format("%.0f", UpgradePercent(UpgradeCache[0].itemEquipmentData.Plus)*100) + "%");
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