package swordofmagic7.Item.ItemExtend;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Pet.PetData;
import swordofmagic7.Pet.PetParameter;

import java.util.Random;

import static swordofmagic7.Data.DataBase.PetList;
import static swordofmagic7.Data.PlayerData.playerData;

public class ItemPetEgg {
    public String PetId;
    public int PetMaxLevel;
    public int PetLevel;

    public void usePetEgg(Player player, ItemParameter clickedItem) {
        PlayerData playerData = playerData(player);
        playerData.ItemInventory.removeItemParameter(clickedItem, 1);
        PetData petData = PetList.get(clickedItem.itemPetEgg.PetId);
        ItemPetEgg petEgg = clickedItem.itemPetEgg;
        Random random = new Random();
        PetParameter pet = new PetParameter(player, playerData, petData, petEgg.PetLevel, petEgg.PetMaxLevel, 0, random.nextDouble()+0.5);
        playerData.PetInventory.addPetParameter(pet);
    }
}
