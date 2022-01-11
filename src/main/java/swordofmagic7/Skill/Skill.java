package swordofmagic7.Skill;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.SkillMenuDisplay;
import static swordofmagic7.Pet.PetManager.ReqAttackTarget;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Skill {
    private final Plugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private boolean CastReady = true;
    public swordofmagic7.Skill.SkillProcess SkillProcess;
    private final HashMap<SkillData, Integer> SkillCoolTime = new HashMap<>();
    private final HashMap<SkillData, Integer> SkillLevel = new HashMap<>();
    int SkillPoint = 0;

    public Skill(Player player, PlayerData playerData, Plugin plugin) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;
        SkillProcess = new SkillProcess(player, playerData, plugin, this);
    }

    public void setCastReady(boolean bool) {
        CastReady = bool;
        SkillProcess.SkillCastTime = 0;
    }

    public boolean isCastReady() {
        return CastReady;
    }

    public void CastSkill(SkillData skillData) {
        if (CastReady && isAlive(player)) {
            if (CategoryCheck(EquipmentSlot.MainHand, skillData.ReqMainHand)) {
                if (hasSkill(skillData.Id)) {
                    if (!SkillCoolTime.containsKey(skillData)) {
                        if (playerData.Status.Mana >= skillData.Mana) {
                            if (skillData.SkillType.isPetSkill()) {
                                if (playerData.PetSelect == null) {
                                    player.sendMessage("§a指揮する§e[ペット]§aを選択してください");
                                    playSound(player, SoundList.Nope);
                                    return;
                                } else if (skillData.SkillType.isPetAttack() && playerData.PetSelect.target == null) {
                                    player.sendMessage(ReqAttackTarget);
                                    playSound(player, SoundList.Nope);
                                    return;
                                }
                            }
                            new BukkitRunnable() {
                                float p = 0;

                                @Override
                                public void run() {
                                    p = (float) SkillProcess.SkillCastTime / skillData.CastTime;
                                    if (p >= 1) this.cancel();
                                    player.sendTitle(" ", "§e" + String.format("%.0f", p * 100) + "%", 0, 10, 0);
                                }
                            }.runTaskTimerAsynchronously(plugin, 0, 1);
                            switch (skillData.Id) {
                                case "Slash" -> SkillProcess.Slash(skillData, 5, 70);
                                case "Bash" -> SkillProcess.Slash(skillData, 6, 90);
                                case "Vertical" -> SkillProcess.Vertical(skillData, 10, 2.5);
                                case "Thrust" -> SkillProcess.Vertical(skillData, 10, 3);
                                case "Smite" -> SkillProcess.Smite(skillData, 4);
                                case "Rain" -> SkillProcess.Rain(skillData, 5);
                                case "DoubleTrigger" -> SkillProcess.TriggerShot(skillData, 2);
                                case "TripleTrigger" -> SkillProcess.TriggerShot(skillData, 3);
                                case "ChargeShot" -> SkillProcess.TriggerShot(skillData, 1);
                                case "Infall" -> SkillProcess.Infall(skillData, 10);
                                case "Heal" -> SkillProcess.Heal(skillData, 15);
                                case "Resurrection" -> SkillProcess.Resurrection(skillData, 15);
                                case "PetAttack" -> SkillProcess.PetAttack(skillData);
                                case "PetHeal" -> SkillProcess.PetHeal(skillData);
                                case "FireBall" -> SkillProcess.FireBall(skillData);
                                case "Teleportation" -> SkillProcess.Teleportation(skillData);
                            }
                            playerData.changeMana(-skillData.Mana);
                            setSkillCoolTime(skillData);
                        } else {
                            player.sendMessage("§b[マナ]§aが足りません");
                            playSound(player, SoundList.Nope);
                        }
                    } else {
                        player.sendMessage("§e[" + skillData.Display + "]§aを§b[使用可能]§aまで§c[" + SkillCoolTime.get(skillData) / 20f + "秒]§aです");
                        playSound(player, SoundList.Nope);
                    }
                } else {
                    player.sendMessage("§e[" + skillData.Display + "]§aの§c[使用条件]§aを満たしていません");
                    playSound(player, SoundList.Nope);
                }
            }
        }
    }

    void setSkillCoolTime(SkillData skillData) {
        SkillCoolTime.put(skillData, skillData.CoolTime);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!SkillCoolTime.containsKey(skillData)) {
                    this.cancel();
                    return;
                }
                SkillCoolTime.put(skillData, SkillCoolTime.get(skillData) - 1);
                if (SkillCoolTime.get(skillData) < 1) {
                    SkillCoolTime.remove(skillData);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    void resetSkillCoolTime(SkillData skillData) {
        SkillCoolTime.remove(skillData);
    }

    public int getSkillCoolTime(SkillData skillData) {
        return SkillCoolTime.getOrDefault(skillData, 0);
    }

    private boolean CategoryCheck(EquipmentSlot slot, EquipmentCategory category) {
        List<EquipmentCategory> list = new ArrayList<>();
        list.add(category);
        return CategoryCheck(slot, list);
    }

    boolean hasSkill(String skill) {
        for (ClassData classData : playerData.Classes.classTier) {
            SkillData skillData = DataBase.getSkillData(skill);
            if (classData != null && classData.SkillList.contains(skillData)) {
                if (playerData.Classes.getLevel(classData) >= skillData.ReqLevel) {
                    return true;
                }
            }
        }
        return false;
    }

    void addSkillLevel(SkillData skillData, int add) {
        if (SkillPoint >= add) {
            SkillPoint -= add;
            SkillLevel.put(skillData, SkillLevel.get(skillData) + add);
        } else {
            player.sendMessage("§eポイント§aが足りません");
        }
    }

    void setSkillLevel(SkillData skillData, int attr) {
        SkillLevel.put(skillData, attr);
    }

    void resetSkillLevel(ClassData classData) {
        SkillPoint = playerData.Classes.getLevel(classData) - 1;
        for (SkillData skillData : classData.SkillList) {
            SkillLevel.put(skillData, 0);
        }
    }

    private boolean CategoryCheck(EquipmentSlot slot, List<EquipmentCategory> categoryList) {
        if (categoryList.size() == 0) return true;
        boolean check = false;
        String Display = "";
        for (EquipmentCategory category : categoryList) {
            if (Display.equals("")) {
                Display = category.Display;
            } else {
                Display += ", " + category.Display;
            }
            if (playerData.Equipment.getEquip(slot).itemEquipmentData.EquipmentCategory == category) {
                check = true;
                break;
            }
        }
        if (check) {
            return true;
        } else {
            player.sendMessage("§aこの§e[スキル]§aの§b発動§aには§e" + slot.Display + "§aに§e[" + Display + "]§aを§e装備§aしてる§c必要§aがあります");
            playSound(player, SoundList.Nope);
            return false;
        }
    }

    private final HashMap<Integer, String> SkillMenuCache = new HashMap<>();
    public void SkillMenuView() {
        SkillMenuCache.clear();
        Inventory inv = decoInv(SkillMenuDisplay, 3);
        int slot = 0;
        int tier = 0;
        while (playerData.Classes.classTier[tier] != null) {
            for (SkillData skill : playerData.Classes.classTier[tier].SkillList) {
                inv.setItem(slot, skill.view());
                SkillMenuCache.put(slot, skill.Id);
                slot++;
            }
            tier++;
            slot = tier*9;
        }
        player.openInventory(inv);
    }

    public void SkillMenuClick(InventoryView view, int Slot) {
        if (equalInv(view, SkillMenuDisplay)) {
            SkillData skillData = getSkillData(SkillMenuCache.get(Slot));
            if (skillData.SkillType.isActive()) {
                playerData.Menu.Trigger.TriggerMenuView();
                playSound(player, SoundList.Click);
            } else {
                player.sendMessage("§e[" + skillData.Display + "]§aは§eパッシブスキル§aです");
                playSound(player, SoundList.Nope);
            }
        }
    }
}



