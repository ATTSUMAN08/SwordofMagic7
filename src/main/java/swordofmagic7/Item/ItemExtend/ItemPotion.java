package swordofmagic7.Item.ItemExtend;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Sound.CustomSound.playSound;

public class ItemPotion implements Cloneable {
    public ItemPotionType PotionType;
    public double[] Value = new double[4];
    public int CoolTime = 0;

    public void usePotion(Player player, ItemParameter currentItem) {
        PlayerData playerData = PlayerData.playerData(player);
        boolean used = false;
        double multiply = 1;
        if (playerData.Skill.hasSkill("PotionSommelier")) {
            multiply += DataBase.getSkillData("PotionSommelier").ParameterValue(0)/100;
        }
        if (playerData.PotionCoolTime.containsKey(PotionType)) {
            if (playerData.NaturalMessage) sendMessage(player, "§c[使用可能]§aまで§c[" + playerData.PotionCoolTime.get(PotionType) + "秒]§aです", SoundList.Nope);
        } else {
            if (PotionType.isHealth()) {
                if (playerData.Status.Health < playerData.Status.MaxHealth) {
                    double value;
                    if (PotionType.isElixir()) {
                        value = playerData.Status.MaxHealth * Value[0]/100f*multiply;
                    } else {
                        value = Value[0]*multiply;
                    }
                    playerData.changeHealth(value);
                    used = true;
                } else {
                    player.sendMessage("§e[体力]§aが減っていません");
                    playSound(player, SoundList.Nope);
                }
            }
            boolean isSprinkleManaPotion = playerData.Skill.hasSkill("SprinkleManaPotion");
            if (PotionType.isMana()) {
                if (playerData.Status.Mana < playerData.Status.MaxMana || isSprinkleManaPotion) {
                    double value;
                    if (PotionType.isElixir()) {
                        value = playerData.Status.MaxMana * Value[0]/100f*multiply;
                    } else {
                        value = Value[0]*multiply;
                    }
                    playerData.changeMana(value);
                    if (isSprinkleManaPotion) {
                        for (Player target : PlayerList.getNearNonDead(player.getLocation(), DataBase.getSkillData("SprinkleManaPotion").ParameterValue(0))) {
                            if (playerData.Skill.SkillProcess.isAllies(target)) {
                                PlayerData.playerData(target).changeMana(value);
                            }
                        }
                    }
                    used = true;
                } else {
                    player.sendMessage("§e[マナ]§aが減っていません");
                    playSound(player, SoundList.Nope);
                }
            }
        }
        if (used) {
            playerData.ItemInventory.removeItemParameter(currentItem, 1);
            playerData.PotionCoolTime.put(PotionType, CoolTime);
            playSound(player, SoundList.Heal);
            MultiThread.TaskRunSynchronized(() -> player.setCooldown(Material.GLASS_BOTTLE, CoolTime*20));
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
