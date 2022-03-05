package swordofmagic7.Skill;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.N;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Skill.SkillClass.*;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Tutorial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.SkillMenuDisplay;
import static swordofmagic7.Pet.PetManager.ReqAttackTarget;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class Skill {
    private final Plugin plugin;
    public final Player player;
    public final PlayerData playerData;
    private boolean CastReady = true;
    public SkillProcess SkillProcess;
    private final HashMap<String, Integer> SkillCoolTime = new HashMap<>();
    private final HashMap<String, Integer> SkillLevel = new HashMap<>();
    int SkillPoint = 0;

    Novice novice;
    Swordman swordman;
    Mage mage;
    Gunner gunner;
    Cleric cleric;
    Tamer tamer;
    Priest priest;

    public Skill(Player player, PlayerData playerData, Plugin plugin) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;
        SkillProcess = new SkillProcess(this);

        novice = new Novice(SkillProcess);
        swordman = new Swordman(SkillProcess);
        mage = new Mage(SkillProcess);
        gunner = new Gunner(SkillProcess);
        cleric = new Cleric(SkillProcess);
        tamer = new Tamer(SkillProcess);
        priest = new Priest(SkillProcess);
    }

    public void setCastReady(boolean bool) {
        CastReady = bool;
        SkillProcess.SkillCastTime = 0;
    }

    public boolean isCastReady() {
        return CastReady && !playerData.EffectManager.hasEffect(EffectType.Rigidity);
    }

    public void CastSkill(SkillData skillDataBase) {
        if (CastReady && isAlive(player) && !player.isInsideVehicle()) {
            SkillData skillData = skillDataBase.clone();
            if (CategoryCheck(EquipmentSlot.MainHand, skillData.ReqMainHand)) {
                if (hasSkill(skillData.Id)) {
                    if (getSkillCoolTime(skillData) == 0) {
                        if (playerData.Status.Mana >= skillData.Mana) {
                            if (!playerData.EffectManager.hasEffect(EffectType.Silence)) {
                                Tutorial.tutorialTrigger(player, 4);
                                if (playerData.Skill.hasSkill("MagicEfficiently")) {
                                    SkillData MagicEfficiently = getSkillData("MagicEfficiently");
                                    skillData.Mana = (int) Math.floor(skillData.Mana * (1-MagicEfficiently.ParameterValue(0)/100));
                                    skillData.CastTime = (int) Math.floor(skillData.CastTime * (1-MagicEfficiently.ParameterValue(1)/100));
                                }
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
                                if (skillData.CastTime > 0) {
                                    new BukkitRunnable() {
                                        float p = 0;

                                        @Override
                                        public void run() {
                                            p = (float) SkillProcess.SkillCastTime / skillData.CastTime;
                                            if (p >= 1) {
                                                this.cancel();
                                                p = 1;
                                            }
                                            player.sendTitle(" ", "§e" + String.format("%.0f", p * 100) + "%", 0, 10, 0);
                                        }
                                    }.runTaskTimerAsynchronously(plugin, 0, 1);
                                }
                                switch (skillData.Id) {
                                    //ノービス
                                    case "Slash" -> novice.Slash(skillData, 5, 70);
                                    case "Vertical" -> novice.Vertical(skillData, 10, 2.5);
                                    case "Smite" -> novice.Smite(skillData, 4);
                                    case "Rain" -> novice.Rain(skillData, 5);
                                    case "DoubleTrigger" -> novice.TriggerShot(skillData, 2);
                                    case "FireBall" -> novice.FireBall(skillData);
                                    //ソードマン
                                    case "Bash" -> novice.Slash(skillData, 6, 90);
                                    case "Thrust" -> novice.Vertical(skillData, 10, 3);
                                    case "PainBarrier" -> swordman.PainBarrier(skillData);
                                    case "Feint" -> swordman.Feint(skillData);
                                    //ガンナー
                                    case "TripleTrigger" -> novice.TriggerShot(skillData, 3);
                                    case "ChargeShot" -> novice.TriggerShot(skillData, 1);
                                    case "Aiming" -> gunner.Aiming(skillData);
                                    case "Rolling" -> gunner.Rolling(skillData);
                                    //メイジ
                                    case "Infall" -> mage.Infall(skillData, 10);
                                    case "Teleportation" -> mage.Teleportation(skillData);
                                    case "MagicMissile" -> mage.MagicMissile(skillData);
                                    //クレシック
                                    case "Heal" -> cleric.Heal(skillData, 15);
                                    case "Cure" -> cleric.Cure(skillData, 15);
                                    case "Fade" -> cleric.Fade(skillData);
                                    case "Resurrection" -> cleric.Resurrection(skillData, 15);
                                    //テイマー
                                    case "PetAttack" -> tamer.PetAttack(skillData);
                                    case "PetHeal" -> tamer.PetHeal(skillData);
                                    case "PetBoost" -> tamer.PetBoost(skillData);
                                    //プリースト
                                    case "MassHeal" -> priest.MassHeal(skillData);
                                    case "Monstrance" -> priest.Monstrance(skillData);
                                    case "HolyDefense" -> priest.HolyBuff(skillData, new ParticleData(Particle.FIREWORKS_SPARK), EffectType.HolyDefense);
                                    case "HolyAttack" -> priest.HolyBuff(skillData, new ParticleData(Particle.REDSTONE), EffectType.HolyAttack);
                                    case "Revive" -> priest.HolyBuff(skillData, new ParticleData(Particle.VILLAGER_HAPPY), EffectType.Revive);
                                }
                                playerData.changeMana(-skillData.Mana);
                                setSkillCoolTime(skillData);
                            } else {
                                player.sendMessage("§c[デバフ効果]§aによりスキルを発動できません");
                                playSound(player, SoundList.Nope);
                            }
                        } else {
                            player.sendMessage("§b[マナ]§aが足りません");
                            playSound(player, SoundList.Nope);
                        }
                    } else {
                        player.sendMessage("§e[" + skillData.Display + "]§aを§b[使用可能]§aまで§c[" + getSkillCoolTime(skillData) / 20f + "秒]§aです");
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
        SkillCoolTime.put(skillData.Id, skillData.CoolTime);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!SkillCoolTime.containsKey(skillData.Id)) {
                    this.cancel();
                    return;
                }
                SkillCoolTime.put(skillData.Id, getSkillCoolTime(skillData) - 1);
                if (getSkillCoolTime(skillData) < 1) {
                    SkillCoolTime.remove(skillData.Id);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    public void resetSkillCoolTime(SkillData skillData) {
        SkillCoolTime.remove(skillData.Id);
    }

    public void resetSkillCoolTimeWaited(SkillData skillData) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> resetSkillCoolTime(skillData), 5);
    }

    public int getSkillCoolTime(SkillData skillData) {
        return SkillCoolTime.getOrDefault(skillData.Id, 0);
    }

    private boolean CategoryCheck(EquipmentSlot slot, EquipmentCategory category) {
        List<EquipmentCategory> list = new ArrayList<>();
        list.add(category);
        return CategoryCheck(slot, list);
    }

    public boolean hasSkill(String skill) {
        for (ClassData classData : playerData.Classes.classSlot) {
            SkillData skillData = DataBase.getSkillData(skill);
            if (classData != null && classData.SkillList.contains(skillData)) {
                if (playerData.Classes.getClassLevel(classData) >= skillData.ReqLevel) {
                    return true;
                }
            }
        }
        return false;
    }

    void addSkillLevel(SkillData skillData, int add) {
        if (SkillPoint >= add) {
            SkillPoint -= add;
            SkillLevel.put(skillData.Id, getSkillLevel(skillData) + add);
        } else {
            player.sendMessage("§eポイント§aが足りません");
        }
    }

    void setSkillLevel(SkillData skillData, int attr) {
        SkillLevel.put(skillData.Id, attr);
    }

    void resetSkillLevel(ClassData classData) {
        SkillPoint = playerData.Classes.getClassLevel(classData) - 1;
        for (SkillData skillData : classData.SkillList) {
            SkillLevel.put(skillData.Id, 0);
        }
    }

    int getSkillLevel(SkillData skillData) {
        return SkillLevel.getOrDefault(skillData.Id, 1);
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
        Inventory inv = decoInv(SkillMenuDisplay, Classes.MaxSlot);
        int slotActive = 0;
        int slotPassive = 8;
        int slot = 0;
        for (int i = 0; i < playerData.Classes.classSlot.length; i++) {
            if (playerData.Classes.classSlot[i] != null) {
                for (SkillData skill : playerData.Classes.classSlot[i].SkillList) {
                    if (skill.SkillType.isPassive()) {
                        SkillMenuCache.put(slotPassive, skill.Id);
                        inv.setItem(slotPassive, skill.view());
                        slotPassive--;
                    } else {
                        SkillMenuCache.put(slotActive, skill.Id);
                        inv.setItem(slotActive, skill.view());
                        slotActive++;
                    }
                }
                slot++;
                slotActive = slot * 9;
                slotPassive = (slot + 1) * 9 - 1;
            }
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



