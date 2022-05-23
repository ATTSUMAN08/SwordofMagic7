package swordofmagic7.Equipment;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Tutorial;

import java.util.*;

import static swordofmagic7.Data.DataBase.ItemFlame;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Equipment {
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<EquipmentSlot, ItemParameter> EquipSlot = new HashMap<>();
    private final HashMap<EquipmentSlot, Set<RuneParameter>> EquipRune = new HashMap<>();

    public Equipment(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            EquipRune.put(equipmentSlot, new HashSet<>());
        }
    }

    public boolean isEquip(EquipmentSlot slot) {
        return EquipSlot.containsKey(slot);
    }

    public boolean isEquip(EquipmentSlot slot, EquipmentCategory category) {
        return isEquip(slot) && playerData.Equipment.getEquip(slot).itemEquipmentData.equipmentCategory == category;
    }
    public boolean isMainHandEquip() {
        return isEquip(EquipmentSlot.MainHand) && playerData.Equipment.getEquip(EquipmentSlot.MainHand).Category.isEquipment();
    }

    public boolean isMainHandEquip(EquipmentCategory category) {
        return isMainHandEquip() && playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.equipmentCategory == category;
    }
    public boolean isOffHandEquip() {
        return isEquip(EquipmentSlot.OffHand) && playerData.Equipment.getEquip(EquipmentSlot.OffHand).Category.isEquipment();
    }

    public boolean isOffHandEquip(EquipmentCategory category) {
        return isMainHandEquip() && playerData.Equipment.getEquip(EquipmentSlot.OffHand).itemEquipmentData.equipmentCategory == category;
    }

    public boolean isArmorEquip() {
        return isEquip(EquipmentSlot.Armor) && playerData.Equipment.getEquip(EquipmentSlot.Armor).Category.isEquipment();
    }

    public boolean isArmorEquip(EquipmentCategory category) {
        return isMainHandEquip() && playerData.Equipment.getEquip(EquipmentSlot.Armor).itemEquipmentData.equipmentCategory == category;
    }


    public ItemParameter getEquip(EquipmentSlot slot) {
        return EquipSlot.getOrDefault(slot, new ItemParameter());
    }

    private int reqLevel = 0;
    public void setToolEquipment(int level) {
        reqLevel = level;
    }

    public void viewEquip() {
        if (isEquip(EquipmentSlot.MainHand)) {
            ItemStack item = EquipSlot.get(EquipmentSlot.MainHand).viewItem(1, playerData.ViewFormat());
            if (EquipSlot.get(EquipmentSlot.MainHand).Category.isTool()) {
                int digSpeed = 0;
                if (item.getType() == Material.IRON_PICKAXE) {
                    digSpeed = (int) Math.floor((playerData.LifeStatus.getLevel(LifeType.Mine)-reqLevel)/5f);
                } else if (item.getType() == Material.IRON_AXE) {
                    digSpeed = (int) Math.floor((playerData.LifeStatus.getLevel(LifeType.Lumber)-reqLevel)/5f);
                }
                if (digSpeed > 0) item.addUnsafeEnchantment(Enchantment.DIG_SPEED, digSpeed);
            }
            player.getInventory().setItem(8, item);
        } else player.getInventory().setItem(8, new ItemStack(Material.AIR));

        if (isEquip(EquipmentSlot.OffHand)) {
            player.getInventory().setItem(40, EquipSlot.get(EquipmentSlot.OffHand).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(40, new ItemStack(Material.AIR));

        if (isEquip(EquipmentSlot.Armor)) {
            player.getInventory().setItem(36, EquipSlot.get(EquipmentSlot.Armor).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(36, ItemFlame);
        if (isEquip(EquipmentSlot.Accessory)) {
            player.getInventory().setItem(37, EquipSlot.get(EquipmentSlot.Accessory).viewItem(1, playerData.ViewFormat()));
        } else player.getInventory().setItem(37, ItemFlame);
        player.getInventory().setItem(38, ItemFlame);
        playerData.Status.StatusUpdate();
        for (PetParameter pet : playerData.PetSummon) {
            pet.updateStatus();
        }
    }

    public boolean Equip(EquipmentSlot slot, ItemParameter param) {
        if (playerData.Skill.isCastReady()) {
            param = param.clone();
            boolean req = false;
            List<String> reqText = new ArrayList<>();
            if (param.isEmpty()) {
                req = true;
                reqText.add("§eアイテムデータ§aが§c不正§aです");
            }
            if (playerData.Level < param.itemEquipmentData.ReqLevel) {
                reqText.add("§aレベルが足りません §c[要求:" + param.itemEquipmentData.ReqLevel + ",現在:" + playerData.Level + "]");
                req = true;
            }
            /*
            for (RuneParameter rune : param.itemEquipmentData.Rune) {
                if (playerData.Level < rune.Level) {
                    reqText.add("§e装着ルーン§aが§eキャラレベル§aを超えているため§e装備§aできません §c[要求:" + rune.Level + ",現在:" + playerData.Level + "]");
                    req = true;
                }
            }
             */
            if (req) {
                for (String msg : reqText) {
                    player.sendMessage(msg);
                }
                playSound(player, SoundList.Nope);
                return false;
            }

            if (EquipSlot.containsKey(slot)) {
                playerData.ItemInventory.addItemParameter(EquipSlot.get(slot), 1);
            }


            EquipSlot.put(slot, param.clone());
            EquipRune.get(slot).clear();
            for (RuneParameter rune : param.itemEquipmentData.Rune) {
                EquipRune.get(slot).add(rune);
            }
            playerData.ItemInventory.removeItemParameter(param, 1);

            player.sendMessage("§e[" + param.Display + "]§aを§e装備§aしました");
            Tutorial.tutorialTrigger(player, 2);
            return true;
        }
        return false;
    }

    public void unEquip(EquipmentSlot slot) {
        if (playerData.Skill.isCastReady()) {
            if (EquipSlot.containsKey(slot)) {
                playerData.ItemInventory.addItemParameter(EquipSlot.get(slot), 1);
                player.sendMessage("§e[" + getEquip(slot).Display + "]§aを外しました");
                EquipSlot.remove(slot);
                EquipRune.get(slot).clear();
            }
        }
    }

    public Set<RuneParameter> RuneList() {
        Set<RuneParameter> runes = new HashSet<>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            runes.addAll(EquipRune.get(equipmentSlot));
        }
        return runes;
    }

    public Set<RuneParameter> RuneList(EquipmentSlot slot) {
        return EquipRune.get(slot);
    }

    public RuneParameter equippedRune(String runeId) {
        for (RuneParameter rune : RuneList()) {
            if (rune.Id.equals(runeId)) return rune;
        }
        return null;
    }

    public RuneParameter equippedRune(EquipmentSlot slot, String runeId) {
        for (RuneParameter rune : RuneList(slot)) {
            if (rune.Id.equals(runeId)) return rune;
        }
        return null;
    }

    public boolean isEquipRune(String runeId) {
        return equippedRune(runeId) != null;
    }

    public boolean isEquipRune(EquipmentSlot slot, String runeId) {
        return equippedRune(slot, runeId) != null;
    }
}
