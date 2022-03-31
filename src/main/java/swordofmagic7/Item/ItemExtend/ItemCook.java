package swordofmagic7.Item.ItemExtend;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.InstantBuff.InstantBuffData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.StatusParameter;

import java.util.HashMap;

import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemCook {
    public boolean isBuff = false;
    public HashMap<StatusParameter, Double> Fixed = new HashMap<>();
    public HashMap<StatusParameter, Double> Multiply = new HashMap<>();
    public double Health;
    public double Mana;
    public int BuffTime = 0;
    public int CoolTime = 0;

    public void useCook(Player player, ItemParameter currentItem) {
        PlayerData playerData = PlayerData.playerData(player);
        if (playerData.useCookCoolTime > 0) {
            if (playerData.NaturalMessage) Function.sendMessage(player, "§c[使用可能]§aまで§c[" + playerData.useCookCoolTime + "秒]§aです", SoundList.Nope);
        } else {
            if (isBuff) {
                InstantBuffData instantBuffData = new InstantBuffData(Fixed, Multiply, BuffTime);
                playerData.instantBuff.instantBuff(currentItem.Display, instantBuffData);
            } else {
                playerData.changeHealth(Health);
                playerData.changeMana(Mana);
            }
            Function.sendMessage(player, "§e[" + currentItem.Display + "]§aを使用しました", SoundList.Eat);
            playerData.ItemInventory.removeItemParameter(currentItem, 1);
            playerData.useCookCoolTime = CoolTime;
            playSound(player, SoundList.Heal);
            MultiThread.TaskRunSynchronized(() -> player.setCooldown(currentItem.Icon, CoolTime*20));
            MultiThread.TaskRun(() -> {
                while (playerData.useCookCoolTime > 0) {
                    playerData.useCookCoolTime--;
                    MultiThread.sleepTick(20);
                }
            }, "useCookCoolTimeTask: " + player.getName());
        }
    }
}
