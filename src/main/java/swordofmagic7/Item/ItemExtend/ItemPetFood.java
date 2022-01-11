package swordofmagic7.Item.ItemExtend;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Pet.PetManager.ReqPetSelect;
import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemPetFood {
    public int Stamina;

    public void usePetFood(Player player, ItemParameter CurrentItem) {
        PlayerData playerData = PlayerData.playerData(player);
        if (playerData.PetSelect != null) {
            if (playerData.PetSelect.Stamina < playerData.PetSelect.MaxStamina) {
                playerData.PetSelect.changeStamina(Stamina);
                playerData.ItemInventory.removeItemParameter(CurrentItem, 1);
                playSound(player, SoundList.Heal);
            } else {
                player.sendMessage("§e[ペット]§aの§eスタミナ§aが§c最大§aです");
                playSound(player, SoundList.Nope);
            }
        } else {
            player.sendMessage(ReqPetSelect);
            playSound(player, SoundList.Nope);
        }
    }
}
