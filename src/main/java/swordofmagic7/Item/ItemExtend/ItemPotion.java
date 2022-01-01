package swordofmagic7.Item.ItemExtend;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemPotion {
    public ItemPotionType PotionType;
    public double[] Value = new double[4];
    public int CoolTime = 0;

    public void usePotion(Player player, ItemParameter currentItem) {
        PlayerData playerData = PlayerData.playerData(player);
        boolean used = false;
        if (PotionType.isHealth()) {
            if (playerData.Status.Health < playerData.Status.MaxHealth) {
                playerData.changeHealth(Value[0]);
                used = true;
            } else {
                player.sendMessage("§e[体力]§aが減っていません");
                playSound(player, SoundList.Nope);
            }
        } else if (PotionType.isMana()) {
            if (playerData.Status.Mana < playerData.Status.MaxMana) {
                playerData.changeMana(Value[0]);
                used = true;
            } else {
                player.sendMessage("§e[マナ]§aが減っていません");
                playSound(player, SoundList.Nope);
            }
        }
        if (used) playerData.ItemInventory.removeItemParameter(currentItem, 1);
    }
}
