package swordofmagic7.Inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.System;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetInventory extends BasicInventory {
    private final java.util.List<PetParameter> List = new ArrayList<>();
    public BukkitTask task;
    public PetInventory(Player player, PlayerData playerData) {
        super(player, playerData);
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(System.plugin, () -> {
            if (List.size() > 0) {
                if (!player.isOnline() || !System.plugin.isEnabled()) {
                    task.cancel();
                }
                for (PetParameter pet : List) {
                    if (!pet.Summoned) {
                        pet.Stamina++;
                        if (pet.Stamina > pet.MaxStamina) pet.Stamina = pet.MaxStamina;
                    }
                    pet.Health += pet.HealthRegen / 5;
                    pet.Mana += pet.ManaRegen / 5;
                    if (pet.Health > pet.MaxHealth) pet.Health = pet.MaxHealth;
                    if (pet.Mana > pet.MaxMana) pet.Mana = pet.MaxMana;
                }
                if (playerData.ViewInventory.isPet()) {
                    viewPet();
                }
            }
        }, 0, 20);
    }

    public List<PetParameter> getList() {
        return List;
    }

    public void clear() {
        List.clear();
    }

    public void addPetParameter(PetParameter pet) {
        if (List.size() < 100) {
            List.add(pet);
            if (List.size() >= 95) {
                player.sendMessage("§e§インベントリ§aが§c残り" + (100 - List.size()) +"スロット§aです");
            }
        } else {
            player.sendMessage("§e§インベントリ§aが§c満杯§aです");
            playSound(player, SoundList.Nope);
        }

    }
    public PetParameter getPetParameter(int i) {
        if (i < List.size()) {
            return List.get(i);
        }
        return null;
    }

    void removePetParameter(int i) {
        List.remove(i);
    }

    public void viewPet() {
        playerData.ViewInventory = ViewInventoryType.PetInventory;
        int index = ScrollTick*8;
        int slot = 9;
        for (int i = index; i < index+24; i++) {
            if (i < List.size()) {
                ItemStack item = List.get(i).viewPet(playerData.ViewFormat());
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
}
