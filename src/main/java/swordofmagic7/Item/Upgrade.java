package swordofmagic7.Item;

import net.somrpg.swordofmagic7.SomCore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Client;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Life.LifeType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.UpgradeDisplay;
import static net.somrpg.swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

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

    public int UpgradeCost(ItemParameter item) {
        double cost = item.itemEquipmentData.UpgradeCost * (1+item.itemEquipmentData.Plus/10f);
        return (int) Math.round(cost);
    }

    public int UpgradeMinCost(ItemParameter item) {
        return Math.round(UpgradeCost(item)/2f * (1/(1+playerData.LifeStatus.getLevel(LifeType.Smith)/30f)));
    }

    public int UpgradeMel(ItemParameter item) {
        return UpgradeMinCost(item)*50;
    }

    public double UpgradePercent(int plus) {
        double percent = plus >= 10 ? 0.5 : 1;
        if (5 <= plus && plus < 10) {
            percent = (14-plus)/10f;
        }
        return percent;
    }

    private boolean isClickTick = false;
    public final ItemParameter[] UpgradeCache = new ItemParameter[2];
    public static final ItemParameter UpgradeStone = getItemParameter("強化石");
    public static final ItemParameter UpgradeProtect = getItemParameter("強化保護結晶");
    public int fastUpgrade = 15;
    private Material upgradeIcon = UpgradeStone.Icon;
    public synchronized void UpgradeClick(InventoryView view, Inventory ClickInventory, int index, int Slot) {
        if (equalInv(view, UpgradeDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (isClickTick) return;
                isClickTick = true;
                MultiThread.TaskRunSynchronizedLater(() -> isClickTick = false, 2);
                if (Slot == AnvilUISlot[1]) {
                    if (upgradeIcon == UpgradeStone.Icon) {
                        if (playerData.ItemInventory.hasItemParameter(UpgradeProtect, 1)) {
                            upgradeIcon = UpgradeProtect.Icon;
                            sendMessage(player, "§e強化値保護§aを§b有効§aにしました", SoundList.TICK);
                        }
                    } else {
                        upgradeIcon = UpgradeStone.Icon;
                        sendMessage(player, "§e強化値保護§aを§c無効§aにしました", SoundList.TICK);
                    }
                } else if (Slot == AnvilUISlot[2]) {
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
                                String itemText = "§e[" + UpgradeCache[1].Display + "§b+" + plus + "§e]";
                                String suffix;
                                if (random.nextDouble() < percent) {
                                    UpgradeCache[0] = UpgradeCache[1].clone();
                                    suffix = "§aの強化に§b成功§aしました " + perText;
                                    if (plus >= fastUpgrade) {
                                        playerData.ItemInventory.addItemParameter(UpgradeCache[0], 1);
                                        UpgradeCache[0] = null;
                                    }
                                    sendMessage(player, itemText + suffix, SoundList.LEVEL_UP);
                                    for (int i = 15; i <= 25; i++) {
                                        if (plus >= i) playerData.titleManager.addTitle("装備強化+" + i);
                                    }
                                } else {
                                    suffix = "§aの強化に§c失敗§aしました " + perText;
                                    player.sendMessage(itemText + suffix);
                                    if (UpgradeCache[0].itemEquipmentData.Plus > 10) {
                                        if (upgradeIcon == UpgradeProtect.Icon) {
                                            playerData.ItemInventory.removeItemParameter(UpgradeProtect, 1);
                                            sendMessage(player, "§e[" + UpgradeProtect.Display + "]§aの効果により§e強化値§aが§b保護§aされました");
                                        } else {
                                            UpgradeCache[0].itemEquipmentData.Plus = 10;
                                            sendMessage(player, "§e[" + UpgradeCache[0].Display + "]§aの§e強化値§aが§e+10§aに落ちました");
                                        }
                                    }
                                    playSound(player, SoundList.TICK);
                                }
                                upgradeIcon = UpgradeStone.Icon;
                                player.sendMessage("§e[強化石]§aを§e[" + removeCost + "個]§a消費しました");
                                playerData.LifeStatus.addLifeExp(LifeType.Smith, cost);

                                if (!SomCore.Companion.isDevServer() && UpgradeCache[1].itemEquipmentData.Plus >= 20) {
                                    TextView text = new TextView(playerData.getNick() + "§aさんが");
                                    text.addView(UpgradeCache[1].getTextView(1, playerData.ViewFormat()));
                                    text.addText(suffix);
                                    Client.sendDisplay(player, text);
                                }
                            } else {
                                player.sendMessage("§e[強化石]§aが§e[" + cost + "個]§a必要です");
                                playSound(player, SoundList.NOPE);
                            }
                        } else {
                            player.sendMessage("§e" + mel + "メル§a必要です");
                            playSound(player, SoundList.NOPE);
                        }
                    }
                } else if (Slot == AnvilUISlot[0] && UpgradeCache[0] != null) {
                    playerData.ItemInventory.addItemParameter(UpgradeCache[0], 1);
                    UpgradeCache[0] = null;
                    playSound(player, SoundList.CLICK);
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
                        playSound(player, SoundList.CLICK);
                    } else {
                        sendMessage(player, "§c強化上限§aです", SoundList.NOPE);
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
                Lore.add(decoLore("消費強化石") + minCost + "～" + cost + "個 §7(" + playerData.ItemInventory.getItemParameterStack(UpgradeStone).Amount + ")");
                Lore.add(decoLore("強化成功率") + String.format("%.0f", UpgradePercent(UpgradeCache[0].itemEquipmentData.Plus)*100) + "%");
                ItemStack viewCost = new ItemStackData(upgradeIcon, decoText("強化コスト"), Lore).view();
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