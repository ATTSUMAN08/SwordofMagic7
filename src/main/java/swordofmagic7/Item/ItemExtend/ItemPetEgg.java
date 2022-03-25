package swordofmagic7.Item.ItemExtend;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Pet.PetData;
import swordofmagic7.Pet.PetParameter;

import static swordofmagic7.Data.DataBase.PetList;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.System.random;

public class ItemPetEgg implements Cloneable  {
    public String PetId;
    public int PetMaxLevel;
    public int PetLevel;

    public void usePetEgg(Player player, ItemParameter clickedItem) {
        PlayerData playerData = playerData(player);
        playerData.ItemInventory.removeItemParameter(clickedItem, 1);
        PetData petData = PetList.get(clickedItem.itemPetEgg.PetId);
        ItemPetEgg petEgg = clickedItem.itemPetEgg;
        PetParameter pet = new PetParameter(player, playerData, petData, petEgg.PetLevel, petEgg.PetMaxLevel, 0, random.nextDouble()+0.5);
        playerData.PetInventory.addPetParameter(pet);
    }

    @Override
    public ItemPetEgg clone() {
        try {
            ItemPetEgg clone = (ItemPetEgg) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
