package swordofmagic7.HotBar;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Sound.SoundList;

import java.util.UUID;

import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Sound.CustomSound.playSound;

public class HotBar {
    public static final int HotBarSize = 128;
    private final Player player;
    private final PlayerData playerData;
    private int SelectSlot = -1;
    private HotBarData[] HotBarData = new HotBarData[HotBarSize];

    public HotBar(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;

        for(int i = 0; i < HotBarData.length; i++) {
            HotBarData[i] = new HotBarData();
        }
    }

    public void use(int index) {
        if (playerData.Skill.isCastReady()) {
            switch (HotBarData[index].category) {
                case Skill -> {
                    if (playerData.Skill.isCastReady()) {
                        playerData.Skill.CastSkill(getSkillData(HotBarData[index].Icon));
                    }
                }
                case Pet -> {
                    if (playerData.PetInventory.getHashMap().containsKey(UUID.fromString(HotBarData[index].Icon))) {
                        playerData.PetInventory.getHashMap().get(UUID.fromString(HotBarData[index].Icon)).spawn();
                    } else {
                        player.sendMessage("§e[ペットケージ]§aに居ません");
                        playSound(player, SoundList.Nope);
                    }
                }
                case Item -> {
                    ItemParameter item = playerData.ItemInventory.getItemParameter(HotBarData[index].Icon);
                    if (item != null) {
                        if (item.Category.isPotion()) {
                            item.itemPotion.usePotion(player, item);
                        } else if (item.Category.isCook()) {
                            item.itemCook.useCook(player, item);
                        } else if (item.Category.isEquipment()) {
                            playerData.Equipment.Equip(item.itemEquipmentData.EquipmentSlot, item);
                            playerData.viewUpdate();
                        } else if (item.Category.isTool()) {
                            playerData.Equipment.Equip(EquipmentSlot.MainHand, item);
                            playerData.viewUpdate();
                        }
                    } else {
                        player.sendMessage("§e[アイテム]§aがありません");
                        playSound(player, SoundList.Nope);
                    }
                }
                default -> player.sendMessage("§e[ホットバー" + (index + 1) + "]§aは§eセット§aされていません");
            }
            UpdateHotBar();
        } else if (playerData.NaturalMessage) {
            sendMessage(player, "§e[硬直]§a中は使用できません", SoundList.Nope);
        }
    }

    public void UpdateHotBar() {
        if (playerData.PlayMode) {
            if (playerData.ViewInventory.isHotBar()) viewTop();
            viewBottom();
        }
    }

    public void setHotBar(HotBarData[] HotBarData) {
        this.HotBarData = HotBarData.clone();
    }

    public void setHotBar(int index, HotBarData HotBarData) {
        this.HotBarData[index] = HotBarData.clone();
    }

    public void setSelectSlot(int slot) {
        SelectSlot = slot;
    }

    public int getSelectSlot() {
        return SelectSlot;
    }

    public void unSelectSlot() {
        SelectSlot = -1;
    }

    public void addHotbar(HotBarData hotBarData) {
        for (int i = 0; i < 32; i++) {
            if (HotBarData[i] != null) {
                if (HotBarData[i].isEmpty()) {
                    HotBarData[i] = hotBarData.clone();
                    return;
                }
            } else {
                HotBarData[i] = hotBarData.clone();
                return;
            }
        }
        player.sendMessage("§e[ホットバー]§aに空きがありません");
    }

    public HotBarData[] getHotBar() {
        return HotBarData;
    }

    HotBarData getHotBar(int index) {
        return HotBarData[index];
    }

    public void viewBottom() {
        if (playerData.PlayMode && player.getGameMode() == GameMode.SURVIVAL) {
            int offset = 0;
            if (playerData.CastMode.isRenewed()) {
                if (player.isSneaking()) offset += 8;
                if (playerData.isRightClickHold()) offset += 16;
            } else if (playerData.CastMode.isHold()) {
                if (player.isSneaking()) offset += 8;
            }
            for (int i = 0; i < 8; i++) {
                int slot = i + offset;
                if (HotBarData[slot] == null) HotBarData[slot] = new HotBarData();
                player.getInventory().setItem(i, HotBarData[slot].view(playerData, slot + 1, SelectSlot == slot));
            }
        }
    }

    public void viewTop() {
        if (playerData.PlayMode) {
            playerData.ViewInventory = ViewInventoryType.HotBar;
            int slot = 9;
            for (int i = 8; i < 32; i++) {
                player.getInventory().setItem(slot, HotBarData[i].view(playerData, i + 1, SelectSlot == i));
                slot++;
                if (slot == 17 || slot == 26 || slot == 35) slot++;
            }
        }
    }

    public void ScrollUp() {
        HotBarData[] HotBarDataOld = HotBarData.clone();
        System.arraycopy(HotBarDataOld, 8, HotBarData, 0, 24);
        System.arraycopy(HotBarDataOld, 0, HotBarData, 24, 8);
        playSound(player, SoundList.Tick);
    }

    public void ScrollDown() {
        HotBarData[] HotBarDataOld = HotBarData.clone();
        System.arraycopy(HotBarDataOld, 0, HotBarData, 8, 24);
        System.arraycopy(HotBarDataOld, 24, HotBarData, 0, 8);
        playSound(player, SoundList.Tick);
    }

    public void SkillSlotCommand(String[] args) {
        try {
            int index = Integer.parseInt(args[1]);
            if (1 <= index && index <= 3) {
                if (args[0].equalsIgnoreCase("save")) {
                    int slot = index * 32;
                    for (int i = 0; i < 32; i++) {
                        HotBarData[slot] = HotBarData[i];
                        slot++;
                    }
                } else if (args[0].equalsIgnoreCase("load")) {
                    int slot = index * 32;
                    for (int i = 0; i < 32; i++) {
                        HotBarData[i] = HotBarData[slot];
                        slot++;
                    }
                }
                playerData.viewUpdate();
            } else {
                sendMessage(player, "§e/skillSlot <save/load> <1~3>");
            }
        } catch (Exception e) {
            sendMessage(player, "§e/skillSlot <save/load> <1~3>");
        }
    }
}
