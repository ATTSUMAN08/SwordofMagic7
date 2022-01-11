package swordofmagic7.Item.ItemExtend;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class ItemPotion implements Cloneable {
    public ItemPotionType PotionType;
    public double[] Value = new double[4];
    public int CoolTime = 0;

    public void usePotion(Player player, ItemParameter currentItem) {
        PlayerData playerData = PlayerData.playerData(player);
        boolean used = false;
        if (playerData.PotionCoolTime.containsKey(PotionType)) {
            player.sendMessage("§c[使用可能]§aまで§c[" + playerData.PotionCoolTime.get(PotionType) + "秒]§aです");
            playSound(player, SoundList.Nope);
        } else if (PotionType.isHealth()) {
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
        if (used) {
            playerData.ItemInventory.removeItemParameter(currentItem, 1);
            playerData.PotionCoolTime.put(PotionType, CoolTime);
            playSound(player, SoundList.Heal);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (playerData.PotionCoolTime.containsKey(PotionType)) {
                        playerData.PotionCoolTime.put(PotionType, playerData.PotionCoolTime.get(PotionType)-1);
                        if (playerData.PotionCoolTime.get(PotionType) < 1) {
                            playerData.PotionCoolTime.remove(PotionType);
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0, 20);
        }
    }

    @Override
    public ItemPotion clone() {
        try {
            ItemPotion clone = (ItemPotion) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
