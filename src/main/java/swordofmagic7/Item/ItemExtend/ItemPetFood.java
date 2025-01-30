package swordofmagic7.Item.ItemExtend;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Pet.PetManager.ReqPetSelect;
import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemPetFood implements Cloneable{
    public int Stamina;

    public void usePetFood(Player player, ItemParameter CurrentItem) {
        PlayerData playerData = PlayerData.playerData(player);
        PetParameter pet = playerData.getPetSelect();
        if (pet != null) {
            if (pet.Stamina < pet.MaxStamina) {
                pet.changeStamina(Stamina);
                playerData.ItemInventory.removeItemParameter(CurrentItem, 1);
                playSound(player, SoundList.HEAL);
            } else {
                player.sendMessage("§e[ペット]§aの§eスタミナ§aが§c最大§aです");
                playSound(player, SoundList.NOPE);
            }
        } else {
            player.sendMessage(ReqPetSelect);
            playSound(player, SoundList.NOPE);
        }
    }

    @Override
    public ItemPetFood clone() {
        try {
            ItemPetFood clone = (ItemPetFood) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
