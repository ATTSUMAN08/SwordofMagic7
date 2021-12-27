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
import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemInventory extends BasicInventory {
    private final java.util.List<ItemParameterStack> List = new ArrayList<>();
    private final String itemStack = decoText("§3§lアイテムスタック");

    public ItemInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    public List<ItemParameterStack> getList() {
        return List;
    }

    public void clear() {
        List.clear();
    }

    public void viewInventory() {
        playerData.ViewInventory = ViewInventoryType.ItemInventory;
        int index = ScrollTick*8;
        int slot = 9;
        for (int i = index; i < index+24; i++) {
            if (i < List.size()) {
                ItemParameterStack stack = List.get(i);
                ItemStack item = stack.itemParameter.viewItem(stack.Amount, playerData.ViewFormat());
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add(itemStack);
                Lore.add(decoLore("個数") + stack.Amount);
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

    ItemParameterStack getItemParameterStack(ItemParameter param) {
        for (ItemParameterStack stack : List) {
            if (ItemStackCheck(stack.itemParameter, param)) {
                return stack;
            }
        }
        return null;
    }

    public ItemParameter getItemParameter(int i) {
        if (i < List.size()) {
            return List.get(i).itemParameter.clone();
        }
        return null;
    }

    public void addItemParameter(ItemParameter param, int addAmount) {
        if (List.size() < 300) {
            ItemParameterStack stack = getItemParameterStack(param);
            if (stack != null) {
                stack.Amount += addAmount;
            } else {
                ItemParameterStack newStack = new ItemParameterStack();
                newStack.itemParameter = param.clone();
                newStack.Amount = addAmount;
                List.add(newStack);
            }
            if (List.size() >= 295) {
                player.sendMessage("§eアイテムインベントリ§aが§c残り" + (300 - List.size()) +"スロット§aです");
            }
        } else {
            player.sendMessage("§eアイテムインベントリ§aが§c満杯§aです");
            playSound(player, SoundList.Nope);
        }
    }

    public void removeItemParameter(ItemParameter param, int removeAmount) {
        ItemParameterStack stack = getItemParameterStack(param);
        if (stack != null) {
            stack.Amount -= removeAmount;
            if (stack.Amount <= 0) {
                List.remove(stack);
            }
        }
    }

    boolean ItemStackCheck(ItemParameter param1, ItemParameter param2) {
        if (param1.Display.equals(param2.Display) &&
                param1.Durable == param2.Durable &&
                param1.Plus == param2.Plus &&
                param1.getRuneSize() == param2.getRuneSize()) {
            if (0 < param1.getRuneSize()) {
                for (int i = 0; i < param1.getRuneSize(); i++) {
                    final RuneParameter rune1 = param1.getRune(i);
                    final RuneParameter rune2 = param2.getRune(i);
                    if (rune1.Display.equals(rune2.Display) &&
                            rune1.Level == rune2.Level &&
                            rune1.Quality == rune2.Quality) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }
}
