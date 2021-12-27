package swordofmagic7.Inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.decoInv;
import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Menu.Data.RuneMenuDisplay;
import static swordofmagic7.Menu.Menu.ignoreSlot;
import static swordofmagic7.Sound.CustomSound.playSound;

public class RuneInventory extends BasicInventory {
    private final java.util.List<RuneParameter> List = new ArrayList<>();

    public RuneInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    public List<RuneParameter> getList() {
        return List;
    }

    public void clear() {
        List.clear();
    }

    public void addRuneParameter(RuneParameter runeParameter) {
        if (List.size() < 300) {
            List.add(runeParameter.clone());
            if (List.size() >= 295) {
                player.sendMessage("§e§インベントリ§aが§c残り" + (300 - List.size()) +"スロット§aです");
            }
        } else {
            player.sendMessage("§e§インベントリ§aが§c満杯§aです");
            playSound(player, SoundList.Nope);
        }

    }
    RuneParameter getRuneParameter(int i) {
        if (i < List.size()) {
            return List.get(i).clone();
        }
        return null;
    }

    void removeRuneParameter(int i) {
        List.remove(i);
    }

    public void viewRune() {
        playerData.ViewInventory = ViewInventoryType.RuneInventory;
        int index = ScrollTick*8;
        int slot = 9;
        for (int i = index; i < index+24; i++) {
            if (i < List.size()) {
                ItemStack item = List.get(i).viewRune(playerData.ViewFormat());
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add("§8" + i);
                meta.setLore(Lore);
                item.setItemMeta(meta);
                player.getInventory().setItem(slot, item);
            } else {
                player.getInventory().setItem(slot, AirItem);
            }
            slot++;
            if (slot == 17 || slot == 26) slot++;
        }
    }

    static List<Integer> RuneSlotIndex() {
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

    public void RuneMenuView() {
        org.bukkit.inventory.Inventory inv = decoInv(RuneMenuDisplay, 5);
        playerData.Menu.ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.ItemInventory);
        for (int slot = 0; slot < 45; slot++) {
            if (slot != 20 && !RuneSlotIndex().contains(slot)) {
                inv.setItem(slot, new ItemStack(Material.BROWN_STAINED_GLASS_PANE));
            }
        }
        player.openInventory(inv);
    }

    private ItemParameter RuneCache;
    public void RuneMenuClick(InventoryView view, org.bukkit.inventory.Inventory ClickInventory, ItemStack currentItem, int index, int Slot) {
        if (equalInv(view, RuneMenuDisplay)) {
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
                            playerData.setView(ViewInventoryType.RuneInventory, false);
                            playSound(player, SoundList.Click);
                        } else if (playerData.ViewInventory.isRune()) {
                            if (RuneCache != null) {
                                if (RuneCache.getRuneSize() < RuneCache.RuneSlot) {
                                    RuneParameter runeParameter = playerData.RuneInventory.getRuneParameter(index);
                                    playerData.RuneInventory.removeRuneParameter(index);
                                    RuneCache.addRune(runeParameter);
                                    playSound(player, SoundList.Click);
                                } else {
                                    player.sendMessage("§eルーンスロット§aに空きがありません");
                                    playSound(player, SoundList.Nope);
                                }
                            } else {
                                player.sendMessage("§e装備§aを§eセット§aしてください");
                                playSound(player, SoundList.Nope);
                            }
                        }
                    }
                } else if (view.getTopInventory() == ClickInventory) {
                    if (Slot == 20) {
                        playerData.ItemInventory.addItemParameter(RuneCache, 1);
                        RuneCache = null;
                        playerData.setView(ViewInventoryType.ItemInventory, false);
                        playSound(player, SoundList.Click);
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
                            playSound(player, SoundList.Click);
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
        }
    }

    public void RuneMenuClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        player.setItemOnCursor(AirItem);
        if (equalInv(view, RuneMenuDisplay)) {
            if (RuneCache != null) {
                playerData.ItemInventory.addItemParameter(RuneCache, 1);
                RuneCache = null;
            }
        }
    }
}