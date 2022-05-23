package swordofmagic7.Inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.StatusParameter;

import java.util.*;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.*;
import static swordofmagic7.SomCore.spawnPlayer;
import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemInventory extends BasicInventory {
    public final int MaxSlot = 300;
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

    public synchronized void viewInventory() {
        playerData.ViewInventory = ViewInventoryType.ItemInventory;
        int index = ScrollTick*8;
        int slot;
        List.removeIf(stack -> stack.Amount <= 0);
        try {
            if (List.size() > 0) switch (Sort) {
                case Name -> List.sort(new ItemSortName());
                case Category -> List.sort(new ItemSortCategory());
                case Amount -> List.sort(new ItemSortAmount());
            }
            if (SortReverse) Collections.reverse(List);
        } catch (Exception e) {
            sendMessage(player, "§eソート処理中§aに§cエラー§aが発生したため§eソート処理§aを§e中断§aしました" + e.getMessage());
        }
        int i = index;
        for (slot = 9; slot < 36; slot++) {
            if (i < List.size()) {
                while (i < List.size()) {
                    ItemParameterStack stack = List.get(i);
                    if (wordSearch == null || stack.itemParameter.Id.contains(wordSearch)) {
                        ItemStack item = stack.itemParameter.viewItem(stack.Amount, playerData.ViewFormat(), false);
                        ItemMeta meta = item.getItemMeta();
                        List<String> Lore = new ArrayList<>(meta.getLore());
                        Lore.add(itemStack);
                        Lore.add(decoLore("個数") + stack.Amount);
                        Lore.add(decoLore("売値") + stack.itemParameter.Sell * stack.Amount);
                        Lore.add("§8SlotID:" + i);
                        meta.setLore(Lore);
                        item.setItemMeta(meta);
                        player.getInventory().setItem(slot, item);
                        i++;
                        break;
                    }
                    i++;
                }
            } else {
                player.getInventory().setItem(slot, AirItem);
            }
            if (slot == 16 || slot == 25) slot++;
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

    public synchronized void addItemParameter(ItemParameter param, int addAmount) {
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

    public static boolean ItemStackCheck(ItemParameter param1, ItemParameter param2) {
        if (param1.Id.equals(param2.Id)) {
            if (param1.Category.isEquipment()) {
                if (param1.itemEquipmentData.Plus == param2.itemEquipmentData.Plus &&
                    param1.itemEquipmentData.getRuneSize() == param2.itemEquipmentData.getRuneSize()
                ) {
                    if (param1.itemEquipmentData.isAccessory()) {
                        Set<StatusParameter> params = new HashSet<>();
                        params.addAll(param1.itemEquipmentData.itemAccessory.Parameter.keySet());
                        params.addAll(param2.itemEquipmentData.itemAccessory.Parameter.keySet());
                        for (StatusParameter key : params) {
                            if (!param1.itemEquipmentData.itemAccessory.Parameter.getOrDefault(key, -1d).equals(param2.itemEquipmentData.itemAccessory.Parameter.getOrDefault(key, -1d))) {
                                return false;
                            }
                        }
                    }
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