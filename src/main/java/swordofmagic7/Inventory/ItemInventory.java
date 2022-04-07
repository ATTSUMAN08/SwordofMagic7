package swordofmagic7.Inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.*;
import static swordofmagic7.SomCore.spawnPlayer;
import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemInventory extends BasicInventory {
    public final int MaxSlot = 1000;
    private final List<ItemParameterStack> List = new ArrayList<>();
    private final String itemStack = decoText("§3§lアイテムスタック");
    public ItemSortType Sort = ItemSortType.Category;
    public boolean SortReverse = false;

    public ItemInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    public List<ItemParameterStack> getList() {
        return List;
    }

    public void clear() {
        List.clear();
    }

    public void ItemInventorySort() {
        switch (Sort) {
            case Name -> Sort = ItemSortType.Category;
            case Category -> Sort = ItemSortType.Amount;
            case Amount -> Sort = ItemSortType.Name;
        }
        player.sendMessage("§e[アイテムインベントリ]§aの§e[ソート方法]§aを§e[" + Sort.Display + "]§aにしました");
        playSound(player, SoundList.Click);
        playerData.viewUpdate();
    }

    public void ItemInventorySortReverse() {
        SortReverse = !SortReverse;
        String msg = "§e[アイテムインベントリ]§aの§e[ソート順]§aを";
        if (!SortReverse) msg += "§b[昇順]";
        else msg += "§c[降順]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
        playerData.viewUpdate();
    }

    public void viewInventory() {
        playerData.ViewInventory = ViewInventoryType.ItemInventory;
        int index = ScrollTick*8;
        int slot = 9;
        List.removeIf(stack -> stack.Amount <= 0);
        Comparator<ItemParameterStack> comparator = null;
        try {
            if (List.size() > 0) switch (Sort) {
                case Name -> comparator = new ItemSortName();
                case Category -> comparator = new ItemSortCategory();
                case Amount -> comparator = new ItemSortAmount();
            }
        } catch (Exception ignored) {}
        if (comparator != null) List.sort(comparator);
        if (SortReverse) Collections.reverse(List);
        for (int i = index; i < index+24; i++) {
            if (i < List.size()) {
                ItemParameterStack stack = List.get(i);
                ItemStack item = stack.itemParameter.viewItem(stack.Amount, playerData.ViewFormat());
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add(itemStack);
                Lore.add(decoLore("個数") + stack.Amount);
                Lore.add(decoLore("売値") + stack.itemParameter.Sell * stack.Amount);
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

    public ItemParameterStack getItemParameterStack(ItemParameter param) {
        for (ItemParameterStack stack : List) {
            if (ItemStackCheck(stack.itemParameter, param)) {
                return stack;
            }
        }
        return new ItemParameterStack(param.clone());
    }

    public ItemParameterStack getItemParameterStack(int i) {
        if (i < List.size()) {
            return List.get(i);
        }
        return null;
    }

    public ItemParameter getItemParameter(int i) {
        if (i < List.size()) {
            return List.get(i).itemParameter.clone();
        } else return null;
    }

    public ItemParameter getItemParameter(String name) {
        for (ItemParameterStack stack : List) {
            if (stack.itemParameter.Id.equals(name)) {
                return stack.itemParameter;
            }
        }
        return null;
    }


    public boolean hasItemParameter(ItemParameterStack stack) {
        return hasItemParameter(stack.itemParameter, stack.Amount);
    }

    public boolean hasItemParameter(ItemParameter param, int amount) {
        for (ItemParameterStack stack : List) {
            if (ItemStackCheck(stack.itemParameter, param)) {
                return stack.Amount >= amount;
            }
        }
        return false;
    }

    public void addItemParameter(ItemParameterStack stack) {
        addItemParameter(stack.itemParameter, stack.Amount);
    }

    public void addItemParameter(ItemParameter param, int addAmount) {
        if (List.size() < MaxSlot) {
            if (List.size() >= MaxSlot-10) {
                sendMessage(player, "§e[アイテムインベントリ]§aが§c残り" + (MaxSlot - List.size()) +"スロット§aです", SoundList.Tick);
            }
        } else {
            sendMessage(player, "§e[アイテムインベントリ]§aが§c満杯§aです", SoundList.Nope);
            spawnPlayer(player);
        }
        ItemParameterStack stack = getItemParameterStack(param);
        if (stack.Amount > 0) {
            stack.Amount += addAmount;
        } else {
            ItemParameterStack newStack = new ItemParameterStack();
            newStack.itemParameter = param.clone();
            newStack.Amount = addAmount;
            List.add(newStack);
        }
    }

    public void removeItemParameter(ItemParameterStack stack) {
        removeItemParameter(stack.itemParameter, stack.Amount);
    }

    public void removeItemParameter(ItemParameter param, int removeAmount) {
        ItemParameterStack stack = getItemParameterStack(param);
        stack.Amount -= removeAmount;
        if (stack.Amount <= 0) {
            List.remove(stack);
        }
    }

    public void removeItemParameter(int index, int removeAmount) {
        ItemParameterStack stack = getItemParameterStack(index);
        stack.Amount -= removeAmount;
        if (stack.Amount <= 0) {
            List.remove(stack);
        }
    }

    boolean ItemStackCheck(ItemParameter param1, ItemParameter param2) {
        if (param1.Id.equals(param2.Id)) {
            if (param1.Category.isEquipment()) {
                if (param1.itemEquipmentData.Durable == param2.itemEquipmentData.Durable &&
                        param1.itemEquipmentData.Plus == param2.itemEquipmentData.Plus &&
                        param1.itemEquipmentData.getRuneSize() == param2.itemEquipmentData.getRuneSize()) {
                    if (0 < param1.itemEquipmentData.getRuneSize()) {
                        for (int i = 0; i < param1.itemEquipmentData.getRuneSize(); i++) {
                            final RuneParameter rune1 = param1.itemEquipmentData.getRune(i);
                            final RuneParameter rune2 = param2.itemEquipmentData.getRune(i);
                            if (!rune1.toString().equals(rune2.toString())) {
                                return false;
                            }
                        }
                    }
                    return true;
                } else return false;
            } else return true;
        }
        return false;
    }
}