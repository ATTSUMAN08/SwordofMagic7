package swordofmagic7.Menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Attribute.AttributeType;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Life.LifeType;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Status.Status;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.ItemStackPlayerHead;
import static swordofmagic7.Data.DataBase.getClassList;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.StatusInfoDisplay;

public class StatusInfo {

    private final Player player;
    private final PlayerData playerData;

    StatusInfo(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }
    public void StatusInfoView(Player player) {
        final Inventory inv = decoInv(StatusInfoDisplay, 1);
        final PlayerData playerData = playerData(player);
        final String format = playerData.ViewFormat();
        final Status status = playerData.Status;
        final Player Viewer = this.player;
        Viewer.openInventory(inv);
        MultiThread.TaskRun(() -> {
            final ItemStack statusIcon = ItemStackPlayerHead(player);
            final ItemMeta statusMeta = statusIcon.getItemMeta();
            while (Viewer.getOpenInventory().getTopInventory().equals(inv)) {
                statusMeta.setDisplayName(decoText(playerData.Nick));
                List<String> statusLore = new ArrayList<>();
                statusLore.add(decoLore("現在位置") + playerData.Map.Display);
                statusLore.add(decoLore("所持メル") + playerData.Mel + "メル");
                statusLore.add(decoLore("レベル") + playerData.Level + "/" + PlayerData.MaxLevel);
                statusLore.add(decoLore("経験値") + playerData.viewExpPercent());
                statusLore.add(decoLore("戦闘力") + String.format(format, playerData.Status.getCombatPower()));
                statusLore.add(decoLore(StatusParameter.MaxHealth.Display) + String.format(format, status.Health) + "/" + String.format(format, status.MaxHealth) + " (" + String.format(format, status.BaseStatus(StatusParameter.MaxHealth)) + ")");
                statusLore.add(StatusParameter.HealthRegen.DecoDisplay + String.format(format, status.HealthRegen) + " (" + String.format(format, status.BaseStatus(StatusParameter.HealthRegen)) + ")");
                statusLore.add(decoLore(StatusParameter.MaxMana.Display) + String.format(format, status.Mana) + "/" + String.format(format, status.MaxMana) + " (" + String.format(format, status.BaseStatus(StatusParameter.MaxMana)) + ")");
                statusLore.add(StatusParameter.ManaRegen.DecoDisplay + String.format(format, status.ManaRegen) + " (" + String.format(format, status.BaseStatus(StatusParameter.ManaRegen)) + ")");
                statusLore.add(StatusParameter.ATK.DecoDisplay + String.format(format, status.ATK) + " (" + String.format(format, status.BaseStatus(StatusParameter.ATK)) + ")");
                statusLore.add(StatusParameter.DEF.DecoDisplay + String.format(format, status.DEF) + " (" + String.format(format, status.BaseStatus(StatusParameter.DEF)) + ")");
                statusLore.add(StatusParameter.HLP.DecoDisplay + String.format(format, status.HLP) + " (" + String.format(format, status.BaseStatus(StatusParameter.HLP)) + ")");
                statusLore.add(StatusParameter.ACC.DecoDisplay + String.format(format, status.ACC) + " (" + String.format(format, status.BaseStatus(StatusParameter.ACC)) + ")");
                statusLore.add(StatusParameter.EVA.DecoDisplay + String.format(format, status.EVA) + " (" + String.format(format, status.BaseStatus(StatusParameter.EVA)) + ")");
                statusLore.add(StatusParameter.CriticalRate.DecoDisplay + String.format(format, status.CriticalRate) + " (" + String.format(format, status.BaseStatus(StatusParameter.CriticalRate)) + ")");
                statusLore.add(StatusParameter.CriticalResist.DecoDisplay + String.format(format, status.CriticalResist) + " (" + String.format(format, status.BaseStatus(StatusParameter.CriticalResist)) + ")");
                statusLore.add(StatusParameter.SkillCastTime.DecoDisplay + String.format(format, status.SkillCastTime*100) + " (" + String.format(format, 100/status.SkillCastTime) + "%)");
                statusLore.add(StatusParameter.SkillRigidTime.DecoDisplay + String.format(format, status.SkillRigidTime*100) + " (" + String.format(format, 100/status.SkillRigidTime) + "%)");
                statusLore.add(StatusParameter.SkillCooltime.DecoDisplay + String.format(format, status.SkillCooltime*100) + " (" + String.format(format, 100/status.SkillCooltime) + "%)");
                statusLore.add(decoLore("クリティカルダメージ") + String.format(format, status.CriticalMultiply*100) + "%");
                statusLore.add(decoLore("物理与ダメージ") + String.format(format, status.DamageCauseMultiply.get(DamageCause.ATK)*100));
                statusLore.add(decoLore("魔法与ダメージ") + String.format(format, status.DamageCauseMultiply.get(DamageCause.MAT)*100));
                statusLore.add(decoLore("物理被ダメージ耐性") + String.format(format, status.DamageCauseResistance.get(DamageCause.ATK)*100) + " (" + String.format(format, 100/status.DamageCauseResistance.get(DamageCause.ATK)) + "%)");
                statusLore.add(decoLore("魔法被ダメージ耐性") + String.format(format, status.DamageCauseResistance.get(DamageCause.MAT)*100) + " (" + String.format(format, 100/status.DamageCauseResistance.get(DamageCause.MAT)) + "%)");
                statusMeta.setLore(statusLore);
                statusIcon.setItemMeta(statusMeta);
                List<String> classLore = new ArrayList<>();
                for (int i = 0; i < Classes.MaxSlot; i++) {
                    ClassData classData = playerData.Classes.classSlot[i];
                    if (classData != null) {
                        classLore.add(decoLore("クラス[" + (i+1) + "]") + classData.Color + "§l" + classData.Display);
                    } else {
                        classLore.add(decoLore("クラス[" + (i+1) + "]") + "§7§l未使用");
                    }
                }
                classLore.add(decoText("§e§lクラス情報"));
                for (ClassData classData : getClassList().values()) {
                    classLore.add("§7・" + classData.Color + "§l" + classData.Display + " §e§lLv" + playerData.Classes.getClassLevel(classData) + "/" + Classes.MaxLevel + " §a§l" + playerData.Classes.viewExpPercent(classData));
                }
                List<String> lifeLore = new ArrayList<>();
                for (LifeType type : LifeType.values()) {
                    int LifeLevel = playerData.LifeStatus.getLevel(type);
                    lifeLore.add(decoLore(type.Display) + " Lv" + LifeLevel + " " + playerData.LifeStatus.viewExpPercent(type));
                }
                List<String> attrLore = new ArrayList<>();
                for (AttributeType attr : AttributeType.values()) {
                    attrLore.add(decoLore(attr.Display) + playerData.Attribute.getAttribute(attr));
                }
                inv.setItem(0, statusIcon);
                inv.setItem(1, new ItemStackData(Material.END_CRYSTAL, decoText("§e§lクラススロット"), classLore).view());
                inv.setItem(2, new ItemStackData(Material.CRAFTING_TABLE, decoText("§3§l生活ステータス"), lifeLore).view());
                inv.setItem(3, new ItemStackData(Material.PAINTING, decoText("§3§l統計情報"), playerData.statistics.getStringList()).view());
                inv.setItem(4, new ItemStackData(Material.RED_DYE, decoText("§3§lアトリビュート"), attrLore).view());

                int slot = 6;
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    if (playerData.Equipment.getEquip(equipmentSlot).isEmpty()) {
                        inv.setItem(slot, new ItemStackData(Material.BARRIER, "§c§l" + equipmentSlot.Display + "未装備").view());
                    } else {
                        inv.setItem(slot, playerData.Equipment.getEquip(equipmentSlot).viewItem(1, playerData.ViewFormat()));
                    }
                    slot++;
                }

                MultiThread.TaskRunSynchronized(() -> Viewer.getOpenInventory().getTopInventory().setContents(inv.getStorageContents()));
                MultiThread.sleepTick(20);
            }
        }, "StatusInfoView" + player.getName());
    }
}
