package swordofmagic7;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.AirItem;
import static swordofmagic7.Function.*;
import static swordofmagic7.SoundList.Click;
import static swordofmagic7.SoundList.Nope;

public class Inventory {
    final Player player;
    final PlayerData playerData;

    int ScrollTick = 0;

    Inventory(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    void setScrollTick(int tick) {
        this.ScrollTick = tick;
    }
    void upScrollTick() {
        if (ScrollTick > 0) {
            this.ScrollTick--;
            playSound(player, Click);
        }
    }
    void downScrollTick(int size) {
        double scroll = size/8f;
        if (ScrollTick+3 < scroll) {
            this.ScrollTick++;
            playSound(player, Click);
        }
        else if (ScrollTick > scroll) this.ScrollTick = (int) Math.floor(scroll);
    }
}

class ItemInventory extends Inventory {
    private final List<ItemParameterStack> List = new ArrayList<>();
    private final String itemStack = decoText("&3&lアイテムスタック");

    ItemInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    List<ItemParameterStack> getList() {
        return List;
    }

    void clear() {
        List.clear();
    }

    void viewInventory() {
        playerData.ViewInventory = ViewInventory.ItemInventory;
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
                Lore.add(colored("&8" + i));
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

    ItemParameter getItemParameter(int i) {
        if (i < List.size()) {
            return List.get(i).itemParameter.clone();
        }
        return null;
    }

    void addItemParameter(ItemParameter param, int addAmount) {
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
                player.sendMessage(colored("&eアイテムインベントリ&aが&c残り" + (300 - List.size()) +"スロット&aです"));
            }
        } else {
            player.sendMessage(colored("&eアイテムインベントリ&aが&c満杯&aです"));
            playSound(player, Nope);
        }
    }

    void removeItemParameter(ItemParameter param, int removeAmount) {
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
                param1.getModuleSize() == param2.getModuleSize()) {
            if (0 < param1.getModuleSize()) {
                for (int i = 0; i < param1.getModuleSize(); i++) {
                    final ModuleParameter module1 = param1.getModule(i);
                    final ModuleParameter module2 = param2.getModule(i);
                    if (module1.Display.equals(module2.Display) &&
                            module1.Level == module2.Level &&
                            module1.Quality == module2.Quality) {
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

class ItemParameterStack {
    ItemParameter itemParameter;
    ItemParameterStack() {
        this.itemParameter = new ItemParameter();
    }

    ItemParameterStack(ItemParameter itemParameter) {
        this.itemParameter = itemParameter;
    }
    int Amount = 0;

    boolean isEmpty() {
        return itemParameter.Icon == Material.BARRIER;
    }
}

class ModuleInventory extends Inventory {
    private final List<ModuleParameter> List = new ArrayList<>();

    ModuleInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    List<ModuleParameter> getList() {
        return List;
    }

    void clear() {
        List.clear();
    }

    void addModuleParameter(ModuleParameter moduleParameter) {
        if (List.size() < 300) {
            List.add(moduleParameter.clone());
            if (List.size() >= 295) {
                player.sendMessage(colored("&eモジュールインベントリ&aが&c残り" + (300 - List.size()) +"スロット&aです"));
            }
        } else {
            player.sendMessage(colored("&eモジュールインベントリ&aが&c満杯&aです"));
            playSound(player, Nope);
        }

    }
    ModuleParameter getModuleParameter(int i) {
        if (i < List.size()) {
            return List.get(i).clone();
        }
        return null;
    }

    void removeModuleParameter(int i) {
        List.remove(i);
    }

    void viewModule() {
        playerData.ViewInventory = ViewInventory.ModuleInventory;
        int index = ScrollTick*8;
        int slot = 9;
        for (int i = index; i < index+24; i++) {
            if (i < List.size()) {
                ItemStack item = List.get(i).viewModule(playerData.ViewFormat());
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add(colored("&8" + i));
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
}