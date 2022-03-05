package swordofmagic7.Menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Life.LifeStatus;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Status.Status;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.ItemStackPlayerHead;
import static swordofmagic7.Data.DataBase.getClassList;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.StatusInfoDisplay;
import static swordofmagic7.System.BTTSet;
import static swordofmagic7.System.plugin;

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
        BTTSet(new BukkitRunnable() {
            final ItemStack statusIcon = ItemStackPlayerHead(player);
            final ItemMeta statusMeta = statusIcon.getItemMeta();
            @Override
            public void run() {
                if (Viewer.getOpenInventory().getTopInventory().equals(inv)) {
                    statusMeta.setDisplayName(decoText(playerData.Nick));
                    List<String> statusLore = new ArrayList<>();
                    statusLore.add(decoLore("現在位置") + playerData.Map.Display);
                    statusLore.add(decoLore("レベル") + playerData.Level + "/" + PlayerData.MaxLevel);
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
                    statusLore.add(StatusParameter.SkillCastTime.DecoDisplay + String.format(format, status.SkillCastTime));
                    statusLore.add(StatusParameter.SkillRigidTime.DecoDisplay + String.format(format, status.SkillRigidTime));
                    statusLore.add(StatusParameter.SkillCooltime.DecoDisplay + String.format(format, status.SkillCooltime));
                    statusLore.add(decoLore("クリティカルダメージ") + String.format(format, status.CriticalMultiply*100) + "%");
                    statusLore.add(decoLore("物理与ダメージ") + String.format(format, status.DamageCauseMultiply.get(DamageCause.ATK)*100) + "%");
                    statusLore.add(decoLore("魔法与ダメージ") + String.format(format, status.DamageCauseMultiply.get(DamageCause.MAT)*100) + "%");
                    statusLore.add(decoLore("物理被ダメージ耐性") + String.format(format, status.DamageCauseResistance.get(DamageCause.ATK)*100) + "%");
                    statusLore.add(decoLore("魔法被ダメージ耐性") + String.format(format, status.DamageCauseResistance.get(DamageCause.MAT)*100) + "%");
                    statusMeta.setLore(statusLore);
                    statusIcon.setItemMeta(statusMeta);
                    List<String> classLore = new ArrayList<>();
                    for (int i = 0; i < Classes.MaxSlot; i++) {
                        ClassData classData = playerData.Classes.classSlot[i];
                        if (classData != null) {
                            classLore.add(decoLore("クラス[" + (i+1) + "]") + classData.Color + "§l" + classData.Display);
                        } else {
                            classLore.add(decoLore("クラス[" + (i+1) + "]") + "§7§l未設定");
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
                    inv.setItem(0, statusIcon);
                    inv.setItem(1, new ItemStackData(Material.END_CRYSTAL, decoText("§e§lクラススロット"), classLore).view());
                    inv.setItem(2, new ItemStackData(Material.CRAFTING_TABLE, decoText("§3§l生活ステータス"), lifeLore).view());
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20), "StatusInfoView" + player.getName());
        Viewer.openInventory(inv);
    }
}
