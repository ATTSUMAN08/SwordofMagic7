package swordofmagic7.Inventory;

import net.somrpg.swordofmagic7.SomCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import java.util.*;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetInventory extends BasicInventory {
    public int MaxSlot;
    public final List<PetParameter> List = new ArrayList<>();
    private final HashMap<UUID, PetParameter> HashMap = new HashMap<>();
    public PetInventory(Player player, PlayerData playerData) {
        super(player, playerData);
        if (player.hasPermission(DataBase.Som7Premium)) {
            MaxSlot = 600;
        } else if (player.hasPermission(DataBase.Som7VIP)) {
            MaxSlot = 400;
        } else {
            MaxSlot = 300;
        }
    }
    public PetSortType Sort = PetSortType.Name;
    public boolean SortReverse = false;

    public List<PetParameter> getList() {
        return List;
    }

    public HashMap<UUID, PetParameter> getHashMap() {
        return HashMap;
    }

    public void clear() {
        List.clear();
    }

    public void addPetParameter(PetParameter pet) {
        if (List.size() < MaxSlot) {
            if (List.size() >= MaxSlot-10) {
                sendMessage(player, "§e[ペットケージ]§aが§c残り" + (MaxSlot - List.size()) +"スロット§aです", SoundList.TICK);
            }
        } else {
            sendMessage(player, "§e[ペットケージ]§aが§c満杯§aです", SoundList.NOPE);
            SomCore.instance.spawnPlayer(player);
        }
        HashMap.put(pet.petUUID, pet);
        List.add(pet);
    }
    public PetParameter getPetParameter(int i) {
        if (i < List.size()) {
            return List.get(i);
        }
        return null;
    }

    public void removePetParameter(int i) {
        List.remove(i);
    }

    public boolean hasPetParameter(String name) {
        for (PetParameter pet : List) {
            if (pet.petData.Id.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void PetInventorySort() {
        switch (Sort) {
            case Name -> Sort = PetSortType.Level;
            case Level -> Sort = PetSortType.GrowthRate;
            case GrowthRate -> Sort = PetSortType.Name;
        }
        player.sendMessage("§e[ペットケージ]§aの§e[ソート方法]§aを§e[" + Sort.Display + "]§aにしました");
        playSound(player, SoundList.CLICK);
        playerData.viewUpdate();
    }

    public void PetInventorySortReverse() {
        SortReverse = !SortReverse;
        String msg = "§e[ペットケージ]§aの§e[ソート順]§aを";
        if (!SortReverse) msg += "§b[昇順]";
        else msg += "§c[降順]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.CLICK);
        playerData.viewUpdate();
    }

    public synchronized void viewPet() {
        playerData.ViewInventory = ViewInventoryType.PetInventory;
        int index = ScrollTick*8;
        int slot = 9;
        try {
            if (!List.isEmpty()) switch (Sort) {
                case Name -> List.sort(new PetSortName());
                case Level -> List.sort(new PetSortLevel());
                case GrowthRate -> List.sort(new PetSortGrowthRate());
            }
            if (SortReverse) Collections.reverse(List);
        } catch (Exception e) {
            sendMessage(player, "§eソート処理中§aに§cエラー§aが発生したため§eソート処理§aを§e中断§aしました §c" + e.getMessage());
        }
        int i = index;
        for (slot = 9; slot < 36; slot++) {
            if (i < List.size()) {
                while (i < List.size()) {
                    PetParameter pet = List.get(i);
                    if (wordSearch == null || pet.petData.Id.contains(wordSearch)) {
                        ItemStack item = pet.viewPet(playerData.ViewFormat());
                        ItemMeta meta = item.getItemMeta();
                        List<String> Lore = new ArrayList<>(meta.getLore());
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
}
